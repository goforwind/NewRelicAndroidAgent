// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.measurement.producer;

import com.newrelic.agent.android.measurement.Measurement;
import com.newrelic.agent.android.measurement.CustomMetricMeasurement;
import com.newrelic.agent.android.metric.MetricUnit;
import com.newrelic.agent.android.measurement.MeasurementType;

public class CustomMetricProducer extends BaseMeasurementProducer
{
    private static final String FILTER_REGEX = "[/\\[\\]|*]";
    
    public CustomMetricProducer() {
        super(MeasurementType.Custom);
    }
    
    public void produceMeasurement(final String name, final String category, final int count, final double totalValue, final double exclusiveValue) {
        this.produceMeasurement(category, name, count, totalValue, exclusiveValue, null, null);
    }
    
    public void produceMeasurement(final String name, final String category, final int count, final double totalValue, final double exclusiveValue, final MetricUnit countUnit, final MetricUnit valueUnit) {
        final String metricName = this.createMetricName(name, category, countUnit, valueUnit);
        final CustomMetricMeasurement custom = new CustomMetricMeasurement(metricName, count, totalValue, exclusiveValue);
        this.produceMeasurement(custom);
    }
    
    private String createMetricName(final String name, final String category, final MetricUnit countUnit, final MetricUnit valueUnit) {
        final StringBuffer metricName = new StringBuffer();
        metricName.append(category.replaceAll("[/\\[\\]|*]", ""));
        metricName.append("/");
        metricName.append(name.replaceAll("[/\\[\\]|*]", ""));
        if (countUnit != null || valueUnit != null) {
            metricName.append("[");
            if (valueUnit != null) {
                metricName.append(valueUnit.getLabel());
            }
            if (countUnit != null) {
                metricName.append("|");
                metricName.append(countUnit.getLabel());
            }
            metricName.append("]");
        }
        return metricName.toString();
    }
}
