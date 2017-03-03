// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import com.newrelic.agent.android.instrumentation.httpclient.ResponseHandlerImpl;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.HttpRequest;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.URLConnection;

public final class HttpInstrumentation
{
    @WrapReturn(className = "java/net/URL", methodName = "openConnection", methodDesc = "()Ljava/net/URLConnection;")
    public static URLConnection openConnection(final URLConnection connection) {
        if (connection instanceof HttpsURLConnection) {
            return new HttpsURLConnectionExtension((HttpsURLConnection)connection);
        }
        if (connection instanceof HttpURLConnection) {
            return new HttpURLConnectionExtension((HttpURLConnection)connection);
        }
        return connection;
    }
    
    @WrapReturn(className = "java.net.URL", methodName = "openConnection", methodDesc = "(Ljava/net/Proxy;)Ljava/net/URLConnection;")
    public static URLConnection openConnectionWithProxy(final URLConnection connection) {
        if (connection instanceof HttpsURLConnection) {
            return new HttpsURLConnectionExtension((HttpsURLConnection)connection);
        }
        if (connection instanceof HttpURLConnection) {
            return new HttpURLConnectionExtension((HttpURLConnection)connection);
        }
        return connection;
    }
    
    @ReplaceCallSite
    public static HttpResponse execute(final HttpClient httpClient, final HttpHost target, final HttpRequest request, final HttpContext context) throws IOException {
        final TransactionState transactionState = new TransactionState();
        try {
            return delegate(httpClient.execute(target, delegate(target, request, transactionState), context), transactionState);
        }
        catch (IOException e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }
    
    @ReplaceCallSite
    public static <T> T execute(final HttpClient httpClient, final HttpHost target, final HttpRequest request, final ResponseHandler<? extends T> responseHandler, final HttpContext context) throws IOException, ClientProtocolException {
        final TransactionState transactionState = new TransactionState();
        try {
            return (T)httpClient.execute(target, delegate(target, request, transactionState), (ResponseHandler)delegate((org.apache.http.client.ResponseHandler<?>)responseHandler, transactionState), context);
        }
        catch (ClientProtocolException e) {
            httpClientError(transactionState, (Exception)e);
            throw e;
        }
        catch (IOException e2) {
            httpClientError(transactionState, e2);
            throw e2;
        }
    }
    
    @ReplaceCallSite
    public static <T> T execute(final HttpClient httpClient, final HttpHost target, final HttpRequest request, final ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        final TransactionState transactionState = new TransactionState();
        try {
            return (T)httpClient.execute(target, delegate(target, request, transactionState), (ResponseHandler)delegate((org.apache.http.client.ResponseHandler<?>)responseHandler, transactionState));
        }
        catch (ClientProtocolException e) {
            httpClientError(transactionState, (Exception)e);
            throw e;
        }
        catch (IOException e2) {
            httpClientError(transactionState, e2);
            throw e2;
        }
    }
    
    @ReplaceCallSite
    public static HttpResponse execute(final HttpClient httpClient, final HttpHost target, final HttpRequest request) throws IOException {
        final TransactionState transactionState = new TransactionState();
        try {
            return delegate(httpClient.execute(target, delegate(target, request, transactionState)), transactionState);
        }
        catch (IOException e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }
    
    @ReplaceCallSite
    public static HttpResponse execute(final HttpClient httpClient, final HttpUriRequest request, final HttpContext context) throws IOException {
        final TransactionState transactionState = new TransactionState();
        try {
            return delegate(httpClient.execute(delegate(request, transactionState), context), transactionState);
        }
        catch (IOException e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }
    
    @ReplaceCallSite
    public static <T> T execute(final HttpClient httpClient, final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler, final HttpContext context) throws IOException, ClientProtocolException {
        final TransactionState transactionState = new TransactionState();
        try {
            return (T)httpClient.execute(delegate(request, transactionState), (ResponseHandler)delegate((org.apache.http.client.ResponseHandler<?>)responseHandler, transactionState), context);
        }
        catch (ClientProtocolException e) {
            httpClientError(transactionState, (Exception)e);
            throw e;
        }
        catch (IOException e2) {
            httpClientError(transactionState, e2);
            throw e2;
        }
    }
    
    @ReplaceCallSite
    public static <T> T execute(final HttpClient httpClient, final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        final TransactionState transactionState = new TransactionState();
        try {
            return (T)httpClient.execute(delegate(request, transactionState), (ResponseHandler)delegate((org.apache.http.client.ResponseHandler<?>)responseHandler, transactionState));
        }
        catch (ClientProtocolException e) {
            httpClientError(transactionState, (Exception)e);
            throw e;
        }
        catch (IOException e2) {
            httpClientError(transactionState, e2);
            throw e2;
        }
    }
    
    @ReplaceCallSite
    public static HttpResponse execute(final HttpClient httpClient, final HttpUriRequest request) throws IOException {
        final TransactionState transactionState = new TransactionState();
        try {
            return delegate(httpClient.execute(delegate(request, transactionState)), transactionState);
        }
        catch (IOException e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }
    
    private static void httpClientError(final TransactionState transactionState, final Exception e) {
        if (!transactionState.isComplete()) {
            TransactionStateUtil.setErrorCodeFromException(transactionState, e);
            final TransactionData transactionData = transactionState.end();
            if (transactionData != null) {
                TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
            }
        }
    }
    
    private static HttpUriRequest delegate(final HttpUriRequest request, final TransactionState transactionState) {
        return TransactionStateUtil.inspectAndInstrument(transactionState, request);
    }
    
    private static HttpRequest delegate(final HttpHost host, final HttpRequest request, final TransactionState transactionState) {
        return TransactionStateUtil.inspectAndInstrument(transactionState, host, request);
    }
    
    private static HttpResponse delegate(final HttpResponse response, final TransactionState transactionState) {
        return TransactionStateUtil.inspectAndInstrument(transactionState, response);
    }
    
    private static <T> ResponseHandler<? extends T> delegate(final ResponseHandler<? extends T> handler, final TransactionState transactionState) {
        return (ResponseHandler<? extends T>)ResponseHandlerImpl.wrap((org.apache.http.client.ResponseHandler<?>)handler, transactionState);
    }
}
