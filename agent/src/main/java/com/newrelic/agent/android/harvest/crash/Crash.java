// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.crash;

import com.newrelic.com.google.gson.JsonParser;
import com.newrelic.agent.android.harvest.DataToken;
import java.util.Iterator;
import com.newrelic.agent.android.harvest.Harvest;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.agent.android.util.SafeJsonPrimitive;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.agent.android.tracing.TraceMachine;
import com.newrelic.agent.android.AgentImpl;
import java.util.HashSet;
import com.newrelic.agent.android.harvest.ActivitySighting;
import java.util.ArrayList;
import com.newrelic.agent.android.crashes.CrashReporter;
import com.newrelic.agent.android.Agent;
import com.newrelic.agent.android.analytics.AnalyticsEvent;
import java.util.Collection;
import com.newrelic.agent.android.analytics.AnalyticAttribute;
import java.util.Set;
import com.newrelic.agent.android.harvest.ActivityHistory;
import java.util.List;
import java.util.UUID;
import com.newrelic.agent.android.harvest.type.HarvestableObject;

public class Crash extends HarvestableObject
{
    public static final int PROTOCOL_VERSION = 1;
    public static final int MAX_UPLOAD_COUNT = 3;
    private final UUID uuid;
    private final String buildId;
    private final long timestamp;
    private final String appToken;
    private boolean analyticsEnabled;
    private DeviceInfo deviceInfo;
    private ApplicationInfo applicationInfo;
    private ExceptionInfo exceptionInfo;
    private List<ThreadInfo> threads;
    private ActivityHistory activityHistory;
    private Set<AnalyticAttribute> sessionAttributes;
    private Collection<AnalyticsEvent> events;
    private int uploadCount;
    
    public Crash(final UUID uuid, final String buildId, final long timestamp) {
        final AgentImpl agentImpl = Agent.getImpl();
        this.uuid = uuid;
        this.buildId = buildId;
        this.timestamp = timestamp;
        this.appToken = CrashReporter.getAgentConfiguration().getApplicationToken();
        this.deviceInfo = new DeviceInfo(agentImpl.getDeviceInformation(), agentImpl.getEnvironmentInformation());
        this.applicationInfo = new ApplicationInfo(agentImpl.getApplicationInformation());
        this.exceptionInfo = new ExceptionInfo();
        this.threads = new ArrayList<ThreadInfo>();
        this.activityHistory = new ActivityHistory(new ArrayList<ActivitySighting>());
        this.sessionAttributes = new HashSet<AnalyticAttribute>();
        this.events = new HashSet<AnalyticsEvent>();
        this.analyticsEnabled = false;
        this.uploadCount = 0;
    }
    
    public Crash(final Throwable throwable) {
        this(throwable, new HashSet<AnalyticAttribute>(), new HashSet<AnalyticsEvent>(), false);
    }
    
    public Crash(final Throwable throwable, final Set<AnalyticAttribute> sessionAttributes, final Collection<AnalyticsEvent> events, final boolean analyticsEnabled) {
        final AgentImpl agentImpl = Agent.getImpl();
        final Throwable cause = getRootCause(throwable);
        this.uuid = UUID.randomUUID();
        this.buildId = getBuildId();
        this.timestamp = System.currentTimeMillis() / 1000L;
        this.appToken = CrashReporter.getAgentConfiguration().getApplicationToken();
        this.deviceInfo = new DeviceInfo(agentImpl.getDeviceInformation(), agentImpl.getEnvironmentInformation());
        this.applicationInfo = new ApplicationInfo(agentImpl.getApplicationInformation());
        this.exceptionInfo = new ExceptionInfo(cause);
        this.threads = ThreadInfo.extractThreads(cause);
        this.activityHistory = TraceMachine.getActivityHistory();
        this.sessionAttributes = sessionAttributes;
        this.events = events;
        this.analyticsEnabled = analyticsEnabled;
        this.uploadCount = 0;
    }
    
    public static String getBuildId() {
        return Agent.getBuildId();
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    public UUID getUuid() {
        return this.uuid;
    }
    
    public ExceptionInfo getExceptionInfo() {
        return this.exceptionInfo;
    }
    
    public void setSessionAttributes(final Set<AnalyticAttribute> sessionAttributes) {
        this.sessionAttributes = sessionAttributes;
    }
    
    public Set<AnalyticAttribute> getSessionAttributes() {
        return this.sessionAttributes;
    }
    
    public void setAnalyticsEvents(final Collection<AnalyticsEvent> events) {
        this.events = events;
    }
    
    @Override
    public JsonObject asJsonObject() {
        final JsonObject data = new JsonObject();
        data.add("protocolVersion", new JsonPrimitive(1));
        data.add("platform", new JsonPrimitive("Android"));
        data.add("uuid", SafeJsonPrimitive.factory(this.uuid.toString()));
        data.add("buildId", SafeJsonPrimitive.factory(this.buildId));
        data.add("timestamp", SafeJsonPrimitive.factory(this.timestamp));
        data.add("appToken", SafeJsonPrimitive.factory(this.appToken));
        data.add("deviceInfo", this.deviceInfo.asJsonObject());
        data.add("appInfo", this.applicationInfo.asJsonObject());
        data.add("exception", this.exceptionInfo.asJsonObject());
        data.add("threads", this.getThreadsAsJson());
        data.add("activityHistory", this.activityHistory.asJsonArrayWithoutDuration());
        final JsonObject attributeObject = new JsonObject();
        if (this.sessionAttributes != null) {
            for (final AnalyticAttribute attribute : this.sessionAttributes) {
                attributeObject.add(attribute.getName(), attribute.asJsonElement());
            }
        }
        data.add("sessionAttributes", attributeObject);
        final JsonArray eventArray = new JsonArray();
        if (this.events != null) {
            for (final AnalyticsEvent event : this.events) {
                eventArray.add(event.asJsonObject());
            }
        }
        data.add("analyticsEvents", eventArray);
        final DataToken dataToken = Harvest.getHarvestConfiguration().getDataToken();
        if (dataToken != null) {
            data.add("dataToken", dataToken.asJsonArray());
        }
        return data;
    }
    
    public static Crash crashFromJsonString(final String json) {
        final JsonElement element = new JsonParser().parse(json);
        final JsonObject crashObject = element.getAsJsonObject();
        final String uuid = crashObject.get("uuid").getAsString();
        final String buildIdentifier = crashObject.get("buildId").getAsString();
        final long timestamp = crashObject.get("timestamp").getAsLong();
        final Crash crash = new Crash(UUID.fromString(uuid), buildIdentifier, timestamp);
        crash.deviceInfo = DeviceInfo.newFromJson(crashObject.get("deviceInfo").getAsJsonObject());
        crash.applicationInfo = ApplicationInfo.newFromJson(crashObject.get("appInfo").getAsJsonObject());
        crash.exceptionInfo = ExceptionInfo.newFromJson(crashObject.get("exception").getAsJsonObject());
        crash.threads = ThreadInfo.newListFromJson(crashObject.get("threads").getAsJsonArray());
        crash.activityHistory = ActivityHistory.newFromJson(crashObject.get("activityHistory").getAsJsonArray());
        crash.analyticsEnabled = (crashObject.has("sessionAttributes") || crashObject.has("analyticsEvents"));
        if (crashObject.has("sessionAttributes")) {
            final Set<AnalyticAttribute> sessionAttributes = AnalyticAttribute.newFromJson(crashObject.get("sessionAttributes").getAsJsonObject());
            crash.setSessionAttributes(sessionAttributes);
        }
        if (crashObject.has("analyticsEvents")) {
            final Collection<AnalyticsEvent> events = AnalyticsEvent.newFromJson(crashObject.get("analyticsEvents").getAsJsonArray());
            crash.setAnalyticsEvents(events);
        }
        if (crashObject.has("uploadCount")) {
            crash.uploadCount = crashObject.get("uploadCount").getAsInt();
        }
        return crash;
    }
    
    protected static Throwable getRootCause(final Throwable throwable) {
        try {
            if (throwable != null) {
                final Throwable cause = throwable.getCause();
                if (cause == null) {
                    return throwable;
                }
                return getRootCause(cause);
            }
        }
        catch (Exception e) {
            if (throwable != null) {
                return throwable;
            }
        }
        return new Throwable("Unknown cause");
    }
    
    protected JsonArray getThreadsAsJson() {
        final JsonArray data = new JsonArray();
        if (this.threads != null) {
            for (final ThreadInfo thread : this.threads) {
                data.add(thread.asJsonObject());
            }
        }
        return data;
    }
    
    public void incrementUploadCount() {
        ++this.uploadCount;
    }
    
    public int getUploadCount() {
        return this.uploadCount;
    }
    
    public boolean isStale() {
        return this.uploadCount >= 3;
    }
}
