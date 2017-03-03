// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.type;

import com.newrelic.com.google.gson.Gson;
import com.newrelic.com.google.gson.JsonObject;
import java.util.Map;

public abstract class HarvestableObject extends BaseHarvestable
{
    public static HarvestableObject fromMap(final Map<String, String> map) {
        return new HarvestableObject() {
            @Override
            public JsonObject asJsonObject() {
                return (JsonObject)new Gson().toJsonTree(map, HarvestableObject$1.GSON_STRING_MAP_TYPE);
            }
        };
    }
    
    public HarvestableObject() {
        super(Harvestable.Type.OBJECT);
    }
    
    @Override
    public abstract JsonObject asJsonObject();
}
