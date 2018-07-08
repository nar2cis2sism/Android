package engine.android.dao;

import static engine.android.core.util.LogFactory.LOG.log;
import static engine.android.dao.DAOUtil.checkNull;
import static engine.android.dao.DAOUtil.extractFromCursor;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import engine.android.core.util.LogFactory;
import engine.android.core.util.LogFactory.LogUtil;
import engine.android.dao.DAOTemplate.DAOClause;
import engine.android.dao.DAOTemplate.DAOExpression;
import engine.android.dao.DAOTemplate.DAOSQLBuilder;
import engine.android.dao.DAOUtil.DAOException;
import engine.android.dao.util.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ProviderTemplate {

    /**
     * Provider批处理
     */
    public interface ProviderBatch {

        /**
         * 批处理执行（抛出异常或返回false表示批处理失败）
         */
        boolean applyBatch(ProviderTemplate pao) throws Exception;
    }

    private final Context context;

    private boolean printLog = true;

    private boolean inBatch;

    private final ArrayList<ContentProviderOperation> operations
    = new ArrayList<ContentProviderOperation>();

    public ProviderTemplate(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 默认打印Provider操作语句，如有性能问题可以关闭
     */
    public void disablePrintLog(boolean disable) {
        printLog = !disable;
    }

    /**
     * 执行批处理操作
     *
     * @see ContentResolver#applyBatch(String, ArrayList)
     */
    public boolean executeBatch(String authority, ProviderBatch batch) {
        boolean success = false;

        if (printLog) log(LogUtil.getCallerStackFrame(), "批处理任务开始");
        try {
            inBatch = true;
            if (batch.applyBatch(this))
            {
                context.getContentResolver().applyBatch(authority, operations);
                success = true;
            }
        } catch (ProviderException e) {
            LOG_ProviderException(e);
        } catch (Exception e) {
            LOG_ProviderException(new ProviderException(e));
        } finally {
            inBatch = false;
            operations.clear();
            if (printLog) log(LogUtil.getCallerStackFrame(), "批处理任务结束:success=" + success);
        }

        return success;
    }

    /**
     * 执行批处理操作时添加一条指令
     */
    public void addBatch(ContentProviderOperation operation) {
        if (inBatch) operations.add(operation);
    }

    /**
     * 保存数据（允许同时保存多条数据，但必须是同一类型）
     *
     * @param obj JavaBean对象，映射到数据库的一张表
     * @return 是否有数据保存
     * @see ContentResolver#insert(Uri, android.content.ContentValues)
     * @see ContentResolver#bulkInsert(Uri, android.content.ContentValues[])
     */
    public <T> boolean save(Uri url, T... obj) {
        checkNull(obj);
        if (obj.length == 0) return false;

        try {
            if (obj.length == 1)
            {
                ContentValues values = ProviderUtil.convert(obj[0]);

                if (printLog)
                {
                    Table table = Table.getTable(obj.getClass().getComponentType());

                    StringBuilder sql = new StringBuilder()
                    .append("INSERT INTO ")
                    .append(table.getTableName())
                    .append("[")
                    .append(values)
                    .append("]");

                    log("insert:" + url, sql.toString());
                }

                if (inBatch)
                {
                    return operations.add(ContentProviderOperation
                    .newInsert(url)
                    .withValues(values)
                    .build());
                }
                else
                {
                    return context.getContentResolver().insert(url, values) != null;
                }
            }
            else
            {
                ContentValues[] valuesArray = ProviderUtil.convert(obj);

                if (printLog)
                {
                    Table table = Table.getTable(obj.getClass().getComponentType());

                    StringBuilder sql = new StringBuilder()
                    .append("INSERT INTO ")
                    .append(table.getTableName())
                    .append("[")
                    .append(TextUtils.join(",\n", valuesArray))
                    .append("]");

                    log("bulkInsert:" + url, sql.toString());
                }

                if (inBatch)
                {
                    for (ContentValues values : valuesArray)
                    {
                        operations.add(ContentProviderOperation
                        .newInsert(url)
                        .withValues(values)
                        .build());
                    }

                    return true;
                }
                else
                {
                    return context.getContentResolver().bulkInsert(url, valuesArray) > 0;
                }
            }
        } catch (Exception e) {
            processException(e);
        }

        return false;
    }

    /**
     * 更新或删除数据
     */
    public <T> ProviderEditBuilder<T> edit(Class<T> c, Uri url) {
        return new ProviderEditBuilder<T>(c, url);
    }

    private <T> boolean remove(Uri url, DAOSQLBuilder<T> builder) {
        String selection = null;
        String[] selectionArgs = null;
        if (builder.where != null)
        {
            StringBuilder where = new StringBuilder();
            LinkedList<Object> args = new LinkedList<Object>();
            builder.appendWhere(where, args);
            
            selection = where.toString();
            selectionArgs = DAOSQLBuilder.convertArgs(args);
        }

        if (printLog)
        {
            StringBuilder sql = new StringBuilder()
            .append("DELETE FROM ")
            .append(builder.table.getTableName())
            .append(SQL_WHERE_LOG(selection, selectionArgs));

            log("delete:" + url, sql.toString());
        }

        try {
            if (inBatch)
            {
                return operations.add(ContentProviderOperation
                .newDelete(url)
                .withSelection(selection, selectionArgs)
                .build());
            }
            else
            {
                return context.getContentResolver().delete(url, selection, selectionArgs) > 0;
            }
        } catch (Exception e) {
            processException(e);
        }

        return false;
    }

    private <T> boolean edit(Uri uri, DAOSQLBuilder<T> builder, T bean, String... fields) {
        checkNull(bean);
        
        String selection = null;
        String[] selectionArgs = null;
        if (builder.where != null)
        {
            StringBuilder where = new StringBuilder();
            LinkedList<Object> args = new LinkedList<Object>();
            builder.appendWhere(where, args);
            
            selection = where.toString();
            selectionArgs = DAOSQLBuilder.convertArgs(args);
        }

        try {
            ContentValues values = ProviderUtil.convert(bean, fields);
            if (printLog)
            {
                StringBuilder sql = new StringBuilder()
                .append("UPDATE ")
                .append(builder.table.getTableName())
                .append(" SET ")
                .append("[").append(values).append("]")
                .append(SQL_WHERE_LOG(selection, selectionArgs));

                log("update:" + uri, sql.toString());
            }

            if (inBatch)
            {
                return operations.add(ContentProviderOperation
                .newUpdate(uri)
                .withValues(values)
                .withSelection(selection, selectionArgs)
                .build());
            }
            else
            {
                return context.getContentResolver().update(uri, values, selection, selectionArgs) > 0;
            }
        } catch (Exception e) {
            processException(e);
        }

        return false;
    }

    /**
     * 查询数据
     * @see {@link ContentResolver#query(Uri, String[], String, String[], String)}
     */
    public <T> ProviderQueryBuilder<T> find(Class<T> c, Uri url) {
        return new ProviderQueryBuilder<T>(c, url);
    }

    private Cursor query(Uri uri, Table table, String[] projection, String selection, String[] selectionArgs) {
        if (printLog)
        {
            StringBuilder sql = new StringBuilder("SELECT ");
            if (projection == null)
            {
                sql.append("*");
            }
            else
            {
                sql.append(TextUtils.join(",", projection));
            }

            sql .append(" FROM ")
                .append(table.getTableName())
                .append(SQL_WHERE_LOG(selection, selectionArgs));

            log("query:" + uri, sql.toString());
        }

        return context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
    }

    private void processException(Exception t) {
        ProviderException e = new ProviderException(t);
        if (inBatch)
        {
            throw e;
        }

        LOG_ProviderException(e);
    }

    private static String SQL_WHERE_LOG(String sql, Object[] bindArgs) {
        if (TextUtils.isEmpty(sql)) return "";
        if (bindArgs != null)
        {
            StringBuilder sb = new StringBuilder(sql);
            int i = 0, index = 0;
    
            while ((index = sb.indexOf("?", index)) >= 0)
            {
                String arg = String.valueOf(bindArgs[i++]);
                sb.replace(index, index + 1, arg);
                index += arg.length();
            }
            
            sql = sb.toString();
        }
    
        return " WHERE " + sql;
    }

    private static class ProviderSQLBuilder<T> extends DAOSQLBuilder<T> {

        protected final Uri uri;

        public ProviderSQLBuilder(Class<T> c, Uri uri) {
            super(c);
            this.uri = uri;
        }
    }

    public class ProviderEditBuilder<T> extends ProviderSQLBuilder<T> {

        ProviderEditBuilder(Class<T> c, Uri uri) {
            super(c, uri);
        }

        @Override
        public ProviderEditBuilder<T> where(DAOExpression expression) {
            super.where(expression);
            return this;
        }

        /**
         * 删除数据
         *
         * @return 是否有数据被删除
         * @see ContentResolver#delete(Uri, String, String[])
         */
        public boolean delete() {
            return remove(uri, this);
        }

        /**
         * 修改数据
         *
         * @param bean JavaBean对象，映射到数据库的一张表
         * @param fields 需要修改的字段，不设置则修改所有字段
         * @return 是否有数据更改
         * @see ContentResolver#update(Uri, ContentValues, String, String[])
         */
        public boolean update(T bean, String... fields) {
            return edit(uri, this, bean, fields);
        }
    }

    /**
     * This is a convenient utility that helps build 数据库查询语句
     */
    public class ProviderQueryBuilder<T> extends ProviderSQLBuilder<T> {
        
        private DAOClause selection;                // 查询指定列

        private DAOClause group;                    // 按条件进行分组

        private DAOExpression having;               // 分组上设置条件

        private DAOClause order;                    // 按指定顺序显示

        private boolean orderDesc;                  // 按降序进行排列

        private Page page;                          // 分页工具

        ProviderQueryBuilder(Class<T> c, Uri uri) {
            super(c, uri);
        }

        public ProviderQueryBuilder<T> select(Object... params) {
            selection = DAOClause.create(params);
            return this;
        }

        @Override
        public ProviderQueryBuilder<T> where(DAOExpression expression) {
            super.where(expression);
            return this;
        }

        public ProviderQueryBuilder<T> groupBy(Object... params) {
            group = DAOClause.create(params);
            return this;
        }

        public ProviderQueryBuilder<T> having(DAOExpression expression) {
            having = expression;
            return this;
        }

        public ProviderQueryBuilder<T> orderBy(Object... params) {
            orderDesc = false;
            order = DAOClause.create(params);
            return this;
        }

        public ProviderQueryBuilder<T> orderDesc(Object... params) {
            orderDesc = true;
            order = DAOClause.create(params);
            return this;
        }

        /**
         * 使用分页技术
         */
        public ProviderQueryBuilder<T> usePage(Page page) {
            this.page = page;
            return this;
        }

        private static final int CONSTRAINT_COUNT = 1;

        private static final int CONSTRAINT_LIMIT = 2;
        
        private String[] projection;

        private final StringBuilder sql = new StringBuilder(120);

        private final LinkedList<Object> args = new LinkedList<Object>();

        private void build(int constraint) {
            StringBuilder sql = this.sql;
            LinkedList<Object> args = this.args;

            appendSelection(constraint);
            appendWhere(sql, args);
            appendGroup(sql);
            appendHaving(sql, args);
            appendOrder(sql);

            if (constraint == CONSTRAINT_LIMIT)
            {
                sql.append(" LIMIT 1");
            }
            else if (page != null)
            {
                sql
                .append(" LIMIT ")
                .append(page.getBeginRecord())
                .append(",")
                .append(page.getPageSize());
            }
        }

        private void appendSelection(int constraint) {
            if (constraint == CONSTRAINT_COUNT)
            {
                projection = new String[] { "COUNT(*)" };
            }
            else if (selection != null)
            {
                projection = selection.build(table);
            }
        }
        
        @Override
        public void appendWhere(StringBuilder sql, List<Object> args) {
            if (where != null) where.appendTo(table, sql, args);
        }

        private void appendGroup(StringBuilder sql) {
            if (group != null) group.appendTo(table, sql.append(" GROUP BY "));
        }

        private void appendHaving(StringBuilder sql, List<Object> args) {
            if (having != null) having.appendTo(table, sql.append(" HAVING "), args);
        }

        private void appendOrder(StringBuilder sql) {
            if (order != null)
            {
                order.appendTo(table, sql.append(" ORDER BY "));
                if (orderDesc) sql.append(" DESC");
            }
        }

        private String getSql() {
            String s = sql.toString();
            sql.setLength(0);
            return s;
        }

        private String[] getArgs() {
            if (args.isEmpty()) return null;
            String[] strs = convertArgs(args);
            args.clear();
            return strs;
        }

        /**
         * 获取符合条件数据的数量
         */
        public long getCount() {
            build(CONSTRAINT_COUNT);
            Cursor cursor = queryCursor();
            if (cursor != null)
            {
                try {
                    if (cursor.moveToFirst())
                    {
                        return cursor.getLong(0);
                    }
                } finally {
                    cursor.close();
                }
            }
            
            return -1;
        }

        /**
         * 返回结果集游标
         */
        public Cursor getCursor() {
            build(0);
            return queryCursor();
        }

        /**
         * 获取数据表里第一条满足条件的数据，如没有则返回Null
         */
        public T get() {
            build(CONSTRAINT_LIMIT);
            try {
                Cursor cursor = queryCursor();
                if (cursor != null)
                {
                    try {
                        if (cursor.moveToFirst())
                        {
                            return extractFromCursor(cursor, table, c);
                        }
                    } finally {
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                processException(e);
            }

            return null;
        }
        
        /**
         * 获取满足条件的数据列表
         */
        public List<T> getAll() {
            build(0);
            try {
                Cursor cursor = queryCursor();
                if (cursor != null)
                {
                    try {
                        List<T> list = new ArrayList<T>(cursor.getCount());
                        while (cursor.moveToNext())
                        {
                            list.add(extractFromCursor(cursor, table, c));
                        }

                        return list;
                    } finally {
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                processException(e);
            }

            return null;
        }
        
        private Cursor queryCursor() {
            return query(uri, table, projection, getSql(), getArgs());
        }
    }

    public static final class ProviderUtil {

        public static <T> ContentValues convert(T obj, String... fields) throws Exception {
            if (obj == null) return null;
            return bindObjectToContentValues(Table.getTable(obj.getClass()), obj, fields);
        }

        public static <T> ContentValues[] convert(T[] obj, String... fields) throws Exception {
            if (obj == null) return null;
            if (obj.length == 0) return new ContentValues[0];
            if (obj.length == 1) return new ContentValues[] { convert(obj[0], fields) };

            Table table = Table.getTable(obj.getClass().getComponentType());
            ContentValues[] cvs = new ContentValues[obj.length];
            for (int i = 0; i < cvs.length; i++)
            {
                cvs[i] = bindObjectToContentValues(table, obj[i], fields);
            }

            return cvs;
        }

        private static ContentValues bindObjectToContentValues(Table table, Object obj,
                String... fields) throws Exception {
            ContentValues cv;

            if (fields == null || fields.length == 0)
            {
                Collection<Property> properties = table.getPropertiesWithModifiablePrimaryKey();

                cv = new ContentValues(properties.size());

                for (Property property : properties)
                {
                    bindContentKeyValue(cv, property.getColumn(), property.getValue(obj));
                }
            }
            else
            {
                cv = new ContentValues(fields.length);

                for (String field : fields)
                {
                    Property property = table.getProperty(field);
                    if (property != null)
                    {
                        bindContentKeyValue(cv, property.getColumn(), property.getValue(obj));
                    }
                }
            }

            return cv;
        }

        private static void bindContentKeyValue(ContentValues cv, String key, Object value) {
            if (value == null)
            {
                cv.putNull(key);
            }
            else if (value instanceof byte[])
            {
                cv.put(key, (byte[]) value);
            }
            else if (value instanceof Boolean)
            {
                cv.put(key, (Boolean) value);
            }
            else if (value instanceof Double)
            {
                cv.put(key, (Double) value);
            }
            else if (value instanceof Float)
            {
                cv.put(key, (Float) value);
            }
            else if (value instanceof Long)
            {
                cv.put(key, (Long) value);
            }
            else if (value instanceof Integer)
            {
                cv.put(key, (Integer) value);
            }
            else if (value instanceof Short)
            {
                cv.put(key, (Short) value);
            }
            else if (value instanceof Byte)
            {
                cv.put(key, (Byte) value);
            }
            else
            {
                cv.put(key, value.toString());
            }
        }
    }

    static
    {
        LogFactory.addLogFile(ProviderTemplate.class, DAOTemplate.class);
    }

    private static void LOG_ProviderException(ProviderException e) {
        log("Provider操作异常", e);
    }

    private static class ProviderException extends DAOException {

        private static final long serialVersionUID = 1L;

        public ProviderException(Throwable throwable) {
            super(throwable);
        }
    }
}