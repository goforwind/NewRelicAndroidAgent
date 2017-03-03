// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement;

public class ThreadInfo
{
    private long id;
    private String name;
    
    public ThreadInfo() {
        this(Thread.currentThread());
    }
    
    public ThreadInfo(final long id, final String name) {
        this.id = id;
        this.name = name;
    }
    
    public ThreadInfo(final Thread thread) {
        this(thread.getId(), thread.getName());
    }
    
    public static ThreadInfo fromThread(final Thread thread) {
        return new ThreadInfo(thread);
    }
    
    public long getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "ThreadInfo{id=" + this.id + ", name='" + this.name + '\'' + '}';
    }
}
