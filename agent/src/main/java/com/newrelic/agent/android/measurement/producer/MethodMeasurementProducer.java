// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.producer;

import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.MethodMeasurement;
import com.newrelic.agent.android.tracing.Trace;
import com.newrelic.agent.android.measurement.MeasurementType;

public class MethodMeasurementProducer extends BaseMeasurementProducer
{
    public MethodMeasurementProducer() {
        super(MeasurementType.Method);
    }
    
    public void produceMeasurement(final Trace trace) {
        final MethodMeasurement methodMeasurement = new MethodMeasurement(trace.displayName, trace.scope, trace.entryTimestamp, trace.exitTimestamp, trace.exclusiveTime, trace.getCategory());
        this.produceMeasurement(methodMeasurement);
    }
}
