// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class ConnectInformation extends HarvestableArray
{
    private static final AgentLog log;
    private ApplicationInformation applicationInformation;
    private DeviceInformation deviceInformation;
    
    public ConnectInformation(final ApplicationInformation applicationInformation, final DeviceInformation deviceInformation) {
        if (null == applicationInformation) {
            ConnectInformation.log.error("null applicationInformation passed into ConnectInformation constructor");
        }
        if (null == deviceInformation) {
            ConnectInformation.log.error("null deviceInformation passed into ConnectInformation constructor");
        }
        this.applicationInformation = applicationInformation;
        this.deviceInformation = deviceInformation;
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        this.notNull(this.applicationInformation);
        array.add(this.applicationInformation.asJsonArray());
        this.notNull(this.deviceInformation);
        array.add(this.deviceInformation.asJsonArray());
        return array;
    }
    
    public ApplicationInformation getApplicationInformation() {
        return this.applicationInformation;
    }
    
    public DeviceInformation getDeviceInformation() {
        return this.deviceInformation;
    }
    
    public void setApplicationInformation(final ApplicationInformation applicationInformation) {
        this.applicationInformation = applicationInformation;
    }
    
    public void setDeviceInformation(final DeviceInformation deviceInformation) {
        this.deviceInformation = deviceInformation;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ConnectInformation that = (ConnectInformation)o;
        Label_0062: {
            if (this.applicationInformation != null) {
                if (this.applicationInformation.equals(that.applicationInformation)) {
                    break Label_0062;
                }
            }
            else if (that.applicationInformation == null) {
                break Label_0062;
            }
            return false;
        }
        if (this.deviceInformation != null) {
            if (this.deviceInformation.equals(that.deviceInformation)) {
                return true;
            }
        }
        else if (that.deviceInformation == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.applicationInformation != null) ? this.applicationInformation.hashCode() : 0;
        result = 31 * result + ((this.deviceInformation != null) ? this.deviceInformation.hashCode() : 0);
        return result;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
