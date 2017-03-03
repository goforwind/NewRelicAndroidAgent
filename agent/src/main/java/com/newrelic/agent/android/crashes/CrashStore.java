// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.crashes;

import java.util.List;
import com.newrelic.agent.android.harvest.crash.Crash;

public interface CrashStore
{
    void store(final Crash p0);
    
    List<Crash> fetchAll();
    
    int count();
    
    void clear();
    
    void delete(final Crash p0);
}
