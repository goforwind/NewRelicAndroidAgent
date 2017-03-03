// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement;

import com.newrelic.agent.android.instrumentation.MetricCategory;

public class CategorizedMeasurement extends BaseMeasurement
{
    private MetricCategory category;
    
    public CategorizedMeasurement(final MeasurementType measurementType) {
        super(measurementType);
    }
    
    public MetricCategory getCategory() {
        return this.category;
    }
    
    public void setCategory(final MetricCategory category) {
        this.category = category;
    }
}
