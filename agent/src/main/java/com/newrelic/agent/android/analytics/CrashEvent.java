// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.Set;

public class CrashEvent extends AnalyticsEvent
{
    public CrashEvent(final String name) {
        super(name, AnalyticsEventCategory.Crash);
    }
    
    public CrashEvent(final String name, final Set<AnalyticAttribute> attributeSet) {
        super(name, AnalyticsEventCategory.Crash, "Mobile", attributeSet);
    }
}
