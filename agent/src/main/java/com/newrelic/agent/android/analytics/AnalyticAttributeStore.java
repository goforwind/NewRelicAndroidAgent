// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.List;

public interface AnalyticAttributeStore
{
    boolean store(final AnalyticAttribute p0);
    
    List<AnalyticAttribute> fetchAll();
    
    int count();
    
    void clear();
    
    void delete(final AnalyticAttribute p0);
}
