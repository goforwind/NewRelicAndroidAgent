// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.logging;

public class ConsoleAgentLog implements AgentLog
{
    private int level;
    
    public ConsoleAgentLog() {
        this.level = 3;
    }
    
    @Override
    public void debug(final String message) {
        if (this.level == 5) {
            print("DEBUG", message);
        }
    }
    
    @Override
    public void verbose(final String message) {
        if (this.level >= 4) {
            print("VERBOSE", message);
        }
    }
    
    @Override
    public void info(final String message) {
        if (this.level >= 3) {
            print("INFO", message);
        }
    }
    
    @Override
    public void warning(final String message) {
        if (this.level >= 2) {
            print("WARN", message);
        }
    }
    
    @Override
    public void error(final String message, final Throwable cause) {
        if (this.level >= 1) {
            print("ERROR", message + " " + cause.getMessage());
        }
    }
    
    @Override
    public void error(final String message) {
        if (this.level >= 1) {
            print("ERROR", message);
        }
    }
    
    @Override
    public int getLevel() {
        return this.level;
    }
    
    @Override
    public void setLevel(final int level) {
        this.level = level;
    }
    
    private static void print(final String tag, final String message) {
        System.out.println("[" + tag + "] " + message);
    }
}
