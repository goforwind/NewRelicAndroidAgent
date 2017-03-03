// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.httpclient;

import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;
import com.newrelic.agent.android.instrumentation.io.StreamCompleteListenerSource;
import com.newrelic.agent.android.instrumentation.io.StreamCompleteEvent;
import com.newrelic.agent.android.instrumentation.io.CountingOutputStream;
import java.io.OutputStream;
import org.apache.http.Header;
import com.newrelic.agent.android.instrumentation.io.CountingInputStream;
import java.io.InputStream;
import java.io.IOException;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.instrumentation.io.StreamCompleteListener;
import org.apache.http.HttpEntity;

public final class HttpRequestEntityImpl implements HttpEntity, StreamCompleteListener
{
    private final HttpEntity impl;
    private final TransactionState transactionState;
    
    public HttpRequestEntityImpl(final HttpEntity impl, final TransactionState transactionState) {
        this.impl = impl;
        this.transactionState = transactionState;
    }
    
    public void consumeContent() throws IOException {
        try {
            this.impl.consumeContent();
        }
        catch (IOException e) {
            this.handleException(e);
            throw e;
        }
    }
    
    public InputStream getContent() throws IOException, IllegalStateException {
        try {
            if (!this.transactionState.isSent()) {
                final CountingInputStream stream = new CountingInputStream(this.impl.getContent());
                stream.addStreamCompleteListener(this);
                return stream;
            }
            return this.impl.getContent();
        }
        catch (IOException e) {
            this.handleException(e);
            throw e;
        }
        catch (IllegalStateException e2) {
            this.handleException(e2);
            throw e2;
        }
    }
    
    public Header getContentEncoding() {
        return this.impl.getContentEncoding();
    }
    
    public long getContentLength() {
        return this.impl.getContentLength();
    }
    
    public Header getContentType() {
        return this.impl.getContentType();
    }
    
    public boolean isChunked() {
        return this.impl.isChunked();
    }
    
    public boolean isRepeatable() {
        return this.impl.isRepeatable();
    }
    
    public boolean isStreaming() {
        return this.impl.isStreaming();
    }
    
    public void writeTo(final OutputStream outstream) throws IOException {
        try {
            if (!this.transactionState.isSent()) {
                final CountingOutputStream stream = new CountingOutputStream(outstream);
                this.impl.writeTo((OutputStream)stream);
                this.transactionState.setBytesSent(stream.getCount());
            }
            else {
                this.impl.writeTo(outstream);
            }
        }
        catch (IOException e) {
            this.handleException(e);
            throw e;
        }
    }
    
    public void streamComplete(final StreamCompleteEvent e) {
        final StreamCompleteListenerSource source = (StreamCompleteListenerSource)e.getSource();
        source.removeStreamCompleteListener(this);
        this.transactionState.setBytesSent(e.getBytes());
    }
    
    public void streamError(final StreamCompleteEvent e) {
        final StreamCompleteListenerSource source = (StreamCompleteListenerSource)e.getSource();
        source.removeStreamCompleteListener(this);
        this.handleException(e.getException(), e.getBytes());
    }
    
    private void handleException(final Exception e) {
        this.handleException(e, null);
    }
    
    private void handleException(final Exception e, final Long streamBytes) {
        TransactionStateUtil.setErrorCodeFromException(this.transactionState, e);
        if (!this.transactionState.isComplete()) {
            if (streamBytes != null) {
                this.transactionState.setBytesSent(streamBytes);
            }
            final TransactionData transactionData = this.transactionState.end();
            if (transactionData != null) {
                TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
            }
        }
    }
}
