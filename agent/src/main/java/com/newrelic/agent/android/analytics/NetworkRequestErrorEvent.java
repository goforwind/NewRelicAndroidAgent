// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.Set;

public class NetworkRequestErrorEvent extends AnalyticsEvent
{
    public NetworkRequestErrorEvent() {
        super(null, AnalyticsEventCategory.RequestError);
    }
    
    public NetworkRequestErrorEvent(final Set<AnalyticAttribute> attributeSet) {
        super(null, AnalyticsEventCategory.RequestError, "MobileRequestError", attributeSet);
    }
}
