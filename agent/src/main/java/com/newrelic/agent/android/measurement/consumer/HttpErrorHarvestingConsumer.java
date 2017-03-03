// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.consumer;

import com.newrelic.agent.android.harvest.Harvest;
import com.newrelic.agent.android.harvest.HttpError;
import com.newrelic.agent.android.measurement.http.HttpErrorMeasurement;
import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.MeasurementType;

public class HttpErrorHarvestingConsumer extends BaseMeasurementConsumer
{
    public HttpErrorHarvestingConsumer() {
        super(MeasurementType.HttpError);
    }
    
    @Override
    public void consumeMeasurement(final Measurement measurement) {
        final HttpError error = new HttpError((HttpErrorMeasurement)measurement);
        Harvest.addHttpErrorTrace(error);
    }
}
