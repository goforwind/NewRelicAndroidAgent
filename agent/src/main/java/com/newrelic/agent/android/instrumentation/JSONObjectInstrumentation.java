// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import java.util.Collection;
import java.util.Arrays;
import com.newrelic.agent.android.tracing.Trace;
import com.newrelic.agent.android.tracing.TraceMachine;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class JSONObjectInstrumentation
{
    private static final ArrayList<String> categoryParams;
    
    @TraceConstructor
    public static JSONObject init(final String json) throws JSONException {
        if (json == null) {
            throw new JSONException("Failed to initialize JSONObject: json string is null.");
        }
        JSONObject jsonObject;
        try {
            TraceMachine.enterMethod(null, "JSONObject#<init>", JSONObjectInstrumentation.categoryParams);
            jsonObject = new JSONObject(json);
            TraceMachine.exitMethod();
        }
        catch (JSONException e) {
            TraceMachine.exitMethod();
            throw e;
        }
        return jsonObject;
    }
    
    @ReplaceCallSite(scope = "org.json.JSONObject")
    public static String toString(final JSONObject jsonObject) {
        TraceMachine.enterMethod(null, "JSONObject#toString", JSONObjectInstrumentation.categoryParams);
        final String jsonString = jsonObject.toString();
        TraceMachine.exitMethod();
        return jsonString;
    }
    
    @ReplaceCallSite(scope = "org.json.JSONObject")
    public static String toString(final JSONObject jsonObject, final int indentFactor) throws JSONException {
        TraceMachine.enterMethod(null, "JSONObject#toString", JSONObjectInstrumentation.categoryParams);
        String jsonString;
        try {
            jsonString = jsonObject.toString(indentFactor);
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
