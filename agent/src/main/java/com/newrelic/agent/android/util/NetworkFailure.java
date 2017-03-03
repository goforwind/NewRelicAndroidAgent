// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.util;

import com.newrelic.agent.android.logging.AgentLogManager;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import javax.net.ssl.SSLException;
import java.net.MalformedURLException;
import java.net.ConnectException;
import org.apache.http.conn.ConnectTimeoutException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import com.newrelic.agent.android.logging.AgentLog;

public enum NetworkFailure
{
    Unknown(-1), 
    BadURL(-1000), 
    TimedOut(-1001), 
    CannotConnectToHost(-1004), 
    DNSLookupFailed(-1006), 
    BadServerResponse(-1011), 
    SecureConnectionFailed(-1200);
    
    private int errorCode;
    private static final AgentLog log;
    
    private NetworkFailure(final int errorCode) {
        this.errorCode = errorCode;
    }
    
    public static NetworkFailure exceptionToNetworkFailure(final Exception e) {
        NetworkFailure.log.error("NetworkFailure.exceptionToNetworkFailure: Attempting to convert network exception " + e.getClass().getName() + " to error code.");
        NetworkFailure error = NetworkFailure.Unknown;
        if (e instanceof UnknownHostException) {
            error = NetworkFailure.DNSLookupFailed;
        }
        else if (e instanceof SocketTimeoutException || e instanceof ConnectTimeoutException) {
            error = NetworkFailure.TimedOut;
        }
        else if (e instanceof ConnectException) {
            error = NetworkFailure.CannotConnectToHost;
        }
        else if (e instanceof MalformedURLException) {
            error = NetworkFailure.BadURL;
        }
        else if (e instanceof SSLException) {
            error = NetworkFailure.SecureConnectionFailed;
        }
        else if (e instanceof HttpResponseException || e instanceof ClientProtocolException) {
            error = NetworkFailure.BadServerResponse;
        }
        return error;
    }
    
    public static int exceptionToErrorCode(final Exception e) {
        return exceptionToNetworkFailure(e).getErrorCode();
    }
    
    public static NetworkFailure fromErrorCode(final int errorCode) {
        NetworkFailure.log.debug("fromErrorCode invoked with errorCode: " + errorCode);
        for (final NetworkFailure failure : values()) {
            if (failure.getErrorCode() == errorCode) {
                NetworkFailure.log.debug("fromErrorCode found matching failure: " + failure);
                return failure;
            }
        }
        return NetworkFailure.Unknown;
    }
    
    public int getErrorCode() {
        return this.errorCode;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
