// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.activity;

public class NamedActivity extends BaseMeasuredActivity
{
    public NamedActivity(final String activityName) {
        this.setName(activityName);
        this.setAutoInstrumented(false);
    }
    
    public void rename(final String activityName) {
        this.setName(activityName);
    }
}
