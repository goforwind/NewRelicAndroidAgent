// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement;

public class ActivityMeasurement extends BaseMeasurement
{
    public ActivityMeasurement(final String name, final long startTime, final long endTime) {
        super(MeasurementType.Activity);
        this.setName(name);
        this.setStartTime(startTime);
        this.setEndTime(endTime);
    }
}
