// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.tracing;

import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.agent.android.harvest.ConnectInformation;
import com.newrelic.agent.android.Agent;
import java.util.Iterator;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.agent.android.util.SafeJsonPrimitive;
import com.newrelic.com.google.gson.Gson;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.activity.MeasuredActivity;
import com.newrelic.agent.android.Measurements;
import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Collections;
import java.util.HashSet;
import com.newrelic.agent.android.metric.Metric;
import com.newrelic.agent.android.logging.AgentLog;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import com.newrelic.agent.android.harvest.ActivitySighting;
import com.newrelic.agent.android.activity.NamedActivity;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class ActivityTrace extends HarvestableArray
{
    public static final String TRACE_VERSION = "1.0";
    public static final int MAX_TRACES = 2000;
    public Trace rootTrace;
    private final ConcurrentHashMap<UUID, Trace> traces;
    private int traceCount;
    private final Set<UUID> missingChildren;
    private NamedActivity measuredActivity;
    private long reportAttemptCount;
    public long lastUpdatedAt;
    public long startedAt;
    public ActivitySighting previousActivity;
    private boolean complete;
    private final HashMap<String, String> params;
    private Map<Sample.SampleType, Collection<Sample>> vitals;
    private final AgentLog log;
    public final Metric networkCountMetric;
    public final Metric networkTimeMetric;
    private static final String SIZE_NORMAL = "NORMAL";
    private static final HashMap<String, String> ENVIRONMENT_TYPE;
    private static final HashMap<String, String> VITALS_TYPE;
    private static final HashMap<String, String> ACTIVITY_HISTORY_TYPE;
    
    public ActivityTrace() {
        this.traces = new ConcurrentHashMap<UUID, Trace>();
        this.traceCount = 0;
        this.missingChildren = Collections.synchronizedSet(new HashSet<UUID>());
        this.reportAttemptCount = 0L;
        this.complete = false;
        this.params = new HashMap<String, String>();
        this.log = AgentLogManager.getAgentLog();
        this.networkCountMetric = new Metric("Mobile/Activity/Network/<activity>/Count");
        this.networkTimeMetric = new Metric("Mobile/Activity/Network/<activity>/Time");
    }
    
    public ActivityTrace(final Trace rootTrace) {
        this.traces = new ConcurrentHashMap<UUID, Trace>();
        this.traceCount = 0;
        this.missingChildren = Collections.synchronizedSet(new HashSet<UUID>());
        this.reportAttemptCount = 0L;
        this.complete = false;
        this.params = new HashMap<String, String>();
        this.log = AgentLogManager.getAgentLog();
        this.networkCountMetric = new Metric("Mobile/Activity/Network/<activity>/Count");
        this.networkTimeMetric = new Metric("Mobile/Activity/Network/<activity>/Time");
        this.rootTrace = rootTrace;
        this.lastUpdatedAt = rootTrace.entryTimestamp;
        this.startedAt = this.lastUpdatedAt;
        this.params.put("traceVersion", "1.0");
        this.params.put("type", "ACTIVITY");
        (this.measuredActivity = (NamedActivity)Measurements.startActivity(rootTrace.displayName)).setStartTime(rootTrace.entryTimestamp);
    }
    
    public String getId() {
        if (this.rootTrace == null) {
            return null;
        }
        return this.rootTrace.myUUID.toString();
    }
    
    public void addTrace(final Trace trace) {
        this.missingChildren.add(trace.myUUID);
        this.lastUpdatedAt = System.currentTimeMillis();
    }
    
    public void addCompletedTrace(final Trace trace) {
        if (trace.getType() == TraceType.NETWORK) {
            this.networkCountMetric.sample(1.0);
            this.networkTimeMetric.sample(trace.getDurationAsSeconds());
            if (this.rootTrace != null) {
                final Trace rootTrace = this.rootTrace;
                rootTrace.childExclusiveTime += trace.getDurationAsMilliseconds();
            }
        }
        trace.traceMachine = null;
        this.missingChildren.remove(trace.myUUID);
        if (this.traceCount > 2000) {
            this.log.verbose("Maximum trace limit reached, discarding trace " + trace.myUUID);
            return;
        }
        this.traces.put(trace.myUUID, trace);
        ++this.traceCount;
        if (trace.exitTimestamp > this.rootTrace.exitTimestamp) {
            this.rootTrace.exitTimestamp = trace.exitTimestamp;
        }
        this.log.verbose("Added trace " + trace.myUUID.toString() + " missing children: " + this.missingChildren.size());
        this.lastUpdatedAt = System.currentTimeMillis();
    }
    
    public boolean hasMissingChildren() {
        return !this.missingChildren.isEmpty();
    }
    
    public boolean isComplete() {
        return this.complete;
    }
    
    public void discard() {
        this.log.debug("Discarding trace of " + this.rootTrace.displayName + ":" + this.rootTrace.myUUID.toString() + "(" + this.traces.size() + " traces)");
        this.rootTrace.traceMachine = null;
        this.complete = true;
        Measurements.endActivityWithoutMeasurement(this.measuredActivity);
    }
    
    public void complete() {
        this.log.debug("Completing trace of " + this.rootTrace.displayName + ":" + this.rootTrace.myUUID.toString() + "(" + this.traces.size() + " traces)");
        if (this.rootTrace.exitTimestamp == 0L) {
            this.rootTrace.exitTimestamp = System.currentTimeMillis();
        }
        if (this.traces.isEmpty()) {
            this.rootTrace.traceMachine = null;
            this.complete = true;
            Measurements.endActivityWithoutMeasurement(this.measuredActivity);
            return;
        }
        this.measuredActivity.setEndTime(this.rootTrace.exitTimestamp);
        Measurements.endActivity(this.measuredActivity);
        this.rootTrace.traceMachine = null;
        this.complete = true;
        TaskQueue.queue(this);
    }
    
    public Map<UUID, Trace> getTraces() {
        return this.traces;
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray tree = new JsonArray();
        if (!this.complete) {
            this.log.verbose("Attempted to serialize trace " + this.rootTrace.myUUID.toString() + " but it has yet to be finalized");
            return null;
        }
        tree.add(new Gson().toJsonTree(this.params, ActivityTrace.GSON_STRING_MAP_TYPE));
        tree.add(SafeJsonPrimitive.factory(this.rootTrace.entryTimestamp));
        tree.add(SafeJsonPrimitive.factory(this.rootTrace.exitTimestamp));
        tree.add(SafeJsonPrimitive.factory(this.rootTrace.displayName));
        final JsonArray segments = new JsonArray();
        segments.add(this.getEnvironment());
        segments.add(this.traceToTree(this.rootTrace));
        segments.add(this.getVitalsAsJson());
        if (this.previousActivity != null) {
            segments.add(this.getPreviousActivityAsJson());
        }
        tree.add(segments);
        return tree;
    }
    
    private JsonArray traceToTree(final Trace trace) {
        final JsonArray segment = new JsonArray();
        trace.prepareForSerialization();
        segment.add(new Gson().toJsonTree(trace.getParams(), ActivityTrace.GSON_STRING_MAP_TYPE));
        segment.add(SafeJsonPrimitive.factory(trace.entryTimestamp));
        segment.add(SafeJsonPrimitive.factory(trace.exitTimestamp));
        segment.add(SafeJsonPrimitive.factory(trace.displayName));
        final JsonArray threadData = new JsonArray();
        threadData.add(SafeJsonPrimitive.factory(trace.threadId));
        threadData.add(SafeJsonPrimitive.factory(trace.threadName));
        segment.add(threadData);
        if (trace.getChildren().isEmpty()) {
            segment.add(new JsonArray());
        }
        else {
            final JsonArray children = new JsonArray();
            for (final UUID traceUUID : trace.getChildren()) {
                final Trace childTrace = this.traces.get(traceUUID);
                if (childTrace != null) {
                    children.add(this.traceToTree(childTrace));
                }
            }
            segment.add(children);
        }
        return segment;
    }
    
    private JsonArray getEnvironment() {
        final JsonArray environment = new JsonArray();
        environment.add(new Gson().toJsonTree(ActivityTrace.ENVIRONMENT_TYPE, ActivityTrace.GSON_STRING_MAP_TYPE));
        final ConnectInformation connectInformation = new ConnectInformation(Agent.getApplicationInformation(), Agent.getDeviceInformation());
        environment.addAll(connectInformation.asJsonArray());
        final HashMap<String, String> environmentParams = new HashMap<String, String>();
        environmentParams.put("size", "NORMAL");
        environment.add(new Gson().toJsonTree(environmentParams, ActivityTrace.GSON_STRING_MAP_TYPE));
        return environment;
    }
    
    public void setVitals(final Map<Sample.SampleType, Collection<Sample>> vitals) {
        this.vitals = vitals;
    }
    
    private JsonArray getVitalsAsJson() {
        final JsonArray vitalsJson = new JsonArray();
        vitalsJson.add(new Gson().toJsonTree(ActivityTrace.VITALS_TYPE, ActivityTrace.GSON_STRING_MAP_TYPE));
        final JsonObject vitalsMap = new JsonObject();
        if (this.vitals != null) {
            for (final Map.Entry<Sample.SampleType, Collection<Sample>> entry : this.vitals.entrySet()) {
                final JsonArray samplesJsonArray = new JsonArray();
                for (final Sample sample : entry.getValue()) {
                    if (sample.getTimestamp() <= this.lastUpdatedAt) {
                        samplesJsonArray.add(sample.asJsonArray());
                    }
                }
                vitalsMap.add(entry.getKey().toString(), samplesJsonArray);
            }
        }
        vitalsJson.add(vitalsMap);
        return vitalsJson;
    }
    
    private JsonArray getPreviousActivityAsJson() {
        final JsonArray historyJson = new JsonArray();
        historyJson.add(new Gson().toJsonTree(ActivityTrace.ACTIVITY_HISTORY_TYPE, ActivityTrace.GSON_STRING_MAP_TYPE));
        historyJson.addAll(this.previousActivity.asJsonArray());
        return historyJson;
    }
    
    public void setLastUpdatedAt(final long lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
    
    public long getLastUpdatedAt() {
        return this.lastUpdatedAt;
    }
    
    public long getReportAttemptCount() {
        return this.reportAttemptCount;
    }
    
    public void incrementReportAttemptCount() {
        ++this.reportAttemptCount;
    }
    
    public String getActivityName() {
        String activityName = "<activity>";
        if (this.rootTrace != null) {
            activityName = this.rootTrace.displayName;
            if (activityName != null) {
                final int hashIndex = activityName.indexOf("#");
                if (hashIndex > 0) {
                    activityName = activityName.substring(0, hashIndex);
                }
            }
        }
        return activityName;
    }
    
    static {
        ENVIRONMENT_TYPE = new HashMap<String, String>() {
            {
                this.put("type", "ENVIRONMENT");
            }
        };
        VITALS_TYPE = new HashMap<String, String>() {
            {
                this.put("type", "VITALS");
            }
        };
        ACTIVITY_HISTORY_TYPE = new HashMap<String, String>() {
            {
                this.put("type", "ACTIVITY_HISTORY");
            }
        };
    }
}
