// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.Iterator;
import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.JsonPrimitive;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.agent.android.harvest.type.HarvestableObject;

public class AgentHealthExceptions extends HarvestableObject
{
    private static final JsonArray keyArray;
    private final Map<String, AgentHealthException> agentHealthExceptions;
    
    public AgentHealthExceptions() {
        this.agentHealthExceptions = new ConcurrentHashMap<String, AgentHealthException>();
        AgentHealthExceptions.keyArray.add(new JsonPrimitive("ExceptionClass"));
        AgentHealthExceptions.keyArray.add(new JsonPrimitive("Message"));
        AgentHealthExceptions.keyArray.add(new JsonPrimitive("ThreadName"));
        AgentHealthExceptions.keyArray.add(new JsonPrimitive("CallStack"));
        AgentHealthExceptions.keyArray.add(new JsonPrimitive("Count"));
        AgentHealthExceptions.keyArray.add(new JsonPrimitive("Extras"));
    }
    
    public void add(final AgentHealthException exception) {
        final String aggregationKey = this.getKey(exception);
        synchronized (this.agentHealthExceptions) {
            final AgentHealthException healthException = this.agentHealthExceptions.get(aggregationKey);
            if (healthException == null) {
                this.agentHealthExceptions.put(aggregationKey, exception);
            }
            else {
                healthException.increment();
            }
        }
    }
    
    public void clear() {
        synchronized (this.agentHealthExceptions) {
            this.agentHealthExceptions.clear();
        }
    }
    
    public boolean isEmpty() {
        return this.agentHealthExceptions.isEmpty();
    }
    
    public Map<String, AgentHealthException> getAgentHealthExceptions() {
        return this.agentHealthExceptions;
    }
    
    public final String getKey(final AgentHealthException exception) {
        String key = this.getClass().getName();
        if (exception != null) {
            key = exception.getExceptionClass() + exception.getStackTrace()[0].toString();
        }
        return key;
    }
    
    @Override
    public JsonObject asJsonObject() {
        final JsonObject exceptions = new JsonObject();
        final JsonArray data = new JsonArray();
        for (final AgentHealthException exception : this.agentHealthExceptions.values()) {
            data.add(exception.asJsonArray());
        }
        exceptions.add("Type", new JsonPrimitive("AgentErrors"));
        exceptions.add("Keys", AgentHealthExceptions.keyArray);
        exceptions.add("Data", data);
        return exceptions;
    }
    
    static {
        keyArray = new JsonArray();
    }
}
