// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.concurrent.TimeUnit;
import com.newrelic.agent.android.background.ApplicationStateMonitor;
import com.newrelic.agent.android.stats.TicToc;
import java.util.concurrent.locks.ReentrantLock;
import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;
import com.newrelic.agent.android.util.NamedThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.ScheduledFuture;
import com.newrelic.agent.android.logging.AgentLog;
import java.util.concurrent.ScheduledExecutorService;

public class HarvestTimer implements Runnable
{
    private static final long DEFAULT_HARVEST_PERIOD = 60000L;
    private static final long HARVEST_PERIOD_LEEWAY = 1000L;
    private static final long NEVER_TICKED = -1L;
    private final ScheduledExecutorService scheduler;
    private final AgentLog log;
    private ScheduledFuture tickFuture;
    protected long period;
    protected final Harvester harvester;
    protected long lastTickTime;
    private long startTimeMs;
    private Lock lock;
    
    public HarvestTimer(final Harvester harvester) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Harvester"));
        this.log = AgentLogManager.getAgentLog();
        this.tickFuture = null;
        this.period = 60000L;
        this.lock = new ReentrantLock();
        this.harvester = harvester;
        this.startTimeMs = 0L;
    }
    
    @Override
    public void run() {
        try {
            this.lock.lock();
            this.tickIfReady();
        }
        catch (Exception e) {
            this.log.error("HarvestTimer: Exception in timer tick: " + e.getMessage());
            e.printStackTrace();
            AgentHealth.noticeException(e);
        }
        finally {
            this.lock.unlock();
        }
    }
    
    private void tickIfReady() {
        final long lastTickDelta = this.timeSinceLastTick();
        if (lastTickDelta + 1000L < this.period && lastTickDelta != -1L) {
            this.log.debug("HarvestTimer: Tick is too soon (" + lastTickDelta + " delta) Last tick time: " + this.lastTickTime + " . Skipping.");
            return;
        }
        this.log.debug("HarvestTimer: time since last tick: " + lastTickDelta);
        final long tickStart = this.now();
        try {
            this.tick();
        }
        catch (Exception e) {
            this.log.error("HarvestTimer: Exception in timer tick: " + e.getMessage());
            e.printStackTrace();
            AgentHealth.noticeException(e);
        }
        this.lastTickTime = tickStart;
        this.log.debug("Set last tick time to: " + this.lastTickTime);
    }
    
    protected void tick() {
        this.log.debug("Harvest: tick");
        final TicToc t = new TicToc();
        t.tic();
        try {
            if (ApplicationStateMonitor.isAppInBackground()) {
                this.log.error("HarvestTimer: Attempting to harvest while app is in background");
            }
            else {
                this.harvester.execute();
                this.log.debug("Harvest: executed");
            }
        }
        catch (Exception e) {
            this.log.error("HarvestTimer: Exception in harvest execute: " + e.getMessage());
            e.printStackTrace();
            AgentHealth.noticeException(e);
        }
        if (this.harvester.isDisabled()) {
            this.stop();
        }
        final long tickDelta = t.toc();
        this.log.debug("HarvestTimer tick took " + tickDelta + "ms");
    }
    
    public void start() {
        if (ApplicationStateMonitor.isAppInBackground()) {
            this.log.warning("HarvestTimer: Attempting to start while app is in background");
            return;
        }
        if (this.isRunning()) {
            this.log.warning("HarvestTimer: Attempting to start while already running");
            return;
        }
        if (this.period <= 0L) {
            this.log.error("HarvestTimer: Refusing to start with a period of 0 ms");
            return;
        }
        this.log.debug("HarvestTimer: Starting with a period of " + this.period + "ms");
        this.startTimeMs = System.currentTimeMillis();
        this.tickFuture = this.scheduler.scheduleAtFixedRate(this, 0L, this.period, TimeUnit.MILLISECONDS);
        this.harvester.start();
    }
    
    public void stop() {
        if (!this.isRunning()) {
            this.log.warning("HarvestTimer: Attempting to stop when not running");
            return;
        }
        this.cancelPendingTasks();
        this.log.debug("HarvestTimer: Stopped.");
        this.startTimeMs = 0L;
        this.harvester.stop();
    }
    
    public void shutdown() {
        this.cancelPendingTasks();
        this.scheduler.shutdownNow();
    }
    
    public void tickNow() {
        final ScheduledFuture future = this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                HarvestTimer.this.tick();
            }
        }, 0L, TimeUnit.SECONDS);
        try {
            future.get();
        }
        catch (Exception e) {
            this.log.error("Exception waiting for tickNow to finish: " + e.getMessage());
            e.printStackTrace();
            AgentHealth.noticeException(e);
        }
    }
    
    public boolean isRunning() {
        return this.tickFuture != null;
    }
    
    public void setPeriod(final long period) {
        this.period = period;
    }
    
    public long timeSinceLastTick() {
        if (this.lastTickTime == 0L) {
            return -1L;
        }
        return this.now() - this.lastTickTime;
    }
    
    public long timeSinceStart() {
        if (this.startTimeMs == 0L) {
            return 0L;
        }
        return this.now() - this.startTimeMs;
    }
    
    private long now() {
        return System.currentTimeMillis();
    }
    
    protected void cancelPendingTasks() {
        try {
            this.lock.lock();
            if (this.tickFuture != null) {
                this.tickFuture.cancel(true);
                this.tickFuture = null;
            }
        }
        finally {
            this.lock.unlock();
        }
    }
}
