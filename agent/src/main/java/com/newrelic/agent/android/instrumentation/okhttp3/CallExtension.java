// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp3;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;
import okhttp3.Callback;
import java.io.IOException;
import okhttp3.Response;
import okhttp3.Request;
import okhttp3.OkHttpClient;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.logging.AgentLog;
import okhttp3.Call;

public class CallExtension implements Call
{
    private static final AgentLog log;
    private TransactionState transactionState;
    private OkHttpClient client;
    private Request request;
    private Call impl;
    
    CallExtension(final OkHttpClient client, final Request request, final Call impl) {
        this.client = client;
        this.request = request;
        this.impl = impl;
    }
    
    public Request request() {
        return this.impl.request();
    }
    
    public Response execute() throws IOException {
        this.getTransactionState();
        Response response = null;
        try {
            response = this.impl.execute();
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        return this.checkResponse(response);
    }
    
    public void enqueue(final Callback responseCallback) {
        this.getTransactionState();
        this.impl.enqueue((Callback)new CallbackExtension(responseCallback, this.transactionState));
    }
    
    public void cancel() {
        this.impl.cancel();
    }
    
    public boolean isExecuted() {
        return false;
    }
    
    public boolean isCanceled() {
        return this.impl.isCanceled();
    }
    
    private Response checkResponse(Response response) {
        if (!this.getTransactionState().isComplete()) {
            response = OkHttp3TransactionStateUtil.inspectAndInstrumentResponse(this.getTransactionState(), response);
        }
        return response;
    }
    
    private TransactionState getTransactionState() {
        if (this.transactionState == null) {
            OkHttp3TransactionStateUtil.inspectAndInstrument(this.transactionState = new TransactionState(), this.request);
        }
        return this.transactionState;
    }
    
    private void error(final Exception e) {
        final TransactionState transactionState = this.getTransactionState();
        TransactionStateUtil.setErrorCodeFromException(transactionState, e);
        if (!transactionState.isComplete()) {
            final TransactionData transactionData = transactionState.end();
            if (transactionData != null) {
                TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
            }
        }
    }
    
    public Call getImpl() {
        return this.impl;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
