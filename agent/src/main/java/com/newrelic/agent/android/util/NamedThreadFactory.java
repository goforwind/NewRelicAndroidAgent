// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory
{
    final ThreadGroup group;
    final String namePrefix;
    final AtomicInteger threadNumber;
    
    public NamedThreadFactory(final String factoryName) {
        this.threadNumber = new AtomicInteger(1);
        final SecurityManager s = System.getSecurityManager();
        this.group = ((s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup());
        this.namePrefix = "NR_" + factoryName + "-";
    }
    
    @Override
    public Thread newThread(final Runnable r) {
        final Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != 5) {
            t.setPriority(5);
        }
        return t;
    }
}
