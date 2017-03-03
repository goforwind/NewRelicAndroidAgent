// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android;

import java.util.UUID;
import com.newrelic.agent.android.analytics.AnalyticAttributeStore;
import com.newrelic.agent.android.crashes.CrashStore;

public class AgentConfiguration
{
    private static final String DEFAULT_COLLECTOR_HOST = "mobile-collector.newrelic.com";
    private static final String DEFAULT_CRASH_COLLECTOR_HOST = "mobile-crash.newrelic.com";
    private String collectorHost;
    private String crashCollectorHost;
    private String applicationToken;
    private String appName;
    private boolean useSsl;
    private boolean useLocationService;
    private boolean reportCrashes;
    private boolean enableAnalyticsEvents;
    private String sessionID;
    private String customApplicationVersion;
    private String customBuildId;
    private CrashStore crashStore;
    private AnalyticAttributeStore analyticAttributeStore;
    private ApplicationPlatform applicationPlatform;
    private String applicationPlatformVersion;
    
    public AgentConfiguration() {
        this.collectorHost = "mobile-collector.newrelic.com";
        this.crashCollectorHost = "mobile-crash.newrelic.com";
        this.useSsl = true;
        this.reportCrashes = true;
        this.enableAnalyticsEvents = true;
        this.sessionID = this.provideSessionId();
        this.customApplicationVersion = null;
        this.customBuildId = null;
        this.applicationPlatform = ApplicationPlatform.Native;
        this.applicationPlatformVersion = Agent.getVersion();
    }
    
    public String getApplicationToken() {
        return this.applicationToken;
    }
    
    public void setApplicationToken(final String applicationToken) {
        this.applicationToken = applicationToken;
    }
    
    public String getAppName() {
        return this.appName;
    }
    
    public void setAppName(final String appName) {
        this.appName = appName;
    }
    
    public String getCollectorHost() {
        return this.collectorHost;
    }
    
    public void setCollectorHost(final String collectorHost) {
        this.collectorHost = collectorHost;
    }
    
    public String getCrashCollectorHost() {
        return this.crashCollectorHost;
    }
    
    public void setCrashCollectorHost(final String crashCollectorHost) {
        this.crashCollectorHost = crashCollectorHost;
    }
    
    public boolean useSsl() {
        return this.useSsl;
    }
    
    public void setUseSsl(final boolean useSsl) {
        this.useSsl = useSsl;
    }
    
    public boolean useLocationService() {
        return this.useLocationService;
    }
    
    public void setUseLocationService(final boolean useLocationService) {
        this.useLocationService = useLocationService;
    }
    
    public boolean getReportCrashes() {
        return this.reportCrashes;
    }
    
    public void setReportCrashes(final boolean reportCrashes) {
        this.reportCrashes = reportCrashes;
    }
    
    public CrashStore getCrashStore() {
        return this.crashStore;
    }
    
    public void setCrashStore(final CrashStore crashStore) {
        this.crashStore = crashStore;
    }
    
    public AnalyticAttributeStore getAnalyticAttributeStore() {
        return this.analyticAttributeStore;
    }
    
    public void setAnalyticAttributeStore(final AnalyticAttributeStore analyticAttributeStore) {
        this.analyticAttributeStore = analyticAttributeStore;
    }
    
    public boolean getEnableAnalyticsEvents() {
        return this.enableAnalyticsEvents;
    }
    
    public void setEnableAnalyticsEvents(final boolean enableAnalyticsEvents) {
        this.enableAnalyticsEvents = enableAnalyticsEvents;
    }
    
    public String getSessionID() {
        return this.sessionID;
    }
    
    public void setSessionID(final String sessionID) {
        this.sessionID = sessionID;
    }
    
    public String getCustomApplicationVersion() {
        return this.customApplicationVersion;
    }
    
    public void setCustomApplicationVersion(final String customApplicationVersion) {
        this.customApplicationVersion = customApplicationVersion;
    }
    
    public String getCustomBuildIdentifier() {
        return this.customBuildId;
    }
    
    public void setCustomBuildIdentifier(final String customBuildId) {
        this.customBuildId = customBuildId;
    }
    
    public ApplicationPlatform getApplicationPlatform() {
        return this.applicationPlatform;
    }
    
    public void setApplicationPlatform(final ApplicationPlatform applicationPlatform) {
        this.applicationPlatform = applicationPlatform;
    }
    
    public String getApplicationPlatformVersion() {
        return (this.applicationPlatformVersion == null || this.applicationPlatformVersion.isEmpty()) ? Agent.getVersion() : this.applicationPlatformVersion;
    }
    
    public void setApplicationPlatformVersion(final String applicationPlatformVersion) {
        this.applicationPlatformVersion = applicationPlatformVersion;
    }
    
    protected String provideSessionId() {
        return this.sessionID = UUID.randomUUID().toString();
    }
}
