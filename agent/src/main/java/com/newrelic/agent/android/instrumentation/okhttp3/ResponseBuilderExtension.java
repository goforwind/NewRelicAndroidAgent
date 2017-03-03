// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp3;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.io.IOException;
import okio.BufferedSource;
import okio.Sink;
import okio.Buffer;
import okhttp3.ResponseBody;
import okhttp3.Headers;
import okhttp3.Handshake;
import okhttp3.Protocol;
import okhttp3.Request;
import com.newrelic.agent.android.logging.AgentLog;
import okhttp3.Response;

public class ResponseBuilderExtension extends Response.Builder
{
    private static final AgentLog log;
    private Response.Builder impl;
    
    public ResponseBuilderExtension(final Response.Builder impl) {
        this.impl = impl;
    }
    
    public Response.Builder request(final Request request) {
        return this.impl.request(request);
    }
    
    public Response.Builder protocol(final Protocol protocol) {
        return this.impl.protocol(protocol);
    }
    
    public Response.Builder code(final int code) {
        return this.impl.code(code);
    }
    
    public Response.Builder message(final String message) {
        return this.impl.message(message);
    }
    
    public Response.Builder handshake(final Handshake handshake) {
        return this.impl.handshake(handshake);
    }
    
    public Response.Builder header(final String name, final String value) {
        return this.impl.header(name, value);
    }
    
    public Response.Builder addHeader(final String name, final String value) {
        return this.impl.addHeader(name, value);
    }
    
    public Response.Builder removeHeader(final String name) {
        return this.impl.removeHeader(name);
    }
    
    public Response.Builder headers(final Headers headers) {
        return this.impl.headers(headers);
    }
    
    public Response.Builder body(final ResponseBody body) {
        try {
            if (body != null) {
                final String bodyClassName = body.getClass().getName();
                if (!bodyClassName.toLowerCase().startsWith("okhttp3.cache")) {
                    final BufferedSource source = body.source();
                    if (source != null) {
                        final Buffer buffer = new Buffer();
                        source.readAll((Sink)buffer);
                        return this.impl.body((ResponseBody)new PrebufferedResponseBody(body, (BufferedSource)buffer));
                    }
                }
            }
        }
        catch (IOException e) {
            ResponseBuilderExtension.log.error("IOException reading from source: ", e);
        }
        catch (IllegalStateException ex) {}
        return this.impl.body(body);
    }
    
    public Response.Builder networkResponse(final Response networkResponse) {
        return this.impl.networkResponse(networkResponse);
    }
    
    public Response.Builder cacheResponse(final Response cacheResponse) {
        return this.impl.cacheResponse(cacheResponse);
    }
    
    public Response.Builder priorResponse(final Response priorResponse) {
        return this.impl.priorResponse(priorResponse);
    }
    
    public Response build() {
        return this.impl.build();
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
