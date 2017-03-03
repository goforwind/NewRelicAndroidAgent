// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp3;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Map;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.Measurements;
import java.util.TreeMap;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import okhttp3.Response;
import okhttp3.Request;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;

public class OkHttp3TransactionStateUtil extends TransactionStateUtil
{
    private static final AgentLog log;
    
    public static void inspectAndInstrument(final TransactionState transactionState, final Request request) {
        if (request == null) {
            OkHttp3TransactionStateUtil.log.warning("Missing request");
        }
        else {
            TransactionStateUtil.inspectAndInstrument(transactionState, request.url().toString(), request.method());
        }
    }
    
    public static Response inspectAndInstrumentResponse(final TransactionState transactionState, final Response response) {
        String appData = "";
        int statusCode = -1;
        long contentLength = 0L;
        if (response == null) {
            statusCode = 500;
            OkHttp3TransactionStateUtil.log.warning("Missing response");
        }
        else {
            appData = response.header("X-NewRelic-App-Data");
            statusCode = response.code();
            try {
                contentLength = response.body().contentLength();
            }
            catch (Exception e) {
                OkHttp3TransactionStateUtil.log.warning("Missing body or content length");
            }
        }
        TransactionStateUtil.inspectAndInstrumentResponse(transactionState, appData, (int)contentLength, statusCode);
        return addTransactionAndErrorData(transactionState, response);
    }
    
    private static Response addTransactionAndErrorData(final TransactionState transactionState, final Response response) {
        final TransactionData transactionData = transactionState.end();
        if (transactionData != null) {
            TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
            if (transactionState.getStatusCode() >= 400L && response != null) {
                final String contentTypeHeader = response.header("Content-Type");
                final String contentType = null;
                final Map<String, String> params = new TreeMap<String, String>();
                if (contentTypeHeader != null && contentTypeHeader.length() > 0 && !"".equals(contentTypeHeader)) {
                    params.put("content_type", contentType);
                }
                params.put("content_length", transactionState.getBytesReceived() + "");
                String responseBodyString = "";
                try {
                    responseBodyString = response.peekBody(response.body().contentLength()).string();
                }
                catch (Exception e) {
                    if (response.message() != null) {
                        OkHttp3TransactionStateUtil.log.warning("Missing response body, using response message");
                        responseBodyString = response.message();
                    }
                }
                Measurements.addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), responseBodyString, params);
            }
        }
        return response;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
