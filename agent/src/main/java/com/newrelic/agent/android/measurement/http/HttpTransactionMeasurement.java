// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.http;

import com.newrelic.agent.android.tracing.TraceMachine;
import com.newrelic.agent.android.util.Util;
import com.newrelic.agent.android.measurement.MeasurementType;
import com.newrelic.agent.android.measurement.BaseMeasurement;

public class HttpTransactionMeasurement extends BaseMeasurement
{
    private String url;
    private String httpMethod;
    private double totalTime;
    private int statusCode;
    private int errorCode;
    private long bytesSent;
    private long bytesReceived;
    private String appData;
    
    public HttpTransactionMeasurement(String url, final String httpMethod, final int statusCode, final long startTime, final double totalTime, final long bytesSent, final long bytesReceived, final String appData) {
        super(MeasurementType.Network);
        url = Util.sanitizeUrl(url);
        this.setName(url);
        this.setScope(TraceMachine.getCurrentScope());
        this.setStartTime(startTime);
        this.setEndTime(startTime + (int)totalTime);
        this.setExclusiveTime((int)(totalTime * 1000.0));
        this.url = url;
        this.httpMethod = httpMethod;
        this.statusCode = statusCode;
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
        this.totalTime = totalTime;
        this.appData = appData;
    }
    
    public HttpTransactionMeasurement(final String url, final String httpMethod, final int statusCode, final int errorCode, final long startTime, final double totalTime, final long bytesSent, final long bytesReceived, final String appData) {
        this(url, httpMethod, statusCode, startTime, totalTime, bytesSent, bytesReceived, appData);
        this.errorCode = errorCode;
    }
    
    @Override
    public double asDouble() {
        return this.totalTime;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public String getHttpMethod() {
        return this.httpMethod;
    }
    
    public double getTotalTime() {
        return this.totalTime;
    }
    
    public int getStatusCode() {
        return this.statusCode;
    }
    
    public int getErrorCode() {
        return this.errorCode;
    }
    
    public long getBytesSent() {
        return this.bytesSent;
    }
    
    public long getBytesReceived() {
        return this.bytesReceived;
    }
    
    public String getAppData() {
        return this.appData;
    }
    
    public void setUrl(final String url) {
        this.url = url;
    }
    
    @Override
    public String toString() {
        return "HttpTransactionMeasurement{url='" + this.url + '\'' + ", httpMethod='" + this.httpMethod + '\'' + ", totalTime=" + this.totalTime + ", statusCode=" + this.statusCode + ", errorCode=" + this.errorCode + ", bytesSent=" + this.bytesSent + ", bytesReceived=" + this.bytesReceived + ", appData='" + this.appData + '\'' + '}';
    }
}
