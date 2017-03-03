// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.metric;

public enum MetricUnit
{
    PERCENT("%"), 
    BYTES("bytes"), 
    SECONDS("sec"), 
    BYTES_PER_SECOND("bytes/second"), 
    OPERATIONS("op");
    
    private String label;
    
    private MetricUnit(final String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public void setLabel(final String label) {
        this.label = label;
    }
}
