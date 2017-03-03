// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.logging;

public class AgentLogManager
{
    private static DefaultAgentLog instance;
    
    public static AgentLog getAgentLog() {
        return AgentLogManager.instance;
    }
    
    public static void setAgentLog(final AgentLog instance) {
        AgentLogManager.instance.setImpl(instance);
    }
    
    static {
        AgentLogManager.instance = new DefaultAgentLog();
    }
}
