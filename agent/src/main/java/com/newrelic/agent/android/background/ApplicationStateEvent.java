// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.background;

import java.util.EventObject;

public class ApplicationStateEvent extends EventObject
{
    private static final long serialVersionUID = 1L;
    
    public ApplicationStateEvent(final Object source) {
        super(source);
    }
}
