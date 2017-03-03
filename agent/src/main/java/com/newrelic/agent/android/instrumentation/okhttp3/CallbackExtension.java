// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp3;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;
import okhttp3.Response;
import java.io.IOException;
import okhttp3.Call;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.logging.AgentLog;
import okhttp3.Callback;

public class CallbackExtension implements Callback
{
    private static final AgentLog log;
    private TransactionState transactionState;
    private Callback impl;
    
    public CallbackExtension(final Callback impl, final TransactionState transactionState) {
        this.impl = impl;
        this.transactionState = transactionState;
    }
    
    public void onFailure(final Call call, final IOException e) {
        this.error(e);
        this.impl.onFailure(call, e);
    }
    
    public void onResponse(final Call call, Response response) throws IOException {
        response = this.checkResponse(response);
        this.impl.onResponse(call, response);
    }
    
    private Response checkResponse(Response response) {
        if (!this.getTransactionState().isComplete()) {
            CallbackExtension.log.debug("CallbackExtension.checkResponse() - transaction is not complete.  Inspecting and instrumenting response.");
            response = OkHttp3TransactionStateUtil.inspectAndInstrumentResponse(this.getTransactionState(), response);
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
