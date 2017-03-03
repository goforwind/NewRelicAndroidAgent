// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.Set;

public class SessionEvent extends AnalyticsEvent
{
    public SessionEvent() {
        super(null, AnalyticsEventCategory.Session);
    }
    
    public SessionEvent(final Set<AnalyticAttribute> attributeSet) {
        super(null, AnalyticsEventCategory.Session, "Mobile", attributeSet);
    }
}
