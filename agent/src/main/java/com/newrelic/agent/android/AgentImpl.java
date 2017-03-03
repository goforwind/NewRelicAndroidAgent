// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android;

import com.newrelic.agent.android.harvest.EnvironmentInformation;
import com.newrelic.agent.android.harvest.ApplicationInformation;
import com.newrelic.agent.android.harvest.DeviceInformation;
import com.newrelic.agent.android.util.Encoder;
import java.util.List;
import com.newrelic.agent.android.api.common.TransactionData;

public interface AgentImpl
{
    void addTransactionData(final TransactionData p0);
    
    List<TransactionData> getAndClearTransactionData();
    
    void mergeTransactionData(final List<TransactionData> p0);
    
    String getCrossProcessId();
    
    int getStackTraceLimit();
    
    int getResponseBodyLimit();
    
    void start();
    
    void stop();
    
    void disable();
    
    boolean isDisabled();
    
    String getNetworkCarrier();
    
    String getNetworkWanType();
    
    void setLocation(final String p0, final String p1);
    
    Encoder getEncoder();
    
    DeviceInformation getDeviceInformation();
    
    ApplicationInformation getApplicationInformation();
    
    EnvironmentInformation getEnvironmentInformation();
    
    boolean updateSavedConnectInformation();
    
    long getSessionDurationMillis();
}
