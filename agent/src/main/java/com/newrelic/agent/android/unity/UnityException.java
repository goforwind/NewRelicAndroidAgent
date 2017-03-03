// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.unity;

import android.text.TextUtils;

public class UnityException extends RuntimeException
{
    private String sourceExceptionType;
    
    public UnityException() {
        this.sourceExceptionType = null;
    }
    
    public UnityException(final String sourceExceptionType, final String detailMessage) {
        super(detailMessage);
        this.sourceExceptionType = null;
        this.sourceExceptionType = sourceExceptionType;
    }
    
    public UnityException(final String detailMessage) {
        super(detailMessage);
        this.sourceExceptionType = null;
    }
    
    public UnityException(final String detailMessage, final StackTraceElement[] stackTraceElements) {
        super(detailMessage);
        this.sourceExceptionType = null;
        this.setStackTrace(stackTraceElements);
    }
    
    public void appendStackFrame(final StackTraceElement stackFrame) {
        final StackTraceElement[] currentStack = this.getStackTrace();
        final StackTraceElement[] newStack = new StackTraceElement[currentStack.length + 1];
        for (int i = 0; i < currentStack.length; ++i) {
            newStack[i] = currentStack[i];
        }
        newStack[currentStack.length] = stackFrame;
        this.setStackTrace(newStack);
    }
    
    public void appendStackFrame(final String className, final String methodName, final String fileName, final int lineNumber) {
        final StackTraceElement stackFrame = new StackTraceElement(className, methodName, fileName, lineNumber);
        final StackTraceElement[] currentStack = this.getStackTrace();
        final StackTraceElement[] newStack = new StackTraceElement[currentStack.length + 1];
        for (int i = 0; i < currentStack.length; ++i) {
            newStack[i] = currentStack[i];
        }
        newStack[currentStack.length] = stackFrame;
        this.setStackTrace(newStack);
    }
    
    public void setSourceExceptionType(final String sourceExceptionType) {
        this.sourceExceptionType = sourceExceptionType;
    }
    
    public String toString() {
        return TextUtils.isEmpty((CharSequence)this.sourceExceptionType) ? this.getClass().getName() : this.sourceExceptionType;
    }
}
