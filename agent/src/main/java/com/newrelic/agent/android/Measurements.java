// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android;

import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.tracing.Trace;
import com.newrelic.agent.android.activity.MeasuredActivity;
import com.newrelic.agent.android.metric.MetricUnit;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import com.newrelic.agent.android.measurement.ThreadInfo;
import java.util.Map;
import com.newrelic.agent.android.harvest.Harvest;
import com.newrelic.agent.android.measurement.consumer.MeasurementConsumer;
import com.newrelic.agent.android.measurement.producer.MeasurementProducer;
import com.newrelic.agent.android.measurement.consumer.CustomMetricConsumer;
import com.newrelic.agent.android.measurement.consumer.SummaryMetricMeasurementConsumer;
import com.newrelic.agent.android.measurement.consumer.MethodMeasurementConsumer;
import com.newrelic.agent.android.measurement.consumer.ActivityMeasurementConsumer;
import com.newrelic.agent.android.measurement.consumer.HttpTransactionHarvestingConsumer;
import com.newrelic.agent.android.measurement.consumer.HttpErrorHarvestingConsumer;
import com.newrelic.agent.android.measurement.producer.CustomMetricProducer;
import com.newrelic.agent.android.measurement.producer.MethodMeasurementProducer;
import com.newrelic.agent.android.measurement.producer.ActivityMeasurementProducer;
import com.newrelic.agent.android.measurement.producer.NetworkMeasurementProducer;
import com.newrelic.agent.android.measurement.producer.HttpErrorMeasurementProducer;
import com.newrelic.agent.android.logging.AgentLog;

public class Measurements
{
    private static final AgentLog log;
    private static final MeasurementEngine measurementEngine;
    private static final HttpErrorMeasurementProducer httpErrorMeasurementProducer;
    private static final NetworkMeasurementProducer networkMeasurementProducer;
    private static final ActivityMeasurementProducer activityMeasurementProducer;
    private static final MethodMeasurementProducer methodMeasurementProducer;
    private static final CustomMetricProducer customMetricProducer;
    private static final HttpErrorHarvestingConsumer httpErrorHarvester;
    private static final HttpTransactionHarvestingConsumer httpTransactionHarvester;
    private static final ActivityMeasurementConsumer activityConsumer;
    private static final MethodMeasurementConsumer methodMeasurementConsumer;
    private static final SummaryMetricMeasurementConsumer summaryMetricMeasurementConsumer;
    private static final CustomMetricConsumer customMetricConsumer;
    private static boolean broadcastNewMeasurements;
    
    public static void initialize() {
        Measurements.log.info("Measurement Engine initialized.");
        TaskQueue.start();
        addMeasurementProducer(Measurements.httpErrorMeasurementProducer);
        addMeasurementProducer(Measurements.networkMeasurementProducer);
        addMeasurementProducer(Measurements.activityMeasurementProducer);
        addMeasurementProducer(Measurements.methodMeasurementProducer);
        addMeasurementProducer(Measurements.customMetricProducer);
        addMeasurementConsumer(Measurements.httpErrorHarvester);
        addMeasurementConsumer(Measurements.httpTransactionHarvester);
        addMeasurementConsumer(Measurements.activityConsumer);
        addMeasurementConsumer(Measurements.methodMeasurementConsumer);
        addMeasurementConsumer(Measurements.summaryMetricMeasurementConsumer);
        addMeasurementConsumer(Measurements.customMetricConsumer);
    }
    
    public static void shutdown() {
        TaskQueue.stop();
        Measurements.measurementEngine.clear();
        Measurements.log.info("Measurement Engine shutting down.");
        removeMeasurementProducer(Measurements.httpErrorMeasurementProducer);
        removeMeasurementProducer(Measurements.networkMeasurementProducer);
        removeMeasurementProducer(Measurements.activityMeasurementProducer);
        removeMeasurementProducer(Measurements.methodMeasurementProducer);
        removeMeasurementProducer(Measurements.customMetricProducer);
        removeMeasurementConsumer(Measurements.httpErrorHarvester);
        removeMeasurementConsumer(Measurements.httpTransactionHarvester);
        removeMeasurementConsumer(Measurements.activityConsumer);
        removeMeasurementConsumer(Measurements.methodMeasurementConsumer);
        removeMeasurementConsumer(Measurements.summaryMetricMeasurementConsumer);
        removeMeasurementConsumer(Measurements.customMetricConsumer);
    }
    
    public static void addHttpError(final String url, final String httpMethod, final int statusCode) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.httpErrorMeasurementProducer.produceMeasurement(url, httpMethod, statusCode);
        newMeasurementBroadcast();
    }
    
    public static void addHttpError(final String url, final String httpMethod, final int statusCode, final String responseBody) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.httpErrorMeasurementProducer.produceMeasurement(url, httpMethod, statusCode, responseBody);
        newMeasurementBroadcast();
    }
    
    public static void addHttpError(final String url, final String httpMethod, final int statusCode, final String responseBody, final Map<String, String> params) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.httpErrorMeasurementProducer.produceMeasurement(url, httpMethod, statusCode, responseBody, params);
        newMeasurementBroadcast();
    }
    
    public static void addHttpError(final String url, final String httpMethod, final int statusCode, final int errorCode, final String responseBody, final Map<String, String> params) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.httpErrorMeasurementProducer.produceMeasurement(url, httpMethod, statusCode, errorCode, responseBody, params, new ThreadInfo());
        newMeasurementBroadcast();
    }
    
    public static void addHttpError(final String url, final String httpMethod, final int statusCode, final String responseBody, final Map<String, String> params, final ThreadInfo threadInfo) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.httpErrorMeasurementProducer.produceMeasurement(url, httpMethod, statusCode, responseBody, params, threadInfo);
        newMeasurementBroadcast();
    }
    
    public static void addHttpTransaction(final HttpTransactionMeasurement transactionMeasurement) {
        if (Harvest.isDisabled()) {
            return;
        }
        if (transactionMeasurement == null) {
            Measurements.log.error("TransactionMeasurement is null. HttpTransactionMeasurement measurement not created.");
        }
        else {
            Measurements.networkMeasurementProducer.produceMeasurement(transactionMeasurement);
            newMeasurementBroadcast();
        }
    }
    
    public static void addHttpError(final TransactionData transactionData, final String responseBody, final Map<String, String> params) {
        if (transactionData == null) {
            Measurements.log.error("TransactionData is null. HttpError measurement not created.");
        }
        else {
            addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), responseBody, params);
        }
    }
    
    public static void addCustomMetric(final String name, final String category, final int count, final double totalValue, final double exclusiveValue) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.customMetricProducer.produceMeasurement(name, category, count, totalValue, exclusiveValue);
        newMeasurementBroadcast();
    }
    
    public static void addCustomMetric(final String name, final String category, final int count, final double totalValue, final double exclusiveValue, final MetricUnit countUnit, final MetricUnit valueUnit) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.customMetricProducer.produceMeasurement(name, category, count, totalValue, exclusiveValue, countUnit, valueUnit);
        newMeasurementBroadcast();
    }
    
    public static void setBroadcastNewMeasurements(final boolean broadcast) {
        Measurements.broadcastNewMeasurements = broadcast;
    }
    
    private static void newMeasurementBroadcast() {
        if (Measurements.broadcastNewMeasurements) {
            broadcast();
        }
    }
    
    public static void broadcast() {
        Measurements.measurementEngine.broadcastMeasurements();
    }
    
    public static MeasuredActivity startActivity(final String activityName) {
        if (Harvest.isDisabled()) {
            return null;
        }
        return Measurements.measurementEngine.startActivity(activityName);
    }
    
    public static void renameActivity(final String oldName, final String newName) {
        Measurements.measurementEngine.renameActivity(oldName, newName);
    }
    
    public static void endActivity(final String activityName) {
        if (Harvest.isDisabled()) {
            return;
        }
        final MeasuredActivity measuredActivity = Measurements.measurementEngine.endActivity(activityName);
        Measurements.activityMeasurementProducer.produceMeasurement(measuredActivity);
        newMeasurementBroadcast();
    }
    
    public static void endActivity(final MeasuredActivity activity) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.measurementEngine.endActivity(activity);
        Measurements.activityMeasurementProducer.produceMeasurement(activity);
        newMeasurementBroadcast();
    }
    
    public static void endActivityWithoutMeasurement(final MeasuredActivity activity) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.measurementEngine.endActivity(activity);
    }
    
    public static void addTracedMethod(final Trace trace) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.methodMeasurementProducer.produceMeasurement(trace);
        newMeasurementBroadcast();
    }
    
    public static void addMeasurementProducer(final MeasurementProducer measurementProducer) {
        Measurements.measurementEngine.addMeasurementProducer(measurementProducer);
    }
    
    public static void removeMeasurementProducer(final MeasurementProducer measurementProducer) {
        Measurements.measurementEngine.removeMeasurementProducer(measurementProducer);
    }
    
    public static void addMeasurementConsumer(final MeasurementConsumer measurementConsumer) {
        Measurements.measurementEngine.addMeasurementConsumer(measurementConsumer);
    }
    
    public static void removeMeasurementConsumer(final MeasurementConsumer measurementConsumer) {
        Measurements.measurementEngine.removeMeasurementConsumer(measurementConsumer);
    }
    
    public static void process() {
        Measurements.measurementEngine.broadcastMeasurements();
    }
    
    static {
        log = AgentLogManager.getAgentLog();
        measurementEngine = new MeasurementEngine();
        httpErrorMeasurementProducer = new HttpErrorMeasurementProducer();
        networkMeasurementProducer = new NetworkMeasurementProducer();
        activityMeasurementProducer = new ActivityMeasurementProducer();
        methodMeasurementProducer = new MethodMeasurementProducer();
        customMetricProducer = new CustomMetricProducer();
        httpErrorHarvester = new HttpErrorHarvestingConsumer();
        httpTransactionHarvester = new HttpTransactionHarvestingConsumer();
        activityConsumer = new ActivityMeasurementConsumer();
        methodMeasurementConsumer = new MethodMeasurementConsumer();
        summaryMetricMeasurementConsumer = new SummaryMetricMeasurementConsumer();
        customMetricConsumer = new CustomMetricConsumer();
        Measurements.broadcastNewMeasurements = true;
    }
}
