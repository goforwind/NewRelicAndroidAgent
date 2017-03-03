// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class ApplicationInformation extends HarvestableArray
{
    private String appName;
    private String appVersion;
    private String appBuild;
    private String packageId;
    private int versionCode;
    
    public ApplicationInformation() {
        this.versionCode = -1;
    }
    
    public ApplicationInformation(final String appName, final String appVersion, final String packageId, final String appBuild) {
        this();
        this.appName = appName;
        this.appVersion = appVersion;
        this.packageId = packageId;
        this.appBuild = appBuild;
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        this.notEmpty(this.appName);
        array.add(new JsonPrimitive(this.appName));
        this.notEmpty(this.appVersion);
        array.add(new JsonPrimitive(this.appVersion));
        this.notEmpty(this.packageId);
        array.add(new JsonPrimitive(this.packageId));
        return array;
    }
    
    public void setAppName(final String appName) {
        this.appName = appName;
    }
    
    public String getAppName() {
        return this.appName;
    }
    
    public void setAppVersion(final String appVersion) {
        this.appVersion = appVersion;
    }
    
    public String getAppVersion() {
        return this.appVersion;
    }
    
    public void setAppBuild(final String appBuild) {
        this.appBuild = appBuild;
    }
    
    public String getAppBuild() {
        return this.appBuild;
    }
    
    public void setPackageId(final String packageId) {
        this.packageId = packageId;
    }
    
    public String getPackageId() {
        return this.packageId;
    }
    
    public void setVersionCode(final int versionCode) {
        this.versionCode = versionCode;
    }
    
    public int getVersionCode() {
        return this.versionCode;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ApplicationInformation that = (ApplicationInformation)o;
        Label_0062: {
            if (this.appName != null) {
                if (this.appName.equals(that.appName)) {
                    break Label_0062;
                }
            }
            else if (that.appName == null) {
                break Label_0062;
            }
            return false;
        }
        Label_0095: {
            if (this.appVersion != null) {
                if (this.appVersion.equals(that.appVersion)) {
                    break Label_0095;
                }
            }
            else if (that.appVersion == null) {
                break Label_0095;
            }
            return false;
        }
        Label_0128: {
            if (this.appBuild != null) {
                if (this.appBuild.equals(that.appBuild)) {
                    break Label_0128;
                }
            }
            else if (that.appBuild == null) {
                break Label_0128;
            }
            return false;
        }
        if (this.packageId != null) {
            if (this.packageId.equals(that.packageId)) {
                return this.versionCode == that.versionCode;
            }
        }
        else if (that.packageId == null) {
            return this.versionCode == that.versionCode;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.appName != null) ? this.appName.hashCode() : 0;
        result = 31 * result + ((this.appVersion != null) ? this.appVersion.hashCode() : 0);
        result = 31 * result + ((this.appBuild != null) ? this.appBuild.hashCode() : 0);
        result = 31 * result + ((this.packageId != null) ? this.packageId.hashCode() : 0);
        return result;
    }
    
    public boolean isAppUpgrade(final ApplicationInformation that) {
        boolean brc = false;
        if (that.versionCode == -1) {
            brc = (this.versionCode >= 0 && that.appVersion != null);
        }
        else {
            brc = (this.versionCode > that.versionCode);
        }
        return brc;
    }
}
