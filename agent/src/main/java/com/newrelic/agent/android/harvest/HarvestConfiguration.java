// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import com.newrelic.agent.android.activity.config.ActivityTraceConfiguration;

public class HarvestConfiguration
{
    private static final int DEFAULT_ACTIVITY_TRACE_LENGTH = 65534;
    private static final int DEFAULT_ACTIVITY_TRACE_MAX_REPORT_ATTEMPTS = 1;
    private static final int DEFAULT_REPORT_PERIOD = 60;
    private static final int DEFAULT_ERROR_LIMIT = 50;
    private static final int DEFAULT_RESPONSE_BODY_LIMIT = 2048;
    private static final int DEFAULT_STACK_TRACE_LIMIT = 100;
    private static final int DEFAULT_MAX_TRANSACTION_AGE = 600;
    private static final int DEFAULT_MAX_TRANSACTION_COUNT = 1000;
    private static final float DEFAULT_ACTIVITY_TRACE_MIN_UTILIZATION = 0.3f;
    private boolean collect_network_errors;
    private String cross_process_id;
    private int data_report_period;
    private int[] data_token;
    private int error_limit;
    private int report_max_transaction_age;
    private int report_max_transaction_count;
    private int response_body_limit;
    private long server_timestamp;
    private int stack_trace_limit;
    private int activity_trace_max_size;
    private int activity_trace_max_report_attempts;
    private double activity_trace_min_utilization;
    private ActivityTraceConfiguration at_capture;
    private static HarvestConfiguration defaultHarvestConfiguration;
    
    public HarvestConfiguration() {
        this.setDefaultValues();
    }
    
    public void setDefaultValues() {
        this.setData_token(new int[2]);
        this.setCollect_network_errors(true);
        this.setData_report_period(60);
        this.setError_limit(50);
        this.setResponse_body_limit(2048);
        this.setStack_trace_limit(100);
        this.setReport_max_transaction_age(600);
        this.setReport_max_transaction_count(1000);
        this.setActivity_trace_max_size(65534);
        this.setActivity_trace_max_report_attempts(1);
        this.setActivity_trace_min_utilization(0.30000001192092896);
        this.setAt_capture(ActivityTraceConfiguration.defaultActivityTraceConfiguration());
    }
    
    public static HarvestConfiguration getDefaultHarvestConfiguration() {
        if (HarvestConfiguration.defaultHarvestConfiguration != null) {
            return HarvestConfiguration.defaultHarvestConfiguration;
        }
        return HarvestConfiguration.defaultHarvestConfiguration = new HarvestConfiguration();
    }
    
    public void reconfigure(final HarvestConfiguration configuration) {
        this.setCollect_network_errors(configuration.isCollect_network_errors());
        if (configuration.getCross_process_id() != null) {
            this.setCross_process_id(configuration.getCross_process_id());
        }
        this.setData_report_period(configuration.getData_report_period());
        if (configuration.getDataToken().isValid()) {
            this.setData_token(configuration.getData_token());
        }
        this.setError_limit(configuration.getError_limit());
        this.setReport_max_transaction_age(configuration.getReport_max_transaction_age());
        this.setReport_max_transaction_count(configuration.getReport_max_transaction_count());
        this.setResponse_body_limit(configuration.getResponse_body_limit());
        this.setServer_timestamp(configuration.getServer_timestamp());
        this.setStack_trace_limit(configuration.getStack_trace_limit());
        this.setActivity_trace_min_utilization(configuration.getActivity_trace_min_utilization());
        this.setActivity_trace_max_report_attempts(configuration.getActivity_trace_max_report_attempts());
        if (configuration.getAt_capture() != null) {
            this.setAt_capture(configuration.getAt_capture());
        }
    }
    
    public void setCollect_network_errors(final boolean collect_network_errors) {
        this.collect_network_errors = collect_network_errors;
    }
    
    public void setCross_process_id(final String cross_process_id) {
        this.cross_process_id = cross_process_id;
    }
    
    public void setData_report_period(final int data_report_period) {
        this.data_report_period = data_report_period;
    }
    
    public void setData_token(final int[] data_token) {
        this.data_token = data_token;
    }
    
    public DataToken getDataToken() {
        if (this.data_token == null) {
            return null;
        }
        return new DataToken(this.data_token[0], this.data_token[1]);
    }
    
    public void setError_limit(final int error_limit) {
        this.error_limit = error_limit;
    }
    
    public void setReport_max_transaction_age(final int report_max_transaction_age) {
        this.report_max_transaction_age = report_max_transaction_age;
    }
    
    public void setReport_max_transaction_count(final int report_max_transaction_count) {
        this.report_max_transaction_count = report_max_transaction_count;
    }
    
    public void setResponse_body_limit(final int response_body_limit) {
        this.response_body_limit = response_body_limit;
    }
    
    public void setServer_timestamp(final long server_timestamp) {
        this.server_timestamp = server_timestamp;
    }
    
    public void setStack_trace_limit(final int stack_trace_limit) {
        this.stack_trace_limit = stack_trace_limit;
    }
    
    public void setActivity_trace_max_size(final int activity_trace_max_size) {
        this.activity_trace_max_size = activity_trace_max_size;
    }
    
    public void setActivity_trace_max_report_attempts(final int activity_trace_max_report_attempts) {
        this.activity_trace_max_report_attempts = activity_trace_max_report_attempts;
    }
    
    public boolean isCollect_network_errors() {
        return this.collect_network_errors;
    }
    
    public String getCross_process_id() {
        return this.cross_process_id;
    }
    
    public int getData_report_period() {
        return this.data_report_period;
    }
    
    public int[] getData_token() {
        return this.data_token;
    }
    
    public int getError_limit() {
        return this.error_limit;
    }
    
    public int getReport_max_transaction_age() {
        return this.report_max_transaction_age;
    }
    
    public long getReportMaxTransactionAgeMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(this.report_max_transaction_age, TimeUnit.SECONDS);
    }
    
    public int getReport_max_transaction_count() {
        return this.report_max_transaction_count;
    }
    
    public int getResponse_body_limit() {
        return this.response_body_limit;
    }
    
    public long getServer_timestamp() {
        return this.server_timestamp;
    }
    
    public int getStack_trace_limit() {
        return this.stack_trace_limit;
    }
    
    public int getActivity_trace_max_size() {
        return this.activity_trace_max_size;
    }
    
    public int getActivity_trace_max_report_attempts() {
        return this.activity_trace_max_report_attempts;
    }
    
    public ActivityTraceConfiguration getAt_capture() {
        return this.at_capture;
    }
    
    public void setAt_capture(final ActivityTraceConfiguration at_capture) {
        this.at_capture = at_capture;
    }
    
    public double getActivity_trace_min_utilization() {
        return this.activity_trace_min_utilization;
    }
    
    public void setActivity_trace_min_utilization(final double activity_trace_min_utilization) {
        this.activity_trace_min_utilization = activity_trace_min_utilization;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final HarvestConfiguration that = (HarvestConfiguration)o;
        if (this.collect_network_errors != that.collect_network_errors) {
            return false;
        }
        if (this.data_report_period != that.data_report_period) {
            return false;
        }
        if (this.error_limit != that.error_limit) {
            return false;
        }
        if (this.report_max_transaction_age != that.report_max_transaction_age) {
            return false;
        }
        if (this.report_max_transaction_count != that.report_max_transaction_count) {
            return false;
        }
        if (this.response_body_limit != that.response_body_limit) {
            return false;
        }
        if (this.stack_trace_limit != that.stack_trace_limit) {
            return false;
        }
        if (this.activity_trace_max_size != that.activity_trace_max_size) {
            return false;
        }
        if (this.activity_trace_max_report_attempts != that.activity_trace_max_report_attempts) {
            return false;
        }
        if (this.cross_process_id == null && that.cross_process_id != null) {
            return false;
        }
        if (this.cross_process_id != null && that.cross_process_id == null) {
            return false;
        }
        if (this.cross_process_id != null && !this.cross_process_id.equals(that.cross_process_id)) {
            return false;
        }
        final int thisMinUtil = (int)this.activity_trace_min_utilization * 100;
        final int thatMinUtil = (int)that.activity_trace_min_utilization * 100;
        if (thisMinUtil != thatMinUtil) {
            return false;
        }
        final boolean dataTokenEqual = Arrays.equals(this.data_token, that.data_token);
        return dataTokenEqual;
    }
    
    @Override
    public int hashCode() {
        int result = this.collect_network_errors ? 1 : 0;
        result = 31 * result + ((this.cross_process_id != null) ? this.cross_process_id.hashCode() : 0);
        result = 31 * result + this.data_report_period;
        result = 31 * result + ((this.data_token != null) ? Arrays.hashCode(this.data_token) : 0);
        result = 31 * result + this.error_limit;
        result = 31 * result + this.report_max_transaction_age;
        result = 31 * result + this.report_max_transaction_count;
        result = 31 * result + this.response_body_limit;
        result = 31 * result + this.stack_trace_limit;
        result = 31 * result + this.activity_trace_max_size;
        result = 31 * result + this.activity_trace_max_report_attempts;
        final long temp = Double.doubleToLongBits(this.activity_trace_min_utilization);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        result = 31 * result + ((this.at_capture != null) ? this.at_capture.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "HarvestConfiguration{collect_network_errors=" + this.collect_network_errors + ", cross_process_id='" + this.cross_process_id + '\'' + ", data_report_period=" + this.data_report_period + ", data_token=" + Arrays.toString(this.data_token) + ", error_limit=" + this.error_limit + ", report_max_transaction_age=" + this.report_max_transaction_age + ", report_max_transaction_count=" + this.report_max_transaction_count + ", response_body_limit=" + this.response_body_limit + ", server_timestamp=" + this.server_timestamp + ", stack_trace_limit=" + this.stack_trace_limit + ", activity_trace_max_size=" + this.activity_trace_max_size + ", activity_trace_max_report_attempts=" + this.activity_trace_max_report_attempts + ", activity_trace_min_utilization=" + this.activity_trace_min_utilization + ", at_capture=" + this.at_capture + '}';
    }
}
