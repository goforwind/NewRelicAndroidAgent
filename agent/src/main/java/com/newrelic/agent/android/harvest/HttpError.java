// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import com.newrelic.agent.android.harvest.type.HarvestableObject;
import java.util.Collections;
import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.agent.android.Agent;
import com.newrelic.agent.android.FeatureFlag;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.agent.android.util.SafeJsonPrimitive;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.agent.android.measurement.http.HttpErrorMeasurement;
import java.util.Map;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class HttpError extends HarvestableArray
{
    private static final AgentLog log;
    private String url;
    private int httpStatusCode;
    private long count;
    private String responseBody;
    private String stackTrace;
    private Map<String, String> params;
    private String appData;
    private String digest;
    private String httpMethod;
    private String wanType;
    private double totalTime;
    private int errorCode;
    private long bytesSent;
    private long bytesReceived;
    private Long timestamp;
    
    public HttpError() {
    }
    
    public HttpError(final String url, final int httpStatusCode, final String responseBody, final String stackTrace, final Map<String, String> params) {
        this.url = url;
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
        this.stackTrace = stackTrace;
        this.params = params;
        this.count = 1L;
        this.digest = this.computeHash();
    }
    
    public HttpError(final HttpErrorMeasurement m) {
        this(m.getUrl(), m.getHttpStatusCode(), m.getResponseBody(), m.getStackTrace(), m.getParams());
        this.setTimestamp(m.getStartTime());
        this.setHttpMethod(m.getHttpMethod());
        this.setWanType(m.getWanType());
        this.setHttpMethod(m.getHttpMethod());
        this.setErrorCode(m.getErrorCode());
    }
    
    @Override
    public JsonArray asJsonArray() {
        final int bodyLimit = Harvest.getHarvestConfiguration().getResponse_body_limit();
        final JsonArray array = new JsonArray();
        array.add(SafeJsonPrimitive.factory(this.url));
        array.add(SafeJsonPrimitive.factory(this.httpStatusCode));
        array.add(SafeJsonPrimitive.factory(this.count));
        String body = "";
        if (FeatureFlag.featureEnabled(FeatureFlag.HttpResponseBodyCapture)) {
            body = this.optional(this.responseBody);
            if (body.length() > bodyLimit) {
                HttpError.log.warning("HTTP Error response BODY is too large. Truncating to " + bodyLimit + " bytes.");
                body = body.substring(0, bodyLimit);
            }
        }
        else {
            HttpError.log.warning("not enabled");
        }
        array.add(SafeJsonPrimitive.factory(Agent.getEncoder().encode(body.getBytes())));
        array.add(SafeJsonPrimitive.factory(this.optional(this.stackTrace)));
        final JsonObject customParams = new JsonObject();
        if (this.params == null) {
            this.params = Collections.emptyMap();
        }
        customParams.add("custom_params", HarvestableObject.fromMap(this.params).asJson());
        array.add(customParams);
        array.add(SafeJsonPrimitive.factory(this.optional(this.appData)));
        return array;
    }
    
    public void incrementCount() {
        ++this.count;
    }
    
    public String getHash() {
        return this.digest;
    }
    
    public void digest() {
        this.digest = this.computeHash();
    }
    
    private String computeHash() {
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            HttpError.log.error("Unable to initialize SHA-1 hash algorithm");
            return null;
        }
        digester.update(this.url.getBytes());
        digester.update(ByteBuffer.allocate(8).putInt(this.httpStatusCode).array());
        if (this.stackTrace != null && this.stackTrace.length() > 0) {
            digester.update(this.stackTrace.getBytes());
        }
        return new String(digester.digest());
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }
    
    public void setUrl(final String url) {
        this.url = url;
    }
    
    public void setHttpStatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
    
    public void setCount(final long count) {
        this.count = count;
    }
    
    public void setResponseBody(final String responseBody) {
        this.responseBody = responseBody;
    }
    
    public void setStackTrace(final String stackTrace) {
        this.stackTrace = stackTrace;
    }
    
    public void setParams(final Map<String, String> params) {
        this.params = params;
    }
    
    public void setAppData(final String appData) {
        this.appData = appData;
    }
    
    public Long getTimestamp() {
        return this.timestamp;
    }
    
    public Map<String, String> getParams() {
        return this.params;
    }
    
    public void setTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getBytesReceived() {
        return this.bytesReceived;
    }
    
    public void setBytesReceived(final long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }
    
    public String getHttpMethod() {
        return this.httpMethod;
    }
    
    public void setHttpMethod(final String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public String getWanType() {
        return this.wanType;
    }
    
    public void setWanType(final String wanType) {
        this.wanType = wanType;
    }
    
    public double getTotalTime() {
        return this.totalTime;
    }
    
    public void setTotalTime(final double totalTime) {
        this.totalTime = totalTime;
    }
    
    public int getErrorCode() {
        return this.errorCode;
    }
    
    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }
    
    public long getBytesSent() {
        return this.bytesSent;
    }
    
    public void setBytesSent(final long bytesSent) {
        this.bytesSent = bytesSent;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
