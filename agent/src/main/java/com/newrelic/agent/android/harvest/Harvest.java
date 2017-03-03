// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.ArrayList;
import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import com.newrelic.agent.android.metric.Metric;
import com.newrelic.agent.android.activity.config.ActivityTraceConfiguration;
import com.newrelic.agent.android.harvest.type.Harvestable;
import com.newrelic.agent.android.tracing.ActivityTrace;
import com.newrelic.agent.android.analytics.AnalyticsEvent;
import com.newrelic.agent.android.analytics.SessionEvent;
import com.newrelic.agent.android.analytics.AnalyticsControllerImpl;
import com.newrelic.agent.android.stats.StatsEngine;
import com.newrelic.agent.android.AgentConfiguration;
import java.util.Collection;
import com.newrelic.agent.android.logging.AgentLog;

public class Harvest
{
    private static final AgentLog log;
    private static final boolean DISABLE_ACTIVITY_TRACE_LIMITS_FOR_DEBUGGING = false;
    public static final long INVALID_SESSION_DURATION = 0L;
    protected static Harvest instance;
    private Harvester harvester;
    private HarvestConnection harvestConnection;
    private HarvestTimer harvestTimer;
    protected HarvestData harvestData;
    private HarvestDataValidator harvestDataValidator;
    private static final Collection<HarvestLifecycleAware> unregisteredLifecycleListeners;
    private static final HarvestableCache activityTraceCache;
    private HarvestConfiguration configuration;
    
    public Harvest() {
        this.configuration = HarvestConfiguration.getDefaultHarvestConfiguration();
    }
    
    public static void initialize(final AgentConfiguration agentConfiguration) {
        Harvest.instance.initializeHarvester(agentConfiguration);
        registerUnregisteredListeners();
        addHarvestListener(StatsEngine.get());
    }
    
    public void initializeHarvester(final AgentConfiguration agentConfiguration) {
        this.createHarvester();
        this.harvester.setAgentConfiguration(agentConfiguration);
        this.harvester.setConfiguration(Harvest.instance.getConfiguration());
        this.flushHarvestableCaches();
    }
    
    public static void setPeriod(final long period) {
        Harvest.instance.getHarvestTimer().setPeriod(period);
    }
    
    public static void start() {
        Harvest.instance.getHarvestTimer().start();
    }
    
    public static void stop() {
        Harvest.instance.getHarvestTimer().stop();
    }
    
    public static void harvestNow() {
        if (!isInitialized()) {
            return;
        }
        final long sessionDuration = getMillisSinceStart();
        if (sessionDuration == 0L) {
            Harvest.log.error("Session duration is invalid!");
            StatsEngine.get().inc("Supportability/AgentHealth/Session/InvalidDuration");
        }
        final float sessionDurationAsSeconds = sessionDuration / 1000.0f;
        StatsEngine.get().sample("Session/Duration", sessionDurationAsSeconds);
        Harvest.log.debug("Harvest.harvestNow - Generating sessionDuration attribute with value " + sessionDurationAsSeconds);
        final AnalyticsControllerImpl analyticsController = AnalyticsControllerImpl.getInstance();
        analyticsController.setAttribute("sessionDuration", sessionDurationAsSeconds, false);
        Harvest.log.debug("Harvest.harvestNow - Generating session event.");
        final SessionEvent sessionEvent = new SessionEvent();
        analyticsController.addEvent(sessionEvent);
        analyticsController.getEventManager().shutdown();
        Harvest.instance.getHarvestTimer().tickNow();
    }
    
    public static void setInstance(final Harvest harvestInstance) {
        if (harvestInstance == null) {
            Harvest.log.error("Attempt to set Harvest instance to null value!");
        }
        else {
            Harvest.instance = harvestInstance;
        }
    }
    
    public void createHarvester() {
        this.harvestConnection = new HarvestConnection();
        this.harvestData = new HarvestData();
        (this.harvester = new Harvester()).setHarvestConnection(this.harvestConnection);
        this.harvester.setHarvestData(this.harvestData);
        this.harvestTimer = new HarvestTimer(this.harvester);
        addHarvestListener(this.harvestDataValidator = new HarvestDataValidator());
    }
    
    public void shutdownHarvester() {
        this.harvestTimer.shutdown();
        this.harvestTimer = null;
        this.harvester = null;
        this.harvestConnection = null;
        this.harvestData = null;
    }
    
    public static void shutdown() {
        if (!isInitialized()) {
            return;
        }
        stop();
        Harvest.instance.shutdownHarvester();
    }
    
    public static void addHttpErrorTrace(final HttpError error) {
        if (!Harvest.instance.shouldCollectNetworkErrors() || isDisabled()) {
            return;
        }
        final HttpErrors errors = Harvest.instance.getHarvestData().getHttpErrors();
        Harvest.instance.getHarvester().expireHttpErrors();
        final int errorLimit = Harvest.instance.getConfiguration().getError_limit();
        if (errors.count() >= errorLimit) {
            StatsEngine.get().inc("Supportability/AgentHealth/ErrorsDropped");
            Harvest.log.debug("Maximum number of HTTP errors (" + errorLimit + ") reached. HTTP Error dropped.");
            return;
        }
        errors.addHttpError(error);
        Harvest.log.verbose("Harvest: " + Harvest.instance + " now contains " + errors.count() + " errors.");
    }
    
    public static void addHttpTransaction(final HttpTransaction txn) {
        if (isDisabled()) {
            return;
        }
        final HttpTransactions transactions = Harvest.instance.getHarvestData().getHttpTransactions();
        Harvest.instance.getHarvester().expireHttpTransactions();
        final int transactionLimit = Harvest.instance.getConfiguration().getReport_max_transaction_count();
        if (transactions.count() >= transactionLimit) {
            StatsEngine.get().inc("Supportability/AgentHealth/TransactionsDropped");
            Harvest.log.debug("Maximum number of transactions (" + transactionLimit + ") reached. HTTP Transaction dropped.");
            return;
        }
        transactions.add(txn);
        final AnalyticsControllerImpl analyticsController = AnalyticsControllerImpl.getInstance();
        if (txn.getStatusCode() >= 400L) {
            analyticsController.createHttpErrorEvent(txn);
        }
        else if (txn.getErrorCode() != 0) {
            analyticsController.createNetworkFailureEvent(txn);
        }
    }
    
    public static void addActivityTrace(final ActivityTrace activityTrace) {
        if (isDisabled()) {
            return;
        }
        if (!isInitialized()) {
            Harvest.activityTraceCache.add(activityTrace);
            return;
        }
        if (activityTrace.rootTrace == null) {
            Harvest.log.error("Activity trace is lacking a root trace!");
            return;
        }
        if (activityTrace.rootTrace.childExclusiveTime == 0L) {
            Harvest.log.error("Total trace exclusive time is zero. Ignoring trace " + activityTrace.rootTrace.displayName);
            return;
        }
        final double traceUtilization = activityTrace.rootTrace.childExclusiveTime / activityTrace.rootTrace.getDurationAsMilliseconds();
        final boolean isBelowMinUtilization = traceUtilization < Harvest.instance.getConfiguration().getActivity_trace_min_utilization();
        if (isBelowMinUtilization) {
            StatsEngine.get().inc("Supportability/AgentHealth/IgnoredTraces");
            Harvest.log.debug("Exclusive trace time is too low (" + activityTrace.rootTrace.childExclusiveTime + "/" + activityTrace.rootTrace.getDurationAsMilliseconds() + "). Ignoring trace " + activityTrace.rootTrace.displayName);
            return;
        }
        final ActivityTraces activityTraces = Harvest.instance.getHarvestData().getActivityTraces();
        final ActivityTraceConfiguration configurations = Harvest.instance.getActivityTraceConfiguration();
        Harvest.instance.getHarvester().expireActivityTraces();
        if (activityTraces.count() >= configurations.getMaxTotalTraceCount()) {
            Harvest.log.debug("Activity trace limit of " + configurations.getMaxTotalTraceCount() + " exceeded. Ignoring trace: " + activityTrace.toJsonString());
            return;
        }
        Harvest.log.debug("Adding activity trace: " + activityTrace.toJsonString());
        activityTraces.add(activityTrace);
    }
    
    public static void addMetric(final Metric metric) {
        if (isDisabled() || !isInitialized()) {
            return;
        }
        Harvest.instance.getHarvestData().getMetrics().addMetric(metric);
    }
    
    public static void addAgentHealthException(final AgentHealthException exception) {
        if (isDisabled() || !isInitialized()) {
            return;
        }
        Harvest.instance.getHarvestData().getAgentHealth().addException(exception);
    }
    
    public static void addHarvestListener(final HarvestLifecycleAware harvestAware) {
        if (harvestAware == null) {
            Harvest.log.error("Harvest: Argument to addHarvestListener cannot be null.");
            return;
        }
        if (!isInitialized()) {
            if (!isUnregisteredListener(harvestAware)) {
                addUnregisteredListener(harvestAware);
            }
            return;
        }
        Harvest.instance.getHarvester().addHarvestListener(harvestAware);
    }
    
    public static void removeHarvestListener(final HarvestLifecycleAware harvestAware) {
        if (harvestAware == null) {
            Harvest.log.error("Harvest: Argument to removeHarvestListener cannot be null.");
            return;
        }
        if (!isInitialized()) {
            if (isUnregisteredListener(harvestAware)) {
                removeUnregisteredListener(harvestAware);
            }
            return;
        }
        Harvest.instance.getHarvester().removeHarvestListener(harvestAware);
    }
    
    public static boolean isInitialized() {
        return Harvest.instance != null && Harvest.instance.getHarvester() != null;
    }
    
    public static int getActivityTraceCacheSize() {
        return Harvest.activityTraceCache.getSize();
    }
    
    public static long getMillisSinceStart() {
        long lTime = 0L;
        final Harvest harvest = getInstance();
        if (harvest != null && harvest.getHarvestTimer() != null) {
            lTime = harvest.getHarvestTimer().timeSinceStart();
            if (lTime < 0L) {
                lTime = 0L;
            }
        }
        return lTime;
    }
    
    public static boolean shouldCollectActivityTraces() {
        if (isDisabled()) {
            return false;
        }
        if (!isInitialized()) {
            return true;
        }
        final ActivityTraceConfiguration configurations = Harvest.instance.getActivityTraceConfiguration();
        return configurations == null || configurations.getMaxTotalTraceCount() > 0;
    }
    
    private void flushHarvestableCaches() {
        try {
            this.flushActivityTraceCache();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void flushActivityTraceCache() {
        final Collection<Harvestable> activityTraces = Harvest.activityTraceCache.flush();
        for (final Harvestable activityTrace : activityTraces) {
            addActivityTrace((ActivityTrace)activityTrace);
        }
    }
    
    private static void addUnregisteredListener(final HarvestLifecycleAware harvestAware) {
        if (harvestAware == null) {
            return;
        }
        synchronized (Harvest.unregisteredLifecycleListeners) {
            Harvest.unregisteredLifecycleListeners.add(harvestAware);
        }
    }
    
    private static void removeUnregisteredListener(final HarvestLifecycleAware harvestAware) {
        if (harvestAware == null) {
            return;
        }
        synchronized (Harvest.unregisteredLifecycleListeners) {
            Harvest.unregisteredLifecycleListeners.remove(harvestAware);
        }
    }
    
    private static void registerUnregisteredListeners() {
        for (final HarvestLifecycleAware harvestAware : Harvest.unregisteredLifecycleListeners) {
            addHarvestListener(harvestAware);
        }
        Harvest.unregisteredLifecycleListeners.clear();
    }
    
    private static boolean isUnregisteredListener(final HarvestLifecycleAware harvestAware) {
        return harvestAware != null && Harvest.unregisteredLifecycleListeners.contains(harvestAware);
    }
    
    protected HarvestTimer getHarvestTimer() {
        return this.harvestTimer;
    }
    
    public static Harvest getInstance() {
        return Harvest.instance;
    }
    
    protected Harvester getHarvester() {
        return this.harvester;
    }
    
    public HarvestData getHarvestData() {
        return this.harvestData;
    }
    
    public HarvestConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public HarvestConnection getHarvestConnection() {
        return this.harvestConnection;
    }
    
    public void setHarvestConnection(final HarvestConnection connection) {
        this.harvestConnection = connection;
    }
    
    public boolean shouldCollectNetworkErrors() {
        return this.configuration.isCollect_network_errors();
    }
    
    public void setConfiguration(final HarvestConfiguration newConfiguration) {
        this.configuration.reconfigure(newConfiguration);
        this.harvestTimer.setPeriod(TimeUnit.MILLISECONDS.convert(this.configuration.getData_report_period(), TimeUnit.SECONDS));
        this.harvestConnection.setServerTimestamp(this.configuration.getServer_timestamp());
        this.harvestData.setDataToken(this.configuration.getDataToken());
        this.harvester.setConfiguration(this.configuration);
    }
    
    public void setConnectInformation(final ConnectInformation connectInformation) {
        this.harvestConnection.setConnectInformation(connectInformation);
        this.harvestData.setDeviceInformation(connectInformation.getDeviceInformation());
    }
    
    public static void setHarvestConfiguration(final HarvestConfiguration configuration) {
        if (!isInitialized()) {
            Harvest.log.error("Cannot configure Harvester before initialization.");
            new Exception().printStackTrace();
            return;
        }
        Harvest.log.debug("Harvest Configuration: " + configuration);
        Harvest.instance.setConfiguration(configuration);
    }
    
    public static HarvestConfiguration getHarvestConfiguration() {
        if (!isInitialized()) {
            return HarvestConfiguration.getDefaultHarvestConfiguration();
        }
        return Harvest.instance.getConfiguration();
    }
    
    public static void setHarvestConnectInformation(final ConnectInformation connectInformation) {
        if (!isInitialized()) {
            Harvest.log.error("Cannot configure Harvester before initialization.");
            new Exception().printStackTrace();
            return;
        }
        Harvest.log.debug("Setting Harvest connect information: " + connectInformation);
        Harvest.instance.setConnectInformation(connectInformation);
    }
    
    public static boolean isDisabled() {
        return isInitialized() && Harvest.instance.getHarvester().isDisabled();
    }
    
    protected ActivityTraceConfiguration getActivityTraceConfiguration() {
        return this.configuration.getAt_capture();
    }
    
    static {
        log = AgentLogManager.getAgentLog();
        Harvest.instance = new Harvest();
        unregisteredLifecycleListeners = new ArrayList<HarvestLifecycleAware>();
        activityTraceCache = new HarvestableCache();
    }
}
