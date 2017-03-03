// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.type;

import com.newrelic.com.google.gson.JsonArray;

public abstract class HarvestableArray extends BaseHarvestable
{
    public HarvestableArray() {
        super(Harvestable.Type.ARRAY);
    }
    
    @Override
    public abstract JsonArray asJsonArray();
}
