// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.activity;

import com.newrelic.agent.android.measurement.MeasurementPool;
import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.ThreadInfo;

public interface MeasuredActivity
{
    String getName();
    
    String getMetricName();
    
    void setName(final String p0);
    
    String getBackgroundMetricName();
    
    long getStartTime();
    
    long getEndTime();
    
    ThreadInfo getStartingThread();
    
    ThreadInfo getEndingThread();
    
    boolean isAutoInstrumented();
    
    Measurement getStartingMeasurement();
    
    Measurement getEndingMeasurement();
    
    MeasurementPool getMeasurementPool();
    
    void finish();
    
    boolean isFinished();
}
