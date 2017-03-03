// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp3;

import okhttp3.MediaType;
import okio.BufferedSource;
import okhttp3.ResponseBody;

public class PrebufferedResponseBody extends ResponseBody
{
    private final ResponseBody impl;
    private final BufferedSource source;
    private final long contentLength;
    private final MediaType contentType;
    
    public PrebufferedResponseBody(final ResponseBody impl, final BufferedSource source) {
        this.impl = impl;
        this.source = source;
        this.contentType = impl.contentType();
        this.contentLength = ((impl.contentLength() >= 0L) ? impl.contentLength() : source.buffer().size());
    }
    
    public MediaType contentType() {
        return this.impl.contentType();
    }
    
    public long contentLength() {
        return this.contentLength;
    }
    
    public BufferedSource source() {
        return this.source;
    }
    
    public void close() {
        this.impl.close();
    }
}
