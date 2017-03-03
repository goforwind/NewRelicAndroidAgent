// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.util;

import com.newrelic.com.google.gson.JsonPrimitive;

public class SafeJsonPrimitive
{
    public static final String NULL_STRING = "null";
    public static final Number NULL_NUMBER;
    public static final Boolean NULL_BOOL;
    public static final char NULL_CHAR = ' ';
    
    public static String checkNull(final String string) {
        return (string == null) ? "null" : string;
    }
    
    public static Boolean checkNull(final Boolean bool) {
        return (bool == null) ? SafeJsonPrimitive.NULL_BOOL : bool;
    }
    
    public static Number checkNull(final Number number) {
        return (number == null) ? SafeJsonPrimitive.NULL_NUMBER : number;
    }
    
    public static Character checkNull(final Character c) {
        return (c == null) ? ' ' : c;
    }
    
    public static JsonPrimitive factory(final Boolean bool) {
        return new JsonPrimitive(checkNull(bool));
    }
    
    public static JsonPrimitive factory(final Number number) {
        return new JsonPrimitive(checkNull(number));
    }
    
    public static JsonPrimitive factory(final String string) {
        return new JsonPrimitive(checkNull(string));
    }
    
    public static JsonPrimitive factory(final Character character) {
        return new JsonPrimitive(checkNull(character));
    }
    
    static {
        NULL_NUMBER = Float.NaN;
        NULL_BOOL = Boolean.FALSE;
    }
}
