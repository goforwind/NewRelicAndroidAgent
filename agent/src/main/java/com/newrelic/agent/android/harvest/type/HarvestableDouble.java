// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.type;

import com.newrelic.com.google.gson.JsonPrimitive;

public class HarvestableDouble extends HarvestableValue
{
    private double value;
    
    public HarvestableDouble() {
    }
    
    public HarvestableDouble(final double value) {
        this();
        this.value = value;
    }
    
    @Override
    public JsonPrimitive asJsonPrimitive() {
        return new JsonPrimitive(this.value);
    }
}
