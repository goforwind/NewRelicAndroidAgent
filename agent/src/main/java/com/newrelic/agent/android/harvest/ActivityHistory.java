// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.ArrayList;
import java.util.Iterator;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.JsonArray;
import java.util.List;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class ActivityHistory extends HarvestableArray
{
    private final List<ActivitySighting> activityHistory;
    
    public ActivityHistory(final List<ActivitySighting> activityHistory) {
        this.activityHistory = activityHistory;
    }
    
    public int size() {
        return this.activityHistory.size();
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray data = new JsonArray();
        for (final ActivitySighting sighting : this.activityHistory) {
            data.add(sighting.asJsonArray());
        }
        return data;
    }
    
    public JsonArray asJsonArrayWithoutDuration() {
        final JsonArray data = new JsonArray();
        for (final ActivitySighting sighting : this.activityHistory) {
            data.add(sighting.asJsonArrayWithoutDuration());
        }
        return data;
    }
    
    public static ActivityHistory newFromJson(final JsonArray jsonArray) {
        final List<ActivitySighting> sightings = new ArrayList<ActivitySighting>();
        for (final JsonElement element : jsonArray) {
            sightings.add(ActivitySighting.newFromJson(element.getAsJsonArray()));
        }
        return new ActivityHistory(sightings);
    }
}
