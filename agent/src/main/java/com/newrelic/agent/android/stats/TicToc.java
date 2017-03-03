// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.stats;

public class TicToc
{
    private long startTime;
    private long endTime;
    private State state;
    
    public void tic() {
        this.state = State.STARTED;
        this.startTime = System.currentTimeMillis();
    }
    
    public long toc() {
        this.endTime = System.currentTimeMillis();
        if (this.state == State.STARTED) {
            this.state = State.STOPPED;
            return this.endTime - this.startTime;
        }
        return -1L;
    }
    
    public long peek() {
        return System.currentTimeMillis() - this.startTime;
    }
    
    private enum State
    {
        STOPPED, 
        STARTED;
    }
}
