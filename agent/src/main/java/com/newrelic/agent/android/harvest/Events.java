// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.Iterator;
import com.newrelic.com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.Collection;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class Events extends HarvestableArray
{
    private final Collection<Event> events;
    
    public Events() {
        this.events = new ArrayList<Event>();
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        for (final Event event : this.events) {
            array.add(event.asJson());
        }
        return array;
    }
    
    public void addEvent(final Event event) {
        this.events.add(event);
    }
}
