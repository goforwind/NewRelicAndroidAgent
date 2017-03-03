// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.okhttp2;

import java.io.IOException;
import com.squareup.okhttp.MediaType;
import okio.BufferedSource;
import com.squareup.okhttp.ResponseBody;

public class PrebufferedResponseBody extends ResponseBody
{
    private ResponseBody impl;
    private BufferedSource source;
    
    public PrebufferedResponseBody(final ResponseBody impl, final BufferedSource source) {
        this.impl = impl;
        this.source = source;
    }
    
    public MediaType contentType() {
        return this.impl.contentType();
    }
    
    public long contentLength() {
        return this.source.buffer().size();
    }
    
    public BufferedSource source() {
        return this.source;
    }
    
    public void close() throws IOException {
        this.impl.close();
    }
}
