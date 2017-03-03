// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.io;

public interface StreamCompleteListener
{
    void streamComplete(final StreamCompleteEvent p0);
    
    void streamError(final StreamCompleteEvent p0);
}
