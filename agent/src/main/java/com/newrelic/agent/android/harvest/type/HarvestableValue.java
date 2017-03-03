// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.type;

import com.newrelic.com.google.gson.JsonPrimitive;

public abstract class HarvestableValue extends BaseHarvestable
{
    public HarvestableValue() {
        super(Harvestable.Type.VALUE);
    }
    
    @Override
    public abstract JsonPrimitive asJsonPrimitive();
}
