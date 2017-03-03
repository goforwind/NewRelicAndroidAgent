// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.analytics;

import com.newrelic.agent.android.logging.AgentLogManager;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import com.newrelic.agent.android.logging.AgentLog;

public class EventManagerImpl implements EventManager
{
    private static final AgentLog log;
    public static int DEFAULT_MAX_EVENT_BUFFER_TIME;
    public static int DEFAULT_MAX_EVENT_BUFFER_SIZE;
    private List<AnalyticsEvent> events;
    private int maxEventPoolSize;
    private int maxBufferTimeInSec;
    private long firstEventTimestamp;
    private AtomicBoolean initialized;
    private AtomicInteger eventsRecorded;
    private AtomicInteger eventsEjected;
    
    public EventManagerImpl() {
        this(EventManagerImpl.DEFAULT_MAX_EVENT_BUFFER_SIZE, EventManagerImpl.DEFAULT_MAX_EVENT_BUFFER_TIME);
    }
    
    public EventManagerImpl(final int maxEventPoolSize, final int maxBufferTimeInSec) {
        this.initialized = new AtomicBoolean(false);
        this.eventsRecorded = new AtomicInteger(0);
        this.eventsEjected = new AtomicInteger(0);
        this.events = Collections.synchronizedList(new ArrayList<AnalyticsEvent>(maxEventPoolSize));
        this.maxBufferTimeInSec = maxBufferTimeInSec;
        this.maxEventPoolSize = maxEventPoolSize;
        this.firstEventTimestamp = 0L;
        this.eventsRecorded.set(0);
        this.eventsEjected.set(0);
    }
    
    @Override
    public void initialize() {
        if (!this.initialized.compareAndSet(false, true)) {
            EventManagerImpl.log.verbose("EventManagerImpl has already been initialized.  Bypassing...");
            return;
        }
        this.firstEventTimestamp = 0L;
        this.eventsRecorded.set(0);
        this.eventsEjected.set(0);
        this.empty();
    }
    
    @Override
    public void shutdown() {
        this.initialized.set(false);
    }
    
    @Override
    public int size() {
        return this.events.size();
    }
    
    @Override
    public void empty() {
        this.events.clear();
        this.firstEventTimestamp = 0L;
    }
    
    @Override
    public boolean isTransmitRequired() {
        return (!this.initialized.get() && this.events.size() > 0) || this.isMaxEventBufferTimeExceeded();
    }
    
    @Override
    public boolean addEvent(final AnalyticsEvent event) {
        final int eventsRecorded = this.eventsRecorded.incrementAndGet();
        if (this.events.size() == 0) {
            EventManagerImpl.log.verbose("EventManagerImpl.addEvent - Queue is currently empty, setting first event timestamp to " + System.currentTimeMillis());
            this.firstEventTimestamp = System.currentTimeMillis();
        }
        if (this.events.size() >= this.maxEventPoolSize) {
            this.eventsEjected.incrementAndGet();
            final int index = (int)(Math.random() * eventsRecorded);
            if (index >= this.maxEventPoolSize) {
                return true;
            }
            this.events.remove(index);
        }
        return this.events.add(event);
    }
    
    @Override
    public int getEventsRecorded() {
        return this.eventsRecorded.get();
    }
    
    @Override
    public int getEventsEjected() {
        return this.eventsEjected.get();
    }
    
    @Override
    public boolean isMaxEventBufferTimeExceeded() {
        return this.firstEventTimestamp > 0L && System.currentTimeMillis() - this.firstEventTimestamp > this.maxBufferTimeInSec * 1000;
    }
    
    @Override
    public boolean isMaxEventPoolSizeExceeded() {
        return this.events.size() > this.maxEventPoolSize;
    }
    
    @Override
    public int getMaxEventPoolSize() {
        return this.maxEventPoolSize;
    }
    
    @Override
    public void setMaxEventPoolSize(final int maxSize) {
        this.maxEventPoolSize = maxSize;
    }
    
    @Override
    public void setMaxEventBufferTime(final int maxBufferTimeInSec) {
        this.maxBufferTimeInSec = maxBufferTimeInSec;
    }
    
    @Override
    public int getMaxEventBufferTime() {
        return this.maxBufferTimeInSec;
    }
    
    @Override
    public Collection<AnalyticsEvent> getQueuedEvents() {
        return Collections.unmodifiableCollection((Collection<? extends AnalyticsEvent>)this.events);
    }
    
    static {
        log = AgentLogManager.getAgentLog();
        EventManagerImpl.DEFAULT_MAX_EVENT_BUFFER_TIME = 600;
        EventManagerImpl.DEFAULT_MAX_EVENT_BUFFER_SIZE = 1000;
    }
}
