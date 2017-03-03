// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp3;

import okhttp3.Callback;
import okhttp3.internal.connection.StreamAllocation;
import java.lang.reflect.Method;
import okhttp3.internal.Internal;
import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.instrumentation.HttpsURLConnectionExtension;
import javax.net.ssl.HttpsURLConnection;
import com.newrelic.agent.android.instrumentation.HttpURLConnectionExtension;
import java.net.HttpURLConnection;
import java.net.URL;
import okhttp3.OkUrlFactory;
import okhttp3.ResponseBody;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import com.newrelic.agent.android.instrumentation.ReplaceCallSite;
import okhttp3.Request;
import com.newrelic.agent.android.logging.AgentLog;

public class OkHttp3Instrumentation
{
    private static final AgentLog log;
    
    @ReplaceCallSite
    public static Request build(final Request.Builder builder) {
        return new RequestBuilderExtension(builder).build();
    }
    
    @ReplaceCallSite
    public static Call newCall(final OkHttpClient client, final Request request) {
        return (Call)new CallExtension(client, request, client.newCall(request));
    }
    
    @ReplaceCallSite
    public static Response.Builder body(final Response.Builder builder, final ResponseBody body) {
        return new ResponseBuilderExtension(builder).body(body);
    }
    
    @ReplaceCallSite
    public static Response.Builder newBuilder(final Response.Builder builder) {
        return new ResponseBuilderExtension(builder);
    }
    
    @ReplaceCallSite(isStatic = false, scope = "okhttp3.OkUrlFactory")
    public static HttpURLConnection open(final OkUrlFactory factory, final URL url) {
        final HttpURLConnection conn = factory.open(url);
        final String protocol = url.getProtocol();
        if (protocol.equals("http")) {
            return new HttpURLConnectionExtension(conn);
        }
        if (protocol.equals("https") && conn instanceof HttpsURLConnection) {
            return new HttpsURLConnectionExtension((HttpsURLConnection)conn);
        }
        return new HttpURLConnectionExtension(conn);
    }
    
    private static void logReflectionError(final String signature) {
        final String crlf = System.getProperty("line.separator");
        OkHttp3Instrumentation.log.error("Unable to resolve method \"" + signature + "\"." + crlf + "This is usually due to building the app with unsupported OkHttp versions." + crlf + "Check your build configuration for compatibility.");
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
    
    public static class OkHttp34
    {
        @ReplaceCallSite
        public static void setCallWebSocket(final Internal internal, Call call) {
            try {
                if (call instanceof CallExtension) {
                    call = ((CallExtension)call).getImpl();
                }
                final Method setCallWebSocket = Internal.class.getMethod("setCallWebSocket", Call.class);
                if (setCallWebSocket != null) {
                    setCallWebSocket.invoke(internal, call);
                }
                else {
                    logReflectionError("setCallWebSocket(Lokhttp3/Call;)V");
                }
            }
            catch (Exception e) {
                OkHttp3Instrumentation.log.error(e.getMessage());
            }
        }
        
        @ReplaceCallSite
        public static StreamAllocation callEngineGetStreamAllocation(final Internal internal, Call call) {
            StreamAllocation streamAllocation = null;
            try {
                if (call instanceof CallExtension) {
                    call = ((CallExtension)call).getImpl();
                }
                final Method callEngineGetStreamAllocation = Internal.class.getMethod("callEngineGetStreamAllocation", Call.class);
                if (callEngineGetStreamAllocation != null) {
                    streamAllocation = (StreamAllocation)callEngineGetStreamAllocation.invoke(internal, call);
                }
                else {
                    logReflectionError("callEngineGetStreamAllocation(Lokhttp3/Call;)Lokhttp3/internal/connection/StreamAllocation;");
                }
            }
            catch (Exception e) {
                OkHttp3Instrumentation.log.error(e.getMessage());
            }
            return streamAllocation;
        }
    }
    
    public static class OkHttp35
    {
        @ReplaceCallSite
        public static Call newWebSocketCall(final Internal internal, final OkHttpClient client, final Request request) {
            Call call = null;
            try {
                final Method newWebSocketCall = Internal.class.getMethod("newWebSocketCall", OkHttpClient.class, Request.class);
                if (newWebSocketCall != null) {
                    final Call impl = (Call)newWebSocketCall.invoke(internal, client, request);
                    call = (Call)new CallExtension(client, request, impl);
                }
                else {
                    logReflectionError("newWebSocketCall(Lokhttp3/OkHttpClient;Lokhttp3/Request;)Lokhttp3/Call;");
                }
            }
            catch (Exception e) {
                OkHttp3Instrumentation.log.error(e.getMessage());
            }
            return call;
        }
    }
    
    public static class OkHttp32
    {
        @ReplaceCallSite
        public static void callEnqueue(final Internal internal, Call call, final Callback responseCallback, final boolean forWebSocket) {
            try {
                if (call instanceof CallExtension) {
                    call = ((CallExtension)call).getImpl();
                }
                final Method callEnqueue = Internal.class.getMethod("callEnqueue", Call.class, Callback.class, Boolean.TYPE);
                if (callEnqueue != null) {
                    callEnqueue.invoke(internal, call, responseCallback, forWebSocket);
                }
                else {
                    logReflectionError("callEnqueue(Lokhttp3/Call;Lokhttp3/Callback;Z)V");
                }
            }
            catch (Exception e) {
                OkHttp3Instrumentation.log.error(e.getMessage());
            }
        }
        
        @ReplaceCallSite
        public static okhttp3.internal.http.StreamAllocation callEngineGetStreamAllocation(final Internal internal, Call call) {
            okhttp3.internal.http.StreamAllocation streamAllocation = null;
            try {
                if (call instanceof CallExtension) {
                    call = ((CallExtension)call).getImpl();
                }
                final Method callEngineGetStreamAllocation = Internal.class.getMethod("callEngineGetStreamAllocation", Call.class);
                if (callEngineGetStreamAllocation != null) {
                    streamAllocation = (okhttp3.internal.http.StreamAllocation)callEngineGetStreamAllocation.invoke(internal, call);
                }
                else {
                    logReflectionError("callEngineGetStreamAllocation(Lokhttp3/Call;)Lokhttp3/internal/http/StreamAllocation;");
                }
            }
            catch (Exception e) {
                OkHttp3Instrumentation.log.error(e.getMessage());
            }
            return streamAllocation;
        }
    }
}
