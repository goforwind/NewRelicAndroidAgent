// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.metric;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MetricStore
{
    private final Map<String, Map<String, Metric>> metricStore;
    
    public MetricStore() {
        this.metricStore = new ConcurrentHashMap<String, Map<String, Metric>>();
    }
    
    public void add(final Metric metric) {
        final String scope = metric.getStringScope();
        final String name = metric.getName();
        if (!this.metricStore.containsKey(scope)) {
            this.metricStore.put(scope, new HashMap<String, Metric>());
        }
        if (this.metricStore.get(scope).containsKey(name)) {
            this.metricStore.get(scope).get(name).aggregate(metric);
        }
        else {
            this.metricStore.get(scope).put(name, metric);
        }
    }
    
    public Metric get(final String name) {
        return this.get(name, "");
    }
    
    public Metric get(final String name, final String scope) {
        try {
            return this.metricStore.get((scope == null) ? "" : scope).get(name);
        }
        catch (NullPointerException e) {
            return null;
        }
    }
    
    public List<Metric> getAll() {
        final List<Metric> metrics = new ArrayList<Metric>();
        for (final Map.Entry<String, Map<String, Metric>> entry : this.metricStore.entrySet()) {
            for (final Map.Entry<String, Metric> metricEntry : entry.getValue().entrySet()) {
                metrics.add(metricEntry.getValue());
            }
        }
        return metrics;
    }
    
    public List<Metric> getAllByScope(final String scope) {
        final List<Metric> metrics = new ArrayList<Metric>();
        try {
            for (final Map.Entry<String, Metric> metricEntry : this.metricStore.get(scope).entrySet()) {
                metrics.add(metricEntry.getValue());
            }
        }
        catch (NullPointerException ex) {}
        return metrics;
    }
    
    public List<Metric> getAllUnscoped() {
        return this.getAllByScope("");
    }
    
    public void remove(final Metric metric) {
        final String scope = metric.getStringScope();
        final String name = metric.getName();
        if (!this.metricStore.containsKey(scope)) {
            return;
        }
        if (!this.metricStore.get(scope).containsKey(name)) {
            return;
        }
        this.metricStore.get(scope).remove(name);
    }
    
    public void removeAll(final List<Metric> metrics) {
        synchronized (this.metricStore) {
            for (final Metric metric : metrics) {
                this.remove(metric);
            }
        }
    }
    
    public List<Metric> removeAllWithScope(final String scope) {
        final List<Metric> metrics = this.getAllByScope(scope);
        if (!metrics.isEmpty()) {
            this.removeAll(metrics);
        }
        return metrics;
    }
    
    public void clear() {
        this.metricStore.clear();
    }
    
    public boolean isEmpty() {
        return this.metricStore.isEmpty();
    }
}
