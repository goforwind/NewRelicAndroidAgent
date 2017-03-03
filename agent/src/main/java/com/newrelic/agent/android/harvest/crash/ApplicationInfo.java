// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.crash;

import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.agent.android.util.SafeJsonPrimitive;
import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.agent.android.harvest.ApplicationInformation;
import com.newrelic.agent.android.harvest.type.HarvestableObject;

public class ApplicationInfo extends HarvestableObject
{
    private String applicationName;
    private String applicationVersion;
    private String applicationBuild;
    private String bundleId;
    private int processId;
    
    public ApplicationInfo() {
        this.applicationName = "";
        this.applicationVersion = "";
        this.applicationBuild = "";
        this.bundleId = "";
        this.processId = 0;
    }
    
    public ApplicationInfo(final ApplicationInformation applicationInformation) {
        this.applicationName = "";
        this.applicationVersion = "";
        this.applicationBuild = "";
        this.bundleId = "";
        this.processId = 0;
        this.applicationName = applicationInformation.getAppName();
        this.applicationVersion = applicationInformation.getAppVersion();
        this.applicationBuild = applicationInformation.getAppBuild();
        this.bundleId = applicationInformation.getPackageId();
    }
    
    @Override
    public JsonObject asJsonObject() {
        final JsonObject data = new JsonObject();
        data.add("appName", SafeJsonPrimitive.factory(this.applicationName));
        data.add("appVersion", SafeJsonPrimitive.factory(this.applicationVersion));
        data.add("appBuild", SafeJsonPrimitive.factory(this.applicationBuild));
        data.add("bundleId", SafeJsonPrimitive.factory(this.bundleId));
        data.add("processId", SafeJsonPrimitive.factory(this.processId));
        return data;
    }
    
    public static ApplicationInfo newFromJson(final JsonObject jsonObject) {
        final ApplicationInfo info = new ApplicationInfo();
        info.applicationName = jsonObject.get("appName").getAsString();
        info.applicationVersion = jsonObject.get("appVersion").getAsString();
        info.applicationBuild = jsonObject.get("appBuild").getAsString();
        info.bundleId = jsonObject.get("bundleId").getAsString();
        info.processId = jsonObject.get("processId").getAsInt();
        return info;
    }
}
