// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import java.util.HashMap;
import java.util.Map;

public enum MetricCategory
{
    NONE("None"), 
    VIEW_LOADING("View Loading"), 
    VIEW_LAYOUT("Layout"), 
    DATABASE("Database"), 
    IMAGE("Images"), 
    JSON("JSON"), 
    NETWORK("Network");
    
    private String categoryName;
    private static final Map<String, MetricCategory> methodMap;
    
    private MetricCategory(final String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getCategoryName() {
        return this.categoryName;
    }
    
    public static MetricCategory categoryForMethod(final String fullMethodName) {
        if (fullMethodName == null) {
            return MetricCategory.NONE;
        }
        String methodName = null;
        final int hashIndex = fullMethodName.indexOf("#");
        if (hashIndex >= 0) {
            methodName = fullMethodName.substring(hashIndex + 1);
        }
        MetricCategory category = MetricCategory.methodMap.get(methodName);
        if (category == null) {
            category = MetricCategory.NONE;
        }
        return category;
    }
    
    static {
        methodMap = new HashMap<String, MetricCategory>() {
            {
                this.put("onCreate", MetricCategory.VIEW_LOADING);
            }
        };
    }
}
