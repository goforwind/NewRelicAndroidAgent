// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import com.newrelic.agent.android.logging.AgentLogManager;
import android.annotation.TargetApi;
import java.util.concurrent.Executor;
import com.newrelic.agent.android.tracing.TracingInactiveException;
import com.newrelic.agent.android.util.ExceptionHelper;
import com.newrelic.agent.android.tracing.TraceMachine;
import com.newrelic.agent.android.api.v2.TraceFieldInterface;
import android.os.AsyncTask;
import com.newrelic.agent.android.logging.AgentLog;

public class AsyncTaskInstrumentation
{
    private static final AgentLog log;
    
    @ReplaceCallSite
    public static final <Params, Progress, Result> AsyncTask execute(final AsyncTask<Params, Progress, Result> task, final Params... params) {
        try {
            final TraceFieldInterface tfi = (TraceFieldInterface)task;
            tfi._nr_setTrace(TraceMachine.getCurrentTrace());
        }
        catch (ClassCastException e) {
            ExceptionHelper.recordSupportabilityMetric(e, "TraceFieldInterface");
            AsyncTaskInstrumentation.log.error("Not a TraceFieldInterface: " + e.getMessage());
        }
        catch (TracingInactiveException ex) {}
        catch (NoSuchFieldError noSuchFieldError) {}
        return task.execute((Object[])params);
    }
    
    @TargetApi(11)
    @ReplaceCallSite
    public static final <Params, Progress, Result> AsyncTask executeOnExecutor(final AsyncTask<Params, Progress, Result> task, final Executor exec, final Params... params) {
        try {
            final TraceFieldInterface tfi = (TraceFieldInterface)task;
            tfi._nr_setTrace(TraceMachine.getCurrentTrace());
        }
        catch (ClassCastException e) {
            ExceptionHelper.recordSupportabilityMetric(e, "TraceFieldInterface");
            AsyncTaskInstrumentation.log.error("Not a TraceFieldInterface: " + e.getMessage());
        }
        catch (TracingInactiveException ex) {}
        catch (NoSuchFieldError noSuchFieldError) {}
        return task.executeOnExecutor(exec, (Object[])params);
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
