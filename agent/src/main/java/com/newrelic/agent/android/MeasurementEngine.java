// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android;

import com.newrelic.agent.android.measurement.producer.MeasurementProducer;
import com.newrelic.agent.android.measurement.consumer.MeasurementConsumer;
import com.newrelic.agent.android.activity.NamedActivity;
import com.newrelic.agent.android.measurement.MeasurementException;
import java.util.concurrent.ConcurrentHashMap;
import com.newrelic.agent.android.measurement.MeasurementPool;
import com.newrelic.agent.android.activity.MeasuredActivity;
import java.util.Map;

public class MeasurementEngine
{
    private final Map<String, MeasuredActivity> activities;
    private final MeasurementPool rootMeasurementPool;
    
    public MeasurementEngine() {
        this.activities = new ConcurrentHashMap<String, MeasuredActivity>();
        this.rootMeasurementPool = new MeasurementPool();
    }
    
    public MeasuredActivity startActivity(final String activityName) {
        if (this.activities.containsKey(activityName)) {
            throw new MeasurementException("An activity with the name '" + activityName + "' has already started.");
        }
        final NamedActivity activity = new NamedActivity(activityName);
        this.activities.put(activityName, activity);
        final MeasurementPool measurementPool = new MeasurementPool();
        activity.setMeasurementPool(measurementPool);
        this.rootMeasurementPool.addMeasurementConsumer(measurementPool);
        return activity;
    }
    
    public void renameActivity(final String oldName, final String newName) {
        final MeasuredActivity namedActivity = this.activities.remove(oldName);
        if (namedActivity != null && namedActivity instanceof NamedActivity) {
            this.activities.put(newName, namedActivity);
            ((NamedActivity)namedActivity).rename(newName);
        }
    }
    
    public MeasuredActivity endActivity(final String activityName) {
        final MeasuredActivity measuredActivity = this.activities.get(activityName);
        if (measuredActivity == null) {
            throw new MeasurementException("Activity '" + activityName + "' has not been started.");
        }
        this.endActivity(measuredActivity);
        return measuredActivity;
    }
    
    public void endActivity(final MeasuredActivity activity) {
        this.rootMeasurementPool.removeMeasurementConsumer(activity.getMeasurementPool());
        this.activities.remove(activity.getName());
        activity.finish();
    }
    
    public void clear() {
        this.activities.clear();
    }
    
    public void addMeasurementProducer(final MeasurementProducer measurementProducer) {
        this.rootMeasurementPool.addMeasurementProducer(measurementProducer);
    }
    
    public void removeMeasurementProducer(final MeasurementProducer measurementProducer) {
        this.rootMeasurementPool.removeMeasurementProducer(measurementProducer);
    }
    
    public void addMeasurementConsumer(final MeasurementConsumer measurementConsumer) {
        this.rootMeasurementPool.addMeasurementConsumer(measurementConsumer);
    }
    
    public void removeMeasurementConsumer(final MeasurementConsumer measurementConsumer) {
        this.rootMeasurementPool.removeMeasurementConsumer(measurementConsumer);
    }
    
    public void broadcastMeasurements() {
        this.rootMeasurementPool.broadcastMeasurements();
    }
}
