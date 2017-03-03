// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp2;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;
import com.squareup.okhttp.Response;
import java.io.IOException;
import com.squareup.okhttp.Request;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.logging.AgentLog;
import com.squareup.okhttp.Callback;

public class CallbackExtension implements Callback
{
    private static final AgentLog log;
    private TransactionState transactionState;
    private Callback impl;
    
    public CallbackExtension(final Callback impl, final TransactionState transactionState) {
        this.impl = impl;
        this.transactionState = transactionState;
    }
    
    public void onFailure(final Request request, final IOException e) {
        this.error(e);
        this.impl.onFailure(request, e);
    }
    
    public void onResponse(Response response) throws IOException {
        response = this.checkResponse(response);
        this.impl.onResponse(response);
    }
    
    private Response checkResponse(Response response) {
        if (!this.getTransactionState().isComplete()) {
            CallbackExtension.log.verbose("CallbackExtension.checkResponse() - transaction is not complete.  Inspecting and instrumenting response.");
            response = OkHttp2TransactionStateUtil.inspectAndInstrumentResponse(this.getTransactionState(), response);
        }
        return response;
    }
    
    private TransactionState getTransactionState() {
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
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
