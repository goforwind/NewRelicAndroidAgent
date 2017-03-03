// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.io;

public interface StreamCompleteListenerSource
{
    void addStreamCompleteListener(final StreamCompleteListener p0);
    
    void removeStreamCompleteListener(final StreamCompleteListener p0);
}
