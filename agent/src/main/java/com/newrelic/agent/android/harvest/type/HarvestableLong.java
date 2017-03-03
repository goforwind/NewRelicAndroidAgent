// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.type;

import com.newrelic.com.google.gson.JsonPrimitive;

public class HarvestableLong extends HarvestableValue
{
    private long value;
    
    public HarvestableLong() {
    }
    
    public HarvestableLong(final long value) {
        this();
        this.value = value;
    }
    
    @Override
    public JsonPrimitive asJsonPrimitive() {
        return new JsonPrimitive(this.value);
    }
}
