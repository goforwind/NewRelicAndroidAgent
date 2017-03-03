// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp2;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Map;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.Measurements;
import okio.BufferedSource;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.ResponseBody;
import okio.Buffer;
import java.util.TreeMap;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Request;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;

public class OkHttp2TransactionStateUtil extends TransactionStateUtil
{
    private static final AgentLog log;
    
    public static void inspectAndInstrument(final TransactionState transactionState, final Request request) {
        if (request == null) {
            OkHttp2TransactionStateUtil.log.warning("Missing request");
        }
        else {
            TransactionStateUtil.inspectAndInstrument(transactionState, request.urlString(), request.method());
        }
    }
    
    public static Response inspectAndInstrumentResponse(final TransactionState transactionState, final Response response) {
        String appData = "";
        int statusCode = -1;
        long contentLength = 0L;
        if (response == null) {
            statusCode = 500;
            OkHttp2TransactionStateUtil.log.warning("Missing response");
        }
        else {
            appData = response.header("X-NewRelic-App-Data");
            statusCode = response.code();
            try {
                contentLength = response.body().contentLength();
            }
            catch (Exception e) {
                OkHttp2TransactionStateUtil.log.warning("Missing body or content length");
            }
        }
        TransactionStateUtil.inspectAndInstrumentResponse(transactionState, appData, (int)contentLength, statusCode);
        return addTransactionAndErrorData(transactionState, response);
    }
    
    private static Response addTransactionAndErrorData(final TransactionState transactionState, Response response) {
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
                    final ResponseBody body = response.body();
                    responseBodyString = body.string();
                    final Buffer contents = new Buffer().write(responseBodyString.getBytes());
                    final ResponseBody responseBody = new ResponseBody() {
                        public MediaType contentType() {
                            return body.contentType();
                        }
                        
                        public long contentLength() {
                            return contents.size();
                        }
                        
                        public BufferedSource source() {
                            return (BufferedSource)contents;
                        }
                    };
                    response = response.newBuilder().body(responseBody).build();
                }
                catch (Exception e) {
                    if (response.message() != null) {
                        OkHttp2TransactionStateUtil.log.warning("Missing response body, using response message");
                        responseBodyString = response.message();
                    }
                }
                if (transactionData.getErrorCode() != 0) {
                    Measurements.addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), responseBodyString, params);
                }
                else {
                    Measurements.addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), responseBodyString, params);
                }
            }
        }
        return response;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
