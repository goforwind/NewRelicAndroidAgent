// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.net.MalformedURLException;
import java.net.URL;
import com.newrelic.agent.android.util.Util;
import com.newrelic.agent.android.tracing.TraceMachine;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.logging.AgentLog;

public final class TransactionState
{
    private static final AgentLog log;
    private String url;
    private String httpMethod;
    private int statusCode;
    private int errorCode;
    private long bytesSent;
    private long bytesReceived;
    private long startTime;
    private long endTime;
    private String appData;
    private String carrier;
    private String wanType;
    private State state;
    private String contentType;
    private TransactionData transactionData;
    
    public TransactionState() {
        this.startTime = System.currentTimeMillis();
        this.carrier = "unknown";
        this.wanType = "unknown";
        this.state = State.READY;
        TraceMachine.enterNetworkSegment("External/unknownhost");
    }
    
    public void setCarrier(final String carrier) {
        if (!this.isSent()) {
            TraceMachine.setCurrentTraceParam("carrier", this.carrier = carrier);
        }
        else {
            TransactionState.log.warning("setCarrier(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public void setWanType(final String wanType) {
        if (!this.isSent()) {
            TraceMachine.setCurrentTraceParam("wan_type", this.wanType = wanType);
        }
        else {
            TransactionState.log.warning("setWanType(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public void setAppData(final String appData) {
        if (!this.isComplete()) {
            TraceMachine.setCurrentTraceParam("encoded_app_data", this.appData = appData);
        }
        else {
            TransactionState.log.warning("setAppData(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public void setUrl(final String urlString) {
        final String url = Util.sanitizeUrl(urlString);
        if (url == null) {
            return;
        }
        if (!this.isSent()) {
            this.url = url;
            try {
                TraceMachine.setCurrentDisplayName("External/" + new URL(url).getHost());
            }
            catch (MalformedURLException e) {
                TransactionState.log.error("unable to parse host name from " + url);
            }
            TraceMachine.setCurrentTraceParam("uri", url);
        }
        else {
            TransactionState.log.warning("setUrl(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public void setHttpMethod(final String httpMethod) {
        if (!this.isSent()) {
            TraceMachine.setCurrentTraceParam("http_method", this.httpMethod = httpMethod);
        }
        else {
            TransactionState.log.warning("setHttpMethod(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public String getHttpMethod() {
        return this.httpMethod;
    }
    
    public boolean isSent() {
        return this.state.ordinal() >= State.SENT.ordinal();
    }
    
    public boolean isComplete() {
        return this.state.ordinal() >= State.COMPLETE.ordinal();
    }
    
    public void setStatusCode(final int statusCode) {
        if (!this.isComplete()) {
            this.statusCode = statusCode;
            TraceMachine.setCurrentTraceParam("status_code", statusCode);
        }
        else {
            TransactionState.log.warning("setStatusCode(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public int getStatusCode() {
        return this.statusCode;
    }
    
    public void setErrorCode(final int errorCode) {
        if (!this.isComplete()) {
            this.errorCode = errorCode;
            TraceMachine.setCurrentTraceParam("error_code", errorCode);
        }
        else {
            if (this.transactionData != null) {
                this.transactionData.setErrorCode(errorCode);
            }
            TransactionState.log.warning("setErrorCode(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public int getErrorCode() {
        return this.errorCode;
    }
    
    public void setBytesSent(final long bytesSent) {
        if (!this.isComplete()) {
            this.bytesSent = bytesSent;
            TraceMachine.setCurrentTraceParam("bytes_sent", bytesSent);
            this.state = State.SENT;
        }
        else {
            TransactionState.log.warning("setBytesSent(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public void setBytesReceived(final long bytesReceived) {
        if (!this.isComplete()) {
            this.bytesReceived = bytesReceived;
            TraceMachine.setCurrentTraceParam("bytes_received", bytesReceived);
        }
        else {
            TransactionState.log.warning("setBytesReceived(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public long getBytesReceived() {
        return this.bytesReceived;
    }
    
    public TransactionData end() {
        if (!this.isComplete()) {
            this.state = State.COMPLETE;
            this.endTime = System.currentTimeMillis();
            TraceMachine.exitMethod();
        }
        return this.toTransactionData();
    }
    
    private TransactionData toTransactionData() {
        if (!this.isComplete()) {
            TransactionState.log.warning("toTransactionData() called on incomplete TransactionState");
        }
        if (this.url == null) {
            TransactionState.log.error("Attempted to convert a TransactionState instance with no URL into a TransactionData");
            return null;
        }
        if (this.transactionData == null) {
            this.transactionData = new TransactionData(this.url, this.httpMethod, this.carrier, (this.endTime - this.startTime) / 1000.0f, this.statusCode, this.errorCode, this.bytesSent, this.bytesReceived, this.appData, this.wanType);
        }
        return this.transactionData;
    }
    
    public String getContentType() {
        return this.contentType;
    }
    
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }
    
    @Override
    public String toString() {
        return "TransactionState{url='" + this.url + '\'' + ", httpMethod='" + this.httpMethod + '\'' + ", statusCode=" + this.statusCode + ", errorCode=" + this.errorCode + ", bytesSent=" + this.bytesSent + ", bytesReceived=" + this.bytesReceived + ", startTime=" + this.startTime + ", endTime=" + this.endTime + ", appData='" + this.appData + '\'' + ", carrier='" + this.carrier + '\'' + ", wanType='" + this.wanType + '\'' + ", state=" + this.state + ", contentType='" + this.contentType + '\'' + ", transactionData=" + this.transactionData + '}';
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
    
    private enum State
    {
        READY, 
        SENT, 
        COMPLETE;
    }
}
