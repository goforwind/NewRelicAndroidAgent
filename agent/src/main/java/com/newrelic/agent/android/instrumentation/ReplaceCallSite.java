// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import java.lang.annotation.Annotation;

public @interface ReplaceCallSite {
    boolean isStatic() default false;
    
    String scope() default "";
}
