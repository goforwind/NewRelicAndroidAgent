// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.type;

import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.com.google.gson.JsonElement;

public interface Harvestable
{
    Type getType();
    
    JsonElement asJson();
    
    JsonObject asJsonObject();
    
    JsonArray asJsonArray();
    
    JsonPrimitive asJsonPrimitive();
    
    String toJsonString();
    
    public enum Type
    {
        OBJECT, 
        ARRAY, 
        VALUE;
    }
}
