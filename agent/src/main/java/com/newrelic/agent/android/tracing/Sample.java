// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.tracing;

import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.agent.android.util.SafeJsonPrimitive;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class Sample extends HarvestableArray
{
    private long timestamp;
    private SampleValue sampleValue;
    private SampleType type;
    
    public Sample(final SampleType type) {
        this.setSampleType(type);
        this.setTimestamp(System.currentTimeMillis());
    }
    
    public Sample(final long timestamp) {
        this.setTimestamp(timestamp);
    }
    
    public Sample(final long timestamp, final SampleValue sampleValue) {
        this.setTimestamp(timestamp);
        this.setSampleValue(sampleValue);
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }
    
    public SampleValue getSampleValue() {
        return this.sampleValue;
    }
    
    public void setSampleValue(final SampleValue sampleValue) {
        this.sampleValue = sampleValue;
    }
    
    public void setSampleValue(final double value) {
        this.sampleValue = new SampleValue(value);
    }
    
    public void setSampleValue(final long value) {
        this.sampleValue = new SampleValue(value);
    }
    
    public Number getValue() {
        return this.sampleValue.getValue();
    }
    
    public SampleType getSampleType() {
        return this.type;
    }
    
    public void setSampleType(final SampleType type) {
        this.type = type;
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray jsonArray = new JsonArray();
        jsonArray.add(SafeJsonPrimitive.factory(this.timestamp));
        jsonArray.add(SafeJsonPrimitive.factory(this.sampleValue.getValue()));
        return jsonArray;
    }
    
    public enum SampleType
    {
        MEMORY, 
        CPU;
    }
}
