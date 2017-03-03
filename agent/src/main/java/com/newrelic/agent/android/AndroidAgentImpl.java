// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android;

import com.newrelic.agent.android.logging.AgentLogManager;
import proguard.canary.NewRelicCanary;
import android.os.Looper;
import com.newrelic.agent.android.util.PersistentUUID;
import android.os.Bundle;
import android.location.LocationManager;
import android.location.Address;
import java.io.IOException;
import android.location.Geocoder;
import android.location.Location;
import com.newrelic.agent.android.background.ApplicationStateEvent;
import com.newrelic.agent.android.api.v1.ConnectionEvent;
import com.newrelic.agent.android.util.Connectivity;
import com.newrelic.agent.android.metric.MetricUnit;
import com.newrelic.agent.android.instrumentation.MetricCategory;
import java.util.List;
import com.newrelic.agent.android.api.v1.DeviceForm;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.content.pm.PackageManager;
import com.newrelic.agent.android.harvest.AgentHealth;
import android.os.StatFs;
import android.os.Environment;
import android.app.ActivityManager;
import com.newrelic.agent.android.harvest.EnvironmentInformation;
import com.newrelic.agent.android.analytics.AnalyticAttribute;
import com.newrelic.agent.android.harvest.ConnectInformation;
import com.newrelic.agent.android.sample.Sampler;
import com.newrelic.agent.android.crashes.CrashReporter;
import com.newrelic.agent.android.stats.StatsEngine;
import com.newrelic.agent.android.measurement.consumer.MeasurementConsumer;
import java.text.MessageFormat;
import com.newrelic.agent.android.harvest.HarvestLifecycleAware;
import com.newrelic.agent.android.harvest.Harvest;
import com.newrelic.agent.android.analytics.AnalyticsControllerImpl;
import android.content.ComponentCallbacks;
import com.newrelic.agent.android.util.UiBackgroundListener;
import android.app.Application;
import com.newrelic.agent.android.util.ActivityLifecycleBackgroundListener;
import android.os.Build;
import com.newrelic.agent.android.background.ApplicationStateMonitor;
import com.newrelic.agent.android.analytics.AnalyticAttributeStore;
import com.newrelic.agent.android.util.SharedPrefsAnalyticAttributeStore;
import com.newrelic.agent.android.crashes.CrashStore;
import com.newrelic.agent.android.util.JsonCrashStore;
import com.newrelic.agent.android.tracing.TraceMachine;
import com.newrelic.agent.android.util.AndroidEncoder;
import java.util.concurrent.locks.ReentrantLock;
import com.newrelic.agent.android.api.common.TransactionData;
import java.util.Comparator;
import com.newrelic.agent.android.sample.MachineMeasurementConsumer;
import com.newrelic.agent.android.harvest.ApplicationInformation;
import com.newrelic.agent.android.harvest.DeviceInformation;
import com.newrelic.agent.android.util.Encoder;
import java.util.concurrent.locks.Lock;
import android.location.LocationListener;
import android.content.Context;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.api.v2.TraceMachineInterface;
import com.newrelic.agent.android.background.ApplicationStateListener;
import com.newrelic.agent.android.api.v1.ConnectionListener;

public class AndroidAgentImpl implements AgentImpl, ConnectionListener, ApplicationStateListener, TraceMachineInterface
{
    private static final float LOCATION_ACCURACY_THRESHOLD = 500.0f;
    private static final AgentLog log;
    private final Context context;
    private SavedState savedState;
    private LocationListener locationListener;
    private final Lock lock;
    private final Encoder encoder;
    private DeviceInformation deviceInformation;
    private ApplicationInformation applicationInformation;
    private final AgentConfiguration agentConfiguration;
    private MachineMeasurementConsumer machineMeasurementConsumer;
    private static final Comparator<TransactionData> cmp;
    
    public AndroidAgentImpl(final Context context, final AgentConfiguration agentConfiguration) throws AgentInitializationException {
        this.lock = new ReentrantLock();
        this.encoder = new AndroidEncoder();
        this.context = appContext(context);
        this.agentConfiguration = agentConfiguration;
        this.savedState = new SavedState(this.context);
        if (this.isDisabled()) {
            throw new AgentInitializationException("This version of the agent has been disabled");
        }
        this.initApplicationInformation();
        if (agentConfiguration.useLocationService() && this.context.getPackageManager().checkPermission("android.permission.ACCESS_FINE_LOCATION", this.getApplicationInformation().getPackageId()) == 0) {
            AndroidAgentImpl.log.debug("Location stats enabled");
            this.addLocationListener();
        }
        TraceMachine.setTraceMachineInterface(this);
        agentConfiguration.setCrashStore(new JsonCrashStore(context));
        agentConfiguration.setAnalyticAttributeStore(new SharedPrefsAnalyticAttributeStore(context));
        ApplicationStateMonitor.getInstance().addApplicationStateListener(this);
        if (Build.VERSION.SDK_INT >= 14) {
            UiBackgroundListener backgroundListener;
            if (Agent.getUnityInstrumentationFlag().equals("YES")) {
                backgroundListener = new ActivityLifecycleBackgroundListener();
                if (backgroundListener instanceof Application.ActivityLifecycleCallbacks) {
                    try {
                        if (context.getApplicationContext() instanceof Application) {
                            final Application application = (Application)context.getApplicationContext();
                            application.registerActivityLifecycleCallbacks((Application.ActivityLifecycleCallbacks)backgroundListener);
                        }
                    }
                    catch (Exception ex) {}
                }
            }
            else {
                backgroundListener = new UiBackgroundListener();
            }
            context.registerComponentCallbacks((ComponentCallbacks)backgroundListener);
            this.setupSession();
        }
    }
    
    protected void initialize() {
        this.setupSession();
        AnalyticsControllerImpl.getInstance();
        AnalyticsControllerImpl.initialize(this.agentConfiguration, this);
        Harvest.addHarvestListener(this.savedState);
        Harvest.initialize(this.agentConfiguration);
        Harvest.setHarvestConfiguration(this.savedState.getHarvestConfiguration());
        Harvest.setHarvestConnectInformation(this.savedState.getConnectInformation());
        Measurements.initialize();
        AndroidAgentImpl.log.info(MessageFormat.format("New Relic Agent v{0}", Agent.getVersion()));
        AndroidAgentImpl.log.verbose(MessageFormat.format("Application token: {0}", this.agentConfiguration.getApplicationToken()));
        Measurements.addMeasurementConsumer(this.machineMeasurementConsumer = new MachineMeasurementConsumer());
        StatsEngine.get().inc("Supportability/AgentHealth/UncaughtExceptionHandler/" + this.getUnhandledExceptionHandlerName());
        CrashReporter.initialize(this.agentConfiguration);
        Sampler.init(this.context);
    }
    
    protected void setupSession() {
        this.agentConfiguration.provideSessionId();
    }
    
    protected void finalizeSession() {
    }
    
    public boolean updateSavedConnectInformation() {
        final ConnectInformation savedConnectInformation = this.savedState.getConnectInformation();
        final ConnectInformation newConnectInformation = new ConnectInformation(this.getApplicationInformation(), this.getDeviceInformation());
        final String savedAppToken = this.savedState.getAppToken();
        if (!newConnectInformation.equals(savedConnectInformation) || !this.agentConfiguration.getApplicationToken().equals(savedAppToken)) {
            if (newConnectInformation.getApplicationInformation().isAppUpgrade(savedConnectInformation.getApplicationInformation())) {
                StatsEngine.get().inc("Mobile/App/Upgrade");
                final AnalyticAttribute attribute = new AnalyticAttribute("upgradeFrom", savedConnectInformation.getApplicationInformation().getAppVersion());
                AnalyticsControllerImpl.getInstance().addAttributeUnchecked(attribute, false);
            }
            this.savedState.clear();
            this.savedState.saveConnectInformation(newConnectInformation);
            this.savedState.saveAppToken(this.agentConfiguration.getApplicationToken());
            return true;
        }
        return false;
    }
    
    public DeviceInformation getDeviceInformation() {
        if (this.deviceInformation != null) {
            return this.deviceInformation;
        }
        final DeviceInformation info = new DeviceInformation();
        info.setOsName("Android");
        info.setOsVersion(Build.VERSION.RELEASE);
        info.setOsBuild(Build.VERSION.INCREMENTAL);
        info.setModel(Build.MODEL);
        info.setAgentName("AndroidAgent");
        info.setAgentVersion(Agent.getVersion());
        info.setManufacturer(Build.MANUFACTURER);
        info.setDeviceId(this.getUUID());
        info.setArchitecture(System.getProperty("os.arch"));
        info.setRunTime(System.getProperty("java.vm.version"));
        info.setSize(deviceForm(this.context).name().toLowerCase());
        info.setApplicationPlatform(this.agentConfiguration.getApplicationPlatform());
        info.setApplicationPlatformVersion(this.agentConfiguration.getApplicationPlatformVersion());
        return this.deviceInformation = info;
    }
    
    public EnvironmentInformation getEnvironmentInformation() {
        final EnvironmentInformation envInfo = new EnvironmentInformation();
        final ActivityManager activityManager = (ActivityManager)this.context.getSystemService("activity");
        final long[] free = new long[2];
        try {
            final StatFs rootStatFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
            final StatFs externalStatFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (Build.VERSION.SDK_INT >= 18) {
                free[0] = rootStatFs.getAvailableBlocksLong() * rootStatFs.getBlockSizeLong();
                free[1] = externalStatFs.getAvailableBlocksLong() * rootStatFs.getBlockSizeLong();
            }
            else {
                free[0] = rootStatFs.getAvailableBlocks() * rootStatFs.getBlockSize();
                free[1] = externalStatFs.getAvailableBlocks() * externalStatFs.getBlockSize();
            }
        }
        catch (Exception e) {
            AgentHealth.noticeException(e);
        }
        finally {
            if (free[0] < 0L) {
                free[0] = 0L;
            }
            if (free[1] < 0L) {
                free[1] = 0L;
            }
            envInfo.setDiskAvailable(free);
        }
        envInfo.setMemoryUsage(Sampler.sampleMemory(activityManager).getSampleValue().asLong());
        envInfo.setOrientation(this.context.getResources().getConfiguration().orientation);
        envInfo.setNetworkStatus(this.getNetworkCarrier());
        envInfo.setNetworkWanType(this.getNetworkWanType());
        return envInfo;
    }
    
    public void initApplicationInformation() throws AgentInitializationException {
        if (this.applicationInformation != null) {
            AndroidAgentImpl.log.debug("attempted to reinitialize ApplicationInformation.");
            return;
        }
        final String packageName = this.context.getPackageName();
        final PackageManager packageManager = this.context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        }
        catch (PackageManager.NameNotFoundException e) {
            throw new AgentInitializationException("Could not determine package version: " + e.getMessage());
        }
        String appVersion = this.agentConfiguration.getCustomApplicationVersion();
        if (TextUtils.isEmpty((CharSequence)appVersion)) {
            if (packageInfo == null || packageInfo.versionName == null || packageInfo.versionName.length() <= 0) {
                throw new AgentInitializationException("Your app doesn't appear to have a version defined. Ensure you have defined 'versionName' in your manifest.");
            }
            appVersion = packageInfo.versionName;
        }
        AndroidAgentImpl.log.debug("Using application version " + appVersion);
        String appName;
        try {
            final ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
            if (info != null) {
                appName = packageManager.getApplicationLabel(info).toString();
            }
            else {
                appName = packageName;
            }
        }
        catch (PackageManager.NameNotFoundException e2) {
            AndroidAgentImpl.log.warning(e2.toString());
            appName = packageName;
        }
        catch (SecurityException e3) {
            AndroidAgentImpl.log.warning(e3.toString());
            appName = packageName;
        }
        AndroidAgentImpl.log.debug("Using application name " + appName);
        String build = this.agentConfiguration.getCustomBuildIdentifier();
        if (TextUtils.isEmpty((CharSequence)build)) {
            if (packageInfo != null) {
                build = String.valueOf(packageInfo.versionCode);
            }
            else {
                build = "";
                AndroidAgentImpl.log.warning("Your app doesn't appear to have a version code defined. Ensure you have defined 'versionCode' in your manifest.");
            }
        }
        AndroidAgentImpl.log.debug("Using build  " + build);
        (this.applicationInformation = new ApplicationInformation(appName, appVersion, packageName, build)).setVersionCode(packageInfo.versionCode);
    }
    
    public ApplicationInformation getApplicationInformation() {
        return this.applicationInformation;
    }
    
    public long getSessionDurationMillis() {
        return Harvest.getMillisSinceStart();
    }
    
    private static DeviceForm deviceForm(final Context context) {
        final int deviceSize = context.getResources().getConfiguration().screenLayout & 0xF;
        switch (deviceSize) {
            case 1: {
                return DeviceForm.SMALL;
            }
            case 2: {
                return DeviceForm.NORMAL;
            }
            case 3: {
                return DeviceForm.LARGE;
            }
            default: {
                if (deviceSize > 3) {
                    return DeviceForm.XLARGE;
                }
                return DeviceForm.UNKNOWN;
            }
        }
    }
    
    private static Context appContext(final Context context) {
        if (!(context instanceof Application)) {
            return context.getApplicationContext();
        }
        return context;
    }
    
    @Deprecated
    public void addTransactionData(final TransactionData transactionData) {
    }
    
    @Deprecated
    public void mergeTransactionData(final List<TransactionData> transactionDataList) {
    }
    
    @Deprecated
    public List<TransactionData> getAndClearTransactionData() {
        return null;
    }
    
    public String getCrossProcessId() {
        this.lock.lock();
        try {
            return this.savedState.getCrossProcessId();
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public int getStackTraceLimit() {
        this.lock.lock();
        try {
            return this.savedState.getStackTraceLimit();
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public int getResponseBodyLimit() {
        this.lock.lock();
        try {
            return this.savedState.getHarvestConfiguration().getResponse_body_limit();
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public void start() {
        if (!this.isDisabled()) {
            this.initialize();
            Harvest.start();
        }
        else {
            this.stop(false);
        }
    }
    
    public void stop() {
        this.stop(true);
    }
    
    private void stop(final boolean finalSendData) {
        this.finalizeSession();
        Sampler.shutdown();
        TraceMachine.haltTracing();
        final int eventsRecorded = AnalyticsControllerImpl.getInstance().getEventManager().getEventsRecorded();
        final int eventsEjected = AnalyticsControllerImpl.getInstance().getEventManager().getEventsEjected();
        Measurements.addCustomMetric("Supportability/Events/Recorded", MetricCategory.NONE.name(), eventsRecorded, eventsEjected, eventsEjected, MetricUnit.OPERATIONS, MetricUnit.OPERATIONS);
        if (finalSendData) {
            if (this.isUIThread()) {
                StatsEngine.get().inc("Supportability/AgentHealth/HarvestOnMainThread");
            }
            Harvest.harvestNow();
        }
        AnalyticsControllerImpl.shutdown();
        TraceMachine.clearActivityHistory();
        Harvest.shutdown();
        Measurements.shutdown();
    }
    
    public void disable() {
        AndroidAgentImpl.log.warning("PERMANENTLY DISABLING AGENT v" + Agent.getVersion());
        try {
            this.savedState.saveDisabledVersion(Agent.getVersion());
        }
        finally {
            try {
                this.stop(false);
            }
            finally {
                Agent.setImpl(NullAgentImpl.instance);
            }
        }
    }
    
    public boolean isDisabled() {
        return Agent.getVersion().equals(this.savedState.getDisabledVersion());
    }
    
    public String getNetworkCarrier() {
        return Connectivity.carrierNameFromContext(this.context);
    }
    
    public String getNetworkWanType() {
        return Connectivity.wanType(this.context);
    }
    
    public static void init(final Context context, final AgentConfiguration agentConfiguration) {
        try {
            Agent.setImpl(new AndroidAgentImpl(context, agentConfiguration));
            Agent.start();
        }
        catch (AgentInitializationException e) {
            AndroidAgentImpl.log.error("Failed to initialize the agent: " + e.toString());
        }
    }
    
    @Deprecated
    public void connected(final ConnectionEvent e) {
        AndroidAgentImpl.log.error("AndroidAgentImpl: connected ");
    }
    
    @Deprecated
    public void disconnected(final ConnectionEvent e) {
        this.savedState.clear();
    }
    
    public void applicationForegrounded(final ApplicationStateEvent e) {
        AndroidAgentImpl.log.info("AndroidAgentImpl: application foregrounded ");
        this.start();
    }
    
    public void applicationBackgrounded(final ApplicationStateEvent e) {
        AndroidAgentImpl.log.info("AndroidAgentImpl: application backgrounded ");
        this.stop();
    }
    
    public void setLocation(final String countryCode, final String adminRegion) {
        if (countryCode == null || adminRegion == null) {
            throw new IllegalArgumentException("Country code and administrative region are required.");
        }
    }
    
    public void setLocation(final Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location must not be null.");
        }
        final Geocoder coder = new Geocoder(this.context);
        List<Address> addresses = null;
        try {
            addresses = (List<Address>)coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        }
        catch (IOException e) {
            AndroidAgentImpl.log.error("Unable to geocode location: " + e.toString());
        }
        if (addresses == null || addresses.size() == 0) {
            return;
        }
        final Address address = addresses.get(0);
        if (address == null) {
            return;
        }
        final String countryCode = address.getCountryCode();
        final String adminArea = address.getAdminArea();
        if (countryCode != null && adminArea != null) {
            this.setLocation(countryCode, adminArea);
            this.removeLocationListener();
        }
    }
    
    private void addLocationListener() {
        final LocationManager locationManager = (LocationManager)this.context.getSystemService("location");
        if (locationManager == null) {
            AndroidAgentImpl.log.error("Unable to retrieve reference to LocationManager. Disabling location listener.");
            return;
        }
        locationManager.requestLocationUpdates("passive", 1000L, 0.0f, this.locationListener = (LocationListener)new LocationListener() {
            public void onLocationChanged(final Location location) {
                if (AndroidAgentImpl.this.isAccurate(location)) {
                    AndroidAgentImpl.this.setLocation(location);
                }
            }
            
            public void onProviderDisabled(final String provider) {
                if ("passive".equals(provider)) {
                    AndroidAgentImpl.this.removeLocationListener();
                }
            }
            
            public void onProviderEnabled(final String provider) {
            }
            
            public void onStatusChanged(final String provider, final int status, final Bundle extras) {
            }
        });
    }
    
    private void removeLocationListener() {
        if (this.locationListener == null) {
            return;
        }
        final LocationManager locationManager = (LocationManager)this.context.getSystemService("location");
        if (locationManager == null) {
            AndroidAgentImpl.log.error("Unable to retrieve reference to LocationManager. Can't unregister location listener.");
            return;
        }
        synchronized (locationManager) {
            locationManager.removeUpdates(this.locationListener);
            this.locationListener = null;
        }
    }
    
    private boolean isAccurate(final Location location) {
        return location != null && 500.0f >= location.getAccuracy();
    }
    
    private String getUUID() {
        String uuid = this.savedState.getConnectInformation().getDeviceInformation().getDeviceId();
        if (TextUtils.isEmpty((CharSequence)uuid)) {
            final PersistentUUID persistentUUID = new PersistentUUID(this.context);
            uuid = persistentUUID.getPersistentUUID();
            this.savedState.saveDeviceId(uuid);
        }
        return uuid;
    }
    
    private String getUnhandledExceptionHandlerName() {
        try {
            return Thread.getDefaultUncaughtExceptionHandler().getClass().getName();
        }
        catch (Exception e) {
            return "unknown";
        }
    }
    
    public Encoder getEncoder() {
        return this.encoder;
    }
    
    public long getCurrentThreadId() {
        return Thread.currentThread().getId();
    }
    
    public boolean isUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
    
    public String getCurrentThreadName() {
        return Thread.currentThread().getName();
    }
    
    private void pokeCanary() {
        NewRelicCanary.canaryMethod();
    }
    
    protected SavedState getSavedState() {
        return this.savedState;
    }
    
    protected void setSavedState(final SavedState savedState) {
        this.savedState = savedState;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
        cmp = new Comparator<TransactionData>() {
            public int compare(final TransactionData lhs, final TransactionData rhs) {
                if (lhs.getTimestamp() > rhs.getTimestamp()) {
                    return -1;
                }
                if (lhs.getTimestamp() < rhs.getTimestamp()) {
                    return 1;
                }
                return 0;
            }
        };
    }
}
