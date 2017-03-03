// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement;

public interface Measurement
{
    MeasurementType getType();
    
    String getName();
    
    String getScope();
    
    long getStartTime();
    
    double getStartTimeInSeconds();
    
    long getEndTime();
    
    double getEndTimeInSeconds();
    
    long getExclusiveTime();
    
    double getExclusiveTimeInSeconds();
    
    ThreadInfo getThreadInfo();
    
    boolean isInstantaneous();
    
    void finish();
    
    boolean isFinished();
    
    double asDouble();
}
