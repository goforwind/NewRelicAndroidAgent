// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.tracing;

import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.stats.StatsEngine;
import com.newrelic.agent.android.harvest.ActivityHistory;
import com.newrelic.agent.android.Measurements;
import java.util.Map;
import com.newrelic.agent.android.util.ExceptionHelper;
import com.newrelic.agent.android.api.v2.TraceFieldInterface;
import com.newrelic.agent.android.TaskQueue;
import java.util.ArrayList;
import java.util.Iterator;
import com.newrelic.agent.android.harvest.AgentHealth;
import com.newrelic.agent.android.harvest.HarvestLifecycleAware;
import com.newrelic.agent.android.harvest.Harvest;
import com.newrelic.agent.android.FeatureFlag;
import com.newrelic.agent.android.api.v2.TraceMachineInterface;
import com.newrelic.agent.android.harvest.ActivitySighting;
import java.util.List;
import java.util.Collection;
import com.newrelic.agent.android.logging.AgentLog;
import java.util.concurrent.atomic.AtomicBoolean;
import com.newrelic.agent.android.harvest.HarvestAdapter;

public class TraceMachine extends HarvestAdapter
{
    public static final String NR_TRACE_FIELD = "_nr_trace";
    public static final String NR_TRACE_TYPE = "Lcom/newrelic/agent/android/tracing/Trace;";
    public static final String ACTIVITY_METRIC_PREFIX = "Mobile/Activity/Name/";
    public static final String ACTIVITY_BACKGROUND_METRIC_PREFIX = "Mobile/Activity/Background/Name/";
    public static final String ACTIVTY_DISPLAY_NAME_PREFIX = "Display ";
    public static final AtomicBoolean enabled;
    private static final AgentLog log;
    public static int HEALTHY_TRACE_TIMEOUT;
    public static int UNHEALTHY_TRACE_TIMEOUT;
    private static final Collection<TraceLifecycleAware> traceListeners;
    private static final ThreadLocal<Trace> threadLocalTrace;
    private static final ThreadLocal<TraceStack> threadLocalTraceStack;
    private static final List<ActivitySighting> activityHistory;
    private static TraceMachine traceMachine;
    private static TraceMachineInterface traceMachineInterface;
    private ActivityTrace activityTrace;
    
    protected static boolean isEnabled() {
        return TraceMachine.enabled.get() && FeatureFlag.featureEnabled(FeatureFlag.InteractionTracing);
    }
    
    protected TraceMachine(final Trace rootTrace) {
        this.activityTrace = new ActivityTrace(rootTrace);
        Harvest.addHarvestListener(this);
    }
    
    public static TraceMachine getTraceMachine() {
        return TraceMachine.traceMachine;
    }
    
    public static void addTraceListener(final TraceLifecycleAware listener) {
        TraceMachine.traceListeners.add(listener);
    }
    
    public static void removeTraceListener(final TraceLifecycleAware listener) {
        TraceMachine.traceListeners.remove(listener);
    }
    
    public static void setTraceMachineInterface(final TraceMachineInterface traceMachineInterface) {
        TraceMachine.traceMachineInterface = traceMachineInterface;
    }
    
    public static void startTracing(final String name) {
        startTracing(name, false);
    }
    
    public static void startTracing(final String name, final boolean customName) {
        startTracing(name, customName, false);
    }
    
    public static void startTracing(final String name, final boolean customName, final boolean customInteraction) {
        try {
            if (!isEnabled()) {
                return;
            }
            if (!customInteraction && !FeatureFlag.featureEnabled(FeatureFlag.DefaultInteractions)) {
                return;
            }
            if (!Harvest.shouldCollectActivityTraces()) {
                return;
            }
            if (isTracingActive()) {
                TraceMachine.traceMachine.completeActivityTrace();
            }
            TraceMachine.threadLocalTrace.remove();
            TraceMachine.threadLocalTraceStack.set(new TraceStack());
            final Trace rootTrace = new Trace();
            if (customName) {
                rootTrace.displayName = name;
            }
            else {
                rootTrace.displayName = formatActivityDisplayName(name);
            }
            rootTrace.metricName = formatActivityMetricName(rootTrace.displayName);
            rootTrace.metricBackgroundName = formatActivityBackgroundMetricName(rootTrace.displayName);
            rootTrace.entryTimestamp = System.currentTimeMillis();
            TraceMachine.log.debug("Started trace of " + name + ":" + rootTrace.myUUID.toString());
            TraceMachine.traceMachine = new TraceMachine(rootTrace);
            rootTrace.traceMachine = TraceMachine.traceMachine;
            pushTraceContext(rootTrace);
            TraceMachine.traceMachine.activityTrace.previousActivity = getLastActivitySighting();
            TraceMachine.activityHistory.add(new ActivitySighting(rootTrace.entryTimestamp, rootTrace.displayName));
            for (final TraceLifecycleAware listener : TraceMachine.traceListeners) {
                listener.onTraceStart(TraceMachine.traceMachine.activityTrace);
            }
        }
        catch (Exception e) {
            TraceMachine.log.error("Caught error while initializing TraceMachine, shutting it down", e);
            AgentHealth.noticeException(e);
            TraceMachine.traceMachine = null;
            TraceMachine.threadLocalTrace.remove();
            TraceMachine.threadLocalTraceStack.remove();
        }
    }
    
    public static void haltTracing() {
        if (isTracingInactive()) {
            return;
        }
        final TraceMachine finishedMachine = TraceMachine.traceMachine;
        TraceMachine.traceMachine = null;
        finishedMachine.activityTrace.discard();
        endLastActivitySighting();
        Harvest.removeHarvestListener(finishedMachine);
        TraceMachine.threadLocalTrace.remove();
        TraceMachine.threadLocalTraceStack.remove();
    }
    
    public static void endTrace() {
        TraceMachine.traceMachine.completeActivityTrace();
    }
    
    public static void endTrace(final String id) {
        try {
            if (getActivityTrace().rootTrace.myUUID.toString().equals(id)) {
                TraceMachine.traceMachine.completeActivityTrace();
            }
        }
        catch (TracingInactiveException ex) {}
    }
    
    public static String formatActivityMetricName(final String name) {
        return "Mobile/Activity/Name/" + name;
    }
    
    public static String formatActivityBackgroundMetricName(final String name) {
        return "Mobile/Activity/Background/Name/" + name;
    }
    
    public static String formatActivityDisplayName(final String name) {
        return "Display " + name;
    }
    
    private static Trace registerNewTrace(final String name) throws TracingInactiveException {
        if (isTracingInactive()) {
            TraceMachine.log.debug("Tried to register a new trace but tracing is inactive!");
            throw new TracingInactiveException();
        }
        final Trace parentTrace = getCurrentTrace();
        final Trace childTrace = new Trace(name, parentTrace.myUUID, TraceMachine.traceMachine);
        try {
            TraceMachine.traceMachine.activityTrace.addTrace(childTrace);
        }
        catch (Exception e) {
            throw new TracingInactiveException();
        }
        TraceMachine.log.verbose("Registering trace of " + name + " with parent " + parentTrace.displayName);
        parentTrace.addChild(childTrace);
        return childTrace;
    }
    
    protected void completeActivityTrace() {
        if (isTracingInactive()) {
            return;
        }
        final TraceMachine finishedMachine = TraceMachine.traceMachine;
        TraceMachine.traceMachine = null;
        finishedMachine.activityTrace.complete();
        endLastActivitySighting();
        for (final TraceLifecycleAware listener : TraceMachine.traceListeners) {
            listener.onTraceComplete(finishedMachine.activityTrace);
        }
        Harvest.removeHarvestListener(finishedMachine);
    }
    
    public static void enterNetworkSegment(final String name) {
        try {
            if (isTracingInactive()) {
                return;
            }
            final Trace currentTrace = getCurrentTrace();
            if (currentTrace.getType() == TraceType.NETWORK) {
                exitMethod();
            }
            enterMethod(null, name, null);
            final Trace networkTrace = getCurrentTrace();
            networkTrace.setType(TraceType.NETWORK);
        }
        catch (TracingInactiveException ex) {}
        catch (Exception e) {
            TraceMachine.log.error("Caught error while calling enterNetworkSegment()", e);
            AgentHealth.noticeException(e);
        }
    }
    
    public static void enterMethod(final String name) {
        enterMethod(null, name, null);
    }
    
    public static void enterMethod(final String name, final ArrayList<String> annotationParams) {
        enterMethod(null, name, annotationParams);
    }
    
    public static void enterMethod(final Trace trace, final String name, final ArrayList<String> annotationParams) {
        try {
            if (isTracingInactive()) {
                return;
            }
            final long currentTime = System.currentTimeMillis();
            final long lastUpdatedAt = TraceMachine.traceMachine.activityTrace.lastUpdatedAt;
            final long inception = TraceMachine.traceMachine.activityTrace.startedAt;
            if (lastUpdatedAt + TraceMachine.HEALTHY_TRACE_TIMEOUT < currentTime && !TraceMachine.traceMachine.activityTrace.hasMissingChildren()) {
                TraceMachine.log.debug("Completing activity trace after hitting healthy timeout (" + TraceMachine.HEALTHY_TRACE_TIMEOUT + "ms)");
                TraceMachine.traceMachine.completeActivityTrace();
                return;
            }
            if (inception + TraceMachine.UNHEALTHY_TRACE_TIMEOUT < currentTime) {
                TraceMachine.log.debug("Completing activity trace after hitting unhealthy timeout (" + TraceMachine.UNHEALTHY_TRACE_TIMEOUT + "ms)");
                TraceMachine.traceMachine.completeActivityTrace();
                return;
            }
            loadTraceContext(trace);
            final Trace childTrace = registerNewTrace(name);
            pushTraceContext(childTrace);
            childTrace.scope = getCurrentScope();
            childTrace.setAnnotationParams(annotationParams);
            for (final TraceLifecycleAware listener : TraceMachine.traceListeners) {
                listener.onEnterMethod();
            }
            childTrace.entryTimestamp = System.currentTimeMillis();
        }
        catch (TracingInactiveException ex) {}
        catch (Exception e) {
            TraceMachine.log.error("Caught error while calling enterMethod()", e);
            AgentHealth.noticeException(e);
        }
    }
    
    public static void exitMethod() {
        try {
            if (isTracingInactive()) {
                return;
            }
            final Trace trace = TraceMachine.threadLocalTrace.get();
            if (trace == null) {
                TraceMachine.log.debug("threadLocalTrace is null");
                return;
            }
            trace.exitTimestamp = System.currentTimeMillis();
            if (trace.threadId == 0L && TraceMachine.traceMachineInterface != null) {
                trace.threadId = TraceMachine.traceMachineInterface.getCurrentThreadId();
                trace.threadName = TraceMachine.traceMachineInterface.getCurrentThreadName();
            }
            for (final TraceLifecycleAware listener : TraceMachine.traceListeners) {
                listener.onExitMethod();
            }
            try {
                trace.complete();
            }
            catch (TracingInactiveException e2) {
                TraceMachine.threadLocalTrace.remove();
                TraceMachine.threadLocalTraceStack.remove();
                if (trace.getType() == TraceType.TRACE) {
                    TaskQueue.queue(trace);
                }
                return;
            }
            TraceMachine.threadLocalTraceStack.get().pop();
            if (TraceMachine.threadLocalTraceStack.get().empty()) {
                TraceMachine.threadLocalTrace.set(null);
            }
            else {
                final Trace parentTrace = TraceMachine.threadLocalTraceStack.get().peek();
                TraceMachine.threadLocalTrace.set(parentTrace);
                final Trace trace2 = parentTrace;
                trace2.childExclusiveTime += trace.getDurationAsMilliseconds();
            }
            if (trace.getType() == TraceType.TRACE) {
                TaskQueue.queue(trace);
            }
        }
        catch (Exception e) {
            TraceMachine.log.error("Caught error while calling exitMethod()", e);
            AgentHealth.noticeException(e);
        }
    }
    
    private static void pushTraceContext(final Trace trace) {
        if (isTracingInactive() || trace == null) {
            return;
        }
        final TraceStack traceStack = TraceMachine.threadLocalTraceStack.get();
        if (traceStack.empty()) {
            traceStack.push(trace);
        }
        else if (traceStack.peek() != trace) {
            traceStack.push(trace);
        }
        TraceMachine.threadLocalTrace.set(trace);
    }
    
    private static void loadTraceContext(Trace trace) {
        if (isTracingInactive()) {
            return;
        }
        if (TraceMachine.threadLocalTrace.get() == null) {
            TraceMachine.threadLocalTrace.set(trace);
            TraceMachine.threadLocalTraceStack.set(new TraceStack());
            if (trace == null) {
                return;
            }
            TraceMachine.threadLocalTraceStack.get().push(trace);
        }
        else if (trace == null) {
            if (TraceMachine.threadLocalTraceStack.get().isEmpty()) {
                TraceMachine.log.debug("No context to load!");
                TraceMachine.threadLocalTrace.set(null);
                return;
            }
            trace = TraceMachine.threadLocalTraceStack.get().peek();
            TraceMachine.threadLocalTrace.set(trace);
        }
        TraceMachine.log.verbose("Trace " + trace.myUUID.toString() + " is now active");
    }
    
    public static void unloadTraceContext(final Object object) {
        try {
            if (isTracingInactive()) {
                return;
            }
            if (TraceMachine.traceMachineInterface != null && TraceMachine.traceMachineInterface.isUIThread()) {
                return;
            }
            if (TraceMachine.threadLocalTrace.get() != null) {
                TraceMachine.log.verbose("Trace " + TraceMachine.threadLocalTrace.get().myUUID.toString() + " is now inactive");
            }
            TraceMachine.threadLocalTrace.remove();
            TraceMachine.threadLocalTraceStack.remove();
            try {
                final TraceFieldInterface tfi = (TraceFieldInterface)object;
                tfi._nr_setTrace(null);
            }
            catch (ClassCastException e) {
                ExceptionHelper.recordSupportabilityMetric(e, "TraceFieldInterface");
                TraceMachine.log.error("Not a TraceFieldInterface: " + e.getMessage());
            }
        }
        catch (Exception e2) {
            TraceMachine.log.error("Caught error while calling unloadTraceContext()", e2);
            AgentHealth.noticeException(e2);
        }
    }
    
    public static Trace getCurrentTrace() throws TracingInactiveException {
        if (isTracingInactive()) {
            throw new TracingInactiveException();
        }
        final Trace trace = TraceMachine.threadLocalTrace.get();
        if (trace != null) {
            return trace;
        }
        return getRootTrace();
    }
    
    public static Map<String, Object> getCurrentTraceParams() throws TracingInactiveException {
        return getCurrentTrace().getParams();
    }
    
    public static void setCurrentTraceParam(final String key, final Object value) {
        if (isTracingInactive()) {
            return;
        }
        try {
            getCurrentTrace().getParams().put(key, value);
        }
        catch (TracingInactiveException e) {}
    }
    
    public static void setCurrentDisplayName(final String name) {
        if (isTracingInactive()) {
            return;
        }
        try {
            getCurrentTrace().displayName = name;
            for (final TraceLifecycleAware listener : TraceMachine.traceListeners) {
                listener.onTraceRename(TraceMachine.traceMachine.activityTrace);
            }
        }
        catch (TracingInactiveException e) {}
    }
    
    public static void setRootDisplayName(final String name) {
        if (isTracingInactive()) {
            return;
        }
        try {
            final Trace rootTrace = getRootTrace();
            Measurements.renameActivity(rootTrace.displayName, name);
            renameActivityHistory(rootTrace.displayName, name);
            rootTrace.metricName = formatActivityMetricName(name);
            rootTrace.metricBackgroundName = formatActivityBackgroundMetricName(name);
            rootTrace.displayName = name;
            final Trace currentTrace = getCurrentTrace();
            currentTrace.scope = getCurrentScope();
        }
        catch (TracingInactiveException e) {}
    }
    
    private static void renameActivityHistory(final String oldName, final String newName) {
        for (final ActivitySighting activitySighting : TraceMachine.activityHistory) {
            if (activitySighting.getName().equals(oldName)) {
                activitySighting.setName(newName);
            }
        }
    }
    
    public static String getCurrentScope() {
        try {
            if (isTracingInactive()) {
                return null;
            }
            if (TraceMachine.traceMachineInterface == null || TraceMachine.traceMachineInterface.isUIThread()) {
                return TraceMachine.traceMachine.activityTrace.rootTrace.metricName;
            }
            return TraceMachine.traceMachine.activityTrace.rootTrace.metricBackgroundName;
        }
        catch (Exception e) {
            TraceMachine.log.error("Caught error while calling getCurrentScope()", e);
            AgentHealth.noticeException(e);
            return null;
        }
    }
    
    public static boolean isTracingActive() {
        return TraceMachine.traceMachine != null;
    }
    
    public static boolean isTracingInactive() {
        return !isTracingActive();
    }
    
    public void storeCompletedTrace(final Trace trace) {
        try {
            if (isTracingInactive()) {
                TraceMachine.log.debug("Attempted to store a completed trace with no trace machine!");
                return;
            }
            this.activityTrace.addCompletedTrace(trace);
        }
        catch (Exception e) {
            TraceMachine.log.error("Caught error while calling storeCompletedTrace()", e);
            AgentHealth.noticeException(e);
        }
    }
    
    public static Trace getRootTrace() throws TracingInactiveException {
        try {
            return TraceMachine.traceMachine.activityTrace.rootTrace;
        }
        catch (NullPointerException e) {
            throw new TracingInactiveException();
        }
    }
    
    public static ActivityTrace getActivityTrace() throws TracingInactiveException {
        try {
            return TraceMachine.traceMachine.activityTrace;
        }
        catch (NullPointerException e) {
            throw new TracingInactiveException();
        }
    }
    
    public static ActivityHistory getActivityHistory() {
        return new ActivityHistory(TraceMachine.activityHistory);
    }
    
    public static ActivitySighting getLastActivitySighting() {
        if (TraceMachine.activityHistory.isEmpty()) {
            return null;
        }
        return TraceMachine.activityHistory.get(TraceMachine.activityHistory.size() - 1);
    }
    
    public static void endLastActivitySighting() {
        final ActivitySighting activitySighting = getLastActivitySighting();
        if (activitySighting != null) {
            activitySighting.end(System.currentTimeMillis());
        }
    }
    
    public static void clearActivityHistory() {
        TraceMachine.activityHistory.clear();
    }
    
    @Override
    public void onHarvestBefore() {
        if (isTracingActive()) {
            final long currentTime = System.currentTimeMillis();
            final long lastUpdatedAt = TraceMachine.traceMachine.activityTrace.lastUpdatedAt;
            final long inception = TraceMachine.traceMachine.activityTrace.startedAt;
            if (lastUpdatedAt + TraceMachine.HEALTHY_TRACE_TIMEOUT < currentTime && !TraceMachine.traceMachine.activityTrace.hasMissingChildren()) {
                TraceMachine.log.debug("Completing activity trace after hitting healthy timeout (" + TraceMachine.HEALTHY_TRACE_TIMEOUT + "ms)");
                this.completeActivityTrace();
                StatsEngine.get().inc("Supportability/AgentHealth/HealthyActivityTraces");
                return;
            }
            if (inception + TraceMachine.UNHEALTHY_TRACE_TIMEOUT < currentTime) {
                TraceMachine.log.debug("Completing activity trace after hitting unhealthy timeout (" + TraceMachine.UNHEALTHY_TRACE_TIMEOUT + "ms)");
                this.completeActivityTrace();
                StatsEngine.get().inc("Supportability/AgentHealth/UnhealthyActivityTraces");
            }
        }
        else {
            TraceMachine.log.debug("TraceMachine is inactive");
        }
    }
    
    @Override
    public void onHarvestSendFailed() {
        try {
            TraceMachine.traceMachine.activityTrace.incrementReportAttemptCount();
        }
        catch (NullPointerException ex) {}
    }
    
    static {
        enabled = new AtomicBoolean(true);
        log = AgentLogManager.getAgentLog();
        TraceMachine.HEALTHY_TRACE_TIMEOUT = 500;
        TraceMachine.UNHEALTHY_TRACE_TIMEOUT = 60000;
        traceListeners = new CopyOnWriteArrayList<TraceLifecycleAware>();
        threadLocalTrace = new ThreadLocal<Trace>();
        threadLocalTraceStack = new ThreadLocal<TraceStack>();
        activityHistory = new CopyOnWriteArrayList<ActivitySighting>();
        TraceMachine.traceMachine = null;
    }
    
    private static class TraceStack extends Stack<Trace>
    {
    }
}
