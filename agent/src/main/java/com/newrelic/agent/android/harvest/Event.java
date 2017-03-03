// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import com.newrelic.com.google.gson.Gson;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.com.google.gson.JsonArray;
import java.util.HashMap;
import java.util.Map;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class Event extends HarvestableArray
{
    private long timestamp;
    private long eventName;
    private Map<String, String> params;
    
    public Event() {
        this.params = new HashMap<String, String>();
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(this.timestamp));
        array.add(new JsonPrimitive(this.eventName));
        array.add(new Gson().toJsonTree(this.params, Event.GSON_STRING_MAP_TYPE));
        return array;
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getEventName() {
        return this.eventName;
    }
    
    public void setEventName(final long eventName) {
        this.eventName = eventName;
    }
    
    public Map<String, String> getParams() {
        return this.params;
    }
    
    public void setParams(final Map<String, String> params) {
        this.params = params;
    }
}
