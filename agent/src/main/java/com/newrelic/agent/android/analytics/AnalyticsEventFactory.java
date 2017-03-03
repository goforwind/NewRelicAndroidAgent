// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.Set;

class AnalyticsEventFactory
{
    static AnalyticsEvent createEvent(final String name, final AnalyticsEventCategory eventCategory, final String eventType, final Set<AnalyticAttribute> eventAttributes) {
        AnalyticsEvent event = null;
        switch (eventCategory) {
            case Session: {
                event = new SessionEvent(eventAttributes);
                break;
            }
            case RequestError: {
                event = new NetworkRequestErrorEvent(eventAttributes);
                break;
            }
            case Interaction: {
                event = new InteractionEvent(name, eventAttributes);
                break;
            }
            case Crash: {
                event = new CrashEvent(name, eventAttributes);
                break;
            }
            case Custom: {
                event = new CustomEvent(name, eventType, eventAttributes);
                break;
            }
        }
        return event;
    }
}
