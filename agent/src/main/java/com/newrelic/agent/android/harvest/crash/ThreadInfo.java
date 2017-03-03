// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.harvest.crash;

import com.newrelic.com.google.gson.JsonArray;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.agent.android.util.SafeJsonPrimitive;
import com.newrelic.com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.newrelic.agent.android.harvest.type.HarvestableObject;

public class ThreadInfo extends HarvestableObject
{
    private boolean crashed;
    private long threadId;
    private String threadName;
    private int threadPriority;
    private StackTraceElement[] stackTrace;
    private String state;
    
    private ThreadInfo() {
    }
    
    public ThreadInfo(final Throwable throwable) {
        this.crashed = true;
        this.threadId = Thread.currentThread().getId();
        this.threadName = Thread.currentThread().getName();
        this.threadPriority = Thread.currentThread().getPriority();
        this.stackTrace = throwable.getStackTrace();
        this.state = Thread.currentThread().getState().toString();
    }
    
    public ThreadInfo(final Thread thread, final StackTraceElement[] stackTrace) {
        this.crashed = false;
        this.threadId = thread.getId();
        this.threadName = thread.getName();
        this.threadPriority = thread.getPriority();
        this.stackTrace = stackTrace;
        this.state = thread.getState().toString();
    }
    
    public long getThreadId() {
        return this.threadId;
    }
    
    public static List<ThreadInfo> extractThreads(final Throwable throwable) {
        final List<ThreadInfo> threads = new ArrayList<ThreadInfo>();
        final ThreadInfo crashedThread = new ThreadInfo(throwable);
        final long crashedThreadId = crashedThread.getThreadId();
        threads.add(crashedThread);
        for (final Map.Entry<Thread, StackTraceElement[]> threadEntry : Thread.getAllStackTraces().entrySet()) {
            final Thread thread = threadEntry.getKey();
            final StackTraceElement[] threadStackTrace = threadEntry.getValue();
            if (thread.getId() != crashedThreadId) {
                threads.add(new ThreadInfo(thread, threadStackTrace));
            }
        }
        return threads;
    }
    
    @Override
    public JsonObject asJsonObject() {
        final JsonObject data = new JsonObject();
        data.add("crashed", SafeJsonPrimitive.factory(Boolean.valueOf(this.crashed)));
        data.add("state", SafeJsonPrimitive.factory(this.state));
        data.add("threadNumber", SafeJsonPrimitive.factory(this.threadId));
        data.add("threadId", SafeJsonPrimitive.factory(this.threadName));
        data.add("priority", SafeJsonPrimitive.factory(this.threadPriority));
        data.add("stack", this.getStackAsJson());
        return data;
    }
    
    public static ThreadInfo newFromJson(final JsonObject jsonObject) {
        final ThreadInfo info = new ThreadInfo();
        info.crashed = jsonObject.get("crashed").getAsBoolean();
        info.state = jsonObject.get("state").getAsString();
        info.threadId = jsonObject.get("threadNumber").getAsLong();
        info.threadName = jsonObject.get("threadId").getAsString();
        info.threadPriority = jsonObject.get("priority").getAsInt();
        info.stackTrace = stackTraceFromJson(jsonObject.get("stack").getAsJsonArray());
        return info;
    }
    
    public static StackTraceElement[] stackTraceFromJson(final JsonArray jsonArray) {
        final StackTraceElement[] stack = new StackTraceElement[jsonArray.size()];
        int i = 0;
        for (final JsonElement jsonElement : jsonArray) {
            String fileName = "unknown";
            if (jsonElement.getAsJsonObject().get("fileName") != null) {
                fileName = jsonElement.getAsJsonObject().get("fileName").getAsString();
            }
            final String className = jsonElement.getAsJsonObject().get("className").getAsString();
            final String methodName = jsonElement.getAsJsonObject().get("methodName").getAsString();
            final int lineNumber = jsonElement.getAsJsonObject().get("lineNumber").getAsInt();
            final StackTraceElement stackTraceElement = new StackTraceElement(className, methodName, fileName, lineNumber);
            stack[i++] = stackTraceElement;
        }
        return stack;
    }
    
    public static List<ThreadInfo> newListFromJson(final JsonArray jsonArray) {
        final List<ThreadInfo> list = new ArrayList<ThreadInfo>();
        for (final JsonElement jsonElement : jsonArray) {
            list.add(newFromJson(jsonElement.getAsJsonObject()));
        }
        return list;
    }
    
    private JsonArray getStackAsJson() {
        final JsonArray data = new JsonArray();
        for (final StackTraceElement element : this.stackTrace) {
            final JsonObject elementJson = new JsonObject();
            if (element.getFileName() != null) {
                elementJson.add("fileName", SafeJsonPrimitive.factory(element.getFileName()));
            }
            elementJson.add("className", SafeJsonPrimitive.factory(element.getClassName()));
            elementJson.add("methodName", SafeJsonPrimitive.factory(element.getMethodName()));
            elementJson.add("lineNumber", SafeJsonPrimitive.factory(element.getLineNumber()));
            data.add(elementJson);
        }
        return data;
    }
}
