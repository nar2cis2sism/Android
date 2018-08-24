package engine.android.util.ui;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * A loader that queries the {@link SQLiteDatabase} and returns a {@link Cursor}.
 * This class implements the {@link Loader} protocol in a standard way for
 * querying cursors, building on {@link AsyncTaskLoader} to perform the cursor
 * query on a background thread so that it does not block the application's UI.
 * 
 * <p>A CursorLoader must be built with the full information for the query to
 * perform.
 * 
 * @author Daimon
 * @version N
 * @since 1/5/2014
 */
public class CursorLoader extends AsyncTaskLoader<Cursor> {

    protected final SQLiteDatabase db;

    private boolean distinct;
    private String table;
    private String[] columns;
    private String selection;
    private String[] selectionArgs;
    private String groupBy;
    private String having;
    private String orderBy;
    private String limit;

    private Cursor mCursor;

    /* Runs on a worker thread */
    @Override
    public Cursor loadInBackground() {
        Cursor cursor = createCursor();
        if (cursor != null) {
            try {
                // Ensure the cursor window is filled.
                cursor.getCount();
            } catch (RuntimeException ex) {
                cursor.close();
                throw ex;
            }
        }

        return cursor;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Creates an empty unspecified CursorLoader. You must follow this with
     * calls to {@link #setTable(String)}, {@link #setSelection(String)}, etc to
     * specify the query to perform.
     */
    public CursorLoader(Context context, SQLiteDatabase db) {
        super(context);
        this.db = db;
    }

    /**
     * Creates a fully-specified CursorLoader. See
     * {@link SQLiteDatabase#query(boolean, String, String[], String, 
     * String[], String, String, String, String)
     * SQLiteDatabase.query()} for documentation on the meaning of the
     * parameters. These will be passed as-is to that call.
     */
    public CursorLoader(Context context, SQLiteDatabase db, boolean distinct, String table,
            String[] columns,
            String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy, String limit) {
        this(context, db);
        this.distinct = distinct;
        this.table = table;
        this.columns = columns;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.groupBy = groupBy;
        this.having = having;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    /**
     * Starts an asynchronous load of the contacts list data. 
     * When the result is ready the callbacks will be called on the UI thread. 
     * If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     *
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

    protected Cursor createCursor() {
        return db.query(distinct, table, columns, selection, selectionArgs, groupBy,
                having, orderBy, limit);
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getHaving() {
        return having;
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix); writer.print("distinct="); writer.println(distinct);
        writer.print(prefix); writer.print("table="); writer.println(table);
        writer.print(prefix); writer.print("columns=");
        writer.println(Arrays.toString(columns));
        writer.print(prefix); writer.print("selection="); writer.println(selection);
        writer.print(prefix); writer.print("selectionArgs=");
        writer.println(Arrays.toString(selectionArgs));
        writer.print(prefix); writer.print("groupBy="); writer.println(groupBy);
        writer.print(prefix); writer.print("having="); writer.println(having);
        writer.print(prefix); writer.print("orderBy="); writer.println(orderBy);
        writer.print(prefix); writer.print("limit="); writer.println(limit);
        writer.print(prefix); writer.print("mCursor="); writer.println(mCursor);
    }
}