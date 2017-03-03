// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.util;

import com.newrelic.agent.android.logging.AgentLogManager;
import android.os.Bundle;
import com.newrelic.agent.android.background.ApplicationStateMonitor;
import android.app.Activity;
import java.util.concurrent.atomic.AtomicBoolean;
import com.newrelic.agent.android.logging.AgentLog;
import android.app.Application;

public class ActivityLifecycleBackgroundListener extends UiBackgroundListener implements Application.ActivityLifecycleCallbacks
{
    private static final AgentLog log;
    private AtomicBoolean isInBackground;
    
    public ActivityLifecycleBackgroundListener() {
        this.isInBackground = new AtomicBoolean(false);
    }
    
    public void onActivityResumed(final Activity activity) {
        ActivityLifecycleBackgroundListener.log.info("ActivityLifecycleBackgroundListener.onActivityResumed");
        if (this.isInBackground.getAndSet(false)) {
            final Runnable runner = new Runnable() {
                public void run() {
                    ApplicationStateMonitor.getInstance().activityStarted();
                }
            };
            this.executor.submit(runner);
        }
    }
    
    public void onTrimMemory(final int level) {
        ActivityLifecycleBackgroundListener.log.info("ActivityLifecycleBackgroundListener.onTrimMemory level: " + level);
        if (20 == level) {
            this.isInBackground.set(true);
        }
        super.onTrimMemory(level);
    }
    
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
        ActivityLifecycleBackgroundListener.log.info("ActivityLifecycleBackgroundListener.onActivityCreated");
        this.isInBackground.set(false);
    }
    
    public void onActivityDestroyed(final Activity activity) {
        ActivityLifecycleBackgroundListener.log.info("ActivityLifecycleBackgroundListener.onActivityDestroyed");
        this.isInBackground.set(false);
    }
    
    public void onActivityStarted(final Activity activity) {
        if (this.isInBackground.compareAndSet(true, false)) {
            final Runnable runner = new Runnable() {
                public void run() {
                    ActivityLifecycleBackgroundListener.log.debug("ActivityLifecycleBackgroundListener.onActivityStarted - notifying ApplicationStateMonitor");
                    ApplicationStateMonitor.getInstance().activityStarted();
                }
            };
            this.executor.submit(runner);
        }
    }
    
    public void onActivityPaused(final Activity activity) {
        if (this.isInBackground.compareAndSet(false, true)) {
            final Runnable runner = new Runnable() {
                public void run() {
                    ActivityLifecycleBackgroundListener.log.debug("ActivityLifecycleBackgroundListener.onActivityPaused - notifying ApplicationStateMonitor");
                    ApplicationStateMonitor.getInstance().uiHidden();
                }
            };
            this.executor.submit(runner);
        }
    }
    
    public void onActivityStopped(final Activity activity) {
    }
    
    public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
