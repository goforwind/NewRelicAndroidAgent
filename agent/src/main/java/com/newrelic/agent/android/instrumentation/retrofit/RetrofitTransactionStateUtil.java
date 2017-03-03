// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.retrofit;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Map;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.Measurements;
import java.util.TreeMap;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import java.util.Iterator;
import retrofit.client.Header;
import java.util.List;
import retrofit.client.Response;
import com.newrelic.agent.android.Agent;
import retrofit.client.Request;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;

public class RetrofitTransactionStateUtil extends TransactionStateUtil
{
    private static final AgentLog log;
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    
    public static void inspectAndInstrument(final TransactionState transactionState, final Request request) {
        transactionState.setUrl(request.getUrl());
        transactionState.setHttpMethod(request.getMethod());
        transactionState.setCarrier(Agent.getActiveNetworkCarrier());
        transactionState.setWanType(Agent.getActiveNetworkWanType());
    }
    
    public static void inspectAndInstrumentResponse(final TransactionState transactionState, final Response response) {
        final String appData = getAppDataHeader(response.getHeaders(), "X-NewRelic-App-Data");
        if (appData != null && !"".equals(appData)) {
            transactionState.setAppData(appData);
        }
        final int statusCode = response.getStatus();
        transactionState.setStatusCode(statusCode);
        final long contentLength = response.getBody().length();
        if (contentLength >= 0L) {
            transactionState.setBytesReceived(contentLength);
        }
        addTransactionAndErrorData(transactionState, response);
    }
    
    private static String getAppDataHeader(final List<Header> headers, final String headerName) {
        if (headers != null) {
            for (final Header header : headers) {
                if (header.getName() != null && header.getName().equalsIgnoreCase(headerName)) {
                    return header.getValue();
                }
            }
        }
        return null;
    }
    
    private static void addTransactionAndErrorData(final TransactionState transactionState, final Response response) {
        final TransactionData transactionData = transactionState.end();
        if (transactionData == null) {
            return;
        }
        TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
        if (transactionState.getStatusCode() >= 400L) {
            final String contentTypeHeader = getAppDataHeader(response.getHeaders(), "Content-Type");
            final String contentType = null;
            final Map<String, String> params = new TreeMap<String, String>();
            if (contentTypeHeader != null && contentTypeHeader.length() > 0 && !"".equals(contentTypeHeader)) {
                params.put("content_type", contentType);
            }
            params.put("content_length", transactionState.getBytesReceived() + "");
            Measurements.addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), response.getReason(), params);
        }
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
