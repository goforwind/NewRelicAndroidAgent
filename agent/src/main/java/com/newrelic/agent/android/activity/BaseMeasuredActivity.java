// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.activity;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.measurement.MeasurementException;
import com.newrelic.agent.android.tracing.TraceMachine;
import com.newrelic.agent.android.measurement.MeasurementPool;
import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.ThreadInfo;
import com.newrelic.agent.android.logging.AgentLog;

public class BaseMeasuredActivity implements MeasuredActivity
{
    private static final AgentLog log;
    private String name;
    private long startTime;
    private long endTime;
    private ThreadInfo startingThread;
    private ThreadInfo endingThread;
    private boolean autoInstrumented;
    private Measurement startingMeasurement;
    private Measurement endingMeasurement;
    private MeasurementPool measurementPool;
    private boolean finished;
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public String getMetricName() {
        return TraceMachine.formatActivityMetricName(this.name);
    }
    
    @Override
    public String getBackgroundMetricName() {
        return TraceMachine.formatActivityBackgroundMetricName(this.name);
    }
    
    @Override
    public long getStartTime() {
        return this.startTime;
    }
    
    @Override
    public long getEndTime() {
        return this.endTime;
    }
    
    @Override
    public ThreadInfo getStartingThread() {
        return this.startingThread;
    }
    
    @Override
    public ThreadInfo getEndingThread() {
        return this.endingThread;
    }
    
    @Override
    public boolean isAutoInstrumented() {
        return this.autoInstrumented;
    }
    
    @Override
    public Measurement getStartingMeasurement() {
        return this.startingMeasurement;
    }
    
    @Override
    public Measurement getEndingMeasurement() {
        return this.endingMeasurement;
    }
    
    @Override
    public MeasurementPool getMeasurementPool() {
        return this.measurementPool;
    }
    
    @Override
    public void setName(final String name) {
        if (!this.logIfFinished()) {
            this.name = name;
        }
    }
    
    public void setStartTime(final long startTime) {
        if (!this.logIfFinished()) {
            this.startTime = startTime;
        }
    }
    
    public void setEndTime(final long endTime) {
        if (!this.logIfFinished()) {
            this.endTime = endTime;
        }
    }
    
    public void setStartingThread(final ThreadInfo startingThread) {
        if (!this.logIfFinished()) {
            this.startingThread = startingThread;
        }
    }
    
    public void setEndingThread(final ThreadInfo endingThread) {
        if (!this.logIfFinished()) {
            this.endingThread = endingThread;
        }
    }
    
    public void setAutoInstrumented(final boolean autoInstrumented) {
        if (!this.logIfFinished()) {
            this.autoInstrumented = autoInstrumented;
        }
    }
    
    public void setStartingMeasurement(final Measurement startingMeasurement) {
        if (!this.logIfFinished()) {
            this.startingMeasurement = startingMeasurement;
        }
    }
    
    public void setEndingMeasurement(final Measurement endingMeasurement) {
        if (!this.logIfFinished()) {
            this.endingMeasurement = endingMeasurement;
        }
    }
    
    public void setMeasurementPool(final MeasurementPool measurementPool) {
        if (!this.logIfFinished()) {
            this.measurementPool = measurementPool;
        }
    }
    
    @Override
    public void finish() {
        this.finished = true;
    }
    
    @Override
    public boolean isFinished() {
        return this.finished;
    }
    
    private void throwIfFinished() {
        if (this.finished) {
            throw new MeasurementException("Attempted to modify finished Measurement");
        }
    }
    
    private boolean logIfFinished() {
        if (this.finished) {
            BaseMeasuredActivity.log.warning("BaseMeasuredActivity: cannot modify finished Activity");
        }
        return this.finished;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
