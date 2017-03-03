// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import com.newrelic.agent.android.harvest.type.Harvestable;
import java.util.Collection;

public class HarvestableCache
{
    private static final int DEFAULT_CACHE_LIMIT = 1024;
    private int limit;
    private final Collection<Harvestable> cache;
    
    public HarvestableCache() {
        this.limit = 1024;
        this.cache = this.getNewCache();
    }
    
    protected Collection<Harvestable> getNewCache() {
        return new CopyOnWriteArrayList<Harvestable>();
    }
    
    public void add(final Harvestable harvestable) {
        if (harvestable == null || this.cache.size() >= this.limit) {
            return;
        }
        this.cache.add(harvestable);
    }
    
    public boolean get(final Object h) {
        return this.cache.contains(h);
    }
    
    public Collection<Harvestable> flush() {
        if (this.cache.size() == 0) {
            return (Collection<Harvestable>)Collections.emptyList();
        }
        synchronized (this) {
            final Collection<Harvestable> oldCache = this.getNewCache();
            oldCache.addAll(this.cache);
            this.cache.clear();
            return oldCache;
        }
    }
    
    public int getSize() {
        return this.cache.size();
    }
    
    public void setLimit(final int limit) {
        this.limit = limit;
    }
}
