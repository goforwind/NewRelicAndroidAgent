// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement;

import com.newrelic.agent.android.metric.Metric;

public class CustomMetricMeasurement extends CategorizedMeasurement
{
    private Metric customMetric;
    
    public CustomMetricMeasurement() {
        super(MeasurementType.Custom);
    }
    
    public CustomMetricMeasurement(final String name, final int count, final double totalValue, final double exclusiveValue) {
        this();
        this.setName(name);
        (this.customMetric = new Metric(name)).sample(totalValue);
        this.customMetric.setCount(count);
        this.customMetric.setExclusive(exclusiveValue);
    }
    
    public Metric getCustomMetric() {
        return this.customMetric;
    }
}
