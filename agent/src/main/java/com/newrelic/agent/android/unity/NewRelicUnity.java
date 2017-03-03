// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.unity;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.util.NetworkFailure;
import com.newrelic.agent.android.NewRelic;
import com.newrelic.agent.android.crashes.CrashReporter;
import com.newrelic.agent.android.logging.AgentLog;

public class NewRelicUnity
{
    private static final AgentLog log;
    private static final String ROOT_TRACE_NAME = "Unity";
    
    static void handleUnityCrash(final UnityException ex) {
        final Thread.UncaughtExceptionHandler currentExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentExceptionHandler != null && currentExceptionHandler instanceof CrashReporter.UncaughtExceptionHandler) {
            currentExceptionHandler.uncaughtException(Thread.currentThread(), ex);
        }
    }
    
    static boolean recordEvent(final UnityEvent event) {
        return NewRelic.recordEvent(event.getName(), event.getAttributes());
    }
    
    static void noticeNetworkFailure(final String url, final String httpMethod, final long startTime, final long endTime, final int failureCode, final String message) {
        final NetworkFailure networkFailure = NetworkFailure.fromErrorCode(failureCode);
        NewRelic.noticeNetworkFailure(url, httpMethod, startTime, endTime, networkFailure, message);
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
