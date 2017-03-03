// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.util;

import com.newrelic.agent.android.background.ApplicationStateMonitor;
import android.content.res.Configuration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import android.content.ComponentCallbacks2;

public class UiBackgroundListener implements ComponentCallbacks2
{
    protected final ScheduledExecutorService executor;
    
    public UiBackgroundListener() {
        this.executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("UiBackgroundListener"));
    }
    
    public void onConfigurationChanged(final Configuration newConfig) {
    }
    
    public void onLowMemory() {
    }
    
    public void onTrimMemory(final int level) {
        switch (level) {
            case 20: {
                final Runnable runner = new Runnable() {
                    public void run() {
                        ApplicationStateMonitor.getInstance().uiHidden();
                    }
                };
                this.executor.submit(runner);
                break;
            }
        }
    }
}
