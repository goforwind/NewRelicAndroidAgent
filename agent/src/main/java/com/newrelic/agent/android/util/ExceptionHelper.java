// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.util;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.harvest.AgentHealth;
import com.newrelic.agent.android.harvest.AgentHealthException;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;
import javax.net.ssl.SSLException;
import java.net.MalformedURLException;
import java.net.ConnectException;
import org.apache.http.conn.ConnectTimeoutException;
import java.net.SocketTimeoutException;
import java.net.PortUnreachableException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import org.apache.http.client.ClientProtocolException;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.harvest.type.HarvestErrorCodes;

public class ExceptionHelper implements HarvestErrorCodes
{
    private static final AgentLog log;
    
    public static int exceptionToErrorCode(final Exception e) {
        int errorCode = -1;
        ExceptionHelper.log.debug("ExceptionHelper: exception " + e.getClass().getName() + " to error code.");
        if (e instanceof ClientProtocolException) {
            errorCode = -1011;
        }
        else if (e instanceof UnknownHostException) {
            errorCode = -1006;
        }
        else if (e instanceof NoRouteToHostException) {
            errorCode = -1003;
        }
        else if (e instanceof PortUnreachableException) {
            errorCode = -1003;
        }
        else if (e instanceof SocketTimeoutException) {
            errorCode = -1001;
        }
        else if (e instanceof ConnectTimeoutException) {
            errorCode = -1001;
        }
        else if (e instanceof ConnectException) {
            errorCode = -1004;
        }
        else if (e instanceof MalformedURLException) {
            errorCode = -1000;
        }
        else if (e instanceof SSLException) {
            errorCode = -1200;
        }
        else if (e instanceof FileNotFoundException) {
            errorCode = -1100;
        }
        else if (e instanceof EOFException) {
            errorCode = -1021;
        }
        else if (e instanceof IOException) {
            recordSupportabilityMetric(e, "IOException");
        }
        else if (e instanceof RuntimeException) {
            recordSupportabilityMetric(e, "RuntimeException");
        }
        return errorCode;
    }
    
    public static void recordSupportabilityMetric(final Exception e, final String baseExceptionKey) {
        final AgentHealthException agentHealthException = new AgentHealthException(e);
        final StackTraceElement topTraceElement = agentHealthException.getStackTrace()[0];
        ExceptionHelper.log.error(String.format("ExceptionHelper: %s:%s(%s:%s) %s[%s] %s", agentHealthException.getSourceClass(), agentHealthException.getSourceMethod(), topTraceElement.getFileName(), topTraceElement.getLineNumber(), baseExceptionKey, agentHealthException.getExceptionClass(), agentHealthException.getMessage()));
        AgentHealth.noticeException(agentHealthException, baseExceptionKey);
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
