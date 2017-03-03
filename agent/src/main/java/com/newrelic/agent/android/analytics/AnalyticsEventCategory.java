// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

public enum AnalyticsEventCategory
{
    Session, 
    Interaction, 
    Crash, 
    Custom, 
    RequestError;
    
    public static AnalyticsEventCategory fromString(final String categoryString) {
        AnalyticsEventCategory category = AnalyticsEventCategory.Custom;
        if (categoryString != null) {
            if (categoryString.equalsIgnoreCase("session")) {
                category = AnalyticsEventCategory.Session;
            }
            else if (categoryString.equalsIgnoreCase("interaction")) {
                category = AnalyticsEventCategory.Interaction;
            }
            else if (categoryString.equalsIgnoreCase("crash")) {
                category = AnalyticsEventCategory.Crash;
            }
            else if (categoryString.equalsIgnoreCase("requesterror")) {
                category = AnalyticsEventCategory.RequestError;
            }
        }
        return category;
    }
}
