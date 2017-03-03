// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.sample;

import com.newrelic.agent.android.tracing.Sample;
import com.newrelic.agent.android.metric.Metric;
import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.MeasurementType;
import com.newrelic.agent.android.measurement.consumer.MetricMeasurementConsumer;

public class MachineMeasurementConsumer extends MetricMeasurementConsumer
{
    public MachineMeasurementConsumer() {
        super(MeasurementType.Machine);
    }
    
    protected String formatMetricName(final String name) {
        return name;
    }
    
    public void consumeMeasurement(final Measurement measurement) {
    }
    
    public void onHarvest() {
        final Sample memorySample = Sampler.sampleMemory();
        if (memorySample != null) {
            final Metric memoryMetric = new Metric("Memory/Used");
            memoryMetric.sample(memorySample.getValue().doubleValue());
            this.addMetric(memoryMetric);
        }
        super.onHarvest();
    }
}
