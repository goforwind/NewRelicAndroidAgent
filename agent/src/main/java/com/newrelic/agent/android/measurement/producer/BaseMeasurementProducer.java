// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.producer;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Collections;
import java.util.Collection;
import com.newrelic.agent.android.measurement.Measurement;
import java.util.ArrayList;
import com.newrelic.agent.android.measurement.MeasurementType;
import com.newrelic.agent.android.logging.AgentLog;

public class BaseMeasurementProducer implements MeasurementProducer
{
    private static final AgentLog log;
    private final MeasurementType producedMeasurementType;
    private final ArrayList<Measurement> producedMeasurements;
    
    public BaseMeasurementProducer(final MeasurementType measurementType) {
        this.producedMeasurements = new ArrayList<Measurement>();
        this.producedMeasurementType = measurementType;
    }
    
    @Override
    public MeasurementType getMeasurementType() {
        return this.producedMeasurementType;
    }
    
    @Override
    public void produceMeasurement(final Measurement measurement) {
        synchronized (this.producedMeasurements) {
            if (measurement != null) {
                this.producedMeasurements.add(measurement);
            }
        }
    }
    
    @Override
    public void produceMeasurements(final Collection<Measurement> measurements) {
        synchronized (this.producedMeasurements) {
            if (measurements != null) {
                this.producedMeasurements.addAll(measurements);
                while (this.producedMeasurements.remove(null)) {}
            }
        }
    }
    
    @Override
    public Collection<Measurement> drainMeasurements() {
        synchronized (this.producedMeasurements) {
            if (this.producedMeasurements.size() == 0) {
                return (Collection<Measurement>)Collections.emptyList();
            }
            final Collection<Measurement> measurements = new ArrayList<Measurement>(this.producedMeasurements);
            this.producedMeasurements.clear();
            return measurements;
        }
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
