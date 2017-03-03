// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation.retrofit;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import com.newrelic.agent.android.instrumentation.io.CountingInputStream;
import com.newrelic.agent.android.logging.AgentLog;
import retrofit.mime.TypedInput;

public class ContentBufferingTypedInput implements TypedInput
{
    private static final AgentLog log;
    private TypedInput impl;
    private CountingInputStream inputStream;
    
    public ContentBufferingTypedInput(TypedInput impl) {
        if (impl == null) {
            impl = (TypedInput)new EmptyBodyTypedInput();
        }
        this.impl = impl;
        this.inputStream = null;
    }
    
    public String mimeType() {
        return this.impl.mimeType();
    }
    
    public long length() {
        try {
            this.prepareInputStream();
            return this.inputStream.available();
        }
        catch (IOException e) {
            ContentBufferingTypedInput.log.error("ContentBufferingTypedInput generated an IO exception: ", e);
            return -1L;
        }
    }
    
    public InputStream in() throws IOException {
        this.prepareInputStream();
        return this.inputStream;
    }
    
    private void prepareInputStream() throws IOException {
        if (this.inputStream == null) {
            try {
                InputStream is = this.impl.in();
                if (is == null) {
                    is = new ByteArrayInputStream(new byte[0]);
                }
                this.inputStream = new CountingInputStream(is, true);
            }
            catch (Exception e) {
                ContentBufferingTypedInput.log.error("ContentBufferingTypedInput: " + e.toString());
            }
        }
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
