// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.Set;

public class InteractionEvent extends AnalyticsEvent
{
    public InteractionEvent(final String name) {
        super(name, AnalyticsEventCategory.Interaction);
    }
    
    public InteractionEvent(final String name, final Set<AnalyticAttribute> attributeSet) {
        super(name, AnalyticsEventCategory.Interaction, "Mobile", attributeSet);
    }
}
