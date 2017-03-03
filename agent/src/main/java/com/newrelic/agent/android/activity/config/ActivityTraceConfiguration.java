// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.activity.config;

public class ActivityTraceConfiguration
{
    private int maxTotalTraceCount;
    
    public static ActivityTraceConfiguration defaultActivityTraceConfiguration() {
        final ActivityTraceConfiguration configuration = new ActivityTraceConfiguration();
        configuration.setMaxTotalTraceCount(1);
        return configuration;
    }
    
    public int getMaxTotalTraceCount() {
        return this.maxTotalTraceCount;
    }
    
    public void setMaxTotalTraceCount(final int maxTotalTraceCount) {
        this.maxTotalTraceCount = maxTotalTraceCount;
    }
    
    @Override
    public String toString() {
        return "ActivityTraceConfiguration{maxTotalTraceCount=" + this.maxTotalTraceCount + '}';
    }
}
