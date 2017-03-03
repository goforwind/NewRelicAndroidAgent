// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.util;

import com.newrelic.agent.android.Agent;

public class AgentBuildOptionsReporter
{
    public static void main(final String[] args) {
        System.out.println("Agent version: " + Agent.getVersion());
        System.out.println("Unity instrumentation: " + Agent.getUnityInstrumentationFlag());
        System.out.println("Build ID: " + Agent.getBuildId());
    }
}
