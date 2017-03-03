// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android;

import com.newrelic.agent.android.analytics.AnalyticsControllerImpl;
import com.newrelic.agent.android.api.common.TransactionData;
import java.util.TreeMap;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.util.NetworkFailure;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import java.util.Map;
import com.newrelic.agent.android.metric.MetricUnit;
import com.newrelic.agent.android.tracing.TracingInactiveException;
import com.newrelic.agent.android.tracing.TraceMachine;
import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.logging.NullAgentLog;
import com.newrelic.agent.android.logging.AndroidAgentLog;
import android.content.Context;
import android.text.TextUtils;
import com.newrelic.agent.android.stats.StatsEngine;
import com.newrelic.agent.android.logging.AgentLog;

public final class NewRelic
{
    private static final String DEFAULT_COLLECTOR_ADDR = "mobile-collector.newrelic.com";
    private static final String UNKNOWN_HTTP_REQUEST_TYPE = "unknown";
    protected static final AgentLog log;
    protected static final AgentConfiguration agentConfiguration;
    protected static boolean started;
    protected boolean loggingEnabled;
    protected int logLevel;
    
    protected NewRelic(final String token) {
        this.loggingEnabled = true;
        this.logLevel = 3;
        NewRelic.agentConfiguration.setApplicationToken(token);
    }
    
    public static NewRelic withApplicationToken(final String token) {
        return new NewRelic(token);
    }
    
    public NewRelic usingSsl(final boolean useSsl) {
        NewRelic.agentConfiguration.setUseSsl(useSsl);
        return this;
    }
    
    public NewRelic usingCollectorAddress(final String address) {
        NewRelic.agentConfiguration.setCollectorHost(address);
        return this;
    }
    
    public NewRelic usingCrashCollectorAddress(final String address) {
        NewRelic.agentConfiguration.setCrashCollectorHost(address);
        return this;
    }
    
    public NewRelic withLocationServiceEnabled(final boolean enabled) {
        NewRelic.agentConfiguration.setUseLocationService(enabled);
        return this;
    }
    
    public NewRelic withLoggingEnabled(final boolean enabled) {
        this.loggingEnabled = enabled;
        return this;
    }
    
    public NewRelic withLogLevel(final int level) {
        this.logLevel = level;
        return this;
    }
    
    public NewRelic withCrashReportingEnabled(final boolean enabled) {
        NewRelic.agentConfiguration.setReportCrashes(enabled);
        if (enabled) {
            enableFeature(FeatureFlag.CrashReporting);
        }
        else {
            disableFeature(FeatureFlag.CrashReporting);
        }
        return this;
    }
    
    public NewRelic withHttpResponseBodyCaptureEnabled(final boolean enabled) {
        if (enabled) {
            enableFeature(FeatureFlag.HttpResponseBodyCapture);
        }
        else {
            disableFeature(FeatureFlag.HttpResponseBodyCapture);
        }
        return this;
    }
    
    public NewRelic withApplicationVersion(final String appVersion) {
        if (appVersion != null) {
            NewRelic.agentConfiguration.setCustomApplicationVersion(appVersion);
        }
        return this;
    }
    
    public NewRelic withApplicationFramework(final ApplicationPlatform applicationPlatform) {
        if (applicationPlatform != null) {
            NewRelic.agentConfiguration.setApplicationPlatform(applicationPlatform);
        }
        return this;
    }
    
    @Deprecated
    public NewRelic withAnalyticsEvents(final boolean enabled) {
        enableFeature(FeatureFlag.AnalyticsEvents);
        return this;
    }
    
    public NewRelic withInteractionTracing(final boolean enabled) {
        if (enabled) {
            enableFeature(FeatureFlag.InteractionTracing);
        }
        else {
            disableFeature(FeatureFlag.InteractionTracing);
        }
        return this;
    }
    
    public NewRelic withDefaultInteractions(final boolean enabled) {
        if (enabled) {
            enableFeature(FeatureFlag.DefaultInteractions);
        }
        else {
            disableFeature(FeatureFlag.DefaultInteractions);
        }
        return this;
    }
    
    public static void enableFeature(final FeatureFlag featureFlag) {
        FeatureFlag.enableFeature(featureFlag);
    }
    
    public static void disableFeature(final FeatureFlag featureFlag) {
        NewRelic.log.debug("Disable feature: " + featureFlag.name());
        FeatureFlag.disableFeature(featureFlag);
    }
    
    @Deprecated
    public NewRelic withBuildIdentifier(final String buildId) {
        StatsEngine.get().inc("Supportability/AgentHealth/Deprecated/WithBuildIdentifier");
        return this.withApplicationBuild(buildId);
    }
    
    public NewRelic withApplicationBuild(final String buildId) {
        if (!TextUtils.isEmpty((CharSequence)buildId)) {
            NewRelic.agentConfiguration.setCustomBuildIdentifier(buildId);
        }
        return this;
    }
    
    public void start(final Context context) {
        if (NewRelic.started) {
            NewRelic.log.debug("NewRelic is already running.");
            return;
        }
        try {
            AgentLogManager.setAgentLog(this.loggingEnabled ? new AndroidAgentLog() : new NullAgentLog());
            NewRelic.log.setLevel(this.logLevel);
            if (!this.isInstrumented()) {
                NewRelic.log.error("Failed to detect New Relic instrumentation.  Something likely went wrong during your build process and you should visit http://support.newrelic.com.");
                return;
            }
            AndroidAgentImpl.init(context, NewRelic.agentConfiguration);
            NewRelic.started = true;
        }
        catch (Throwable e) {
            NewRelic.log.error("Error occurred while starting the New Relic agent!", e);
        }
    }
    
    public static boolean isStarted() {
        return NewRelic.started;
    }
    
    @Deprecated
    public static void shutdown() {
        StatsEngine.get().inc("Supportability/AgentHealth/Deprecated/Shutdown");
        if (NewRelic.started) {
            try {
                Agent.getImpl().stop();
            }
            finally {
                Agent.setImpl(NullAgentImpl.instance);
                NewRelic.started = false;
            }
        }
    }
    
    private boolean isInstrumented() {
        NewRelic.log.info("isInstrumented: checking for Mono instrumentation flag - " + Agent.getMonoInstrumentationFlag());
        return Agent.getMonoInstrumentationFlag().equals("YES");
    }
    
    public static String startInteraction(final String actionName) {
        checkNull(actionName, "startInteraction: actionName must be an action/method name.");
        NewRelic.log.debug("NewRelic.startInteraction invoked. actionName: " + actionName);
        TraceMachine.startTracing(actionName.replace("/", "."), true, FeatureFlag.featureEnabled(FeatureFlag.InteractionTracing));
        try {
            return TraceMachine.getActivityTrace().getId();
        }
        catch (TracingInactiveException e) {
            return null;
        }
    }
    
    @Deprecated
    public static String startInteraction(final Context activityContext, final String actionName) {
        checkNull(activityContext, "startInteraction: context must be an Activity instance.");
        checkNull(actionName, "startInteraction: actionName must be an action/method name.");
        TraceMachine.startTracing(activityContext.getClass().getSimpleName() + "#" + actionName.replace("/", "."), false, FeatureFlag.featureEnabled(FeatureFlag.InteractionTracing));
        try {
            return TraceMachine.getActivityTrace().getId();
        }
        catch (TracingInactiveException e) {
            return null;
        }
    }
    
    @Deprecated
    public static String startInteraction(final Context context, final String actionName, final boolean cancelRunningTrace) {
        if (TraceMachine.isTracingActive() && !cancelRunningTrace) {
            NewRelic.log.warning("startInteraction: An interaction is already being traced, and invalidateActiveTrace is false. This interaction will not be traced.");
            return null;
        }
        return startInteraction(context, actionName);
    }
    
    public static void endInteraction(final String id) {
        NewRelic.log.debug("NewRelic.endInteraction invoked. id: " + id);
        TraceMachine.endTrace(id);
    }
    
    public static void setInteractionName(final String name) {
        TraceMachine.setRootDisplayName(name);
    }
    
    public static void startMethodTrace(final String actionName) {
        checkNull(actionName, "startMethodTrace: actionName must be an action/method name.");
        TraceMachine.enterMethod(actionName);
    }
    
    public static void endMethodTrace() {
        NewRelic.log.debug("NewRelic.endMethodTrace invoked.");
        TraceMachine.exitMethod();
    }
    
    public static void recordMetric(final String name, final String category, final int count, final double totalValue, final double exclusiveValue) {
        recordMetric(name, category, count, totalValue, exclusiveValue, null, null);
    }
    
    public static void recordMetric(final String name, final String category, final int count, final double totalValue, final double exclusiveValue, final MetricUnit countUnit, final MetricUnit valueUnit) {
        NewRelic.log.debug("NewRelic.recordMeric invoked for name " + name + ", category: " + category + ", count: " + count + ", totalValue " + totalValue + ", exclusiveValue: " + exclusiveValue + ", countUnit: " + countUnit + ", valueUnit: " + valueUnit);
        checkNull(category, "recordMetric: category must not be null. If no MetricCategory is applicable, use MetricCategory.NONE.");
        checkEmpty(name, "recordMetric: name must not be empty.");
        if (!checkNegative(count, "recordMetric: count must not be negative.")) {
            Measurements.addCustomMetric(name, category, count, totalValue, exclusiveValue, countUnit, valueUnit);
        }
    }
    
    public static void recordMetric(final String name, final String category, final double value) {
        recordMetric(name, category, 1, value, value, null, null);
    }
    
    public static void recordMetric(final String name, final String category) {
        recordMetric(name, category, 1.0);
    }
    
    public static void noticeHttpTransaction(final String url, final String httpMethod, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived) {
        _noticeHttpTransaction(url, httpMethod, statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, null, null, null);
    }
    
    public static void noticeHttpTransaction(final String url, final String httpMethod, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody) {
        _noticeHttpTransaction(url, httpMethod, statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, null, null);
    }
    
    public static void noticeHttpTransaction(final String url, final String httpMethod, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody, final Map<String, String> params) {
        _noticeHttpTransaction(url, httpMethod, statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, params, null);
    }
    
    public static void noticeHttpTransaction(final String url, final String httpMethod, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody, final Map<String, String> params, final String appData) {
        _noticeHttpTransaction(url, httpMethod, statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, params, appData);
    }
    
    public static void noticeHttpTransaction(final String url, final String httpMethod, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody, final Map<String, String> params, final HttpResponse httpResponse) {
        if (httpResponse != null) {
            final Header header = httpResponse.getFirstHeader("X-NewRelic-ID");
            if (header != null && header.getValue() != null && header.getValue().length() > 0) {
                _noticeHttpTransaction(url, httpMethod, statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, params, header.getValue());
                return;
            }
        }
        _noticeHttpTransaction(url, httpMethod, statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, params, null);
    }
    
    public static void noticeHttpTransaction(final String url, final String httpMethod, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody, final Map<String, String> params, final URLConnection urlConnection) {
        if (urlConnection != null) {
            final String header = urlConnection.getHeaderField("X-NewRelic-ID");
            if (header != null && header.length() > 0) {
                _noticeHttpTransaction(url, httpMethod, statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, params, header);
                return;
            }
        }
        _noticeHttpTransaction(url, httpMethod, statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, params, null);
    }
    
    @Deprecated
    public static void noticeHttpTransaction(final String url, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody, final Map<String, String> params, final HttpResponse httpResponse) {
        noticeHttpTransaction(url, "unknown", statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, params, httpResponse);
    }
    
    @Deprecated
    public static void noticeHttpTransaction(final String url, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody, final Map<String, String> params, final URLConnection urlConnection) {
        noticeHttpTransaction(url, "unknown", statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, params, urlConnection);
    }
    
    @Deprecated
    public static void noticeHttpTransaction(final String url, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived) {
        _noticeHttpTransaction(url, "unknown", statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, null, null, null);
    }
    
    @Deprecated
    public static void noticeHttpTransaction(final String url, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody) {
        _noticeHttpTransaction(url, "unknown", statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, null, null);
    }
    
    @Deprecated
    public static void noticeHttpTransaction(final String url, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody, final Map<String, String> params) {
        _noticeHttpTransaction(url, "unknown", statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, params, null);
    }
    
    @Deprecated
    public static void noticeHttpTransaction(final String url, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody, final Map<String, String> params, final String appData) {
        _noticeHttpTransaction(url, "unknown", statusCode, startTimeMs, endTimeMs, bytesSent, bytesReceived, responseBody, params, appData);
    }
    
    protected static void _noticeHttpTransaction(final String url, final String httpMethod, final int statusCode, final long startTimeMs, final long endTimeMs, final long bytesSent, final long bytesReceived, final String responseBody, final Map<String, String> params, final String appData) {
        checkEmpty(url, "noticeHttpTransaction: url must not be empty.");
        checkEmpty(httpMethod, "noticeHttpTransaction: httpMethod must not be empty.");
        try {
            new URL(url);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException("noticeHttpTransaction: URL is malformed: " + url);
        }
        double totalTime = endTimeMs - startTimeMs;
        if (!checkNegative((int)totalTime, "noticeHttpTransaction: the startTimeMs is later than the endTimeMs, resulting in a negative total time.")) {
            totalTime /= 1000.0;
            TaskQueue.queue(new HttpTransactionMeasurement(url, httpMethod, statusCode, 0, startTimeMs, totalTime, bytesSent, bytesReceived, appData));
            if (statusCode >= 400L) {
                Measurements.addHttpError(url, httpMethod, statusCode, responseBody, params);
            }
        }
    }
    
    private static void noticeNetworkFailureDelegate(final String url, final String httpMethod, final long startTime, final long endTime, final NetworkFailure failure, final String message) {
        final float durationInSeconds = (endTime - startTime) / 1000.0f;
        final TransactionState ts = new TransactionState();
        TransactionStateUtil.inspectAndInstrument(ts, url, httpMethod);
        ts.setErrorCode(failure.getErrorCode());
        final TransactionData transactionData = ts.end();
        final Map<String, String> params = new TreeMap<String, String>();
        params.put("content_length", "0");
        params.put("content_type", "text/html");
        TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), startTime, durationInSeconds, transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
        if (ts.getErrorCode() != 0) {
            Measurements.addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), message, params);
        }
        else {
            Measurements.addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), message, params);
        }
    }
    
    public static void noticeNetworkFailure(final String url, final String httpMethod, final long startTime, final long endTime, final NetworkFailure failure, final String message) {
        noticeNetworkFailureDelegate(url, httpMethod, startTime, endTime, failure, message);
    }
    
    public static void noticeNetworkFailure(final String url, final String httpMethod, final long startTime, final long endTime, final NetworkFailure failure) {
        noticeNetworkFailure(url, httpMethod, startTime, endTime, failure, "");
    }
    
    public static void noticeNetworkFailure(final String url, final String httpMethod, final long startTime, final long endTime, final Exception e) {
        checkEmpty(url, "noticeHttpException: url must not be empty.");
        final NetworkFailure failure = NetworkFailure.exceptionToNetworkFailure(e);
        noticeNetworkFailure(url, httpMethod, startTime, endTime, failure, e.getMessage());
    }
    
    @Deprecated
    public static void noticeNetworkFailure(final String url, final long startTime, final long endTime, final NetworkFailure failure) {
        noticeNetworkFailure(url, "unknown", startTime, endTime, failure);
    }
    
    @Deprecated
    public static void noticeNetworkFailure(final String url, final long startTime, final long endTime, final Exception e) {
        noticeNetworkFailure(url, "unknown", startTime, endTime, e);
    }
    
    private static void checkNull(final Object object, final String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
    
    private static void checkEmpty(final String string, final String message) {
        checkNull(string, message);
        if (string.length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }
    
    private static boolean checkNegative(final int number, final String message) {
        if (number < 0) {
            NewRelic.log.error(message);
            return true;
        }
        return false;
    }
    
    public static void crashNow() {
        crashNow("This is a demonstration crash courtesy of New Relic");
    }
    
    public static void crashNow(final String message) {
        throw new RuntimeException(message);
    }
    
    public static boolean setAttribute(final String name, final String value) {
        return AnalyticsControllerImpl.getInstance().setAttribute(name, value);
    }
    
    public static boolean setAttribute(final String name, final float value) {
        return AnalyticsControllerImpl.getInstance().setAttribute(name, value);
    }
    
    public static boolean setAttribute(final String name, final boolean value) {
        return AnalyticsControllerImpl.getInstance().setAttribute(name, value);
    }
    
    public static boolean incrementAttribute(final String name) {
        return AnalyticsControllerImpl.getInstance().incrementAttribute(name, 1.0f);
    }
    
    public static boolean incrementAttribute(final String name, final float value) {
        return AnalyticsControllerImpl.getInstance().incrementAttribute(name, value);
    }
    
    public static boolean removeAttribute(final String name) {
        return AnalyticsControllerImpl.getInstance().removeAttribute(name);
    }
    
    public static boolean removeAllAttributes() {
        return AnalyticsControllerImpl.getInstance().removeAllAttributes();
    }
    
    public static boolean setUserId(final String userId) {
        return AnalyticsControllerImpl.getInstance().setAttribute("userId", userId);
    }
    
    public static boolean recordEvent(final String name, final Map<String, Object> eventAttributes) {
        return AnalyticsControllerImpl.getInstance().recordEvent(name, eventAttributes);
    }
    
    public static void setMaxEventPoolSize(final int maxSize) {
        AnalyticsControllerImpl.getInstance().setMaxEventPoolSize(maxSize);
    }
    
    public static void setMaxEventBufferTime(final int maxBufferTimeInSec) {
        AnalyticsControllerImpl.getInstance().setMaxEventBufferTime(maxBufferTimeInSec);
    }
    
    public static String currentSessionId() {
        return NewRelic.agentConfiguration.getSessionID();
    }
    
    static {
        log = AgentLogManager.getAgentLog();
        agentConfiguration = new AgentConfiguration();
        NewRelic.started = false;
    }
}
