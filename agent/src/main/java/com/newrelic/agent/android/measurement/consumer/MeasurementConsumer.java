// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.consumer;

import java.util.Collection;
import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.MeasurementType;

public interface MeasurementConsumer
{
    MeasurementType getMeasurementType();
    
    void consumeMeasurement(final Measurement p0);
    
    void consumeMeasurements(final Collection<Measurement> p0);
}
