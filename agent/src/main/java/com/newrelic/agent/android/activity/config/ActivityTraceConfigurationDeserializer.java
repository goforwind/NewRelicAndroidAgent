// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.activity.config;

import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.com.google.gson.JsonParseException;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.com.google.gson.JsonDeserializationContext;
import java.lang.reflect.Type;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.com.google.gson.JsonDeserializer;

public class ActivityTraceConfigurationDeserializer implements JsonDeserializer<ActivityTraceConfiguration>
{
    private final AgentLog log;
    
    public ActivityTraceConfigurationDeserializer() {
        this.log = AgentLogManager.getAgentLog();
    }
    
    @Override
    public ActivityTraceConfiguration deserialize(final JsonElement root, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final ActivityTraceConfiguration configuration = new ActivityTraceConfiguration();
        if (!root.isJsonArray()) {
            this.error("Expected root element to be an array.");
            return null;
        }
        final JsonArray array = root.getAsJsonArray();
        if (array.size() != 2) {
            this.error("Root array must contain 2 elements.");
            return null;
        }
        final Integer maxTotalTraceCount = this.getInteger(array.get(0));
        if (maxTotalTraceCount == null) {
            return null;
        }
        if (maxTotalTraceCount < 0) {
            this.error("The first element of the root array must not be negative.");
            return null;
        }
        configuration.setMaxTotalTraceCount(maxTotalTraceCount);
        return configuration;
    }
    
    private Integer getInteger(final JsonElement element) {
        if (!element.isJsonPrimitive()) {
            this.error("Expected an integer.");
            return null;
        }
        final JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isNumber()) {
            this.error("Expected an integer.");
            return null;
        }
        final int value = primitive.getAsInt();
        if (value < 0) {
            this.error("Integer value must not be negative");
            return null;
        }
        return value;
    }
    
    private void error(final String message) {
        this.log.error("ActivityTraceConfigurationDeserializer: " + message);
    }
}
