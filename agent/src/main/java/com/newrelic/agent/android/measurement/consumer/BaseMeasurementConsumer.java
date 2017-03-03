// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.consumer;

import java.util.Iterator;
import java.util.Collection;
import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.MeasurementType;
import com.newrelic.agent.android.harvest.HarvestAdapter;

public class BaseMeasurementConsumer extends HarvestAdapter implements MeasurementConsumer
{
    private final MeasurementType measurementType;
    
    public BaseMeasurementConsumer(final MeasurementType measurementType) {
        this.measurementType = measurementType;
    }
    
    @Override
    public MeasurementType getMeasurementType() {
        return this.measurementType;
    }
    
    @Override
    public void consumeMeasurement(final Measurement measurement) {
    }
    
    @Override
    public void consumeMeasurements(final Collection<Measurement> measurements) {
        for (final Measurement measurement : measurements) {
            this.consumeMeasurement(measurement);
        }
    }
}
