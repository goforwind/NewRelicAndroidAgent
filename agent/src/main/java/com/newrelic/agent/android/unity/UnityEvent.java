// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.unity;

import java.util.HashMap;
import java.util.Map;

public class UnityEvent
{
    private String name;
    private Map<String, Object> attributes;
    
    public UnityEvent(final String name) {
        this.name = name;
        this.attributes = new HashMap<String, Object>();
    }
    
    public String getName() {
        return this.name;
    }
    
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }
    
    public void setAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public void addAttribute(final String name, final String value) {
        this.attributes.put(name, value);
    }
    
    public void addAttribute(final String name, final Double value) {
        this.attributes.put(name, value);
    }
}
