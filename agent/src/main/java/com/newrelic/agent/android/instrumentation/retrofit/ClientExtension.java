// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.retrofit;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;
import java.util.List;
import java.util.Collection;
import retrofit.client.Header;
import java.util.ArrayList;
import com.newrelic.agent.android.Agent;
import java.io.IOException;
import retrofit.mime.TypedInput;
import retrofit.client.Response;
import retrofit.client.Request;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.logging.AgentLog;
import retrofit.client.Client;

public class ClientExtension implements Client
{
    private static final AgentLog log;
    private Client impl;
    private TransactionState transactionState;
    private Request request;
    
    public ClientExtension(final Client impl) {
        this.impl = impl;
    }
    
    public Response execute(Request request) throws IOException {
        this.request = request;
        this.getTransactionState();
        request = this.setCrossProcessHeader(request);
        Response response = null;
        try {
            response = this.impl.execute(request);
            response = new Response(response.getUrl(), response.getStatus(), response.getReason(), response.getHeaders(), (TypedInput)new ContentBufferingTypedInput(response.getBody()));
        }
        catch (IOException ex) {
            this.error(ex);
            throw ex;
        }
        this.checkResponse(response);
        return response;
    }
    
    private Request setCrossProcessHeader(Request request) {
        final String crossProcessId = Agent.getCrossProcessId();
        if (crossProcessId != null) {
            final List<Header> headers = new ArrayList<Header>(request.getHeaders());
            headers.add(new Header("X-NewRelic-ID", crossProcessId));
            request = new Request(request.getMethod(), request.getUrl(), (List)headers, request.getBody());
        }
        return request;
    }
    
    private void checkResponse(final Response response) {
        if (!this.getTransactionState().isComplete()) {
            RetrofitTransactionStateUtil.inspectAndInstrumentResponse(this.getTransactionState(), response);
        }
    }
    
    private TransactionState getTransactionState() {
        if (this.transactionState == null) {
            RetrofitTransactionStateUtil.inspectAndInstrument(this.transactionState = new TransactionState(), this.request);
        }
        return this.transactionState;
    }
    
    private void error(final Exception e) {
        ClientExtension.log.debug("handling exception: " + e.toString());
        final TransactionState transactionState = this.getTransactionState();
        TransactionStateUtil.setErrorCodeFromException(transactionState, e);
        if (!transactionState.isComplete()) {
            final TransactionData transactionData = transactionState.end();
            if (transactionData != null) {
                TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
            }
        }
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
