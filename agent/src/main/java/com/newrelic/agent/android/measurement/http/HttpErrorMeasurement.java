// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.http;

import com.newrelic.agent.android.measurement.MeasurementType;
import java.util.Map;
import com.newrelic.agent.android.measurement.BaseMeasurement;

public class HttpErrorMeasurement extends BaseMeasurement
{
    private String url;
    private int httpStatusCode;
    private int errorCode;
    private String responseBody;
    private String stackTrace;
    private Map<String, String> params;
    private String httpMethod;
    private String wanType;
    private double totalTime;
    private long bytesSent;
    private long bytesReceived;
    
    public HttpErrorMeasurement(final String url, final int httpStatusCode) {
        super(MeasurementType.HttpError);
        this.setUrl(url);
        this.setName(url);
        this.setHttpStatusCode(httpStatusCode);
        this.setStartTime(System.currentTimeMillis());
    }
    
    public void setUrl(final String url) {
        this.url = url;
    }
    
    public void setHttpStatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
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
    
    public String getUrl() {
        return this.url;
    }
    
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }
    
    public String getResponseBody() {
        return this.responseBody;
    }
    
    public String getStackTrace() {
        return this.stackTrace;
    }
    
    public Map<String, String> getParams() {
        return this.params;
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
    
    public long getBytesSent() {
        return this.bytesSent;
    }
    
    public void setBytesSent(final long bytesSent) {
        this.bytesSent = bytesSent;
    }
    
    public long getBytesReceived() {
        return this.bytesReceived;
    }
    
    public void setBytesReceived(final long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }
    
    public int getErrorCode() {
        return this.errorCode;
    }
    
    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }
}
