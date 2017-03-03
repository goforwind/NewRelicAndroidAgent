// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.consumer;

import com.newrelic.agent.android.metric.Metric;
import com.newrelic.agent.android.measurement.CustomMetricMeasurement;
import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.MeasurementType;

public class CustomMetricConsumer extends MetricMeasurementConsumer
{
    private static final String METRIC_PREFIX = "Custom/";
    
    public CustomMetricConsumer() {
        super(MeasurementType.Custom);
    }
    
    @Override
    protected String formatMetricName(final String name) {
        return "Custom/" + name;
    }
    
    @Override
    public void consumeMeasurement(final Measurement measurement) {
        final CustomMetricMeasurement custom = (CustomMetricMeasurement)measurement;
        final Metric metric = custom.getCustomMetric();
        metric.setName(this.formatMetricName(metric.getName()));
        this.addMetric(metric);
    }
}
