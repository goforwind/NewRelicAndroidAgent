// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement;

import com.newrelic.agent.android.instrumentation.MetricCategory;

public class MethodMeasurement extends CategorizedMeasurement
{
    public MethodMeasurement(final String name, final String scope, final long startTime, final long endTime, final long exclusiveTime, final MetricCategory category) {
        super(MeasurementType.Method);
        this.setName(name);
        this.setScope(scope);
        this.setStartTime(startTime);
        this.setEndTime(endTime);
        this.setExclusiveTime(exclusiveTime);
        this.setCategory(category);
    }
}
