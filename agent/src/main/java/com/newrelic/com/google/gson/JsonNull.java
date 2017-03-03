// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.com.google.gson;

public final class JsonNull extends JsonElement
{
    public static final JsonNull INSTANCE;
    
    JsonNull deepCopy() {
        return JsonNull.INSTANCE;
    }
    
    public int hashCode() {
        return JsonNull.class.hashCode();
    }
    
    public boolean equals(final Object other) {
        return this == other || other instanceof JsonNull;
    }
    
    static {
        INSTANCE = new JsonNull();
    }
}
