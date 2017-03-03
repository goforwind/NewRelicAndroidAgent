// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.logging;

public interface AgentLog
{
    public static final int DEBUG = 5;
    public static final int VERBOSE = 4;
    public static final int INFO = 3;
    public static final int WARNING = 2;
    public static final int ERROR = 1;
    
    void debug(final String p0);
    
    void verbose(final String p0);
    
    void info(final String p0);
    
    void warning(final String p0);
    
    void error(final String p0);
    
    void error(final String p0, final Throwable p1);
    
    int getLevel();
    
    void setLevel(final int p0);
}
