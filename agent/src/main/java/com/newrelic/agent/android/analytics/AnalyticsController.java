// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.Map;
import java.util.Set;

public interface AnalyticsController
{
    AnalyticAttribute getAttribute(final String p0);
    
    Set<AnalyticAttribute> getSystemAttributes();
    
    Set<AnalyticAttribute> getUserAttributes();
    
    Set<AnalyticAttribute> getSessionAttributes();
    
    int getSystemAttributeCount();
    
    int getUserAttributeCount();
    
    int getSessionAttributeCount();
    
    boolean setAttribute(final String p0, final String p1);
    
    boolean setAttribute(final String p0, final String p1, final boolean p2);
    
    boolean setAttribute(final String p0, final float p1);
    
    boolean setAttribute(final String p0, final float p1, final boolean p2);
    
    boolean setAttribute(final String p0, final boolean p1);
    
    boolean setAttribute(final String p0, final boolean p1, final boolean p2);
    
    boolean incrementAttribute(final String p0, final float p1);
    
    boolean incrementAttribute(final String p0, final float p1, final boolean p2);
    
    boolean removeAttribute(final String p0);
    
    boolean removeAllAttributes();
    
    boolean addEvent(final AnalyticsEvent p0);
    
    boolean addEvent(final String p0, final Set<AnalyticAttribute> p1);
    
    boolean addEvent(final String p0, final AnalyticsEventCategory p1, final String p2, final Set<AnalyticAttribute> p3);
    
    int getMaxEventPoolSize();
    
    void setMaxEventPoolSize(final int p0);
    
    int getMaxEventBufferTime();
    
    void setMaxEventBufferTime(final int p0);
    
    EventManager getEventManager();
    
    boolean recordEvent(final String p0, final Map<String, Object> p1);
}
