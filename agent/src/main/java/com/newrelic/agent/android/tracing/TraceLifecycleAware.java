// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.tracing;

public interface TraceLifecycleAware
{
    void onEnterMethod();
    
    void onExitMethod();
    
    void onTraceStart(final ActivityTrace p0);
    
    void onTraceComplete(final ActivityTrace p0);
    
    void onTraceRename(final ActivityTrace p0);
}
