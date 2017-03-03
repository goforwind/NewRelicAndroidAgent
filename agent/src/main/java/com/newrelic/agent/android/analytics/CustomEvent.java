// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.Set;

public class CustomEvent extends AnalyticsEvent
{
    public CustomEvent(final String name) {
        super(name, AnalyticsEventCategory.Custom);
    }
    
    public CustomEvent(final String name, final Set<AnalyticAttribute> attributeSet) {
        super(name, AnalyticsEventCategory.Custom, null, attributeSet);
    }
    
    public CustomEvent(final String name, final String eventType, final Set<AnalyticAttribute> attributeSet) {
        super(name, AnalyticsEventCategory.Custom, eventType, attributeSet);
    }
}
