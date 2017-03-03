// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.sample;

import com.newrelic.agent.android.logging.AgentLogManager;
import android.os.Process;
import com.newrelic.agent.android.tracing.ActivityTrace;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import android.os.Debug;
import java.util.Iterator;
import com.newrelic.agent.android.stats.TicToc;
import java.util.concurrent.TimeUnit;
import com.newrelic.agent.android.harvest.AgentHealth;
import com.newrelic.agent.android.tracing.TraceMachine;
import java.util.ArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;
import com.newrelic.agent.android.util.NamedThreadFactory;
import android.content.Context;
import com.newrelic.agent.android.metric.Metric;
import java.io.RandomAccessFile;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Collection;
import com.newrelic.agent.android.tracing.Sample;
import java.util.EnumMap;
import android.app.ActivityManager;
import java.util.concurrent.locks.ReentrantLock;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.tracing.TraceLifecycleAware;

public class Sampler implements TraceLifecycleAware, Runnable
{
    protected static final long SAMPLE_FREQ_MS = 100L;
    protected static final long SAMPLE_FREQ_MS_MAX = 250L;
    private static final int[] PID;
    private static final int KB_IN_MB = 1024;
    private static final AgentLog log;
    private static final ReentrantLock samplerLock;
    protected static Sampler sampler;
    protected static boolean cpuSamplingDisabled;
    private final ActivityManager activityManager;
    private final EnumMap<Sample.SampleType, Collection<Sample>> samples;
    private final ScheduledExecutorService scheduler;
    protected final AtomicBoolean isRunning;
    protected long sampleFreqMs;
    protected ScheduledFuture sampleFuture;
    private Long lastCpuTime;
    private Long lastAppCpuTime;
    private RandomAccessFile procStatFile;
    private RandomAccessFile appStatFile;
    private Metric samplerServiceMetric;
    
    protected Sampler(final Context context) {
        this.samples = new EnumMap<Sample.SampleType, Collection<Sample>>(Sample.SampleType.class);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Sampler"));
        this.isRunning = new AtomicBoolean(false);
        this.sampleFreqMs = 100L;
        this.activityManager = (ActivityManager)context.getSystemService("activity");
        this.samples.put(Sample.SampleType.MEMORY, new ArrayList<Sample>());
        this.samples.put(Sample.SampleType.CPU, new ArrayList<Sample>());
    }
    
    public static void init(final Context context) {
        Sampler.samplerLock.lock();
        try {
            if (Sampler.sampler == null) {
                Sampler.sampler = provideSampler(context);
                Sampler.sampler.sampleFreqMs = 100L;
                Sampler.sampler.samplerServiceMetric = new Metric("samplerServiceTime");
                TraceMachine.addTraceListener(Sampler.sampler);
                Sampler.log.debug("Sampler initialized");
            }
        }
        catch (Exception e) {
            Sampler.log.error("Sampler init failed: " + e.getMessage());
            shutdown();
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    
    protected static Sampler provideSampler(final Context context) {
        return new Sampler(context);
    }
    
    public static void start() {
        Sampler.samplerLock.lock();
        try {
            if (Sampler.sampler != null) {
                Sampler.sampler.schedule();
                Sampler.log.debug("Sampler started");
            }
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    
    public static void stop() {
        Sampler.samplerLock.lock();
        try {
            if (Sampler.sampler != null) {
                Sampler.sampler.stop(false);
                Sampler.log.debug("Sampler stopped");
            }
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    
    public static void stopNow() {
        Sampler.samplerLock.lock();
        try {
            if (Sampler.sampler != null) {
                Sampler.sampler.stop(true);
                Sampler.log.debug("Sampler hard stopped");
            }
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    
    public static void shutdown() {
        Sampler.samplerLock.lock();
        try {
            if (Sampler.sampler != null) {
                TraceMachine.removeTraceListener(Sampler.sampler);
                stopNow();
                Sampler.sampler = null;
                Sampler.log.debug("Sampler shutdown");
            }
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    
    public void run() {
        try {
            if (this.isRunning.get()) {
                this.sample();
            }
        }
        catch (Exception e) {
            Sampler.log.error("Caught exception while running the sampler", e);
            AgentHealth.noticeException(e);
        }
    }
    
    protected void schedule() {
        Sampler.samplerLock.lock();
        try {
            if (!this.isRunning.get()) {
                this.clear();
                this.sampleFuture = this.scheduler.scheduleWithFixedDelay(this, 0L, this.sampleFreqMs, TimeUnit.MILLISECONDS);
                this.isRunning.set(true);
                Sampler.log.debug(String.format("Sampler scheduler started; sampling will occur every %d ms.", this.sampleFreqMs));
            }
        }
        catch (Exception e) {
            Sampler.log.error("Sampler scheduling failed: " + e.getMessage());
            AgentHealth.noticeException(e);
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    
    protected void stop(final boolean immediate) {
        Sampler.samplerLock.lock();
        try {
            if (this.isRunning.get()) {
                this.isRunning.set(false);
                if (this.sampleFuture != null) {
                    this.sampleFuture.cancel(immediate);
                }
                this.resetCpuSampler();
                Sampler.log.debug("Sampler canceled");
            }
        }
        catch (Exception e) {
            Sampler.log.error("Sampler stop failed: " + e.getMessage());
            AgentHealth.noticeException(e);
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    
    protected static boolean isRunning() {
        return Sampler.sampler != null && Sampler.sampler.sampleFuture != null && !Sampler.sampler.sampleFuture.isDone();
    }
    
    protected void monitorSamplerServiceTime(final double serviceTime) {
        this.samplerServiceMetric.sample(serviceTime);
        final Double serviceTimeAvg = this.samplerServiceMetric.getTotal() / this.samplerServiceMetric.getCount();
        if (serviceTimeAvg > this.sampleFreqMs) {
            Sampler.log.debug("Sampler: sample service time has been exceeded. Increase by 10%");
            this.sampleFreqMs = Math.min((long)(this.sampleFreqMs * 1.1f), 250L);
            if (this.sampleFuture != null) {
                this.sampleFuture.cancel(true);
            }
            this.sampleFuture = this.scheduler.scheduleWithFixedDelay(this, 0L, this.sampleFreqMs, TimeUnit.MILLISECONDS);
            Sampler.log.debug(String.format("Sampler scheduler restarted; sampling will now occur every %d ms.", this.sampleFreqMs));
            this.samplerServiceMetric.clear();
        }
    }
    
    protected void sample() {
        final TicToc timer = new TicToc();
        Sampler.samplerLock.lock();
        try {
            timer.tic();
            final Sample memorySample = sampleMemory();
            if (memorySample != null) {
                this.getSampleCollection(Sample.SampleType.MEMORY).add(memorySample);
            }
            final Sample cpuSample = this.sampleCpu();
            if (cpuSample != null) {
                this.getSampleCollection(Sample.SampleType.CPU).add(cpuSample);
            }
        }
        catch (Exception e) {
            Sampler.log.error("Sampling failed: " + e.getMessage());
            AgentHealth.noticeException(e);
        }
        finally {
            Sampler.samplerLock.unlock();
        }
        this.monitorSamplerServiceTime(timer.toc());
    }
    
    protected void clear() {
        for (final Collection<Sample> sampleCollection : this.samples.values()) {
            sampleCollection.clear();
        }
    }
    
    public static Sample sampleMemory() {
        if (Sampler.sampler == null) {
            return null;
        }
        return sampleMemory(Sampler.sampler.activityManager);
    }
    
    public static Sample sampleMemory(final ActivityManager activityManager) {
        try {
            final Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(Sampler.PID);
            if (memInfo.length > 0) {
                final int totalPss = memInfo[0].getTotalPss();
                if (totalPss >= 0) {
                    final Sample sample = new Sample(Sample.SampleType.MEMORY);
                    sample.setSampleValue(totalPss / 1024.0);
                    return sample;
                }
            }
        }
        catch (Exception e) {
            Sampler.log.error("Sample memory failed: " + e.getMessage());
            AgentHealth.noticeException(e);
        }
        return null;
    }
    
    protected static Sample sampleCpuInstance() {
        if (Sampler.sampler == null) {
            return null;
        }
        return Sampler.sampler.sampleCpu();
    }
    
    public Sample sampleCpu() {
        if (Sampler.cpuSamplingDisabled) {
            return null;
        }
        try {
            if (this.procStatFile == null || this.appStatFile == null) {
                this.procStatFile = new RandomAccessFile("/proc/stat", "r");
                this.appStatFile = new RandomAccessFile("/proc/" + Sampler.PID[0] + "/stat", "r");
            }
            else {
                this.procStatFile.seek(0L);
                this.appStatFile.seek(0L);
            }
            final String procStatString = this.procStatFile.readLine();
            final String appStatString = this.appStatFile.readLine();
            final String[] procStats = procStatString.split(" ");
            final String[] appStats = appStatString.split(" ");
            final long cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3]) + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5]) + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7]) + Long.parseLong(procStats[8]);
            final long appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);
            if (this.lastCpuTime == null && this.lastAppCpuTime == null) {
                this.lastCpuTime = cpuTime;
                this.lastAppCpuTime = appTime;
                return null;
            }
            final Sample sample = new Sample(Sample.SampleType.CPU);
            sample.setSampleValue((appTime - this.lastAppCpuTime) / (cpuTime - this.lastCpuTime) * 100.0);
            this.lastCpuTime = cpuTime;
            this.lastAppCpuTime = appTime;
            return sample;
        }
        catch (Exception e) {
            Sampler.cpuSamplingDisabled = true;
            Sampler.log.debug("Exception hit while CPU sampling: " + e.getMessage());
            AgentHealth.noticeException(e);
            return null;
        }
    }
    
    private void resetCpuSampler() {
        this.lastCpuTime = null;
        this.lastAppCpuTime = null;
        if (this.appStatFile != null && this.procStatFile != null) {
            try {
                this.appStatFile.close();
                this.procStatFile.close();
                this.appStatFile = null;
                this.procStatFile = null;
            }
            catch (IOException e) {
                Sampler.log.debug("Exception hit while resetting CPU sampler: " + e.getMessage());
                AgentHealth.noticeException(e);
            }
        }
    }
    
    public static Map<Sample.SampleType, Collection<Sample>> copySamples() {
        Sampler.samplerLock.lock();
        EnumMap<Sample.SampleType, Collection<Sample>> copy;
        try {
            if (Sampler.sampler == null) {
                Sampler.samplerLock.unlock();
                return new HashMap<Sample.SampleType, Collection<Sample>>();
            }
            copy = new EnumMap<Sample.SampleType, Collection<Sample>>(Sampler.sampler.samples);
            for (final Sample.SampleType key : Sampler.sampler.samples.keySet()) {
                copy.put(key, new ArrayList<Sample>(Sampler.sampler.samples.get(key)));
            }
        }
        finally {
            Sampler.samplerLock.unlock();
        }
        return Collections.unmodifiableMap((Map<? extends Sample.SampleType, ? extends Collection<Sample>>)copy);
    }
    
    private Collection<Sample> getSampleCollection(final Sample.SampleType type) {
        return this.samples.get(type);
    }
    
    public void onEnterMethod() {
        if (this.isRunning.get()) {
            return;
        }
        start();
    }
    
    public void onExitMethod() {
    }
    
    public void onTraceStart(final ActivityTrace activityTrace) {
        start();
    }
    
    public void onTraceComplete(final ActivityTrace activityTrace) {
        this.scheduler.execute(new Runnable() {
            public void run() {
                try {
                    Sampler.this.stop(true);
                    activityTrace.setVitals(Sampler.copySamples());
                    Sampler.this.clear();
                }
                catch (RuntimeException e) {
                    Sampler.log.error(e.toString());
                }
            }
        });
    }
    
    public void onTraceRename(final ActivityTrace activityTrace) {
    }
    
    static {
        PID = new int[] { Process.myPid() };
        log = AgentLogManager.getAgentLog();
        samplerLock = new ReentrantLock();
        Sampler.cpuSamplingDisabled = false;
    }
}
