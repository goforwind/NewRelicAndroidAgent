// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.producer;

import java.util.Collection;
import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.MeasurementType;

public interface MeasurementProducer
{
    MeasurementType getMeasurementType();
    
    void produceMeasurement(final Measurement p0);
    
    void produceMeasurements(final Collection<Measurement> p0);
    
    Collection<Measurement> drainMeasurements();
}
