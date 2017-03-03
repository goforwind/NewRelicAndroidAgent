// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.retrofit;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.instrumentation.ReplaceCallSite;
import retrofit.client.Client;
import retrofit.RestAdapter;
import com.newrelic.agent.android.logging.AgentLog;

public final class RetrofitInstrumentation
{
    private static final AgentLog log;
    
    @ReplaceCallSite
    public static RestAdapter.Builder setClient(final RestAdapter.Builder builder, final Client client) {
        return new RestAdapterBuilderExtension(builder).setClient(client);
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
