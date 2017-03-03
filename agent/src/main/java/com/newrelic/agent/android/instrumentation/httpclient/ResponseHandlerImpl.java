// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.httpclient;

import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;
import org.apache.http.HttpResponse;
import com.newrelic.agent.android.instrumentation.TransactionState;
import org.apache.http.client.ResponseHandler;

public final class ResponseHandlerImpl<T> implements ResponseHandler<T>
{
    private final ResponseHandler<T> impl;
    private final TransactionState transactionState;
    
    private ResponseHandlerImpl(final ResponseHandler<T> impl, final TransactionState transactionState) {
        this.impl = impl;
        this.transactionState = transactionState;
    }
    
    public T handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        TransactionStateUtil.inspectAndInstrument(this.transactionState, response);
        return (T)this.impl.handleResponse(response);
    }
    
    public static <T> ResponseHandler<? extends T> wrap(final ResponseHandler<? extends T> impl, final TransactionState transactionState) {
        return (ResponseHandler<? extends T>)new ResponseHandlerImpl((org.apache.http.client.ResponseHandler<Object>)impl, transactionState);
    }
}
