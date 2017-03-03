// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.tracing;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.instrumentation.MetricCategory;
import java.util.Iterator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.HashSet;
import com.newrelic.agent.android.util.Util;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.newrelic.agent.android.logging.AgentLog;

public class Trace
{
    private static final String CATEGORY_PARAMETER = "category";
    private static final AgentLog log;
    public final UUID parentUUID;
    public final UUID myUUID;
    public long entryTimestamp;
    public long exitTimestamp;
    public long exclusiveTime;
    public long childExclusiveTime;
    public String metricName;
    public String metricBackgroundName;
    public String displayName;
    public String scope;
    public long threadId;
    public String threadName;
    private volatile Map<String, Object> params;
    private List<String> rawAnnotationParams;
    private volatile Set<UUID> children;
    private TraceType type;
    private boolean isComplete;
    public TraceMachine traceMachine;
    
    public Trace() {
        this.myUUID = new UUID(Util.getRandom().nextLong(), Util.getRandom().nextLong());
        this.entryTimestamp = 0L;
        this.exitTimestamp = 0L;
        this.exclusiveTime = 0L;
        this.childExclusiveTime = 0L;
        this.threadId = 0L;
        this.threadName = "main";
        this.type = TraceType.TRACE;
        this.isComplete = false;
        this.parentUUID = null;
    }
    
    public Trace(final String displayName, final UUID parentUUID, final TraceMachine traceMachine) {
        this.myUUID = new UUID(Util.getRandom().nextLong(), Util.getRandom().nextLong());
        this.entryTimestamp = 0L;
        this.exitTimestamp = 0L;
        this.exclusiveTime = 0L;
        this.childExclusiveTime = 0L;
        this.threadId = 0L;
        this.threadName = "main";
        this.type = TraceType.TRACE;
        this.isComplete = false;
        this.displayName = displayName;
        this.parentUUID = parentUUID;
        this.traceMachine = traceMachine;
    }
    
    public void addChild(final Trace trace) {
        if (this.children == null) {
            synchronized (this) {
                if (this.children == null) {
                    this.children = Collections.synchronizedSet(new HashSet<UUID>());
                }
            }
        }
        this.children.add(trace.myUUID);
    }
    
    public Set<UUID> getChildren() {
        if (this.children == null) {
            synchronized (this) {
                if (this.children == null) {
                    this.children = Collections.synchronizedSet(new HashSet<UUID>());
                }
            }
        }
        return this.children;
    }
    
    public Map<String, Object> getParams() {
        if (this.params == null) {
            synchronized (this) {
                if (this.params == null) {
                    this.params = new ConcurrentHashMap<String, Object>();
                }
            }
        }
        return this.params;
    }
    
    public void setAnnotationParams(final List<String> rawAnnotationParams) {
        this.rawAnnotationParams = rawAnnotationParams;
    }
    
    public Map<String, Object> getAnnotationParams() {
        final HashMap<String, Object> annotationParams = new HashMap<String, Object>();
        if (this.rawAnnotationParams != null && this.rawAnnotationParams.size() > 0) {
            final Iterator<String> i = this.rawAnnotationParams.iterator();
            while (i.hasNext()) {
                final String paramName = i.next();
                final String paramClass = i.next();
                final String paramValue = i.next();
                final Object param = createParameter(paramName, paramClass, paramValue);
                if (param != null) {
                    annotationParams.put(paramName, param);
                }
            }
        }
        return annotationParams;
    }
    
    public boolean isComplete() {
        return this.isComplete;
    }
    
    public void complete() throws TracingInactiveException {
        if (this.isComplete) {
            Trace.log.warning("Attempted to double complete trace " + this.myUUID.toString());
            return;
        }
        if (this.exitTimestamp == 0L) {
            this.exitTimestamp = System.currentTimeMillis();
        }
        this.exclusiveTime = this.getDurationAsMilliseconds() - this.childExclusiveTime;
        this.isComplete = true;
        try {
            this.traceMachine.storeCompletedTrace(this);
        }
        catch (NullPointerException e) {
            throw new TracingInactiveException();
        }
    }
    
    public void prepareForSerialization() {
        this.getParams().put("type", this.type.toString());
    }
    
    public TraceType getType() {
        return this.type;
    }
    
    public void setType(final TraceType type) {
        this.type = type;
    }
    
    public long getDurationAsMilliseconds() {
        return this.exitTimestamp - this.entryTimestamp;
    }
    
    public float getDurationAsSeconds() {
        return (this.exitTimestamp - this.entryTimestamp) / 1000.0f;
    }
    
    public MetricCategory getCategory() {
        if (!this.getAnnotationParams().containsKey("category")) {
            return null;
        }
        final Object category = this.getAnnotationParams().get("category");
        if (!(category instanceof MetricCategory)) {
            Trace.log.error("Category annotation parameter is not of type MetricCategory");
            return null;
        }
        return (MetricCategory)category;
    }
    
    private static Object createParameter(final String parameterName, final String parameterClass, final String parameterValue) {
        Class clazz;
        try {
            clazz = Class.forName(parameterClass);
        }
        catch (ClassNotFoundException e) {
            Trace.log.error("Unable to resolve parameter class in enterMethod: " + e.getMessage(), e);
            return null;
        }
        if (MetricCategory.class == clazz) {
            return MetricCategory.valueOf(parameterValue);
        }
        if (String.class == clazz) {
            return parameterValue;
        }
        return null;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
