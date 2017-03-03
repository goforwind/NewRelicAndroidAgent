// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import java.util.Collection;
import java.util.Arrays;
import com.newrelic.agent.android.tracing.TraceMachine;
import org.json.JSONException;
import org.json.JSONArray;
import java.util.ArrayList;

public class JSONArrayInstrumentation
{
    private static final ArrayList<String> categoryParams;
    
    @TraceConstructor
    public static JSONArray init(final String json) throws JSONException {
        if (json == null) {
            throw new JSONException("Failed to initialize JSONArray: json string is null.");
        }
        JSONArray jsonArray;
        try {
            TraceMachine.enterMethod("JSONArray#<init>", JSONArrayInstrumentation.categoryParams);
            jsonArray = new JSONArray(json);
            TraceMachine.exitMethod();
        }
        catch (JSONException e) {
            TraceMachine.exitMethod();
            throw e;
        }
        return jsonArray;
    }
    
    @ReplaceCallSite(scope = "org.json.JSONArray")
    public static String toString(final JSONArray jsonArray) {
        TraceMachine.enterMethod("JSONArray#toString", JSONArrayInstrumentation.categoryParams);
        final String jsonString = jsonArray.toString();
        TraceMachine.exitMethod();
        return jsonString;
    }
    
    @ReplaceCallSite(scope = "org.json.JSONArray")
    public static String toString(final JSONArray jsonArray, final int indentFactor) throws JSONException {
        String jsonString;
        try {
            TraceMachine.enterMethod("JSONArray#toString", JSONArrayInstrumentation.categoryParams);
            jsonString = jsonArray.toString(indentFactor);
            TraceMachine.exitMethod();
        }
        catch (JSONException e) {
            TraceMachine.exitMethod();
            throw e;
        }
        return jsonString;
    }
    
    static {
        categoryParams = new ArrayList<String>(Arrays.asList("category", MetricCategory.class.getName(), "JSON"));
    }
}
