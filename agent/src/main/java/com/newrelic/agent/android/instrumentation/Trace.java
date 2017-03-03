// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;

@Target({ ElementType.METHOD })
public @interface Trace {
    public static final String NULL = "";
    
    String metricName() default "";
    
    boolean skipTransactionTrace() default false;
    
    MetricCategory category() default MetricCategory.NONE;
}
