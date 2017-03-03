// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.api.v2;

public interface TraceMachineInterface
{
    long getCurrentThreadId();
    
    String getCurrentThreadName();
    
    boolean isUIThread();
}
