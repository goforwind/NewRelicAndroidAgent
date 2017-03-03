// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp2;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.instrumentation.HttpsURLConnectionExtension;
import javax.net.ssl.HttpsURLConnection;
import com.newrelic.agent.android.instrumentation.HttpURLConnectionExtension;
import java.net.HttpURLConnection;
import java.net.URL;
import com.squareup.okhttp.OkUrlFactory;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.newrelic.agent.android.instrumentation.ReplaceCallSite;
import com.squareup.okhttp.Request;
import com.newrelic.agent.android.logging.AgentLog;

public class OkHttp2Instrumentation
{
    private static final AgentLog log;
    
    @ReplaceCallSite
    public static Request build(final Request.Builder builder) {
        return new RequestBuilderExtension(builder).build();
    }
    
    @ReplaceCallSite
    public static Call newCall(final OkHttpClient client, final Request request) {
        return new CallExtension(client, request, client.newCall(request));
    }
    
    @ReplaceCallSite
    public static Response.Builder body(final Response.Builder builder, final ResponseBody body) {
        return new ResponseBuilderExtension(builder).body(body);
    }
    
    @ReplaceCallSite
    public static Response.Builder newBuilder(final Response.Builder builder) {
        return new ResponseBuilderExtension(builder);
    }
    
    @ReplaceCallSite(isStatic = false, scope = "com.squareup.okhttp.OkUrlFactory")
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
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
