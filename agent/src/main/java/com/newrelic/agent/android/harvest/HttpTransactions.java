// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest;

import java.util.Iterator;
import com.newrelic.com.google.gson.JsonArray;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Collection;
import com.newrelic.agent.android.harvest.type.HarvestableArray;

public class HttpTransactions extends HarvestableArray
{
    private final Collection<HttpTransaction> httpTransactions;
    
    public HttpTransactions() {
        this.httpTransactions = new CopyOnWriteArrayList<HttpTransaction>();
    }
    
    public synchronized void add(final HttpTransaction httpTransaction) {
        this.httpTransactions.add(httpTransaction);
    }
    
    public synchronized void remove(final HttpTransaction transaction) {
        this.httpTransactions.remove(transaction);
    }
    
    public void clear() {
        this.httpTransactions.clear();
    }
    
    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        for (final HttpTransaction transaction : this.httpTransactions) {
            array.add(transaction.asJson());
        }
        return array;
    }
    
    public Collection<HttpTransaction> getHttpTransactions() {
        return this.httpTransactions;
    }
    
    public int count() {
        return this.httpTransactions.size();
    }
    
    @Override
    public String toString() {
        return "HttpTransactions{httpTransactions=" + this.httpTransactions + '}';
    }
}
