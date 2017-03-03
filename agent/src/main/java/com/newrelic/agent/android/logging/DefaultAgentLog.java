// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.logging;

public class DefaultAgentLog implements AgentLog
{
    private AgentLog impl;
    
    public DefaultAgentLog() {
        this.impl = new NullAgentLog();
    }
    
    public void setImpl(final AgentLog impl) {
        synchronized (this) {
            this.impl = impl;
        }
    }
    
    @Override
    public void debug(final String message) {
        synchronized (this) {
            this.impl.debug(message);
        }
    }
    
    @Override
    public void info(final String message) {
        synchronized (this) {
            this.impl.info(message);
        }
    }
    
    @Override
    public void verbose(final String message) {
        synchronized (this) {
            this.impl.verbose(message);
        }
    }
    
    @Override
    public void warning(final String message) {
        synchronized (this) {
            this.impl.warning(message);
        }
    }
    
    @Override
    public void error(final String message) {
        synchronized (this) {
            this.impl.error(message);
        }
    }
    
    @Override
    public void error(final String message, final Throwable cause) {
        synchronized (this) {
            this.impl.error(message, cause);
        }
    }
    
    @Override
    public int getLevel() {
        synchronized (this) {
            return this.impl.getLevel();
        }
    }
    
    @Override
    public void setLevel(final int level) {
        synchronized (this) {
            this.impl.setLevel(level);
        }
    }
}
