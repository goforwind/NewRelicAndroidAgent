// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.api.v1;

public interface ConnectionListener
{
    void connected(final ConnectionEvent p0);
    
    void disconnected(final ConnectionEvent p0);
}
