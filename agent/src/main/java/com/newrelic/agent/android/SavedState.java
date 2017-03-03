// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android;

import com.newrelic.agent.android.logging.AgentLogManager;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONTokener;
import com.newrelic.agent.android.harvest.Harvest;
import java.util.concurrent.locks.ReentrantLock;
import com.newrelic.agent.android.harvest.DeviceInformation;
import com.newrelic.agent.android.harvest.ApplicationInformation;
import android.content.Context;
import java.util.concurrent.locks.Lock;
import android.content.SharedPreferences;
import com.newrelic.agent.android.harvest.ConnectInformation;
import com.newrelic.agent.android.harvest.HarvestConfiguration;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.harvest.HarvestAdapter;

public class SavedState extends HarvestAdapter
{
    private static final AgentLog log;
    private final String PREFERENCE_FILE_PREFIX = "com.newrelic.android.agent.v1_";
    private final String PREF_MAX_TRANSACTION_COUNT = "maxTransactionCount";
    private final String PREF_MAX_TRANSACTION_AGE = "maxTransactionAgeInSeconds";
    private final String PREF_HARVEST_INTERVAL = "harvestIntervalInSeconds";
    private final String PREF_SERVER_TIMESTAMP = "serverTimestamp";
    private final String PREF_CROSS_PROCESS_ID = "crossProcessId";
    private final String PREF_DATA_TOKEN = "dataToken";
    private final String PREF_APP_TOKEN = "appToken";
    private final String PREF_STACK_TRACE_LIMIT = "stackTraceLimit";
    private final String PREF_RESPONSE_BODY_LIMIT = "responseBodyLimit";
    private final String PREF_COLLECT_NETWORK_ERRORS = "collectNetworkErrors";
    private final String PREF_ERROR_LIMIT = "errorLimit";
    private final String NEW_RELIC_AGENT_DISABLED_VERSION_KEY = "NewRelicAgentDisabledVersion";
    private final String PREF_ACTIVITY_TRACE_MIN_UTILIZATION = "activityTraceMinUtilization";
    private Float activityTraceMinUtilization;
    private final HarvestConfiguration configuration;
    private final String PREF_APP_NAME = "appName";
    private final String PREF_APP_VERSION = "appVersion";
    private final String PREF_APP_BUILD = "appBuild";
    private final String PREF_PACKAGE_ID = "packageId";
    private final String PREF_VERSION_CODE = "versionCode";
    private final String PREF_AGENT_NAME = "agentName";
    private final String PREF_AGENT_VERSION = "agentVersion";
    private final String PREF_DEVICE_ARCHITECTURE = "deviceArchitecture";
    private final String PREF_DEVICE_ID = "deviceId";
    private final String PREF_DEVICE_MODEL = "deviceModel";
    private final String PREF_DEVICE_MANUFACTURER = "deviceManufacturer";
    private final String PREF_DEVICE_RUN_TIME = "deviceRunTime";
    private final String PREF_DEVICE_SIZE = "deviceSize";
    private final String PREF_OS_NAME = "osName";
    private final String PREF_OS_BUILD = "osBuild";
    private final String PREF_OS_VERSION = "osVersion";
    private final String PREF_PLATFORM = "platform";
    private final String PREF_PLATFORM_VERSION = "platformVersion";
    private final ConnectInformation connectInformation;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Lock lock;
    
    public SavedState(final Context context) {
        this.configuration = new HarvestConfiguration();
        this.connectInformation = new ConnectInformation(new ApplicationInformation(), new DeviceInformation());
        this.lock = new ReentrantLock();
        this.prefs = context.getSharedPreferences(this.getPreferenceFileName(context.getPackageName()), 0);
        this.editor = this.prefs.edit();
        this.loadHarvestConfiguration();
        this.loadConnectInformation();
    }
    
    public void saveHarvestConfiguration(final HarvestConfiguration newConfiguration) {
        if (this.configuration.equals(newConfiguration)) {
            return;
        }
        if (!newConfiguration.getDataToken().isValid()) {
            newConfiguration.setData_token(this.configuration.getData_token());
        }
        SavedState.log.info("Saving configuration: " + newConfiguration);
        final String newDataToken = newConfiguration.getDataToken().toJsonString();
        SavedState.log.debug("!! saving data token: " + newDataToken);
        this.save("dataToken", newDataToken);
        this.save("crossProcessId", newConfiguration.getCross_process_id());
        this.save("serverTimestamp", newConfiguration.getServer_timestamp());
        this.save("harvestIntervalInSeconds", (long)newConfiguration.getData_report_period());
        this.save("maxTransactionAgeInSeconds", (long)newConfiguration.getReport_max_transaction_age());
        this.save("maxTransactionCount", (long)newConfiguration.getReport_max_transaction_count());
        this.save("stackTraceLimit", newConfiguration.getStack_trace_limit());
        this.save("responseBodyLimit", newConfiguration.getResponse_body_limit());
        this.save("collectNetworkErrors", newConfiguration.isCollect_network_errors());
        this.save("errorLimit", newConfiguration.getError_limit());
        this.saveActivityTraceMinUtilization((float)newConfiguration.getActivity_trace_min_utilization());
        this.loadHarvestConfiguration();
    }
    
    public void loadHarvestConfiguration() {
        if (this.has("dataToken")) {
            this.configuration.setData_token(this.getDataToken());
        }
        if (this.has("crossProcessId")) {
            this.configuration.setCross_process_id(this.getCrossProcessId());
        }
        if (this.has("serverTimestamp")) {
            this.configuration.setServer_timestamp(this.getServerTimestamp());
        }
        if (this.has("harvestIntervalInSeconds")) {
            this.configuration.setData_report_period((int)this.getHarvestIntervalInSeconds());
        }
        if (this.has("maxTransactionAgeInSeconds")) {
            this.configuration.setReport_max_transaction_age((int)this.getMaxTransactionAgeInSeconds());
        }
        if (this.has("maxTransactionCount")) {
            this.configuration.setReport_max_transaction_count((int)this.getMaxTransactionCount());
        }
        if (this.has("stackTraceLimit")) {
            this.configuration.setStack_trace_limit(this.getStackTraceLimit());
        }
        if (this.has("responseBodyLimit")) {
            this.configuration.setResponse_body_limit(this.getResponseBodyLimit());
        }
        if (this.has("collectNetworkErrors")) {
            this.configuration.setCollect_network_errors(this.isCollectingNetworkErrors());
        }
        if (this.has("errorLimit")) {
            this.configuration.setError_limit(this.getErrorLimit());
        }
        if (this.has("activityTraceMinUtilization")) {
            this.configuration.setActivity_trace_min_utilization(this.getActivityTraceMinUtilization());
        }
        SavedState.log.info("Loaded configuration: " + this.configuration);
    }
    
    public void saveConnectInformation(final ConnectInformation newConnectInformation) {
        if (this.connectInformation.equals(newConnectInformation)) {
            return;
        }
        this.saveApplicationInformation(newConnectInformation.getApplicationInformation());
        this.saveDeviceInformation(newConnectInformation.getDeviceInformation());
        this.loadConnectInformation();
    }
    
    public void saveDeviceId(final String deviceId) {
        this.save("deviceId", deviceId);
        this.connectInformation.getDeviceInformation().setDeviceId(deviceId);
    }
    
    public String getAppToken() {
        return this.getString("appToken");
    }
    
    public void saveAppToken(final String appToken) {
        this.save("appToken", appToken);
    }
    
    private void saveApplicationInformation(final ApplicationInformation applicationInformation) {
        this.save("appName", applicationInformation.getAppName());
        this.save("appVersion", applicationInformation.getAppVersion());
        this.save("appBuild", applicationInformation.getAppBuild());
        this.save("packageId", applicationInformation.getPackageId());
        this.save("versionCode", applicationInformation.getVersionCode());
    }
    
    private void saveDeviceInformation(final DeviceInformation deviceInformation) {
        this.save("agentName", deviceInformation.getAgentName());
        this.save("agentVersion", deviceInformation.getAgentVersion());
        this.save("deviceArchitecture", deviceInformation.getArchitecture());
        this.save("deviceId", deviceInformation.getDeviceId());
        this.save("deviceModel", deviceInformation.getModel());
        this.save("deviceManufacturer", deviceInformation.getManufacturer());
        this.save("deviceRunTime", deviceInformation.getRunTime());
        this.save("deviceSize", deviceInformation.getSize());
        this.save("osName", deviceInformation.getOsName());
        this.save("osBuild", deviceInformation.getOsBuild());
        this.save("osVersion", deviceInformation.getOsVersion());
        this.save("platform", deviceInformation.getApplicationPlatform().toString());
        this.save("platformVersion", deviceInformation.getApplicationPlatformVersion());
    }
    
    public void loadConnectInformation() {
        final ApplicationInformation applicationInformation = new ApplicationInformation();
        if (this.has("appName")) {
            applicationInformation.setAppName(this.getAppName());
        }
        if (this.has("appVersion")) {
            applicationInformation.setAppVersion(this.getAppVersion());
        }
        if (this.has("appBuild")) {
            applicationInformation.setAppBuild(this.getAppBuild());
        }
        if (this.has("packageId")) {
            applicationInformation.setPackageId(this.getPackageId());
        }
        if (this.has("versionCode")) {
            applicationInformation.setVersionCode(this.getVersionCode());
        }
        final DeviceInformation deviceInformation = new DeviceInformation();
        if (this.has("agentName")) {
            deviceInformation.setAgentName(this.getAgentName());
        }
        if (this.has("agentVersion")) {
            deviceInformation.setAgentVersion(this.getAgentVersion());
        }
        if (this.has("deviceArchitecture")) {
            deviceInformation.setArchitecture(this.getDeviceArchitecture());
        }
        if (this.has("deviceId")) {
            deviceInformation.setDeviceId(this.getDeviceId());
        }
        if (this.has("deviceModel")) {
            deviceInformation.setModel(this.getDeviceModel());
        }
        if (this.has("deviceManufacturer")) {
            deviceInformation.setManufacturer(this.getDeviceManufacturer());
        }
        if (this.has("deviceRunTime")) {
            deviceInformation.setRunTime(this.getDeviceRunTime());
        }
        if (this.has("deviceSize")) {
            deviceInformation.setSize(this.getDeviceSize());
        }
        if (this.has("osName")) {
            deviceInformation.setOsName(this.getOsName());
        }
        if (this.has("osBuild")) {
            deviceInformation.setOsBuild(this.getOsBuild());
        }
        if (this.has("osVersion")) {
            deviceInformation.setOsVersion(this.getOsVersion());
        }
        if (this.has("platform")) {
            deviceInformation.setApplicationPlatform(this.getPlatform());
        }
        if (this.has("platformVersion")) {
            deviceInformation.setApplicationPlatformVersion(this.getPlatformVersion());
        }
        this.connectInformation.setApplicationInformation(applicationInformation);
        this.connectInformation.setDeviceInformation(deviceInformation);
    }
    
    public HarvestConfiguration getHarvestConfiguration() {
        return this.configuration;
    }
    
    public ConnectInformation getConnectInformation() {
        return this.connectInformation;
    }
    
    private boolean has(final String key) {
        return this.prefs.contains(key);
    }
    
    public void onHarvestConnected() {
        this.saveHarvestConfiguration(Harvest.getHarvestConfiguration());
    }
    
    public void onHarvestComplete() {
        this.saveHarvestConfiguration(Harvest.getHarvestConfiguration());
    }
    
    public void onHarvestDisconnected() {
        SavedState.log.info("Clearing harvest configuration.");
        this.clear();
    }
    
    public void onHarvestDisabled() {
        final String agentVersion = Agent.getDeviceInformation().getAgentVersion();
        SavedState.log.info("Disabling agent version " + agentVersion);
        this.saveDisabledVersion(agentVersion);
    }
    
    public void save(final String key, final String value) {
        this.lock.lock();
        try {
            this.editor.putString(key, value);
            this.editor.commit();
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public void save(final String key, final boolean value) {
        this.lock.lock();
        try {
            this.editor.putBoolean(key, value);
            this.editor.commit();
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public void save(final String key, final int value) {
        this.lock.lock();
        try {
            this.editor.putInt(key, value);
            this.editor.commit();
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public void save(final String key, final long value) {
        this.lock.lock();
        try {
            this.editor.putLong(key, value);
            this.editor.commit();
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public void save(final String key, final float value) {
        this.lock.lock();
        try {
            this.editor.putFloat(key, value);
            this.editor.commit();
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public String getString(final String key) {
        if (!this.prefs.contains(key)) {
            return null;
        }
        return this.prefs.getString(key, (String)null);
    }
    
    public boolean getBoolean(final String key) {
        return this.prefs.getBoolean(key, false);
    }
    
    public long getLong(final String key) {
        return this.prefs.getLong(key, 0L);
    }
    
    public int getInt(final String key) {
        return this.prefs.getInt(key, 0);
    }
    
    public Float getFloat(final String key) {
        if (!this.prefs.contains(key)) {
            return null;
        }
        final float f = this.prefs.getFloat(key, 0.0f);
        return (int)(f * 100.0f) / 100.0f;
    }
    
    public String getDisabledVersion() {
        return this.getString("NewRelicAgentDisabledVersion");
    }
    
    public void saveDisabledVersion(final String version) {
        this.save("NewRelicAgentDisabledVersion", version);
    }
    
    public int[] getDataToken() {
        final int[] dataToken = new int[2];
        final String dataTokenString = this.getString("dataToken");
        if (dataTokenString == null) {
            return null;
        }
        try {
            final JSONTokener tokener = new JSONTokener(dataTokenString);
            if (tokener == null) {
                return null;
            }
            final JSONArray array = (JSONArray)tokener.nextValue();
            if (array == null) {
                return null;
            }
            dataToken[0] = array.getInt(0);
            dataToken[1] = array.getInt(1);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return dataToken;
    }
    
    public String getCrossProcessId() {
        return this.getString("crossProcessId");
    }
    
    public boolean isCollectingNetworkErrors() {
        return this.getBoolean("collectNetworkErrors");
    }
    
    public long getServerTimestamp() {
        return this.getLong("serverTimestamp");
    }
    
    public long getHarvestInterval() {
        return this.getLong("harvestIntervalInSeconds");
    }
    
    public long getMaxTransactionAge() {
        return this.getLong("maxTransactionAgeInSeconds");
    }
    
    public long getMaxTransactionCount() {
        return this.getLong("maxTransactionCount");
    }
    
    public int getStackTraceLimit() {
        return this.getInt("stackTraceLimit");
    }
    
    public int getResponseBodyLimit() {
        return this.getInt("responseBodyLimit");
    }
    
    public int getErrorLimit() {
        return this.getInt("errorLimit");
    }
    
    public void saveActivityTraceMinUtilization(final float activityTraceMinUtilization) {
        this.activityTraceMinUtilization = activityTraceMinUtilization;
        this.save("activityTraceMinUtilization", activityTraceMinUtilization);
    }
    
    public float getActivityTraceMinUtilization() {
        if (this.activityTraceMinUtilization == null) {
            this.activityTraceMinUtilization = this.getFloat("activityTraceMinUtilization");
        }
        return this.activityTraceMinUtilization;
    }
    
    public long getHarvestIntervalInSeconds() {
        return this.getHarvestInterval();
    }
    
    public long getMaxTransactionAgeInSeconds() {
        return this.getMaxTransactionAge();
    }
    
    public String getAppName() {
        return this.getString("appName");
    }
    
    public String getAppVersion() {
        return this.getString("appVersion");
    }
    
    public int getVersionCode() {
        return this.getInt("versionCode");
    }
    
    public String getAppBuild() {
        return this.getString("appBuild");
    }
    
    public String getPackageId() {
        return this.getString("packageId");
    }
    
    public String getAgentName() {
        return this.getString("agentName");
    }
    
    public String getAgentVersion() {
        return this.getString("agentVersion");
    }
    
    public String getDeviceArchitecture() {
        return this.getString("deviceArchitecture");
    }
    
    public String getDeviceId() {
        return this.getString("deviceId");
    }
    
    public String getDeviceModel() {
        return this.getString("deviceModel");
    }
    
    public String getDeviceManufacturer() {
        return this.getString("deviceManufacturer");
    }
    
    public String getDeviceRunTime() {
        return this.getString("deviceRunTime");
    }
    
    public String getDeviceSize() {
        return this.getString("deviceSize");
    }
    
    public String getOsName() {
        return this.getString("osName");
    }
    
    public String getOsBuild() {
        return this.getString("osBuild");
    }
    
    public String getOsVersion() {
        return this.getString("osVersion");
    }
    
    public String getApplicationPlatform() {
        return this.getString("platform");
    }
    
    public String getApplicationPlatformVersion() {
        return this.getString("platformVersion");
    }
    
    public ApplicationPlatform getPlatform() {
        ApplicationPlatform applicationPlatform = ApplicationPlatform.Native;
        try {
            applicationPlatform = ApplicationPlatform.valueOf(this.getString("platform"));
        }
        catch (IllegalArgumentException ex) {}
        return applicationPlatform;
    }
    
    public String getPlatformVersion() {
        return this.getString("platformVersion");
    }
    
    private String getPreferenceFileName(final String packageName) {
        return "com.newrelic.android.agent.v1_" + packageName;
    }
    
    public void clear() {
        this.lock.lock();
        try {
            this.editor.clear();
            this.editor.commit();
            this.configuration.setDefaultValues();
        }
        finally {
            this.lock.unlock();
        }
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
