// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.Measurements;
import java.util.TreeMap;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import java.net.URL;
import com.newrelic.agent.android.instrumentation.io.CountingOutputStream;
import java.io.OutputStream;
import com.newrelic.agent.android.instrumentation.io.StreamCompleteEvent;
import com.newrelic.agent.android.instrumentation.io.StreamCompleteListener;
import java.util.List;
import java.util.Map;
import java.net.ProtocolException;
import java.security.Permission;
import com.newrelic.agent.android.instrumentation.io.CountingInputStream;
import java.io.InputStream;
import java.io.IOException;
import com.newrelic.agent.android.logging.AgentLog;
import java.net.HttpURLConnection;

public class HttpURLConnectionExtension extends HttpURLConnection
{
    private HttpURLConnection impl;
    private TransactionState transactionState;
    private static final AgentLog log;
    
    public HttpURLConnectionExtension(final HttpURLConnection impl) {
        super(impl.getURL());
        TransactionStateUtil.setCrossProcessHeader(this.impl = impl);
    }
    
    @Override
    public void addRequestProperty(final String field, final String newValue) {
        this.impl.addRequestProperty(field, newValue);
    }
    
    @Override
    public void disconnect() {
        if (this.transactionState != null && !this.transactionState.isComplete()) {
            this.addTransactionAndErrorData(this.transactionState);
        }
        this.impl.disconnect();
    }
    
    @Override
    public boolean usingProxy() {
        return this.impl.usingProxy();
    }
    
    @Override
    public void connect() throws IOException {
        this.getTransactionState();
        try {
            this.impl.connect();
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
    }
    
    @Override
    public boolean getAllowUserInteraction() {
        return this.impl.getAllowUserInteraction();
    }
    
    @Override
    public int getConnectTimeout() {
        return this.impl.getConnectTimeout();
    }
    
    @Override
    public Object getContent() throws IOException {
        this.getTransactionState();
        Object object;
        try {
            object = this.impl.getContent();
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        final int contentLength = this.impl.getContentLength();
        if (contentLength >= 0) {
            final TransactionState transactionState = this.getTransactionState();
            if (!transactionState.isComplete()) {
                transactionState.setBytesReceived(contentLength);
                this.addTransactionAndErrorData(transactionState);
            }
        }
        return object;
    }
    
    @Override
    public Object getContent(final Class[] types) throws IOException {
        this.getTransactionState();
        Object object;
        try {
            object = this.impl.getContent(types);
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        this.checkResponse();
        return object;
    }
    
    @Override
    public String getContentEncoding() {
        this.getTransactionState();
        final String contentEncoding = this.impl.getContentEncoding();
        this.checkResponse();
        return contentEncoding;
    }
    
    @Override
    public int getContentLength() {
        this.getTransactionState();
        final int contentLength = this.impl.getContentLength();
        this.checkResponse();
        return contentLength;
    }
    
    @Override
    public String getContentType() {
        this.getTransactionState();
        final String contentType = this.impl.getContentType();
        this.checkResponse();
        return contentType;
    }
    
    @Override
    public long getDate() {
        this.getTransactionState();
        final long date = this.impl.getDate();
        this.checkResponse();
        return date;
    }
    
    @Override
    public InputStream getErrorStream() {
        this.getTransactionState();
        CountingInputStream in;
        try {
            in = new CountingInputStream(this.impl.getErrorStream(), true);
        }
        catch (Exception e) {
            HttpURLConnectionExtension.log.error("HttpURLConnectionExtension: " + e.toString());
            return this.impl.getErrorStream();
        }
        return in;
    }
    
    @Override
    public long getHeaderFieldDate(final String field, final long defaultValue) {
        this.getTransactionState();
        final long date = this.impl.getHeaderFieldDate(field, defaultValue);
        this.checkResponse();
        return date;
    }
    
    @Override
    public boolean getInstanceFollowRedirects() {
        return this.impl.getInstanceFollowRedirects();
    }
    
    @Override
    public Permission getPermission() throws IOException {
        return this.impl.getPermission();
    }
    
    @Override
    public String getRequestMethod() {
        return this.impl.getRequestMethod();
    }
    
    @Override
    public int getResponseCode() throws IOException {
        this.getTransactionState();
        int responseCode;
        try {
            responseCode = this.impl.getResponseCode();
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        this.checkResponse();
        return responseCode;
    }
    
    @Override
    public String getResponseMessage() throws IOException {
        this.getTransactionState();
        String message;
        try {
            message = this.impl.getResponseMessage();
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        this.checkResponse();
        return message;
    }
    
    @Override
    public void setChunkedStreamingMode(final int chunkLength) {
        this.impl.setChunkedStreamingMode(chunkLength);
    }
    
    @Override
    public void setFixedLengthStreamingMode(final int contentLength) {
        this.impl.setFixedLengthStreamingMode(contentLength);
    }
    
    @Override
    public void setInstanceFollowRedirects(final boolean followRedirects) {
        this.impl.setInstanceFollowRedirects(followRedirects);
    }
    
    @Override
    public void setRequestMethod(final String method) throws ProtocolException {
        this.getTransactionState();
        try {
            this.impl.setRequestMethod(method);
        }
        catch (ProtocolException e) {
            this.error(e);
            throw e;
        }
    }
    
    @Override
    public boolean getDefaultUseCaches() {
        return this.impl.getDefaultUseCaches();
    }
    
    @Override
    public boolean getDoInput() {
        return this.impl.getDoInput();
    }
    
    @Override
    public boolean getDoOutput() {
        return this.impl.getDoOutput();
    }
    
    @Override
    public long getExpiration() {
        this.getTransactionState();
        final long expiration = this.impl.getExpiration();
        this.checkResponse();
        return expiration;
    }
    
    @Override
    public String getHeaderField(final int pos) {
        this.getTransactionState();
        final String header = this.impl.getHeaderField(pos);
        this.checkResponse();
        return header;
    }
    
    @Override
    public String getHeaderField(final String key) {
        this.getTransactionState();
        final String header = this.impl.getHeaderField(key);
        this.checkResponse();
        return header;
    }
    
    @Override
    public int getHeaderFieldInt(final String field, final int defaultValue) {
        this.getTransactionState();
        final int header = this.impl.getHeaderFieldInt(field, defaultValue);
        this.checkResponse();
        return header;
    }
    
    @Override
    public String getHeaderFieldKey(final int posn) {
        this.getTransactionState();
        final String key = this.impl.getHeaderFieldKey(posn);
        this.checkResponse();
        return key;
    }
    
    @Override
    public Map<String, List<String>> getHeaderFields() {
        this.getTransactionState();
        final Map<String, List<String>> fields = this.impl.getHeaderFields();
        this.checkResponse();
        return fields;
    }
    
    @Override
    public long getIfModifiedSince() {
        this.getTransactionState();
        final long ifModifiedSince = this.impl.getIfModifiedSince();
        this.checkResponse();
        return ifModifiedSince;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        final TransactionState transactionState = this.getTransactionState();
        CountingInputStream in;
        try {
            in = new CountingInputStream(this.impl.getInputStream());
            TransactionStateUtil.inspectAndInstrumentResponse(transactionState, this.impl);
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        in.addStreamCompleteListener(new StreamCompleteListener() {
            @Override
            public void streamError(final StreamCompleteEvent e) {
                if (!transactionState.isComplete()) {
                    transactionState.setBytesReceived(e.getBytes());
                }
                HttpURLConnectionExtension.this.error(e.getException());
            }
            
            @Override
            public void streamComplete(final StreamCompleteEvent e) {
                if (!transactionState.isComplete()) {
                    final long contentLength = HttpURLConnectionExtension.this.impl.getContentLength();
                    long numBytes = e.getBytes();
                    if (contentLength >= 0L) {
                        numBytes = contentLength;
                    }
                    transactionState.setBytesReceived(numBytes);
                    HttpURLConnectionExtension.this.addTransactionAndErrorData(transactionState);
                }
            }
        });
        return in;
    }
    
    @Override
    public long getLastModified() {
        this.getTransactionState();
        final long lastModified = this.impl.getLastModified();
        this.checkResponse();
        return lastModified;
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        final TransactionState transactionState = this.getTransactionState();
        CountingOutputStream out;
        try {
            out = new CountingOutputStream(this.impl.getOutputStream());
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        out.addStreamCompleteListener(new StreamCompleteListener() {
            @Override
            public void streamError(final StreamCompleteEvent e) {
                if (!transactionState.isComplete()) {
                    transactionState.setBytesSent(e.getBytes());
                }
                HttpURLConnectionExtension.this.error(e.getException());
            }
            
            @Override
            public void streamComplete(final StreamCompleteEvent e) {
                if (!transactionState.isComplete()) {
                    final String header = HttpURLConnectionExtension.this.impl.getRequestProperty("content-length");
                    long numBytes = e.getBytes();
                    if (header != null) {
                        try {
                            numBytes = Long.parseLong(header);
                        }
                        catch (NumberFormatException ex) {}
                    }
                    transactionState.setBytesSent(numBytes);
                    HttpURLConnectionExtension.this.addTransactionAndErrorData(transactionState);
                }
            }
        });
        return out;
    }
    
    @Override
    public int getReadTimeout() {
        return this.impl.getReadTimeout();
    }
    
    @Override
    public Map<String, List<String>> getRequestProperties() {
        return this.impl.getRequestProperties();
    }
    
    @Override
    public String getRequestProperty(final String field) {
        return this.impl.getRequestProperty(field);
    }
    
    @Override
    public URL getURL() {
        return this.impl.getURL();
    }
    
    @Override
    public boolean getUseCaches() {
        return this.impl.getUseCaches();
    }
    
    @Override
    public void setAllowUserInteraction(final boolean newValue) {
        this.impl.setAllowUserInteraction(newValue);
    }
    
    @Override
    public void setConnectTimeout(final int timeoutMillis) {
        this.impl.setConnectTimeout(timeoutMillis);
    }
    
    @Override
    public void setDefaultUseCaches(final boolean newValue) {
        this.impl.setDefaultUseCaches(newValue);
    }
    
    @Override
    public void setDoInput(final boolean newValue) {
        this.impl.setDoInput(newValue);
    }
    
    @Override
    public void setDoOutput(final boolean newValue) {
        this.impl.setDoOutput(newValue);
    }
    
    @Override
    public void setIfModifiedSince(final long newValue) {
        this.impl.setIfModifiedSince(newValue);
    }
    
    @Override
    public void setReadTimeout(final int timeoutMillis) {
        this.impl.setReadTimeout(timeoutMillis);
    }
    
    @Override
    public void setRequestProperty(final String field, final String newValue) {
        this.impl.setRequestProperty(field, newValue);
    }
    
    @Override
    public void setUseCaches(final boolean newValue) {
        this.impl.setUseCaches(newValue);
    }
    
    @Override
    public String toString() {
        return this.impl.toString();
    }
    
    private void checkResponse() {
        if (!this.getTransactionState().isComplete()) {
            TransactionStateUtil.inspectAndInstrumentResponse(this.getTransactionState(), this.impl);
        }
    }
    
    private TransactionState getTransactionState() {
        if (this.transactionState == null) {
            TransactionStateUtil.inspectAndInstrument(this.transactionState = new TransactionState(), this.impl);
        }
        return this.transactionState;
    }
    
    private void error(final Exception e) {
        final TransactionState transactionState = this.getTransactionState();
        TransactionStateUtil.setErrorCodeFromException(transactionState, e);
        if (!transactionState.isComplete()) {
            TransactionStateUtil.inspectAndInstrumentResponse(transactionState, this.impl);
            final TransactionData transactionData = transactionState.end();
            if (transactionData != null) {
                TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
            }
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
                final InputStream errorStream = this.getErrorStream();
                if (errorStream instanceof CountingInputStream) {
                    responseBody.append(((CountingInputStream)errorStream).getBufferAsString());
                }
            }
            catch (Exception e) {
                HttpURLConnectionExtension.log.error(e.toString());
            }
            final Map<String, String> params = new TreeMap<String, String>();
            final String contentType = this.impl.getContentType();
            if (contentType != null && !"".equals(contentType)) {
                params.put("content_type", contentType);
            }
            params.put("content_length", transactionState.getBytesReceived() + "");
            Measurements.addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), responseBody.toString(), params);
        }
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
