// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import com.newrelic.com.google.gson.JsonArray;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Collection;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class HttpErrors extends HarvestableArray
{
    private final Collection<HttpError> httpErrors;
    
    public HttpErrors() {
        this.httpErrors = new CopyOnWriteArrayList<HttpError>();
    }
    
    public void addHttpError(final HttpError httpError) {
        synchronized (httpError) {
            for (final HttpError error : this.httpErrors) {
                if (httpError.getHash().equals(error.getHash())) {
                    error.incrementCount();
                    return;
                }
            }
            this.httpErrors.add(httpError);
        }
    }
    
    public synchronized void removeHttpError(final HttpError error) {
        this.httpErrors.remove(error);
    }
    
    public void clear() {
        this.httpErrors.clear();
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        for (final HttpError httpError : this.httpErrors) {
            array.add(httpError.asJson());
        }
        return array;
    }
    
    public Collection<HttpError> getHttpErrors() {
        return this.httpErrors;
    }
    
    public int count() {
        return this.httpErrors.size();
    }
}
