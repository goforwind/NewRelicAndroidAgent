// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

public interface HarvestLifecycleAware
{
    void onHarvestStart();
    
    void onHarvestStop();
    
    void onHarvestBefore();
    
    void onHarvest();
    
    void onHarvestFinalize();
    
    void onHarvestError();
    
    void onHarvestSendFailed();
    
    void onHarvestComplete();
    
    void onHarvestConnected();
    
    void onHarvestDisconnected();
    
    void onHarvestDisabled();
}
