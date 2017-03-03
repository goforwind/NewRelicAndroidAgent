// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.com.google.gson.Gson;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.com.google.gson.JsonArray;
import java.util.HashMap;
import java.util.Map;
import com.newrelic.agent.android.ApplicationPlatform;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class DeviceInformation extends HarvestableArray
{
    private static final AgentLog log;
    private String osName;
    private String osVersion;
    private String osBuild;
    private String model;
    private String agentName;
    private String agentVersion;
    private String deviceId;
    private String countryCode;
    private String regionCode;
    private String manufacturer;
    private String architecture;
    private String runTime;
    private String size;
    private ApplicationPlatform applicationPlatform;
    private String applicationPlatformVersion;
    private Map<String, String> misc;
    
    public DeviceInformation() {
        this.misc = new HashMap<String, String>();
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        this.notEmpty(this.osName);
        array.add(new JsonPrimitive(this.osName));
        this.notEmpty(this.osVersion);
        array.add(new JsonPrimitive(this.osVersion));
        this.notEmpty(this.manufacturer);
        this.notEmpty(this.model);
        array.add(new JsonPrimitive(this.manufacturer + " " + this.model));
        this.notEmpty(this.agentName);
        array.add(new JsonPrimitive(this.agentName));
        this.notEmpty(this.agentVersion);
        array.add(new JsonPrimitive(this.agentVersion));
        this.notEmpty(this.deviceId);
        array.add(new JsonPrimitive(this.deviceId));
        array.add(new JsonPrimitive(this.optional(this.countryCode)));
        array.add(new JsonPrimitive(this.optional(this.regionCode)));
        array.add(new JsonPrimitive(this.manufacturer));
        final Map<String, String> miscMap = new HashMap<String, String>();
        if (this.misc != null && !this.misc.isEmpty()) {
            miscMap.putAll(this.misc);
        }
        if (this.applicationPlatform != null) {
            miscMap.put("platform", this.applicationPlatform.toString());
            if (this.applicationPlatformVersion != null) {
                miscMap.put("platformVersion", this.applicationPlatformVersion);
            }
        }
        final JsonElement map = new Gson().toJsonTree(miscMap, DeviceInformation.GSON_STRING_MAP_TYPE);
        array.add(map);
        return array;
    }
    
    public void setOsName(final String osName) {
        this.osName = osName;
    }
    
    public void setOsVersion(final String osVersion) {
        this.osVersion = osVersion;
    }
    
    public void setOsBuild(final String osBuild) {
        this.osBuild = osBuild;
    }
    
    public void setManufacturer(final String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public void setModel(final String model) {
        this.model = model;
    }
    
    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }
    
    public void setRegionCode(final String regionCode) {
        this.regionCode = regionCode;
    }
    
    public void setAgentName(final String agentName) {
        this.agentName = agentName;
    }
    
    public void setAgentVersion(final String agentVersion) {
        this.agentVersion = agentVersion;
    }
    
    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }
    
    public void setArchitecture(final String architecture) {
        this.architecture = architecture;
    }
    
    public void setRunTime(final String runTime) {
        this.runTime = runTime;
    }
    
    public void setSize(final String size) {
        this.addMisc("size", this.size = size);
    }
    
    public void setApplicationPlatform(final ApplicationPlatform applicationPlatform) {
        this.applicationPlatform = applicationPlatform;
    }
    
    public void setApplicationPlatformVersion(final String applicationPlatformVersion) {
        this.applicationPlatformVersion = applicationPlatformVersion;
    }
    
    public void setMisc(final Map<String, String> misc) {
        this.misc = new HashMap<String, String>(misc);
    }
    
    public void addMisc(final String key, final String value) {
        this.misc.put(key, value);
    }
    
    public String getOsName() {
        return this.osName;
    }
    
    public String getOsVersion() {
        return this.osVersion;
    }
    
    public String getOsBuild() {
        return this.osBuild;
    }
    
    public String getModel() {
        return this.model;
    }
    
    public String getAgentName() {
        return this.agentName;
    }
    
    public String getAgentVersion() {
        return this.agentVersion;
    }
    
    public String getDeviceId() {
        return this.deviceId;
    }
    
    public String getCountryCode() {
        return this.countryCode;
    }
    
    public String getRegionCode() {
        return this.regionCode;
    }
    
    public String getManufacturer() {
        return this.manufacturer;
    }
    
    public String getArchitecture() {
        return this.architecture;
    }
    
    public String getRunTime() {
        return this.runTime;
    }
    
    public String getSize() {
        return this.size;
    }
    
    public ApplicationPlatform getApplicationPlatform() {
        return this.applicationPlatform;
    }
    
    public String getApplicationPlatformVersion() {
        return this.applicationPlatformVersion;
    }
    
    @Override
    public String toJsonString() {
        return "DeviceInformation{manufacturer='" + this.manufacturer + '\'' + ", osName='" + this.osName + '\'' + ", osVersion='" + this.osVersion + '\'' + ", model='" + this.model + '\'' + ", agentName='" + this.agentName + '\'' + ", agentVersion='" + this.agentVersion + '\'' + ", deviceId='" + this.deviceId + '\'' + ", countryCode='" + this.countryCode + '\'' + ", regionCode='" + this.regionCode + '\'' + '}';
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final DeviceInformation that = (DeviceInformation)o;
        Label_0062: {
            if (this.agentName != null) {
                if (this.agentName.equals(that.agentName)) {
                    break Label_0062;
                }
            }
            else if (that.agentName == null) {
                break Label_0062;
            }
            return false;
        }
        Label_0095: {
            if (this.agentVersion != null) {
                if (this.agentVersion.equals(that.agentVersion)) {
                    break Label_0095;
                }
            }
            else if (that.agentVersion == null) {
                break Label_0095;
            }
            return false;
        }
        Label_0128: {
            if (this.architecture != null) {
                if (this.architecture.equals(that.architecture)) {
                    break Label_0128;
                }
            }
            else if (that.architecture == null) {
                break Label_0128;
            }
            return false;
        }
        Label_0161: {
            if (this.deviceId != null) {
                if (this.deviceId.equals(that.deviceId)) {
                    break Label_0161;
                }
            }
            else if (that.deviceId == null) {
                break Label_0161;
            }
            return false;
        }
        Label_0194: {
            if (this.manufacturer != null) {
                if (this.manufacturer.equals(that.manufacturer)) {
                    break Label_0194;
                }
            }
            else if (that.manufacturer == null) {
                break Label_0194;
            }
            return false;
        }
        Label_0227: {
            if (this.model != null) {
                if (this.model.equals(that.model)) {
                    break Label_0227;
                }
            }
            else if (that.model == null) {
                break Label_0227;
            }
            return false;
        }
        Label_0260: {
            if (this.osBuild != null) {
                if (this.osBuild.equals(that.osBuild)) {
                    break Label_0260;
                }
            }
            else if (that.osBuild == null) {
                break Label_0260;
            }
            return false;
        }
        Label_0293: {
            if (this.osName != null) {
                if (this.osName.equals(that.osName)) {
                    break Label_0293;
                }
            }
            else if (that.osName == null) {
                break Label_0293;
            }
            return false;
        }
        Label_0326: {
            if (this.osVersion != null) {
                if (this.osVersion.equals(that.osVersion)) {
                    break Label_0326;
                }
            }
            else if (that.osVersion == null) {
                break Label_0326;
            }
            return false;
        }
        Label_0359: {
            if (this.runTime != null) {
                if (this.runTime.equals(that.runTime)) {
                    break Label_0359;
                }
            }
            else if (that.runTime == null) {
                break Label_0359;
            }
            return false;
        }
        if (this.size != null) {
            if (this.size.equals(that.size)) {
                return true;
            }
        }
        else if (that.size == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.osName != null) ? this.osName.hashCode() : 0;
        result = 31 * result + ((this.osVersion != null) ? this.osVersion.hashCode() : 0);
        result = 31 * result + ((this.osBuild != null) ? this.osBuild.hashCode() : 0);
        result = 31 * result + ((this.model != null) ? this.model.hashCode() : 0);
        result = 31 * result + ((this.agentName != null) ? this.agentName.hashCode() : 0);
        result = 31 * result + ((this.agentVersion != null) ? this.agentVersion.hashCode() : 0);
        result = 31 * result + ((this.deviceId != null) ? this.deviceId.hashCode() : 0);
        result = 31 * result + ((this.manufacturer != null) ? this.manufacturer.hashCode() : 0);
        result = 31 * result + ((this.architecture != null) ? this.architecture.hashCode() : 0);
        result = 31 * result + ((this.runTime != null) ? this.runTime.hashCode() : 0);
        result = 31 * result + ((this.size != null) ? this.size.hashCode() : 0);
        return result;
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
