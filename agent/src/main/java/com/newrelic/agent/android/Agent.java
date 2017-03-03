// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android;

import com.newrelic.agent.android.harvest.ApplicationInformation;
import com.newrelic.agent.android.harvest.DeviceInformation;
import com.newrelic.agent.android.util.Encoder;
import java.util.List;
import com.newrelic.agent.android.api.common.TransactionData;

public class Agent
{
    public static final String VERSION = "5.11.0";
    public static final String MONO_INSTRUMENTATION_FLAG = "NO";
    public static final String UNITY_INSTRUMENTATION_FLAG = "NO";
    public static final String DEFAULT_BUILD_ID = "";
    private static final AgentImpl NULL_AGENT_IMPL;
    private static Object implLock;
    private static AgentImpl impl;
    
    public static void setImpl(final AgentImpl impl) {
        synchronized (Agent.implLock) {
            if (impl == null) {
                Agent.impl = Agent.NULL_AGENT_IMPL;
            }
            else {
                Agent.impl = impl;
            }
        }
    }
    
    public static AgentImpl getImpl() {
        synchronized (Agent.implLock) {
            return Agent.impl;
        }
    }
    
    public static String getVersion() {
        return "5.11.0";
    }
    
    public static String getMonoInstrumentationFlag() {
        return "NO";
    }
    
    public static String getUnityInstrumentationFlag() {
        return "NO";
    }
    
    public static String getBuildId() {
        return (getUnityInstrumentationFlag().equals("YES") || getMonoInstrumentationFlag().equals("YES")) ? "" : "";
    }
    
    public static String getCrossProcessId() {
        return getImpl().getCrossProcessId();
    }
    
    public static int getStackTraceLimit() {
        return getImpl().getStackTraceLimit();
    }
    
    public static int getResponseBodyLimit() {
        return getImpl().getResponseBodyLimit();
    }
    
    public static void addTransactionData(final TransactionData transactionData) {
        getImpl().addTransactionData(transactionData);
    }
    
    public static List<TransactionData> getAndClearTransactionData() {
        return getImpl().getAndClearTransactionData();
    }
    
    public static void mergeTransactionData(final List<TransactionData> transactionDataList) {
        getImpl().mergeTransactionData(transactionDataList);
    }
    
    public static String getActiveNetworkCarrier() {
        return getImpl().getNetworkCarrier();
    }
    
    public static String getActiveNetworkWanType() {
        return getImpl().getNetworkWanType();
    }
    
    public static void disable() {
        getImpl().disable();
    }
    
    public static boolean isDisabled() {
        return getImpl().isDisabled();
    }
    
    public static void start() {
        getImpl().start();
    }
    
    public static void stop() {
        getImpl().stop();
    }
    
    public static void setLocation(final String countryCode, final String adminRegion) {
        getImpl().setLocation(countryCode, adminRegion);
    }
    
    public static Encoder getEncoder() {
        return getImpl().getEncoder();
    }
    
    public static DeviceInformation getDeviceInformation() {
        return getImpl().getDeviceInformation();
    }
    
    public static ApplicationInformation getApplicationInformation() {
        return getImpl().getApplicationInformation();
    }
    
    static {
        NULL_AGENT_IMPL = new NullAgentImpl();
        Agent.implLock = new Object();
        Agent.impl = Agent.NULL_AGENT_IMPL;
    }
}
