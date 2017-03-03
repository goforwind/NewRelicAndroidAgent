// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.retrofit;

import com.newrelic.agent.android.logging.AgentLogManager;
import retrofit.ErrorHandler;
import retrofit.Profiler;
import retrofit.converter.Converter;
import retrofit.RequestInterceptor;
import java.util.concurrent.Executor;
import retrofit.client.Client;
import retrofit.Endpoint;
import com.newrelic.agent.android.logging.AgentLog;
import retrofit.RestAdapter;

public class RestAdapterBuilderExtension extends RestAdapter.Builder
{
    private static final AgentLog log;
    private RestAdapter.Builder impl;
    
    public RestAdapterBuilderExtension(final RestAdapter.Builder impl) {
        this.impl = impl;
    }
    
    public RestAdapter.Builder setEndpoint(final String endpoint) {
        return this.impl.setEndpoint(endpoint);
    }
    
    public RestAdapter.Builder setEndpoint(final Endpoint endpoint) {
        return this.impl.setEndpoint(endpoint);
    }
    
    public RestAdapter.Builder setClient(final Client client) {
        RestAdapterBuilderExtension.log.debug("RestAdapterBuilderExtension.setClient() wrapping client " + client + " with new ClientExtension.");
        return this.impl.setClient((Client)new ClientExtension(client));
    }
    
    public RestAdapter.Builder setClient(final Client.Provider clientProvider) {
        return this.impl.setClient(clientProvider);
    }
    
    public RestAdapter.Builder setExecutors(final Executor httpExecutor, final Executor callbackExecutor) {
        return this.impl.setExecutors(httpExecutor, callbackExecutor);
    }
    
    public RestAdapter.Builder setRequestInterceptor(final RequestInterceptor requestInterceptor) {
        return this.impl.setRequestInterceptor(requestInterceptor);
    }
    
    public RestAdapter.Builder setConverter(final Converter converter) {
        return this.impl.setConverter(converter);
    }
    
    public RestAdapter.Builder setProfiler(final Profiler profiler) {
        return this.impl.setProfiler(profiler);
    }
    
    public RestAdapter.Builder setErrorHandler(final ErrorHandler errorHandler) {
        return this.impl.setErrorHandler(errorHandler);
    }
    
    public RestAdapter.Builder setLog(final RestAdapter.Log log) {
        return this.impl.setLog(log);
    }
    
    public RestAdapter.Builder setLogLevel(final RestAdapter.LogLevel logLevel) {
        return this.impl.setLogLevel(logLevel);
    }
    
    public RestAdapter build() {
        return this.impl.build();
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
