// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.httpclient;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Map;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.Measurements;
import java.util.TreeMap;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;
import com.newrelic.agent.android.instrumentation.io.StreamCompleteListenerSource;
import com.newrelic.agent.android.instrumentation.io.StreamCompleteEvent;
import com.newrelic.agent.android.instrumentation.io.CountingOutputStream;
import java.io.OutputStream;
import org.apache.http.Header;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.message.AbstractHttpMessage;
import java.io.InputStream;
import java.io.IOException;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.instrumentation.io.CountingInputStream;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.instrumentation.io.StreamCompleteListener;
import org.apache.http.HttpEntity;

public final class HttpResponseEntityImpl implements HttpEntity, StreamCompleteListener
{
    private static final String TRANSFER_ENCODING_HEADER = "Transfer-Encoding";
    private static final String ENCODING_CHUNKED = "chunked";
    private final HttpEntity impl;
    private final TransactionState transactionState;
    private final long contentLengthFromHeader;
    private CountingInputStream contentStream;
    private static final AgentLog log;
    
    public HttpResponseEntityImpl(final HttpEntity impl, final TransactionState transactionState, final long contentLengthFromHeader) {
        this.impl = impl;
        this.transactionState = transactionState;
        this.contentLengthFromHeader = contentLengthFromHeader;
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
        if (this.contentStream != null) {
            return this.contentStream;
        }
        try {
            boolean shouldBuffer = true;
            if (this.impl instanceof AbstractHttpMessage) {
                final AbstractHttpMessage message = (AbstractHttpMessage)this.impl;
                final Header transferEncodingHeader = message.getLastHeader("Transfer-Encoding");
                if (transferEncodingHeader != null && "chunked".equalsIgnoreCase(transferEncodingHeader.getValue())) {
                    shouldBuffer = false;
                }
            }
            else if (this.impl instanceof HttpEntityWrapper) {
                final HttpEntityWrapper entityWrapper = (HttpEntityWrapper)this.impl;
                shouldBuffer = !entityWrapper.isChunked();
            }
            try {
                (this.contentStream = new CountingInputStream(this.impl.getContent(), shouldBuffer)).addStreamCompleteListener(this);
            }
            catch (IllegalArgumentException e) {
                HttpResponseEntityImpl.log.error("HttpResponseEntityImpl: " + e.toString());
            }
            return this.contentStream;
        }
        catch (IOException e2) {
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
        if (!this.transactionState.isComplete()) {
            CountingOutputStream outputStream = null;
            try {
                outputStream = new CountingOutputStream(outstream);
                this.impl.writeTo((OutputStream)outputStream);
            }
            catch (IOException e) {
                if (outputStream != null) {
                    this.handleException(e, outputStream.getCount());
                }
                e.printStackTrace();
                throw e;
            }
            if (!this.transactionState.isComplete()) {
                if (this.contentLengthFromHeader >= 0L) {
                    this.transactionState.setBytesReceived(this.contentLengthFromHeader);
                }
                else {
                    this.transactionState.setBytesReceived(outputStream.getCount());
                }
                this.addTransactionAndErrorData(this.transactionState);
            }
        }
        else {
            this.impl.writeTo(outstream);
        }
    }
    
    public void streamComplete(final StreamCompleteEvent e) {
        final StreamCompleteListenerSource source = (StreamCompleteListenerSource)e.getSource();
        source.removeStreamCompleteListener(this);
        if (!this.transactionState.isComplete()) {
            if (this.contentLengthFromHeader >= 0L) {
                this.transactionState.setBytesReceived(this.contentLengthFromHeader);
            }
            else {
                this.transactionState.setBytesReceived(e.getBytes());
            }
            this.addTransactionAndErrorData(this.transactionState);
        }
    }
    
    public void streamError(final StreamCompleteEvent e) {
        final StreamCompleteListenerSource source = (StreamCompleteListenerSource)e.getSource();
        source.removeStreamCompleteListener(this);
        TransactionStateUtil.setErrorCodeFromException(this.transactionState, e.getException());
        if (!this.transactionState.isComplete()) {
            this.transactionState.setBytesReceived(e.getBytes());
        }
    }
    
    private void addTransactionAndErrorData(final TransactionState transactionState) {
        final TransactionData transactionData = transactionState.end();
        if (transactionData == null) {
            return;
        }
        TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
        if (transactionState.getStatusCode() >= 400L) {
            final StringBuilder responseBody = new StringBuilder();
            try {
                final InputStream errorStream = this.getContent();
                if (errorStream instanceof CountingInputStream) {
                    responseBody.append(((CountingInputStream)errorStream).getBufferAsString());
                }
            }
            catch (Exception e) {
                HttpResponseEntityImpl.log.error(e.toString());
            }
            final Header contentType = this.impl.getContentType();
            final Map<String, String> params = new TreeMap<String, String>();
            if (contentType != null && contentType.getValue() != null && !"".equals(contentType.getValue())) {
                params.put("content_type", contentType.getValue());
            }
            params.put("content_length", transactionState.getBytesReceived() + "");
            Measurements.addHttpError(transactionData, responseBody.toString(), params);
        }
    }
    
    private void handleException(final Exception e) {
        this.handleException(e, null);
    }
    
    private void handleException(final Exception e, final Long streamBytes) {
        TransactionStateUtil.setErrorCodeFromException(this.transactionState, e);
        if (!this.transactionState.isComplete()) {
            if (streamBytes != null) {
                this.transactionState.setBytesReceived(streamBytes);
            }
            final TransactionData transactionData = this.transactionState.end();
            if (transactionData != null) {
                TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
            }
        }
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
