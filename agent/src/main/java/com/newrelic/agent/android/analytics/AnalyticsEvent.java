// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.ArrayList;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.com.google.gson.JsonElement;
import java.util.Map;
import java.util.Collection;
import com.newrelic.com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashSet;
import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Set;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.harvest.type.HarvestableObject;

public class AnalyticsEvent extends HarvestableObject
{
    private final AgentLog log;
    private String name;
    private long timestamp;
    private AnalyticsEventCategory category;
    private String eventType;
    private Set<AnalyticAttribute> attributeSet;
    
    protected AnalyticsEvent(final String name) {
        this(name, AnalyticsEventCategory.Custom, null, null);
    }
    
    protected AnalyticsEvent(final String name, final AnalyticsEventCategory category) {
        this(name, category, null, null);
    }
    
    protected AnalyticsEvent(final String name, final AnalyticsEventCategory category, final String eventType, final Set<AnalyticAttribute> initialAttributeSet) {
        this(name, category, eventType, System.currentTimeMillis(), initialAttributeSet);
    }
    
    private AnalyticsEvent(final String name, final AnalyticsEventCategory category, final String eventType, final long timeStamp, final Set<AnalyticAttribute> initialAttributeSet) {
        this.log = AgentLogManager.getAgentLog();
        this.attributeSet = Collections.synchronizedSet(new HashSet<AnalyticAttribute>());
        this.name = name;
        if (category == null) {
            this.category = AnalyticsEventCategory.Custom;
        }
        else {
            this.category = category;
        }
        if (eventType == null) {
            this.eventType = "Mobile";
        }
        else {
            this.eventType = eventType;
        }
        this.timestamp = timeStamp;
        if (initialAttributeSet != null) {
            for (final AnalyticAttribute attribute : initialAttributeSet) {
                this.attributeSet.add(new AnalyticAttribute(attribute));
            }
        }
        if (name != null) {
            this.attributeSet.add(new AnalyticAttribute("name", this.name));
        }
        this.attributeSet.add(new AnalyticAttribute("timestamp", String.valueOf(this.timestamp)));
        this.attributeSet.add(new AnalyticAttribute("category", this.category.name()));
        this.attributeSet.add(new AnalyticAttribute("eventType", this.eventType));
    }
    
    public void addAttributes(final Set<AnalyticAttribute> attributeSet) {
        if (attributeSet != null) {
            for (final AnalyticAttribute attribute : attributeSet) {
                if (!this.attributeSet.add(attribute)) {
                    this.log.error("Failed to add attribute " + attribute.getName() + " to event " + this.getName() + ": the event already contains that attribute.");
                }
            }
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public AnalyticsEventCategory getCategory() {
        return this.category;
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    public String getEventType() {
        return this.eventType;
    }
    
    @Override
    public JsonObject asJsonObject() {
        final JsonObject data = new JsonObject();
        synchronized (this) {
            for (final AnalyticAttribute attribute : this.attributeSet) {
                data.add(attribute.getName(), attribute.asJsonElement());
            }
        }
        return data;
    }
    
    public Collection<AnalyticAttribute> getAttributeSet() {
        return Collections.unmodifiableCollection((Collection<? extends AnalyticAttribute>)this.attributeSet);
    }
    
    public static AnalyticsEvent newFromJson(final JsonObject analyticsEventJson) {
        final Iterator<Map.Entry<String, JsonElement>> entry = analyticsEventJson.entrySet().iterator();
        String name = null;
        String eventType = null;
        AnalyticsEventCategory category = null;
        long timestamp = 0L;
        final Set<AnalyticAttribute> attributeSet = new HashSet<AnalyticAttribute>();
        while (entry.hasNext()) {
            final Map.Entry<String, JsonElement> elem = entry.next();
            final String key = elem.getKey();
            if (key.equalsIgnoreCase("name")) {
                name = elem.getValue().getAsString();
            }
            else if (key.equalsIgnoreCase("category")) {
                category = AnalyticsEventCategory.fromString(elem.getValue().getAsString());
            }
            else if (key.equalsIgnoreCase("eventType")) {
                eventType = elem.getValue().getAsString();
            }
            else if (key.equalsIgnoreCase("timestamp")) {
                timestamp = elem.getValue().getAsLong();
            }
            else {
                final JsonPrimitive value = elem.getValue().getAsJsonPrimitive();
                if (value.isString()) {
                    attributeSet.add(new AnalyticAttribute(key, value.getAsString(), false));
                }
                else if (value.isBoolean()) {
                    attributeSet.add(new AnalyticAttribute(key, value.getAsBoolean(), false));
                }
                else {
                    if (!value.isNumber()) {
                        continue;
                    }
                    attributeSet.add(new AnalyticAttribute(key, value.getAsFloat(), false));
                }
            }
        }
        return new AnalyticsEvent(name, category, eventType, timestamp, attributeSet);
    }
    
    public static Collection<AnalyticsEvent> newFromJson(final JsonArray analyticsEventsJson) {
        final ArrayList<AnalyticsEvent> events = new ArrayList<AnalyticsEvent>();
        for (final JsonElement e : analyticsEventsJson) {
            events.add(newFromJson(e.getAsJsonObject()));
        }
        return events;
    }
}
