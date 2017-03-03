// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import com.newrelic.agent.android.logging.AgentLogManager;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import com.newrelic.agent.android.logging.AgentLog;

public class OkHttpInstrumentation
{
    private static final AgentLog log;
    
    @WrapReturn(className = "com/squareup/okhttp/OkHttpClient", methodName = "open", methodDesc = "(Ljava/net/URL;)Ljava/net/HttpURLConnection;")
    public static HttpURLConnection open(final HttpURLConnection connection) {
        if (connection instanceof HttpsURLConnection) {
            return new HttpsURLConnectionExtension((HttpsURLConnection)connection);
        }
        if (connection != null) {
            return new HttpURLConnectionExtension(connection);
        }
        return null;
    }
    
    @WrapReturn(className = "com/squareup/okhttp/OkHttpClient", methodName = "open", methodDesc = "(Ljava/net/URL;Ljava/net/Proxy)Ljava/net/HttpURLConnection;")
    public static HttpURLConnection openWithProxy(final HttpURLConnection connection) {
        if (connection instanceof HttpsURLConnection) {
            return new HttpsURLConnectionExtension((HttpsURLConnection)connection);
        }
        if (connection != null) {
            return new HttpURLConnectionExtension(connection);
        }
        return null;
    }
    
    @WrapReturn(className = "com/squareup/okhttp/OkUrlFactory", methodName = "open", methodDesc = "(Ljava/net/URL;)Ljava/net/HttpURLConnection;")
    public static HttpURLConnection urlFactoryOpen(final HttpURLConnection connection) {
        OkHttpInstrumentation.log.debug("OkHttpInstrumentation - wrapping return of call to OkUrlFactory.open...");
        if (connection instanceof HttpsURLConnection) {
            return new HttpsURLConnectionExtension((HttpsURLConnection)connection);
        }
        if (connection != null) {
            return new HttpURLConnectionExtension(connection);
        }
        return null;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
