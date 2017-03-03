// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

public class EnvironmentInformation
{
    private long memoryUsage;
    private int orientation;
    private String networkStatus;
    private String networkWanType;
    private long[] diskAvailable;
    
    public EnvironmentInformation() {
    }
    
    public EnvironmentInformation(final long memoryUsage, final int orientation, final String networkStatus, final String networkWanType, final long[] diskAvailable) {
        this.memoryUsage = memoryUsage;
        this.orientation = orientation;
        this.networkStatus = networkStatus;
        this.networkWanType = networkWanType;
        this.diskAvailable = diskAvailable;
    }
    
    public void setMemoryUsage(final long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
    
    public void setOrientation(final int orientation) {
        this.orientation = orientation;
    }
    
    public void setNetworkStatus(final String networkStatus) {
        this.networkStatus = networkStatus;
    }
    
    public void setNetworkWanType(final String networkWanType) {
        this.networkWanType = networkWanType;
    }
    
    public void setDiskAvailable(final long[] diskAvailable) {
        this.diskAvailable = diskAvailable;
    }
    
    public long getMemoryUsage() {
        return this.memoryUsage;
    }
    
    public int getOrientation() {
        return this.orientation;
    }
    
    public String getNetworkStatus() {
        return this.networkStatus;
    }
    
    public String getNetworkWanType() {
        return this.networkWanType;
    }
    
    public long[] getDiskAvailable() {
        return this.diskAvailable;
    }
}
