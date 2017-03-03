// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.tracing;

public class SampleValue
{
    private Double value;
    private boolean isDouble;
    
    public SampleValue(final double value) {
        this.value = 0.0;
        this.setValue(value);
    }
    
    public SampleValue(final long value) {
        this.value = 0.0;
        this.setValue(value);
    }
    
    public Number getValue() {
        if (this.isDouble) {
            return this.asDouble();
        }
        return this.asLong();
    }
    
    public Double asDouble() {
        return this.value;
    }
    
    public Long asLong() {
        return (long)(Object)this.value;
    }
    
    public void setValue(final double value) {
        this.value = value;
        this.isDouble = true;
    }
    
    public void setValue(final long value) {
        this.value = (double)value;
        this.isDouble = false;
    }
    
    public boolean isDouble() {
        return this.isDouble;
    }
    
    public void setDouble(final boolean aDouble) {
        this.isDouble = aDouble;
    }
}
