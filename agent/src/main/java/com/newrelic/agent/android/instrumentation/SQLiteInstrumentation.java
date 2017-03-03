// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.instrumentation;

import java.util.Collection;
import java.util.Arrays;
import android.database.SQLException;
import android.content.ContentValues;
import android.os.CancellationSignal;
import com.newrelic.agent.android.tracing.TraceMachine;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class SQLiteInstrumentation
{
    private static final ArrayList<String> categoryParams;
    
    @ReplaceCallSite
    public static Cursor query(final SQLiteDatabase database, final boolean distinct, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        TraceMachine.enterMethod("SQLiteDatabase#query", SQLiteInstrumentation.categoryParams);
        final Cursor cursor = database.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        TraceMachine.exitMethod();
        return cursor;
    }
    
    @ReplaceCallSite
    public static Cursor query(final SQLiteDatabase database, final boolean distinct, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit, final CancellationSignal cancellationSignal) {
        TraceMachine.enterMethod("SQLiteDatabase#query", SQLiteInstrumentation.categoryParams);
        final Cursor cursor = database.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);
        TraceMachine.exitMethod();
        return cursor;
    }
    
    @ReplaceCallSite
    public static Cursor query(final SQLiteDatabase database, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy) {
        TraceMachine.enterMethod("SQLiteDatabase#query", SQLiteInstrumentation.categoryParams);
        final Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        TraceMachine.exitMethod();
        return cursor;
    }
    
    @ReplaceCallSite
    public static Cursor query(final SQLiteDatabase database, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        TraceMachine.enterMethod("SQLiteDatabase#query", SQLiteInstrumentation.categoryParams);
        final Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        TraceMachine.exitMethod();
        return cursor;
    }
    
    @ReplaceCallSite
    public static Cursor queryWithFactory(final SQLiteDatabase database, final SQLiteDatabase.CursorFactory cursorFactory, final boolean distinct, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        TraceMachine.enterMethod("SQLiteDatabase#queryWithFactory", SQLiteInstrumentation.categoryParams);
        final Cursor cursor = database.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        TraceMachine.exitMethod();
        return cursor;
    }
    
    @ReplaceCallSite
    public static Cursor queryWithFactory(final SQLiteDatabase database, final SQLiteDatabase.CursorFactory cursorFactory, final boolean distinct, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit, final CancellationSignal cancellationSignal) {
        TraceMachine.enterMethod("SQLiteDatabase#queryWithFactory", SQLiteInstrumentation.categoryParams);
        final Cursor cursor = database.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);
        TraceMachine.exitMethod();
        return cursor;
    }
    
    @ReplaceCallSite
    public static Cursor rawQuery(final SQLiteDatabase database, final String sql, final String[] selectionArgs) {
        TraceMachine.enterMethod("SQLiteDatabase#rawQuery", SQLiteInstrumentation.categoryParams);
        final Cursor cursor = database.rawQuery(sql, selectionArgs);
        TraceMachine.exitMethod();
        return cursor;
    }
    
    @ReplaceCallSite
    public static Cursor rawQuery(final SQLiteDatabase database, final String sql, final String[] selectionArgs, final CancellationSignal cancellationSignal) {
        TraceMachine.enterMethod("SQLiteDatabase#rawQuery", SQLiteInstrumentation.categoryParams);
        final Cursor cursor = database.rawQuery(sql, selectionArgs, cancellationSignal);
        TraceMachine.exitMethod();
        return cursor;
    }
    
    @ReplaceCallSite
    public static Cursor rawQueryWithFactory(final SQLiteDatabase database, final SQLiteDatabase.CursorFactory cursorFactory, final String sql, final String[] selectionArgs, final String editTable) {
        TraceMachine.enterMethod("SQLiteDatabase#rawQueryWithFactory", SQLiteInstrumentation.categoryParams);
        final Cursor cursor = database.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable);
        TraceMachine.exitMethod();
        return cursor;
    }
    
    @ReplaceCallSite
    public static Cursor rawQueryWithFactory(final SQLiteDatabase database, final SQLiteDatabase.CursorFactory cursorFactory, final String sql, final String[] selectionArgs, final String editTable, final CancellationSignal cancellationSignal) {
        TraceMachine.enterMethod("SQLiteDatabase#rawQueryWithFactory", SQLiteInstrumentation.categoryParams);
        final Cursor cursor = database.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable, cancellationSignal);
        TraceMachine.exitMethod();
        return cursor;
    }
    
    @ReplaceCallSite
    public static long insert(final SQLiteDatabase database, final String table, final String nullColumnHack, final ContentValues values) {
        TraceMachine.enterMethod("SQLiteDatabase#insert", SQLiteInstrumentation.categoryParams);
        final long result = database.insert(table, nullColumnHack, values);
        TraceMachine.exitMethod();
        return result;
    }
    
    @ReplaceCallSite
    public static long insertOrThrow(final SQLiteDatabase database, final String table, final String nullColumnHack, final ContentValues values) throws SQLException {
        TraceMachine.enterMethod("SQLiteDatabase#insertOrThrow", SQLiteInstrumentation.categoryParams);
        final long result = database.insertOrThrow(table, nullColumnHack, values);
        TraceMachine.exitMethod();
        return result;
    }
    
    @ReplaceCallSite
    public static long insertWithOnConflict(final SQLiteDatabase database, final String table, final String nullColumnHack, final ContentValues initialValues, final int conflictAlgorithm) {
        TraceMachine.enterMethod("SQLiteDatabase#insertWithOnConflict", SQLiteInstrumentation.categoryParams);
        final long result = database.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
        TraceMachine.exitMethod();
        return result;
    }
    
    @ReplaceCallSite
    public static long replace(final SQLiteDatabase database, final String table, final String nullColumnHack, final ContentValues initialValues) {
        TraceMachine.enterMethod("SQLiteDatabase#replace", SQLiteInstrumentation.categoryParams);
        final long result = database.replace(table, nullColumnHack, initialValues);
        TraceMachine.exitMethod();
        return result;
    }
    
    @ReplaceCallSite
    public static long replaceOrThrow(final SQLiteDatabase database, final String table, final String nullColumnHack, final ContentValues initialValues) throws SQLException {
        TraceMachine.enterMethod("SQLiteDatabase#replaceOrThrow", SQLiteInstrumentation.categoryParams);
        final long result = database.replaceOrThrow(table, nullColumnHack, initialValues);
        TraceMachine.exitMethod();
        return result;
    }
    
    @ReplaceCallSite
    public static int delete(final SQLiteDatabase database, final String table, final String whereClause, final String[] whereArgs) {
        TraceMachine.enterMethod("SQLiteDatabase#delete", SQLiteInstrumentation.categoryParams);
        final int result = database.delete(table, whereClause, whereArgs);
        TraceMachine.exitMethod();
        return result;
    }
    
    @ReplaceCallSite
    public static int update(final SQLiteDatabase database, final String table, final ContentValues values, final String whereClause, final String[] whereArgs) {
        TraceMachine.enterMethod("SQLiteDatabase#update", SQLiteInstrumentation.categoryParams);
        final int result = database.update(table, values, whereClause, whereArgs);
        TraceMachine.exitMethod();
        return result;
    }
    
    @ReplaceCallSite
    public static int updateWithOnConflict(final SQLiteDatabase database, final String table, final ContentValues values, final String whereClause, final String[] whereArgs, final int conflictAlgorithm) {
        TraceMachine.enterMethod("SQLiteDatabase#updateWithOnConflict", SQLiteInstrumentation.categoryParams);
        final int result = database.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm);
        TraceMachine.exitMethod();
        return result;
    }
    
    @ReplaceCallSite
    public static void execSQL(final SQLiteDatabase database, final String sql) throws SQLException {
        TraceMachine.enterMethod("SQLiteDatabase#execSQL", SQLiteInstrumentation.categoryParams);
        database.execSQL(sql);
        TraceMachine.exitMethod();
    }
    
    @ReplaceCallSite
    public static void execSQL(final SQLiteDatabase database, final String sql, final Object[] bindArgs) throws SQLException {
        TraceMachine.enterMethod("SQLiteDatabase#execSQL", SQLiteInstrumentation.categoryParams);
        database.execSQL(sql, bindArgs);
        TraceMachine.exitMethod();
    }
    
    static {
        categoryParams = new ArrayList<String>(Arrays.asList("category", MetricCategory.class.getName(), "DATABASE"));
    }
}
