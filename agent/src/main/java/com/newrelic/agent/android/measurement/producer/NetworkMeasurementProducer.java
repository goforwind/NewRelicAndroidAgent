// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.producer;

import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;
import com.newrelic.agent.android.util.Util;
import com.newrelic.agent.android.measurement.MeasurementType;

public class NetworkMeasurementProducer extends BaseMeasurementProducer
{
    public NetworkMeasurementProducer() {
        super(MeasurementType.Network);
    }
    
    public void produceMeasurement(final String urlString, final String httpMethod, final int statusCode, final int errorCode, final long startTime, final double totalTime, final long bytesSent, final long bytesReceived, final String appData) {
        final String url = Util.sanitizeUrl(urlString);
        if (url == null) {
            return;
        }
        this.produceMeasurement(new HttpTransactionMeasurement(url, httpMethod, statusCode, errorCode, startTime, totalTime, bytesSent, bytesReceived, appData));
    }
    
    public void produceMeasurement(final HttpTransactionMeasurement transactionMeasurement) {
        final String url = Util.sanitizeUrl(transactionMeasurement.getUrl());
        if (url == null) {
            return;
        }
        transactionMeasurement.setUrl(url);
        super.produceMeasurement(transactionMeasurement);
    }
}
