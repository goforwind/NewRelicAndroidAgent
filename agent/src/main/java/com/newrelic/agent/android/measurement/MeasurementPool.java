// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Iterator;
import java.util.List;
import com.newrelic.agent.android.util.ExceptionHelper;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import com.newrelic.agent.android.measurement.producer.MeasurementProducer;
import java.util.Collection;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.measurement.consumer.MeasurementConsumer;
import com.newrelic.agent.android.measurement.producer.BaseMeasurementProducer;

public class MeasurementPool extends BaseMeasurementProducer implements MeasurementConsumer
{
    private static final AgentLog log;
    private final Collection<MeasurementProducer> producers;
    private final Collection<MeasurementConsumer> consumers;
    
    public MeasurementPool() {
        super(MeasurementType.Any);
        this.producers = new CopyOnWriteArrayList<MeasurementProducer>();
        this.consumers = new CopyOnWriteArrayList<MeasurementConsumer>();
        this.addMeasurementProducer(this);
    }
    
    public void addMeasurementProducer(final MeasurementProducer producer) {
        if (producer != null) {
            synchronized (this.producers) {
                if (this.producers.contains(producer)) {
                    MeasurementPool.log.debug("Attempted to add the same MeasurementProducer " + producer + "  multiple times.");
                    return;
                }
                this.producers.add(producer);
            }
        }
        else {
            MeasurementPool.log.debug("Attempted to add null MeasurementProducer.");
        }
    }
    
    public void removeMeasurementProducer(final MeasurementProducer producer) {
        synchronized (this.producers) {
            if (!this.producers.contains(producer)) {
                MeasurementPool.log.debug("Attempted to remove MeasurementProducer " + producer + " which is not registered.");
                return;
            }
            this.producers.remove(producer);
        }
    }
    
    public void addMeasurementConsumer(final MeasurementConsumer consumer) {
        if (consumer != null) {
            synchronized (this.consumers) {
                if (this.consumers.contains(consumer)) {
                    MeasurementPool.log.debug("Attempted to add the same MeasurementConsumer " + consumer + " multiple times.");
                    return;
                }
                this.consumers.add(consumer);
            }
        }
        else {
            MeasurementPool.log.debug("Attempted to add null MeasurementConsumer.");
        }
    }
    
    public void removeMeasurementConsumer(final MeasurementConsumer consumer) {
        synchronized (this.consumers) {
            if (!this.consumers.contains(consumer)) {
                MeasurementPool.log.debug("Attempted to remove MeasurementConsumer " + consumer + " which is not registered.");
                return;
            }
            this.consumers.remove(consumer);
        }
    }
    
    public void broadcastMeasurements() {
        final List<Measurement> allProducedMeasurements = new ArrayList<Measurement>();
        synchronized (this.producers) {
            for (final MeasurementProducer producer : this.producers) {
                final Collection<Measurement> measurements = producer.drainMeasurements();
                if (measurements.size() > 0) {
                    allProducedMeasurements.addAll(measurements);
                    while (allProducedMeasurements.remove(null)) {}
                }
            }
        }
        if (allProducedMeasurements.size() > 0) {
            synchronized (this.consumers) {
                for (final MeasurementConsumer consumer : this.consumers) {
                    final List<Measurement> measurements2 = new ArrayList<Measurement>(allProducedMeasurements);
                    for (final Measurement measurement : measurements2) {
                        if (consumer.getMeasurementType() != measurement.getType()) {
                            if (consumer.getMeasurementType() != MeasurementType.Any) {
                                continue;
                            }
                        }
                        try {
                            consumer.consumeMeasurement(measurement);
                        }
                        catch (Exception e) {
                            ExceptionHelper.exceptionToErrorCode(e);
                            MeasurementPool.log.error("broadcastMeasurements exception[" + e.getClass().getName() + "]");
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void consumeMeasurement(final Measurement measurement) {
        this.produceMeasurement(measurement);
    }
    
    @Override
    public void consumeMeasurements(final Collection<Measurement> measurements) {
        this.produceMeasurements(measurements);
    }
    
    @Override
    public MeasurementType getMeasurementType() {
        return MeasurementType.Any;
    }
    
    public Collection<MeasurementProducer> getMeasurementProducers() {
        return this.producers;
    }
    
    public Collection<MeasurementConsumer> getMeasurementConsumers() {
        return this.consumers;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
