// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.background;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Iterator;
import java.util.Collection;
import java.util.concurrent.ThreadFactory;
import com.newrelic.agent.android.util.NamedThreadFactory;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.atomic.AtomicLong;
import com.newrelic.agent.android.logging.AgentLog;

public class ApplicationStateMonitor implements Runnable
{
    private static final AgentLog log;
    private AtomicLong count;
    private AtomicLong snoozeStartTime;
    private final Lock snoozeLock;
    private final int activitySnoozeTimeInMilliseconds;
    protected final ArrayList<ApplicationStateListener> applicationStateListeners;
    protected AtomicBoolean foregrounded;
    private final Lock foregroundLock;
    private static ApplicationStateMonitor instance;
    protected final ScheduledThreadPoolExecutor executor;
    
    private ApplicationStateMonitor() {
        this(5, 5, TimeUnit.SECONDS, 5000);
    }
    
    ApplicationStateMonitor(final int initialDelay, final int period, final TimeUnit timeUnit, final int snoozeTimeInMilliseconds) {
        this.count = new AtomicLong(0L);
        this.snoozeStartTime = new AtomicLong(0L);
        this.snoozeLock = new ReentrantLock();
        this.applicationStateListeners = new ArrayList<ApplicationStateListener>();
        this.foregrounded = new AtomicBoolean(true);
        this.foregroundLock = new ReentrantLock();
        this.executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("AppStateMon"));
        this.activitySnoozeTimeInMilliseconds = snoozeTimeInMilliseconds;
        this.executor.scheduleAtFixedRate(this, initialDelay, period, timeUnit);
        ApplicationStateMonitor.log.info("Application state monitor has started");
    }
    
    public static ApplicationStateMonitor getInstance() {
        if (ApplicationStateMonitor.instance == null) {
            ApplicationStateMonitor.instance = new ApplicationStateMonitor();
        }
        return ApplicationStateMonitor.instance;
    }
    
    public void addApplicationStateListener(final ApplicationStateListener listener) {
        synchronized (this.applicationStateListeners) {
            this.applicationStateListeners.add(listener);
        }
    }
    
    public void removeApplicationStateListener(final ApplicationStateListener listener) {
        synchronized (this.applicationStateListeners) {
            this.applicationStateListeners.remove(listener);
        }
    }
    
    @Override
    public void run() {
        try {
            this.foregroundLock.lock();
            if (this.foregrounded.get() && this.getSnoozeTime() >= this.activitySnoozeTimeInMilliseconds) {
                this.foregrounded.set(false);
                this.notifyApplicationInBackground();
            }
        }
        finally {
            this.foregroundLock.unlock();
        }
    }
    
    public void uiHidden() {
        final Runnable runner = new Runnable() {
            @Override
            public void run() {
                try {
                    ApplicationStateMonitor.this.foregroundLock.lock();
                    if (ApplicationStateMonitor.this.foregrounded.get()) {
                        ApplicationStateMonitor.log.info("UI has become hidden (app backgrounded)");
                        ApplicationStateMonitor.this.notifyApplicationInBackground();
                        ApplicationStateMonitor.this.foregrounded.set(false);
                    }
                }
                finally {
                    ApplicationStateMonitor.this.foregroundLock.unlock();
                }
            }
        };
        this.executor.execute(runner);
    }
    
    public void activityStopped() {
        final Runnable runner = new Runnable() {
            @Override
            public void run() {
                try {
                    ApplicationStateMonitor.this.foregroundLock.lock();
                    try {
                        ApplicationStateMonitor.this.snoozeLock.lock();
                        if (ApplicationStateMonitor.this.count.decrementAndGet() == 0L) {
                            ApplicationStateMonitor.this.snoozeStartTime.set(System.currentTimeMillis());
                        }
                    }
                    finally {
                        ApplicationStateMonitor.this.snoozeLock.unlock();
                    }
                }
                finally {
                    ApplicationStateMonitor.this.foregroundLock.unlock();
                }
            }
        };
        this.executor.execute(runner);
    }
    
    public void activityStarted() {
        final Runnable runner = new Runnable() {
            @Override
            public void run() {
                try {
                    ApplicationStateMonitor.this.foregroundLock.lock();
                    try {
                        ApplicationStateMonitor.this.snoozeLock.lock();
                        if (ApplicationStateMonitor.this.count.incrementAndGet() == 1L) {
                            ApplicationStateMonitor.this.snoozeStartTime.set(0L);
                        }
                    }
                    finally {
                        ApplicationStateMonitor.this.snoozeLock.unlock();
                    }
                    if (!ApplicationStateMonitor.this.foregrounded.get()) {
                        ApplicationStateMonitor.log.verbose("Application appears to be in the foreground");
                        ApplicationStateMonitor.this.foregrounded.set(true);
                        ApplicationStateMonitor.this.notifyApplicationInForeground();
                    }
                }
                finally {
                    ApplicationStateMonitor.this.foregroundLock.unlock();
                }
            }
        };
        this.executor.execute(runner);
    }
    
    private void notifyApplicationInBackground() {
        ApplicationStateMonitor.log.verbose("Application appears to have gone to the background");
        final ArrayList<ApplicationStateListener> listeners;
        synchronized (this.applicationStateListeners) {
            listeners = new ArrayList<ApplicationStateListener>(this.applicationStateListeners);
        }
        final ApplicationStateEvent e = new ApplicationStateEvent(this);
        for (final ApplicationStateListener listener : listeners) {
            listener.applicationBackgrounded(e);
        }
    }
    
    private void notifyApplicationInForeground() {
        final ArrayList<ApplicationStateListener> listeners;
        synchronized (this.applicationStateListeners) {
            listeners = new ArrayList<ApplicationStateListener>(this.applicationStateListeners);
        }
        final ApplicationStateEvent e = new ApplicationStateEvent(this);
        for (final ApplicationStateListener listener : listeners) {
            listener.applicationForegrounded(e);
        }
    }
    
    private long getSnoozeTime() {
        long snoozeValue = 0L;
        try {
            this.foregroundLock.lock();
            try {
                this.snoozeLock.lock();
                final long snoozeTime = this.snoozeStartTime.get();
                if (snoozeTime != 0L) {
                    snoozeValue = System.currentTimeMillis() - snoozeTime;
                }
            }
            finally {
                this.snoozeLock.unlock();
            }
        }
        finally {
            this.foregroundLock.unlock();
        }
        return snoozeValue;
    }
    
    public ScheduledThreadPoolExecutor getExecutor() {
        return this.executor;
    }
    
    public boolean getForegrounded() {
        return this.foregrounded.get();
    }
    
    public static boolean isAppInBackground() {
        return !getInstance().getForegrounded();
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
