// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.com.google.gson;

public enum LongSerializationPolicy
{
    DEFAULT {
        public JsonElement serialize(final Long value) {
            return new JsonPrimitive(value);
        }
    }, 
    STRING {
        public JsonElement serialize(final Long value) {
            return new JsonPrimitive(String.valueOf(value));
        }
    };
    
    public abstract JsonElement serialize(final Long p0);
}
