// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android;

import java.util.HashSet;
import java.util.Set;

public enum FeatureFlag
{
    HttpResponseBodyCapture, 
    CrashReporting, 
    AnalyticsEvents, 
    InteractionTracing, 
    DefaultInteractions;
    
    public static final Set<FeatureFlag> enabledFeatures;
    
    public static void enableFeature(final FeatureFlag featureFlag) {
        FeatureFlag.enabledFeatures.add(featureFlag);
    }
    
    public static void disableFeature(final FeatureFlag featureFlag) {
        FeatureFlag.enabledFeatures.remove(featureFlag);
    }
    
    public static boolean featureEnabled(final FeatureFlag featureFlag) {
        return FeatureFlag.enabledFeatures.contains(featureFlag);
    }
    
    static {
        enabledFeatures = new HashSet<FeatureFlag>();
        enableFeature(FeatureFlag.HttpResponseBodyCapture);
        enableFeature(FeatureFlag.CrashReporting);
        enableFeature(FeatureFlag.AnalyticsEvents);
        enableFeature(FeatureFlag.InteractionTracing);
        enableFeature(FeatureFlag.DefaultInteractions);
    }
}
