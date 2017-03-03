// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.Iterator;
import com.newrelic.com.google.gson.JsonArray;
import java.util.ArrayList;
import com.newrelic.agent.android.tracing.ActivityTrace;
import java.util.Collection;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class ActivityTraces extends HarvestableArray
{
    private final Collection<ActivityTrace> activityTraces;
    
    public ActivityTraces() {
        this.activityTraces = new ArrayList<ActivityTrace>();
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        for (final ActivityTrace activityTrace : this.activityTraces) {
            array.add(activityTrace.asJson());
        }
        return array;
    }
    
    public synchronized void add(final ActivityTrace activityTrace) {
        this.activityTraces.add(activityTrace);
    }
    
    public synchronized void remove(final ActivityTrace activityTrace) {
        this.activityTraces.remove(activityTrace);
    }
    
    public void clear() {
        this.activityTraces.clear();
    }
    
    public int count() {
        return this.activityTraces.size();
    }
    
    public Collection<ActivityTrace> getActivityTraces() {
        return this.activityTraces;
    }
}
