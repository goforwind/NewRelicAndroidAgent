// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;
import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.agent.android.util.SafeJsonPrimitive;
import com.newrelic.com.google.gson.JsonElement;
import java.util.Set;

public class AnalyticAttribute
{
    public static final String USERNAME_ATTRIBUTE = "username";
    public static final String USER_EMAIL_ATTRIBUTE = "email";
    public static final String USER_ID_ATTRIBUTE = "userId";
    public static final String CAMPAIGN_ID_ATTRIBUTE = "campaignId";
    public static final String SESSION_REVENUE_ATTRIBUTE = "sessionRevenue";
    public static final String SUBSCRIPTION_ATTRIBUTE = "subscription";
    public static final String SUBSCRIPTION_REVENUE_ATTRIBUTE = "subscriptionRevenue";
    public static final String ACCOUNT_ID_ATTRIBUTE = "accountId";
    public static final String APP_ID_ATTRIBUTE = "appId";
    public static final String APP_BUILD_ATTRIBUTE = "appBuild";
    public static final String APP_NAME_ATTRIBUTE = "appName";
    public static final String BUNDLE_ID_ATTRIBUTE = "bundleId";
    public static final String PROCESS_ID_ATTRIBUTE = "processId";
    public static final String APPLICATION_PLATFORM_ATTRIBUTE = "platform";
    public static final String APPLICATION_PLATFORM_VERSION_ATTRIBUTE = "platformVersion";
    public static final String UUID_ATTRIBUTE = "uuid";
    public static final String OS_NAME_ATTRIBUTE = "osName";
    public static final String OS_VERSION_ATTRIBUTE = "osVersion";
    public static final String OS_MAJOR_VERSION_ATTRIBUTE = "osMajorVersion";
    public static final String OS_BUILD_ATTRIBUTE = "osBuild";
    public static final String ARCHITECTURE_ATTRIBUTE = "architecture";
    public static final String RUNTIME_ATTRIBUTE = "runTime";
    public static final String DEVICE_MANUFACTURER_ATTRIBUTE = "deviceManufacturer";
    public static final String DEVICE_MODEL_ATTRIBUTE = "deviceModel";
    public static final String CARRIER_ATTRIBUTE = "carrier";
    public static final String NEW_RELIC_VERSION_ATTRIBUTE = "newRelicVersion";
    public static final String MEM_USAGE_MB_ATTRIBUTE = "memUsageMb";
    public static final String SESSION_ID_ATTRIBUTE = "sessionId";
    public static final String SESSION_DURATION_ATTRIBUTE = "sessionDuration";
    public static final String SESSION_TIME_SINCE_LOAD_ATTRIBUTE = "timeSinceLoad";
    public static final String INTERACTION_DURATION_ATTRIBUTE = "interactionDuration";
    public static final String SCREEN_RESOLUTION_ATTRIBUTE = "screenResolution";
    public static final String LAST_INTERACTION_ATTRIBUTE = "lastInteraction";
    public static final String TIME_SINCE_LAST_INTERACTION_ATTRIBUTE = "timeSinceLastInteraction";
    public static final String EVENT_CATEGORY_ATTRIBUTE = "category";
    public static final String EVENT_NAME_ATTRIBUTE = "name";
    public static final String EVENT_TIMESTAMP_ATTRIBUTE = "timestamp";
    public static final String EVENT_TIME_SINCE_LOAD_ATTRIBUTE = "timeSinceLoad";
    public static final String EVENT_SESSION_ELAPSED_TIME_ATTRIBUTE = "sessionElapsedTime";
    public static final String EVENT_TYPE_ATTRIBUTE = "eventType";
    public static final String EVENT_TYPE_ATTRIBUTE_MOBILE = "Mobile";
    public static final String EVENT_TYPE_ATTRIBUTE_MOBILE_REQUEST = "MobileRequest";
    public static final String EVENT_TYPE_ATTRIBUTE_MOBILE_REQUEST_ERROR = "MobileRequestError";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String PURCHASE_EVENT_ATTRIBUTE = "Purchase";
    public static final String PURCHASE_EVENT_SKU_ATTRIBUTE = "sku";
    public static final String PURCHASE_EVENT_QUANTITY_ATTRIBUTE = "quantity";
    public static final String PURCHASE_EVENT_UNIT_PRICE_ATTRIBUTE = "unitprice";
    public static final String PURCHASE_EVENT_TOTAL_PRICE_ATTRIBUTE = "total";
    public static final String APP_INSTALL_ATTRIBUTE = "install";
    public static final String APP_UPGRADE_ATTRIBUTE = "upgradeFrom";
    public static final String EVENT_NAME_ATTRIBUTE_REQUEST_ERROR = "requestError";
    public static final String REQUEST_URL_ATTRIBUTE = "requestUrl";
    public static final String REQUEST_DOMAIN_ATTRIBUTE = "requestDomain";
    public static final String REQUEST_PATH_ATTRIBUTE = "requestPath";
    public static final String REQUEST_METHOD_ATTRIBUTE = "requestMethod";
    public static final String CONNECTION_TYPE_ATTRIBUTE = "connectionType";
    public static final String STATUS_CODE_ATTRIBUTE = "statusCode";
    public static final String BYTES_RECEIVED_ATTRIBUTE = "bytesReceived";
    public static final String BYTES_SENT_ATTRIBUTE = "byteSent";
    public static final String RESPONSE_TIME_ATTRIBUTE = "responseTime";
    public static final String NETWORK_ERROR_CODE_ATTRIBUTE = "networkErrorCode";
    public static final String ANDROID_EXCEPTION_ATTRIBUTE = "androidException";
    public static final int ATTRIBUTE_NAME_MAX_LENGTH = 256;
    public static final int ATTRIBUTE_VALUE_MAX_LENGTH = 4096;
    private static final String STRING_ATTRIBUTE_FORMAT = "\"%s\"=\"%s\"";
    private static final String DOUBLE_ATTRIBUTE_FORMAT = "\"%s\"=%f";
    private String name;
    private String stringValue;
    private float floatValue;
    private boolean isPersistent;
    private AttributeDataType attributeDataType;
    protected static Set<String> blackList;
    
    protected AnalyticAttribute() {
        this.stringValue = null;
        this.floatValue = Float.NaN;
        this.isPersistent = false;
        this.attributeDataType = AttributeDataType.VOID;
    }
    
    public AnalyticAttribute(final String name, final String stringValue) {
        this(name, stringValue, true);
    }
    
    public AnalyticAttribute(final String name, final String stringValue, final boolean isPersistent) {
        this.name = name;
        this.setStringValue(stringValue);
        this.isPersistent = isPersistent;
    }
    
    public AnalyticAttribute(final String name, final float floatValue) {
        this(name, floatValue, true);
    }
    
    public AnalyticAttribute(final String name, final float floatValue, final boolean isPersistent) {
        this.name = name;
        this.setFloatValue(floatValue);
        this.isPersistent = isPersistent;
    }
    
    public AnalyticAttribute(final String name, final boolean boolValue) {
        this(name, boolValue, true);
    }
    
    public AnalyticAttribute(final String name, final boolean boolValue, final boolean isPersistent) {
        this.name = name;
        this.setBooleanValue(boolValue);
        this.isPersistent = isPersistent;
    }
    
    public AnalyticAttribute(final AnalyticAttribute clone) {
        this.name = clone.name;
        this.floatValue = clone.floatValue;
        this.stringValue = clone.stringValue;
        this.isPersistent = clone.isPersistent;
        this.attributeDataType = clone.attributeDataType;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isStringAttribute() {
        return this.attributeDataType == AttributeDataType.STRING;
    }
    
    public boolean isFloatAttribute() {
        return this.attributeDataType == AttributeDataType.FLOAT;
    }
    
    public boolean isBooleanAttribute() {
        return this.attributeDataType == AttributeDataType.BOOLEAN;
    }
    
    public String getStringValue() {
        return (this.attributeDataType == AttributeDataType.STRING) ? this.stringValue : null;
    }
    
    public void setStringValue(final String stringValue) {
        this.stringValue = stringValue;
        this.floatValue = Float.NaN;
        this.attributeDataType = AttributeDataType.STRING;
    }
    
    public float getFloatValue() {
        return (this.attributeDataType == AttributeDataType.FLOAT) ? this.floatValue : Float.NaN;
    }
    
    public void setFloatValue(final float floatValue) {
        this.floatValue = floatValue;
        this.stringValue = null;
        this.attributeDataType = AttributeDataType.FLOAT;
    }
    
    public boolean getBooleanValue() {
        return this.attributeDataType == AttributeDataType.BOOLEAN && Boolean.valueOf(this.stringValue);
    }
    
    public void setBooleanValue(final boolean boolValue) {
        this.stringValue = Boolean.toString(boolValue);
        this.floatValue = Float.NaN;
        this.attributeDataType = AttributeDataType.BOOLEAN;
    }
    
    public boolean isPersistent() {
        return this.isPersistent && !isAttributeBlacklisted(this);
    }
    
    public void setPersistent(final boolean isPersistent) {
        this.isPersistent = isPersistent;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final AnalyticAttribute attribute = (AnalyticAttribute)o;
        return this.name.equals(attribute.name);
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("AnalyticAttribute{");
        stringBuilder.append("name='" + this.name + "'");
        switch (this.attributeDataType) {
            case STRING: {
                stringBuilder.append(",stringValue='" + this.stringValue + "'");
                break;
            }
            case FLOAT: {
                stringBuilder.append(",floatValue='" + this.floatValue + "'");
                break;
            }
            case BOOLEAN: {
                stringBuilder.append(",booleanValue=" + Boolean.valueOf(this.stringValue).toString());
                break;
            }
        }
        stringBuilder.append(",isPersistent=" + this.isPersistent);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
    
    public static boolean isAttributeBlacklisted(final AnalyticAttribute attribute) {
        return AnalyticAttribute.blackList.contains(attribute.getName());
    }
    
    public AttributeDataType getAttributeDataType() {
        return this.attributeDataType;
    }
    
    public String valueAsString() {
        String value = null;
        switch (this.attributeDataType) {
            case STRING: {
                value = this.stringValue;
                break;
            }
            case FLOAT: {
                value = Float.toString(this.floatValue);
                break;
            }
            case BOOLEAN: {
                value = Boolean.valueOf(this.getBooleanValue()).toString();
                break;
            }
            default: {
                value = null;
                break;
            }
        }
        return value;
    }
    
    public JsonElement asJsonElement() {
        JsonPrimitive jsonPrimitive = null;
        switch (this.attributeDataType) {
            case STRING: {
                jsonPrimitive = SafeJsonPrimitive.factory(this.getStringValue());
                break;
            }
            case FLOAT: {
                jsonPrimitive = SafeJsonPrimitive.factory(this.getFloatValue());
                break;
            }
            case BOOLEAN: {
                jsonPrimitive = SafeJsonPrimitive.factory(Boolean.valueOf(this.getBooleanValue()));
                break;
            }
            default: {
                jsonPrimitive = null;
                break;
            }
        }
        return jsonPrimitive;
    }
    
    public static Set<AnalyticAttribute> newFromJson(final JsonObject attributesJson) {
        final Set<AnalyticAttribute> attributeSet = new HashSet<AnalyticAttribute>();
        for (final Map.Entry<String, JsonElement> elem : attributesJson.entrySet()) {
            final String key = elem.getKey();
            if (elem.getValue().isJsonPrimitive()) {
                final JsonPrimitive value = elem.getValue().getAsJsonPrimitive();
                if (value.isString()) {
                    attributeSet.add(new AnalyticAttribute(key, value.getAsString(), false));
                }
                else if (value.isBoolean()) {
                    attributeSet.add(new AnalyticAttribute(key, value.getAsBoolean(), false));
                }
                else {
                    if (!value.isNumber()) {
                        continue;
                    }
                    attributeSet.add(new AnalyticAttribute(key, value.getAsFloat(), false));
                }
            }
            else {
                attributeSet.add(new AnalyticAttribute(key, elem.getValue().getAsString(), false));
            }
        }
        return attributeSet;
    }
    
    static {
        AnalyticAttribute.blackList = new HashSet<String>() {
            {
                this.add("install");
                this.add("upgradeFrom");
                this.add("sessionDuration");
            }
        };
    }
    
    public enum AttributeDataType
    {
        VOID, 
        STRING, 
        FLOAT, 
        BOOLEAN;
    }
}
