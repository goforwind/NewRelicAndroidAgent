// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.background;

public interface ApplicationStateListener
{
    void applicationForegrounded(final ApplicationStateEvent p0);
    
    void applicationBackgrounded(final ApplicationStateEvent p0);
}
