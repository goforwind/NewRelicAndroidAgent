// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

public class Location
{
    private final String countryCode;
    private final String region;
    
    public Location(final String countryCode, final String region) {
        if (countryCode == null || region == null) {
            throw new IllegalArgumentException("Country code and region must not be null.");
        }
        this.countryCode = countryCode;
        this.region = region;
    }
    
    public String getCountryCode() {
        return this.countryCode;
    }
    
    public String getRegion() {
        return this.region;
    }
}
