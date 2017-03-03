// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.consumer;

import com.newrelic.agent.android.measurement.MeasurementType;

public class ActivityMeasurementConsumer extends MetricMeasurementConsumer
{
    public ActivityMeasurementConsumer() {
        super(MeasurementType.Activity);
    }
    
    @Override
    protected String formatMetricName(final String name) {
        return name;
    }
}
