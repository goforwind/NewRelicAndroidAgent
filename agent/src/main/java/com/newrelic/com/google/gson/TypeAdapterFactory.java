// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.com.google.gson;

import com.newrelic.com.google.gson.reflect.TypeToken;

public interface TypeAdapterFactory
{
     <T> TypeAdapter<T> create(final Gson p0, final TypeToken<T> p1);
}
