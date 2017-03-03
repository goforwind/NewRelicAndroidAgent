// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.type;

import java.util.Map;
import com.newrelic.com.google.gson.reflect.TypeToken;
import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.com.google.gson.JsonElement;
import java.lang.reflect.Type;

public class BaseHarvestable implements Harvestable
{
    private final Type type;
    protected static final java.lang.reflect.Type GSON_STRING_MAP_TYPE;
    
    public BaseHarvestable(final Type type) {
        this.type = type;
    }
    
    @Override
    public JsonElement asJson() {
        switch (this.type) {
            case OBJECT: {
                return this.asJsonObject();
            }
            case ARRAY: {
                return this.asJsonArray();
            }
            case VALUE: {
                return this.asJsonPrimitive();
            }
            default: {
                return null;
            }
        }
    }
    
    @Override
    public Type getType() {
        return this.type;
    }
    
    @Override
    public String toJsonString() {
        return this.asJson().toString();
    }
    
    @Override
    public JsonArray asJsonArray() {
        return null;
    }
    
    @Override
    public JsonObject asJsonObject() {
        return null;
    }
    
    @Override
    public JsonPrimitive asJsonPrimitive() {
        return null;
    }
    
    protected void notEmpty(final String argument) {
        if (argument == null || argument.length() == 0) {
            throw new IllegalArgumentException("Missing Harvestable field.");
        }
    }
    
    protected void notNull(final Object argument) {
        if (argument == null) {
            throw new IllegalArgumentException("Null field in Harvestable object");
        }
    }
    
    protected String optional(final String argument) {
        if (argument == null) {
            return "";
        }
        return argument;
    }
    
    static {
        GSON_STRING_MAP_TYPE = new TypeToken<Map>() {}.getType();
    }
}
