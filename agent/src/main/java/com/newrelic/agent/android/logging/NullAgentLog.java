// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.logging;

public class NullAgentLog implements AgentLog
{
    @Override
    public void debug(final String message) {
    }
    
    @Override
    public void info(final String message) {
    }
    
    @Override
    public void verbose(final String message) {
    }
    
    @Override
    public void error(final String message) {
    }
    
    @Override
    public void error(final String message, final Throwable cause) {
    }
    
    @Override
    public void warning(final String message) {
    }
    
    @Override
    public int getLevel() {
        return 5;
    }
    
    @Override
    public void setLevel(final int level) {
    }
}
