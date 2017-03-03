// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.consumer;

import com.newrelic.agent.android.measurement.MeasurementType;

public class MethodMeasurementConsumer extends MetricMeasurementConsumer
{
    private static final String METRIC_PREFIX = "Method/";
    
    public MethodMeasurementConsumer() {
        super(MeasurementType.Method);
    }
    
    @Override
    protected String formatMetricName(final String name) {
        return "Method/" + name.replace("#", "/");
    }
}
