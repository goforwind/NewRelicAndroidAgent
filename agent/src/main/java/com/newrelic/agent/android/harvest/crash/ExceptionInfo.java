// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.crash;

import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.agent.android.harvest.type.HarvestableObject;

public class ExceptionInfo extends HarvestableObject
{
    private String className;
    private String message;
    
    public ExceptionInfo() {
    }
    
    public ExceptionInfo(final Throwable throwable) {
        if (throwable.getClass().getName().equalsIgnoreCase("com.newrelic.agent.android.unity.UnityException")) {
            this.className = throwable.toString();
        }
        else {
            this.className = throwable.getClass().getName();
        }
        if (throwable.getMessage() != null) {
            this.message = throwable.getMessage();
        }
        else {
            this.message = "";
        }
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    @Override
    public JsonObject asJsonObject() {
        final JsonObject data = new JsonObject();
        data.add("name", new JsonPrimitive((this.className != null) ? this.className : ""));
        data.add("cause", new JsonPrimitive((this.message != null) ? this.message : ""));
        return data;
    }
    
    public static ExceptionInfo newFromJson(final JsonObject jsonObject) {
        final ExceptionInfo info = new ExceptionInfo();
        info.className = (jsonObject.has("name") ? jsonObject.get("name").getAsString() : "");
        info.message = (jsonObject.has("cause") ? jsonObject.get("cause").getAsString() : "");
        return info;
    }
}
