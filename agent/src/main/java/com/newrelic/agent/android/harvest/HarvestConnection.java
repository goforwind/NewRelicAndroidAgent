// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import com.newrelic.agent.android.util.ExceptionHelper;
import java.io.InputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import com.newrelic.agent.android.harvest.type.Harvestable;
import com.newrelic.agent.android.stats.StatsEngine;
import org.apache.http.HttpResponse;
import java.io.IOException;
import org.apache.http.client.methods.HttpUriRequest;
import com.newrelic.agent.android.stats.TicToc;
import java.io.UnsupportedEncodingException;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.BasicHttpParams;
import java.util.concurrent.TimeUnit;
import com.newrelic.agent.android.logging.AgentLogManager;
import org.apache.http.client.HttpClient;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.harvest.type.HarvestErrorCodes;

public class HarvestConnection implements HarvestErrorCodes
{
    private final AgentLog log;
    private static final String COLLECTOR_CONNECT_URI = "/mobile/v3/connect";
    private static final String COLLECTOR_DATA_URI = "/mobile/v3/data";
    private static final String APPLICATION_TOKEN_HEADER = "X-App-License-Key";
    private static final String CONNECT_TIME_HEADER = "X-NewRelic-Connect-Time";
    private static final Boolean DISABLE_COMPRESSION_FOR_DEBUGGING;
    private String collectorHost;
    private String applicationToken;
    private long serverTimestamp;
    private final HttpClient collectorClient;
    private ConnectInformation connectInformation;
    private boolean useSsl;
    
    public HarvestConnection() {
        this.log = AgentLogManager.getAgentLog();
        final int TIMEOUT_IN_SECONDS = 20;
        final int CONNECTION_TIMEOUT = (int)TimeUnit.MILLISECONDS.convert(20L, TimeUnit.SECONDS);
        final int SOCKET_BUFFER_SIZE = 8192;
        final BasicHttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout((HttpParams)params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout((HttpParams)params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setTcpNoDelay((HttpParams)params, true);
        HttpConnectionParams.setSocketBufferSize((HttpParams)params, 8192);
        this.collectorClient = (HttpClient)new DefaultHttpClient((HttpParams)params);
    }
    
    public HttpPost createPost(final String uri, final String message) {
        final String contentEncoding = (message.length() <= 512 || HarvestConnection.DISABLE_COMPRESSION_FOR_DEBUGGING) ? "identity" : "deflate";
        final HttpPost post = new HttpPost(uri);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Content-Encoding", contentEncoding);
        post.addHeader("User-Agent", System.getProperty("http.agent"));
        if (this.applicationToken == null) {
            this.log.error("Cannot create POST without an Application Token.");
            return null;
        }
        post.addHeader("X-App-License-Key", this.applicationToken);
        if (this.serverTimestamp != 0L) {
            post.addHeader("X-NewRelic-Connect-Time", Long.valueOf(this.serverTimestamp).toString());
        }
        if ("deflate".equals(contentEncoding)) {
            final byte[] deflated = this.deflate(message);
            post.setEntity((HttpEntity)new ByteArrayEntity(deflated));
        }
        else {
            try {
                post.setEntity((HttpEntity)new StringEntity(message, "utf-8"));
            }
            catch (UnsupportedEncodingException e) {
                this.log.error("UTF-8 is unsupported");
                throw new IllegalArgumentException(e);
            }
        }
        return post;
    }
    
    public HarvestResponse send(final HttpPost post) {
        final HarvestResponse harvestResponse = new HarvestResponse();
        HttpResponse response;
        try {
            final TicToc timer = new TicToc();
            timer.tic();
            response = this.collectorClient.execute((HttpUriRequest)post);
            harvestResponse.setResponseTime(timer.toc());
        }
        catch (Exception e) {
            this.log.error("Failed to send POST to collector: " + e.getMessage());
            this.recordCollectorError(e);
            return null;
        }
        harvestResponse.setStatusCode(response.getStatusLine().getStatusCode());
        try {
            harvestResponse.setResponseBody(readResponse(response));
        }
        catch (IOException e2) {
            e2.printStackTrace();
            this.log.error("Failed to retrieve collector response: " + e2.getMessage());
        }
        return harvestResponse;
    }
    
    public HarvestResponse sendConnect() {
        if (this.connectInformation == null) {
            throw new IllegalArgumentException();
        }
        final HttpPost connectPost = this.createConnectPost(this.connectInformation.toJsonString());
        if (connectPost == null) {
            this.log.error("Failed to create connect POST");
            return null;
        }
        final TicToc timer = new TicToc();
        timer.tic();
        final HarvestResponse response = this.send(connectPost);
        StatsEngine.get().sampleTimeMs("Supportability/AgentHealth/Collector/Connect", timer.toc());
        return response;
    }
    
    public HarvestResponse sendData(final Harvestable harvestable) {
        if (harvestable == null) {
            throw new IllegalArgumentException();
        }
        final HttpPost dataPost = this.createDataPost(harvestable.toJsonString());
        if (dataPost == null) {
            this.log.error("Failed to create data POST");
            return null;
        }
        return this.send(dataPost);
    }
    
    public HttpPost createConnectPost(final String message) {
        return this.createPost(this.getCollectorConnectUri(), message);
    }
    
    public HttpPost createDataPost(final String message) {
        return this.createPost(this.getCollectorDataUri(), message);
    }
    
    private byte[] deflate(final String message) {
        final int DEFLATE_BUFFER_SIZE = 8192;
        final Deflater deflater = new Deflater();
        deflater.setInput(message.getBytes());
        deflater.finish();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] buf = new byte[8192];
        while (!deflater.finished()) {
            final int byteCount = deflater.deflate(buf);
            if (byteCount <= 0) {
                this.log.error("HTTP request contains an incomplete payload");
            }
            baos.write(buf, 0, byteCount);
        }
        deflater.end();
        return baos.toByteArray();
    }
    
    public static String readResponse(final HttpResponse response) throws IOException {
        final int RESPONSE_BUFFER_SIZE = 8192;
        final char[] buf = new char[8192];
        final StringBuilder sb = new StringBuilder();
        final InputStream in = response.getEntity().getContent();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while (true) {
                final int n = reader.read(buf);
                if (n < 0) {
                    break;
                }
                sb.append(buf, 0, n);
            }
        }
        finally {
            in.close();
        }
        return sb.toString();
    }
    
    private void recordCollectorError(final Exception e) {
        this.log.error("HarvestConnection: Attempting to convert network exception " + e.getClass().getName() + " to error code.");
        StatsEngine.get().inc("Supportability/AgentHealth/Collector/ResponseErrorCodes/" + ExceptionHelper.exceptionToErrorCode(e));
    }
    
    private String getCollectorUri(final String resource) {
        final String protocol = this.useSsl ? "https://" : "http://";
        return protocol + this.collectorHost + resource;
    }
    
    private String getCollectorConnectUri() {
        return this.getCollectorUri("/mobile/v3/connect");
    }
    
    private String getCollectorDataUri() {
        return this.getCollectorUri("/mobile/v3/data");
    }
    
    public void setServerTimestamp(final long serverTimestamp) {
        this.log.debug("Setting server timestamp: " + serverTimestamp);
        this.serverTimestamp = serverTimestamp;
    }
    
    public void useSsl(final boolean useSsl) {
        this.useSsl = useSsl;
    }
    
    public void setApplicationToken(final String applicationToken) {
        this.applicationToken = applicationToken;
    }
    
    public void setCollectorHost(final String collectorHost) {
        this.collectorHost = collectorHost;
    }
    
    public void setConnectInformation(final ConnectInformation connectInformation) {
        this.connectInformation = connectInformation;
    }
    
    public ConnectInformation getConnectInformation() {
        return this.connectInformation;
    }
    
    static {
        DISABLE_COMPRESSION_FOR_DEBUGGING = false;
    }
}
