// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

public class HarvestResponse
{
    private static final String DISABLE_STRING = "DISABLE_NEW_RELIC";
    private int statusCode;
    private String responseBody;
    private long responseTime;
    
    public Code getResponseCode() {
        if (this.isOK()) {
            return Code.OK;
        }
        for (final Code code : Code.values()) {
            if (code.getStatusCode() == this.statusCode) {
                return code;
            }
        }
        return Code.UNKNOWN;
    }
    
    public boolean isDisableCommand() {
        return Code.FORBIDDEN == this.getResponseCode() && "DISABLE_NEW_RELIC".equals(this.getResponseBody());
    }
    
    public boolean isError() {
        return this.statusCode >= 400;
    }
    
    public boolean isUnknown() {
        return this.getResponseCode() == Code.UNKNOWN;
    }
    
    public boolean isOK() {
        return !this.isError();
    }
    
    public int getStatusCode() {
        return this.statusCode;
    }
    
    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getResponseBody() {
        return this.responseBody;
    }
    
    public void setResponseBody(final String responseBody) {
        this.responseBody = responseBody;
    }
    
    public long getResponseTime() {
        return this.responseTime;
    }
    
    public void setResponseTime(final long responseTime) {
        this.responseTime = responseTime;
    }
    
    public enum Code
    {
        OK(200), 
        UNAUTHORIZED(401), 
        FORBIDDEN(403), 
        ENTITY_TOO_LARGE(413), 
        INVALID_AGENT_ID(450), 
        UNSUPPORTED_MEDIA_TYPE(415), 
        INTERNAL_SERVER_ERROR(500), 
        UNKNOWN(-1);
        
        int statusCode;
        
        private Code(final int statusCode) {
            this.statusCode = statusCode;
        }
        
        public int getStatusCode() {
            return this.statusCode;
        }
        
        public boolean isError() {
            return this != Code.OK;
        }
        
        public boolean isOK() {
            return !this.isError();
        }
    }
}
