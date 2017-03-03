// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.Collection;

public interface EventManager
{
    void initialize();
    
    void shutdown();
    
    int size();
    
    void empty();
    
    boolean isTransmitRequired();
    
    boolean addEvent(final AnalyticsEvent p0);
    
    int getEventsRecorded();
    
    int getEventsEjected();
    
    boolean isMaxEventBufferTimeExceeded();
    
    boolean isMaxEventPoolSizeExceeded();
    
    int getMaxEventPoolSize();
    
    void setMaxEventPoolSize(final int p0);
    
    int getMaxEventBufferTime();
    
    void setMaxEventBufferTime(final int p0);
    
    Collection<AnalyticsEvent> getQueuedEvents();
}
