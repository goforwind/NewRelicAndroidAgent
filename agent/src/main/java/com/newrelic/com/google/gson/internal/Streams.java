// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.com.google.gson.internal;

import java.io.Writer;
import com.newrelic.com.google.gson.stream.JsonWriter;
import com.newrelic.com.google.gson.JsonParseException;
import java.io.IOException;
import com.newrelic.com.google.gson.JsonIOException;
import com.newrelic.com.google.gson.stream.MalformedJsonException;
import java.io.EOFException;
import com.newrelic.com.google.gson.JsonSyntaxException;
import com.newrelic.com.google.gson.JsonNull;
import com.newrelic.com.google.gson.internal.bind.TypeAdapters;
import com.newrelic.com.google.gson.JsonElement;
import com.newrelic.com.google.gson.stream.JsonReader;

public final class Streams
{
    public static JsonElement parse(final JsonReader reader) throws JsonParseException {
        boolean isEmpty = true;
        try {
            reader.peek();
            isEmpty = false;
            return TypeAdapters.JSON_ELEMENT.read(reader);
        }
        catch (EOFException e) {
            if (isEmpty) {
                return JsonNull.INSTANCE;
            }
            throw new JsonSyntaxException(e);
        }
        catch (MalformedJsonException e2) {
            throw new JsonSyntaxException(e2);
        }
        catch (IOException e3) {
            throw new JsonIOException(e3);
        }
        catch (NumberFormatException e4) {
            throw new JsonSyntaxException(e4);
        }
    }
    
    public static void write(final JsonElement element, final JsonWriter writer) throws IOException {
        TypeAdapters.JSON_ELEMENT.write(writer, element);
    }
    
    public static Writer writerForAppendable(final Appendable appendable) {
        return (appendable instanceof Writer) ? ((Writer)appendable) : new AppendableWriter(appendable);
    }
    
    private static final class AppendableWriter extends Writer
    {
        private final Appendable appendable;
        private final CurrentWrite currentWrite;
        
        private AppendableWriter(final Appendable appendable) {
            this.currentWrite = new CurrentWrite();
            this.appendable = appendable;
        }
        
        public void write(final char[] chars, final int offset, final int length) throws IOException {
            this.currentWrite.chars = chars;
            this.appendable.append(this.currentWrite, offset, offset + length);
        }
        
        public void write(final int i) throws IOException {
            this.appendable.append((char)i);
        }
        
        public void flush() {
        }
        
        public void close() {
        }
        
        static class CurrentWrite implements CharSequence
        {
            char[] chars;
            
            public int length() {
                return this.chars.length;
            }
            
            public char charAt(final int i) {
                return this.chars[i];
            }
            
            public CharSequence subSequence(final int start, final int end) {
                return new String(this.chars, start, end - start);
            }
        }
    }
}
