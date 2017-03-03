// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp3;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.Agent;
import okhttp3.RequestBody;
import okhttp3.CacheControl;
import okhttp3.Headers;
import java.net.URL;
import com.newrelic.agent.android.logging.AgentLog;
import okhttp3.Request;

public class RequestBuilderExtension extends Request.Builder
{
    private static final AgentLog log;
    private Request.Builder impl;
    
    public RequestBuilderExtension(final Request.Builder impl) {
        this.impl = impl;
        this.setCrossProcessHeader();
    }
    
    public Request.Builder url(final String url) {
        return this.impl.url(url);
    }
    
    public Request.Builder url(final URL url) {
        return this.impl.url(url);
    }
    
    public Request.Builder header(final String name, final String value) {
        return this.impl.header(name, value);
    }
    
    public Request.Builder addHeader(final String name, final String value) {
        return this.impl.addHeader(name, value);
    }
    
    public Request.Builder removeHeader(final String name) {
        return this.impl.removeHeader(name);
    }
    
    public Request.Builder headers(final Headers headers) {
        return this.impl.headers(headers);
    }
    
    public Request.Builder cacheControl(final CacheControl cacheControl) {
        return this.impl.cacheControl(cacheControl);
    }
    
    public Request.Builder get() {
        return this.impl.get();
    }
    
    public Request.Builder head() {
        return this.impl.head();
    }
    
    public Request.Builder post(final RequestBody body) {
        return this.impl.post(body);
    }
    
    public Request.Builder delete() {
        return this.impl.delete();
    }
    
    public Request.Builder put(final RequestBody body) {
        return this.impl.put(body);
    }
    
    public Request.Builder patch(final RequestBody body) {
        return this.impl.patch(body);
    }
    
    public Request.Builder method(final String method, final RequestBody body) {
        return this.impl.method(method, body);
    }
    
    public Request.Builder tag(final Object tag) {
        return this.impl.tag(tag);
    }
    
    public Request build() {
        return this.impl.build();
    }
    
    private void setCrossProcessHeader() {
        final String crossProcessId = Agent.getCrossProcessId();
        if (crossProcessId != null) {
            this.impl.removeHeader("X-NewRelic-ID");
            this.impl.addHeader("X-NewRelic-ID", crossProcessId);
        }
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
