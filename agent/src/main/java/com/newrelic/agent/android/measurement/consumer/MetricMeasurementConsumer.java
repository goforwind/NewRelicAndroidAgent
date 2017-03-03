// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.consumer;

import java.util.Iterator;
import com.newrelic.agent.android.metric.Metric;
import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.harvest.Harvest;
import com.newrelic.agent.android.measurement.MeasurementType;
import com.newrelic.agent.android.metric.MetricStore;
import com.newrelic.agent.android.harvest.HarvestLifecycleAware;

public abstract class MetricMeasurementConsumer extends BaseMeasurementConsumer implements HarvestLifecycleAware
{
    protected MetricStore metrics;
    protected boolean recordUnscopedMetrics;
    
    public MetricMeasurementConsumer(final MeasurementType measurementType) {
        super(measurementType);
        this.recordUnscopedMetrics = true;
        this.metrics = new MetricStore();
        Harvest.addHarvestListener(this);
    }
    
    protected abstract String formatMetricName(final String p0);
    
    @Override
    public void consumeMeasurement(final Measurement measurement) {
        final String name = this.formatMetricName(measurement.getName());
        final String scope = measurement.getScope();
        final double delta = measurement.getEndTimeInSeconds() - measurement.getStartTimeInSeconds();
        if (scope != null) {
            Metric scopedMetric = this.metrics.get(name, scope);
            if (scopedMetric == null) {
                scopedMetric = new Metric(name, scope);
                this.metrics.add(scopedMetric);
            }
            scopedMetric.sample(delta);
            scopedMetric.addExclusive(measurement.getExclusiveTimeInSeconds());
        }
        if (!this.recordUnscopedMetrics) {
            return;
        }
        Metric unscopedMetric = this.metrics.get(name);
        if (unscopedMetric == null) {
            unscopedMetric = new Metric(name);
            this.metrics.add(unscopedMetric);
        }
        unscopedMetric.sample(delta);
        unscopedMetric.addExclusive(measurement.getExclusiveTimeInSeconds());
    }
    
    protected void addMetric(final Metric newMetric) {
        Metric metric;
        if (newMetric.getScope() != null) {
            metric = this.metrics.get(newMetric.getName(), newMetric.getScope());
        }
        else {
            metric = this.metrics.get(newMetric.getName());
        }
        if (metric != null) {
            metric.aggregate(newMetric);
        }
        else {
            this.metrics.add(newMetric);
        }
    }
    
    @Override
    public void onHarvest() {
        for (final Metric metric : this.metrics.getAll()) {
            Harvest.addMetric(metric);
        }
    }
    
    @Override
    public void onHarvestComplete() {
        this.metrics.clear();
    }
    
    @Override
    public void onHarvestError() {
        this.metrics.clear();
    }
    
    @Override
    public void onHarvestSendFailed() {
        this.metrics.clear();
    }
}
