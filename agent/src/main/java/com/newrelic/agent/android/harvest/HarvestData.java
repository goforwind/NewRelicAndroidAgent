// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Iterator;
import com.newrelic.com.google.gson.JsonObject;
import com.newrelic.agent.android.stats.StatsEngine;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.JsonPrimitive;
import com.newrelic.com.google.gson.JsonArray;
import java.util.HashSet;
import com.newrelic.agent.android.Agent;
import com.newrelic.agent.android.analytics.AnalyticsEvent;
import java.util.Collection;
import com.newrelic.agent.android.analytics.AnalyticAttribute;
import java.util.Set;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class HarvestData extends HarvestableArray
{
    private static final AgentLog log;
    private DataToken dataToken;
    private DeviceInformation deviceInformation;
    private double harvestTimeDelta;
    private HttpTransactions httpTransactions;
    private MachineMeasurements machineMeasurements;
    private HttpErrors httpErrors;
    private ActivityTraces activityTraces;
    private AgentHealth agentHealth;
    private Set<AnalyticAttribute> sessionAttributes;
    private Collection<AnalyticsEvent> analyticsEvents;
    private boolean analyticsEnabled;
    
    public HarvestData() {
        this.dataToken = new DataToken();
        this.httpTransactions = new HttpTransactions();
        this.httpErrors = new HttpErrors();
        this.activityTraces = new ActivityTraces();
        this.machineMeasurements = new MachineMeasurements();
        this.deviceInformation = Agent.getDeviceInformation();
        this.agentHealth = new AgentHealth();
        this.sessionAttributes = new HashSet<AnalyticAttribute>();
        this.analyticsEvents = new HashSet<AnalyticsEvent>();
        this.analyticsEnabled = false;
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        array.add(this.dataToken.asJson());
        array.add(this.deviceInformation.asJson());
        array.add(new JsonPrimitive(this.harvestTimeDelta));
        array.add(this.httpTransactions.asJson());
        array.add(this.machineMeasurements.asJson());
        array.add(this.httpErrors.asJson());
        final JsonElement activityTracesElement = this.activityTraces.asJson();
        final String activityTraceJson = activityTracesElement.toString();
        if (activityTraceJson.length() < Harvest.getHarvestConfiguration().getActivity_trace_max_size()) {
            array.add(activityTracesElement);
        }
        else {
            StatsEngine.get().sample("Supportability/AgentHealth/BigActivityTracesDropped", activityTraceJson.length());
        }
        array.add(this.agentHealth.asJson());
        if (this.analyticsEnabled) {
            final JsonObject sessionAttrObj = new JsonObject();
            for (final AnalyticAttribute attribute : this.sessionAttributes) {
                switch (attribute.getAttributeDataType()) {
                    case STRING: {
                        sessionAttrObj.addProperty(attribute.getName(), attribute.getStringValue());
                        continue;
                    }
                    case FLOAT: {
                        sessionAttrObj.addProperty(attribute.getName(), attribute.getFloatValue());
                        continue;
                    }
                    case BOOLEAN: {
                        sessionAttrObj.addProperty(attribute.getName(), Boolean.valueOf(attribute.getBooleanValue()));
                        continue;
                    }
                }
            }
            array.add(sessionAttrObj);
            final JsonArray events = new JsonArray();
            for (final AnalyticsEvent event : this.analyticsEvents) {
                events.add(event.asJsonObject());
            }
            array.add(events);
        }
        return array;
    }
    
    public boolean isValid() {
        return this.dataToken.isValid();
    }
    
    public void reset() {
        this.httpErrors.clear();
        this.httpTransactions.clear();
        this.activityTraces.clear();
        this.machineMeasurements.clear();
        this.agentHealth.clear();
        this.sessionAttributes.clear();
        this.analyticsEvents.clear();
    }
    
    public void setDataToken(final DataToken dataToken) {
        if (dataToken == null) {
            return;
        }
        this.dataToken = dataToken;
    }
    
    public void setDeviceInformation(final DeviceInformation deviceInformation) {
        this.deviceInformation = deviceInformation;
    }
    
    public void setHarvestTimeDelta(final double harvestTimeDelta) {
        this.harvestTimeDelta = harvestTimeDelta;
    }
    
    public void setHttpTransactions(final HttpTransactions httpTransactions) {
        this.httpTransactions = httpTransactions;
    }
    
    public void setMachineMeasurements(final MachineMeasurements machineMeasurements) {
        this.machineMeasurements = machineMeasurements;
    }
    
    public void setActivityTraces(final ActivityTraces activityTraces) {
        this.activityTraces = activityTraces;
    }
    
    public void setHttpErrors(final HttpErrors httpErrors) {
        this.httpErrors = httpErrors;
    }
    
    public Set<AnalyticAttribute> getSessionAttributes() {
        return this.sessionAttributes;
    }
    
    public void setSessionAttributes(final Set<AnalyticAttribute> sessionAttributes) {
        HarvestData.log.debug("HarvestData.setSessionAttributes invoked with attribute set " + sessionAttributes);
        this.sessionAttributes = new HashSet<AnalyticAttribute>(sessionAttributes);
    }
    
    public Collection<AnalyticsEvent> getAnalyticsEvents() {
        return this.analyticsEvents;
    }
    
    public void setAnalyticsEvents(final Collection<AnalyticsEvent> analyticsEvents) {
        this.analyticsEvents = new HashSet<AnalyticsEvent>(analyticsEvents);
    }
    
    public DeviceInformation getDeviceInformation() {
        return this.deviceInformation;
    }
    
    public HttpErrors getHttpErrors() {
        return this.httpErrors;
    }
    
    public HttpTransactions getHttpTransactions() {
        return this.httpTransactions;
    }
    
    public MachineMeasurements getMetrics() {
        return this.machineMeasurements;
    }
    
    public ActivityTraces getActivityTraces() {
        return this.activityTraces;
    }
    
    public AgentHealth getAgentHealth() {
        return this.agentHealth;
    }
    
    public DataToken getDataToken() {
        return this.dataToken;
    }
    
    public boolean isAnalyticsEnabled() {
        return this.analyticsEnabled;
    }
    
    public void setAnalyticsEnabled(final boolean analyticsEnabled) {
        this.analyticsEnabled = analyticsEnabled;
    }
    
    @Override
    public String toString() {
        return "HarvestData{\n\tdataToken=" + this.dataToken + ", \n\tdeviceInformation=" + this.deviceInformation + ", \n\tharvestTimeDelta=" + this.harvestTimeDelta + ", \n\thttpTransactions=" + this.httpTransactions + ", \n\tmachineMeasurements=" + this.machineMeasurements + ", \n\thttpErrors=" + this.httpErrors + ", \n\tactivityTraces=" + this.activityTraces + ", \n\tsessionAttributes=" + this.sessionAttributes + ", \n\tanalyticAttributes=" + this.analyticsEvents + '}';
    }
    
    static {
        log = AgentLogManager.getAgentLog();
    }
}
