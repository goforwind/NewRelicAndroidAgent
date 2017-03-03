// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.Iterator;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.Gson;
import java.util.HashMap;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.agent.android.metric.Metric;
import com.newrelic.agent.android.metric.MetricStore;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class MachineMeasurements extends HarvestableArray
{
    private final MetricStore metrics;
    
    public MachineMeasurements() {
        this.metrics = new MetricStore();
    }
    
    public void addMetric(final String name, final double value) {
        final Metric metric = new Metric(name);
        metric.sample(value);
        this.addMetric(metric);
    }
    
    public void addMetric(final Metric metric) {
        this.metrics.add(metric);
    }
    
    public void clear() {
        this.metrics.clear();
    }
    
    public boolean isEmpty() {
        return this.metrics.isEmpty();
    }
    
    public MetricStore getMetrics() {
        return this.metrics;
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray metricArray = new JsonArray();
        for (final Metric metric : this.metrics.getAll()) {
            final JsonArray metricJson = new JsonArray();
            final HashMap<String, String> header = new HashMap<String, String>();
            header.put("name", metric.getName());
            header.put("scope", metric.getStringScope());
            metricJson.add(new Gson().toJsonTree(header, MachineMeasurements.GSON_STRING_MAP_TYPE));
            metricJson.add(metric.asJsonObject());
            metricArray.add(metricJson);
        }
        return metricArray;
    }
}
