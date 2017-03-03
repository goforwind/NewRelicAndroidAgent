// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import com.newrelic.agent.android.tracing.ActivityTrace;
import java.util.Iterator;
import com.newrelic.com.google.gson.Gson;
import com.newrelic.com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import com.newrelic.agent.android.activity.config.ActivityTraceConfigurationDeserializer;
import com.newrelic.agent.android.activity.config.ActivityTraceConfiguration;
import com.newrelic.com.google.gson.GsonBuilder;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.analytics.AnalyticsEvent;
import java.util.Set;
import com.newrelic.agent.android.analytics.EventManager;
import com.newrelic.agent.android.harvest.type.Harvestable;
import com.newrelic.agent.android.analytics.AnalyticAttribute;
import java.util.HashSet;
import com.newrelic.agent.android.analytics.AnalyticsControllerImpl;
import com.newrelic.agent.android.FeatureFlag;
import com.newrelic.agent.android.stats.StatsEngine;
import com.newrelic.agent.android.Agent;
import java.util.ArrayList;
import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Collection;
import com.newrelic.agent.android.AgentConfiguration;
import com.newrelic.agent.android.logging.AgentLog;

public class Harvester
{
    private final AgentLog log;
    private State state;
    protected boolean stateChanged;
    private HarvestConnection harvestConnection;
    private AgentConfiguration agentConfiguration;
    private HarvestConfiguration configuration;
    private HarvestData harvestData;
    private final Collection<HarvestLifecycleAware> harvestListeners;
    
    public Harvester() {
        this.log = AgentLogManager.getAgentLog();
        this.state = State.UNINITIALIZED;
        this.configuration = HarvestConfiguration.getDefaultHarvestConfiguration();
        this.harvestListeners = new ArrayList<HarvestLifecycleAware>();
    }
    
    public void start() {
        this.fireOnHarvestStart();
    }
    
    public void stop() {
        this.fireOnHarvestStop();
    }
    
    protected void uninitialized() {
        if (this.agentConfiguration == null) {
            this.log.error("Agent configuration unavailable.");
            return;
        }
        if (Agent.getImpl().updateSavedConnectInformation()) {
            this.configureHarvester(HarvestConfiguration.getDefaultHarvestConfiguration());
            this.harvestData.getDataToken().clear();
        }
        Harvest.setHarvestConnectInformation(new ConnectInformation(Agent.getApplicationInformation(), Agent.getDeviceInformation()));
        this.harvestConnection.setApplicationToken(this.agentConfiguration.getApplicationToken());
        this.harvestConnection.setCollectorHost(this.agentConfiguration.getCollectorHost());
        this.harvestConnection.useSsl(this.agentConfiguration.useSsl());
        this.transition(State.DISCONNECTED);
        this.execute();
    }
    
    protected void disconnected() {
        if (null == this.configuration) {
            this.configureHarvester(HarvestConfiguration.getDefaultHarvestConfiguration());
        }
        if (this.harvestData.isValid()) {
            this.log.verbose("Skipping connect call, saved state is available: " + this.harvestData.getDataToken());
            StatsEngine.get().sample("Session/Start", 1.0f);
            this.fireOnHarvestConnected();
            this.transition(State.CONNECTED);
            this.execute();
            return;
        }
        this.log.info("Connecting, saved state is not available: " + this.harvestData.getDataToken());
        final HarvestResponse response = this.harvestConnection.sendConnect();
        if (response == null) {
            this.log.error("Unable to connect to the Collector.");
            return;
        }
        if (!response.isOK()) {
            this.log.debug("Harvest connect response: " + response.getResponseCode());
            switch (response.getResponseCode()) {
                case UNAUTHORIZED:
                case INVALID_AGENT_ID: {
                    this.harvestData.getDataToken().clear();
                    this.fireOnHarvestDisconnected();
                    return;
                }
                case FORBIDDEN: {
                    if (response.isDisableCommand()) {
                        this.log.error("Collector has commanded Agent to disable.");
                        this.fireOnHarvestDisabled();
                        this.transition(State.DISABLED);
                        return;
                    }
                    this.log.error("Unexpected Collector response: FORBIDDEN");
                    break;
                }
                case UNSUPPORTED_MEDIA_TYPE:
                case ENTITY_TOO_LARGE: {
                    this.log.error("Invalid ConnectionInformation was sent to the Collector.");
                    break;
                }
                default: {
                    this.log.error("An unknown error occurred when connecting to the Collector.");
                    break;
                }
            }
            this.fireOnHarvestError();
            return;
        }
        final HarvestConfiguration configuration = this.parseHarvesterConfiguration(response);
        if (configuration == null) {
            this.log.error("Unable to configure Harvester using Collector configuration.");
            return;
        }
        this.configureHarvester(configuration);
        StatsEngine.get().sampleTimeMs("Supportability/AgentHealth/Collector/Harvest", response.getResponseTime());
        this.fireOnHarvestConnected();
        this.transition(State.CONNECTED);
    }
    
    protected void connected() {
        this.log.info("Harvester: connected");
        this.log.info("Harvester: Sending " + this.harvestData.getHttpTransactions().count() + " HTTP transactions.");
        this.log.info("Harvester: Sending " + this.harvestData.getHttpErrors().count() + " HTTP errors.");
        this.log.info("Harvester: Sending " + this.harvestData.getActivityTraces().count() + " activity traces.");
        this.harvestData.setAnalyticsEnabled(this.agentConfiguration.getEnableAnalyticsEvents());
        if (this.agentConfiguration.getEnableAnalyticsEvents() && FeatureFlag.featureEnabled(FeatureFlag.AnalyticsEvents)) {
            final EventManager eventManager = AnalyticsControllerImpl.getInstance().getEventManager();
            if (eventManager.isTransmitRequired()) {
                final Set<AnalyticAttribute> sessionAttributes = new HashSet<AnalyticAttribute>();
                sessionAttributes.addAll(AnalyticsControllerImpl.getInstance().getSystemAttributes());
                sessionAttributes.addAll(AnalyticsControllerImpl.getInstance().getUserAttributes());
                this.harvestData.setSessionAttributes(sessionAttributes);
                this.log.info("Harvester: Sending " + this.harvestData.getSessionAttributes().size() + " session attributes.");
                final Collection<AnalyticsEvent> events = eventManager.getQueuedEvents();
                this.harvestData.setAnalyticsEvents(events);
                eventManager.empty();
            }
            this.log.info("Harvester: Sending " + this.harvestData.getAnalyticsEvents().size() + " analytics events.");
        }
        final HarvestResponse response = this.harvestConnection.sendData(this.harvestData);
        if (response == null || response.isUnknown()) {
            this.fireOnHarvestSendFailed();
            return;
        }
        this.harvestData.reset();
        StatsEngine.get().sampleTimeMs("Supportability/AgentHealth/Collector/Harvest", response.getResponseTime());
        this.log.debug("Harvest data response: " + response.getResponseCode());
        this.log.debug("Harvest data response status code: " + response.getStatusCode());
        this.log.debug("Harvest data response BODY: " + response.getResponseBody());
        if (response.isError()) {
            this.fireOnHarvestError();
            switch (response.getResponseCode()) {
                case UNAUTHORIZED:
                case INVALID_AGENT_ID: {
                    this.harvestData.getDataToken().clear();
                    this.transition(State.DISCONNECTED);
                    break;
                }
                case FORBIDDEN: {
                    if (response.isDisableCommand()) {
                        this.log.error("Collector has commanded Agent to disable.");
                        this.transition(State.DISABLED);
                        break;
                    }
                    this.log.error("Unexpected Collector response: FORBIDDEN");
                    this.transition(State.DISCONNECTED);
                    break;
                }
                case UNSUPPORTED_MEDIA_TYPE:
                case ENTITY_TOO_LARGE: {
                    this.log.error("Invalid ConnectionInformation was sent to the Collector.");
                    break;
                }
                default: {
                    this.log.error("An unknown error occurred when connecting to the Collector.");
                    break;
                }
            }
            return;
        }
        final HarvestConfiguration configuration = this.parseHarvesterConfiguration(response);
        if (configuration == null) {
            this.log.error("Unable to configure Harvester using Collector configuration.");
            return;
        }
        this.configureHarvester(configuration);
        this.fireOnHarvestComplete();
    }
    
    protected void disabled() {
        Harvest.stop();
        this.fireOnHarvestDisabled();
    }
    
    protected void execute() {
        this.log.debug("Harvester state: " + this.state);
        this.stateChanged = false;
        try {
            this.expireHarvestData();
            switch (this.state) {
                case UNINITIALIZED: {
                    this.uninitialized();
                    break;
                }
                case DISCONNECTED: {
                    this.fireOnHarvestBefore();
                    this.disconnected();
                    break;
                }
                case CONNECTED: {
                    this.fireOnHarvestBefore();
                    this.fireOnHarvest();
                    this.fireOnHarvestFinalize();
                    TaskQueue.synchronousDequeue();
                    this.connected();
                    break;
                }
                case DISABLED: {
                    this.disabled();
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
        }
        catch (Exception e) {
            this.log.error("Exception encountered while attempting to harvest", e);
            AgentHealth.noticeException(e);
        }
    }
    
    protected void transition(final State newState) {
        if (this.stateChanged) {
            this.log.debug("Ignoring multiple transition: " + newState);
            return;
        }
        if (this.state == newState) {
            return;
        }
        switch (this.state) {
            case UNINITIALIZED: {
                if (this.stateIn(newState, State.DISCONNECTED, newState, State.CONNECTED, State.DISABLED)) {
                    break;
                }
                throw new IllegalStateException();
            }
            case DISCONNECTED: {
                if (this.stateIn(newState, State.UNINITIALIZED, State.CONNECTED, State.DISABLED)) {
                    break;
                }
                throw new IllegalStateException();
            }
            case CONNECTED: {
                if (this.stateIn(newState, State.DISCONNECTED, State.DISABLED)) {
                    break;
                }
                throw new IllegalStateException();
            }
            default: {
                throw new IllegalStateException();
            }
        }
        this.changeState(newState);
    }
    
    private HarvestConfiguration parseHarvesterConfiguration(final HarvestResponse response) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ActivityTraceConfiguration.class, new ActivityTraceConfigurationDeserializer());
        final Gson gson = gsonBuilder.create();
        HarvestConfiguration config = null;
        try {
            config = gson.fromJson(response.getResponseBody(), HarvestConfiguration.class);
        }
        catch (JsonSyntaxException e) {
            this.log.error("Unable to parse collector configuration: " + e.getMessage());
            AgentHealth.noticeException(e);
        }
        return config;
    }
    
    private void configureHarvester(final HarvestConfiguration harvestConfiguration) {
        this.configuration.reconfigure(harvestConfiguration);
        this.harvestData.setDataToken(this.configuration.getDataToken());
        Harvest.setHarvestConfiguration(this.configuration);
    }
    
    private void changeState(final State newState) {
        this.log.debug("Harvester changing state: " + this.state + " -> " + newState);
        if (this.state == State.CONNECTED) {
            if (newState == State.DISCONNECTED) {
                this.fireOnHarvestDisconnected();
            }
            else if (newState == State.DISABLED) {
                this.fireOnHarvestDisabled();
            }
        }
        this.state = newState;
        this.stateChanged = true;
    }
    
    private boolean stateIn(final State testState, final State... legalStates) {
        for (final State state : legalStates) {
            if (testState == state) {
                return true;
            }
        }
        return false;
    }
    
    public State getCurrentState() {
        return this.state;
    }
    
    public boolean isDisabled() {
        return State.DISABLED == this.state;
    }
    
    public void addHarvestListener(final HarvestLifecycleAware harvestAware) {
        if (harvestAware == null) {
            this.log.error("Can't add null harvest listener");
            new Exception().printStackTrace();
            return;
        }
        synchronized (this.harvestListeners) {
            if (this.harvestListeners.contains(harvestAware)) {
                return;
            }
            this.harvestListeners.add(harvestAware);
        }
    }
    
    public void removeHarvestListener(final HarvestLifecycleAware harvestAware) {
        synchronized (this.harvestListeners) {
            if (!this.harvestListeners.contains(harvestAware)) {
                return;
            }
            this.harvestListeners.remove(harvestAware);
        }
    }
    
    public void expireHarvestData() {
        this.expireHttpErrors();
        this.expireHttpTransactions();
        this.expireActivityTraces();
    }
    
    public void expireHttpErrors() {
        final HttpErrors errors = this.harvestData.getHttpErrors();
        synchronized (errors) {
            final Collection<HttpError> oldErrors = new ArrayList<HttpError>();
            final long now = System.currentTimeMillis();
            final long maxAge = this.configuration.getReportMaxTransactionAgeMilliseconds();
            for (final HttpError error : errors.getHttpErrors()) {
                if (error.getTimestamp() < now - maxAge) {
                    this.log.debug("HttpError too old, purging: " + error);
                    oldErrors.add(error);
                }
            }
            for (final HttpError error : oldErrors) {
                errors.removeHttpError(error);
            }
        }
    }
    
    public void expireHttpTransactions() {
        final HttpTransactions transactions = this.harvestData.getHttpTransactions();
        synchronized (transactions) {
            final Collection<HttpTransaction> oldTransactions = new ArrayList<HttpTransaction>();
            final long now = System.currentTimeMillis();
            final long maxAge = this.configuration.getReportMaxTransactionAgeMilliseconds();
            for (final HttpTransaction txn : transactions.getHttpTransactions()) {
                if (txn.getTimestamp() < now - maxAge) {
                    this.log.debug("HttpTransaction too old, purging: " + txn);
                    oldTransactions.add(txn);
                }
            }
            for (final HttpTransaction txn : oldTransactions) {
                transactions.remove(txn);
            }
        }
    }
    
    public void expireActivityTraces() {
        final ActivityTraces traces = this.harvestData.getActivityTraces();
        synchronized (traces) {
            final Collection<ActivityTrace> expiredTraces = new ArrayList<ActivityTrace>();
            final long maxAttempts = this.configuration.getActivity_trace_max_report_attempts();
            for (final ActivityTrace trace : traces.getActivityTraces()) {
                if (trace.getReportAttemptCount() >= maxAttempts) {
                    this.log.debug("ActivityTrace has had " + trace.getReportAttemptCount() + " report attempts, purging: " + trace);
                    expiredTraces.add(trace);
                }
            }
            for (final ActivityTrace trace : expiredTraces) {
                traces.remove(trace);
            }
        }
    }
    
    public void setAgentConfiguration(final AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
    }
    
    public void setHarvestConnection(final HarvestConnection connection) {
        this.harvestConnection = connection;
    }
    
    public HarvestConnection getHarvestConnection() {
        return this.harvestConnection;
    }
    
    public void setHarvestData(final HarvestData harvestData) {
        this.harvestData = harvestData;
    }
    
    public HarvestData getHarvestData() {
        return this.harvestData;
    }
    
    private void fireOnHarvestBefore() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvestBefore();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvestBefore", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private void fireOnHarvestStart() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvestStart();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvestStart", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private void fireOnHarvestStop() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvestStop();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvestStop", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private void fireOnHarvest() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvest();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvest", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private void fireOnHarvestFinalize() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvestFinalize();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvestFinalize", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private void fireOnHarvestDisabled() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvestDisabled();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvestDisabled", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private void fireOnHarvestDisconnected() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvestDisconnected();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvestDisconnected", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private void fireOnHarvestError() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvestError();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvestError", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private void fireOnHarvestSendFailed() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvestSendFailed();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvestSendFailed", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private void fireOnHarvestComplete() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvestComplete();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvestComplete", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private void fireOnHarvestConnected() {
        try {
            for (final HarvestLifecycleAware harvestAware : this.getHarvestListeners()) {
                harvestAware.onHarvestConnected();
            }
        }
        catch (Exception e) {
            this.log.error("Error in fireOnHarvestConnected", e);
            AgentHealth.noticeException(e);
        }
    }
    
    public void setConfiguration(final HarvestConfiguration configuration) {
        this.configuration = configuration;
    }
    
    private Collection<HarvestLifecycleAware> getHarvestListeners() {
        return new ArrayList<HarvestLifecycleAware>(this.harvestListeners);
    }
    
    protected enum State
    {
        UNINITIALIZED, 
        DISCONNECTED, 
        CONNECTED, 
        DISABLED;
    }
}
