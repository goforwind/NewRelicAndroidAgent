// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.Iterator;
import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.agent.android.util.SafeJsonPrimitive;
import com.newrelic.com.google.gson.JsonArray;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class AgentHealthException extends HarvestableArray
{
    private String exceptionClass;
    private String message;
    private String threadName;
    private StackTraceElement[] stackTrace;
    private final AtomicLong count;
    private Map<String, String> extras;
    
    public AgentHealthException(final Exception e) {
        this(e, Thread.currentThread().getName());
    }
    
    public AgentHealthException(final Exception e, final String threadName) {
        this(e.getClass().getName(), e.getMessage(), threadName, e.getStackTrace());
    }
    
    public AgentHealthException(final String exceptionClass, final String message, final String threadName, final StackTraceElement[] stackTrace) {
        this(exceptionClass, message, threadName, stackTrace, null);
    }
    
    public AgentHealthException(final String exceptionClass, final String message, final String threadName, final StackTraceElement[] stackTrace, final Map<String, String> extras) {
        this.count = new AtomicLong(1L);
        this.exceptionClass = exceptionClass;
        this.message = message;
        this.threadName = threadName;
        this.stackTrace = stackTrace;
        this.extras = extras;
    }
    
    public void increment() {
        this.count.getAndIncrement();
    }
    
    public void increment(final long i) {
        this.count.getAndAdd(i);
    }
    
    public String getExceptionClass() {
        return this.exceptionClass;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public String getThreadName() {
        return this.threadName;
    }
    
    public StackTraceElement[] getStackTrace() {
        return this.stackTrace;
    }
    
    public long getCount() {
        return this.count.get();
    }
    
    public Map<String, String> getExtras() {
        return this.extras;
    }
    
    public String getSourceClass() {
        return this.stackTrace[0].getClassName();
    }
    
    public String getSourceMethod() {
        return this.stackTrace[0].getMethodName();
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray data = new JsonArray();
        data.add(SafeJsonPrimitive.factory(this.exceptionClass));
        data.add(SafeJsonPrimitive.factory((this.message == null) ? "" : this.message));
        data.add(SafeJsonPrimitive.factory(this.threadName));
        data.add(this.stackTraceToJson());
        data.add(SafeJsonPrimitive.factory(this.count.get()));
        data.add(this.extrasToJson());
        return data;
    }
    
    private JsonArray stackTraceToJson() {
        final JsonArray stack = new JsonArray();
        for (final StackTraceElement element : this.stackTrace) {
            stack.add(SafeJsonPrimitive.factory(element.toString()));
        }
        return stack;
    }
    
    private JsonObject extrasToJson() {
        final JsonObject data = new JsonObject();
        if (this.extras != null) {
            for (final Map.Entry<String, String> entry : this.extras.entrySet()) {
                data.add(entry.getKey(), SafeJsonPrimitive.factory(entry.getValue()));
            }
        }
        return data;
    }
}
