package engine.android.dao;

import static engine.android.core.util.LogFactory.LOG.log;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteProgram;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import engine.android.core.ApplicationManager;
import engine.android.core.util.LogFactory;
import engine.android.core.util.LogFactory.LogUtil;
import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;
import engine.android.dao.util.Page;
import engine.android.util.file.FileManager;

/**
 * 操作数据库的模板，尽量面向对象，以简化DAO层<br>
 * 不支持多表联合操作，使用原生SQL语句或事务处理效率更高<br>
 * 支持{@link ContentProvider}操作（跨进程通讯）<br>
 * only support 1.NULL：空值。 2.INTEGER：带符号的整型，具体取决于存入数字的范围大小。
 * 3.REAL：浮点数字，存储为8-byte IEEE浮点数。 4.TEXT：字符串文本。 5.BLOB：二进制对象。
 * 
 * @author Daimon
 * @version N
 * @since 4/5/2015
 * 
 * @see http://www.w3cschool.cc/sqlite
 */
public class DAOTemplate {

    /**
     * 数据库更新监听器
     */
    public interface DBUpdateListener {

        void onCreate(DAOTemplate dao);

        void onUpdate(DAOTemplate dao, int oldVersion, int newVersion);
    }

    /**
     * 数据库事务处理
     */
    public interface DAOTransaction {

        /**
         * 事务执行（抛出异常或返回false表示事务处理失败）
         */
        boolean execute(DAOTemplate dao) throws Exception;
    }

    private final Context context;

    private final AtomicReference<SQLiteDatabase> db
    = new AtomicReference<SQLiteDatabase>();

    private boolean printLog = true;

    private final DAOHelper dao;

    private class DAOHelper extends SQLiteOpenHelper {

        private final DBUpdateListener listener;

        public DAOHelper(Context context, String name, int version,
                DBUpdateListener listener) {
            super(context, name, null, version < 1 ? 1 : version);
            this.listener = listener;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            log("", "创建数据库");
            if (listener != null)
            {
                DAOTemplate.this.db.set(db);
                listener.onCreate(DAOTemplate.this);
                DAOTemplate.this.db.set(null);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            log("", String.format("数据库版本由%d更新为%d", oldVersion, newVersion));
            if (listener != null)
            {
                DAOTemplate.this.db.set(db);
                listener.onUpdate(DAOTemplate.this, oldVersion, newVersion);
                DAOTemplate.this.db.set(null);
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            log("", String.format("数据库版本由%d更新为%d", oldVersion, newVersion));
            if (listener != null)
            {
                DAOTemplate.this.db.set(db);
                listener.onUpdate(DAOTemplate.this, oldVersion, newVersion);
                DAOTemplate.this.db.set(null);
            }
        }
    }

    /**
     * 数据库表监听器
     */
    public interface DAOListener {

        /** Daimon:标志位 **/
        int ALL     = ~0;

        int INSERT  = 1 << 0;

        int DELETE  = 1 << 1;

        int UPDATE  = 1 << 2;

        void onChange();
    }

    /**
     * 数据库观察者
     */
    private static class DAOObserver {

        public final DAOListener listener;

        private final int op;

        public DAOObserver(DAOListener listener, int op) {
            this.listener = listener;
            this.op = op;
        }

        public boolean hasChange(int op) {
            return (this.op & op) != 0;
        }

        public void notifyChange() {
            listener.onChange();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof DAOObserver)
            {
                DAOObserver observer = (DAOObserver) o;
                return observer.listener == listener && observer.op == op;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return listener.hashCode() + 31 * op;
        }
    }

    private final ConcurrentHashMap<String, CopyOnWriteArraySet<DAOObserver>> listeners
    = new ConcurrentHashMap<String, CopyOnWriteArraySet<DAOObserver>>(); // 表名为索引

    private final CopyOnWriteArraySet<DAOObserver> pendingListeners
    = new CopyOnWriteArraySet<DAOObserver>();

    /**
     * @param name 数据库名称
     * @param version 数据库版本
     * @param listener 数据库更新监听器
     */
    public DAOTemplate(Context context, String name, int version,
            DBUpdateListener listener) {
        dao = new DAOHelper(this.context = context.getApplicationContext(),
                name, version, listener);
    }

    /**
     * 关闭数据库，之后任何操作将再次打开数据库
     */
    public void close() {
        dao.close();
    }

    /**
     * 删除自身数据库
     */
    public void deleteSelf() {
        log(LogUtil.getCallerStackFrame(), "删除数据库:" + dao.getDatabaseName());
        context.deleteDatabase(dao.getDatabaseName());
    }

    /**
     * 导出数据库文件
     */
    public boolean export(File dir) {
        return FileManager.copyTo(dir, context.getDatabasePath(dao.getDatabaseName()));
    }

    /**
     * 默认打印数据库执行语句，如有性能问题可以关闭
     */
    public void disablePrintLog(boolean disable) {
        printLog = !disable;
    }

    public SQLiteDatabase getDataBase() {
        SQLiteDatabase db = this.db.get();
        if (db == null)
        {
            return dao.getWritableDatabase();
        }
    
        return db;
    }

    /******************************* 华丽丽的分割线 *******************************/

    /**
     * @param createIfNotExist If true, create an empty set if it's not existed.
     */
    private CopyOnWriteArraySet<DAOObserver> getObservers(Table table,
            boolean createIfNotExist) {
        String key = table.getTableName();

        if (!listeners.contains(key) && createIfNotExist)
        {
            listeners.putIfAbsent(key, new CopyOnWriteArraySet<DAOObserver>());
        }

        return listeners.get(key);
    }

    public void registerListener(Class<?> c, DAOListener listener, int op) {
        checkNull(listener);
        getObservers(Table.getTable(c), true).add(new DAOObserver(listener, op));
    }

    public void registerListener(Class<?> c, DAOListener listener) {
        registerListener(c, listener, DAOListener.ALL);
    }

    public void unregisterListener(Class<?> c, DAOListener listener) {
        CopyOnWriteArraySet<DAOObserver> observers = getObservers(Table.getTable(c), false);
        if (observers == null || observers.isEmpty()) return;
        
        if (listener == null)
        {
            observers.clear();
            return;
        }

        Iterator<DAOObserver> iter = observers.iterator();
        while (iter.hasNext())
        {
            DAOObserver observer = iter.next();
            if (observer.listener == listener)
            {
                observers.remove(observer);
            }
        }
    }

    /**
     * 外界可以通过此方法自行通知数据库表更新
     */
    public void notifyChange(Class<?> c) {
        notifyChange(Table.getTable(c), DAOListener.ALL);
    }

    /**
     * 目前只对增删改进行通知
     * 
     * @see #save(Object...)
     * @see #remove(DAOSQLBuilder)
     * @see #edit(DAOSQLBuilder, Object, String...)
     */
    private void notifyChange(Table table, int op) {
        CopyOnWriteArraySet<DAOObserver> observers = getObservers(table, false);
        if (observers != null && !observers.isEmpty())
        {
            dispatchChange(observers, op);
        }
    }

    private void dispatchChange(CopyOnWriteArraySet<DAOObserver> observers, int op) {
        if (getDataBase().inTransaction())
        {
            for (DAOObserver observer : observers)
            {
                if (observer.hasChange(op))
                {
                    pendingListeners.add(observer);
                }
            }
        }
        else
        {
            for (DAOObserver observer : observers)
            {
                if (observer.hasChange(op))
                {
                    observer.notifyChange();
                }
            }
        }
    }

    private void dispatchChange(boolean success) {
        if (success)
        {
            if (pendingListeners.isEmpty()) return;
            
            Iterator<DAOObserver> iter = pendingListeners.iterator();
            pendingListeners.clear();
            while (iter.hasNext()) iter.next().notifyChange();
        }
        else
        {
            pendingListeners.clear();
        }
    }

    /******************************* 华丽丽的分割线 *******************************/
    
    Cursor rawQuery(String sql, String[] selectionArgs) {
        if (printLog) LOG_SQL(sql, selectionArgs);
        return getDataBase().rawQuery(sql, selectionArgs);
    }

    /**
     * 执行查询语句
     * 
     * @param sql 查询条件
     * @param selectionArgs 查询参数
     * @return 结果集游标
     */
    public Cursor queryCursor(String sql, String[] selectionArgs) {
        try {
            return rawQuery(sql, selectionArgs);
        } catch (Exception e) {
            processException(e);
        }

        return null;
    }

    /**
     * 执行查询语句<br>
     * e.g. SELECT count(*)
     * 
     * @param sql 查询条件
     * @param selectionArgs 查询参数
     * @return 结果数量
     */
    public long queryCount(String sql, String[] selectionArgs) {
        try {
            if (printLog) LOG_SQL(sql, selectionArgs);
            return DatabaseUtils.longForQuery(getDataBase(), sql, selectionArgs);
        } catch (Exception e) {
            processException(e);
        }

        return -1;
    }

    /**
     * 执行SQL语句，一般用来执行建表语句
     * 
     * @param sql 遵循数据库语法规则，用;隔开
     */
    public void execute(String sql) {
        if (TextUtils.isEmpty(sql)) return;
        SQLiteDatabase db = getDataBase();
        try {
            String[] strs = sql.split(";");
            for (String s : strs)
            {
                if (printLog) LOG_SQL(s);
                db.execSQL(s);
            }
        } catch (Exception e) {
            processException(e);
        }
    }

    /**
     * 执行一条SQL语句
     */
    public void execute(String sql, Object[] bindArgs) {
        if (TextUtils.isEmpty(sql)) return;
        SQLiteDatabase db = getDataBase();
        try {
            if (printLog) LOG_SQL(sql, bindArgs);
            if (bindArgs == null)
            {
                db.execSQL(sql);
            }
            else
            {
                db.execSQL(sql, bindArgs);
            }
        } catch (Exception e) {
            processException(e);
        }
    }

    /**
     * 执行事务
     */
    public boolean execute(DAOTransaction transaction) {
        boolean success = false;

        SQLiteDatabase db = getDataBase();
        db.beginTransaction();
        if (printLog) log(LogUtil.getCallerStackFrame(), "事务开始");
        try {
            if (transaction.execute(this))
            {
                db.setTransactionSuccessful();
                success = true;
            }
        } catch (DAOException e) {
            LOG_DAOException(e);
        } catch (Exception e) {
            LOG_DAOException(new DAOException(e));
        } finally {
            db.endTransaction();
            if (printLog) log(LogUtil.getCallerStackFrame(), "事务结束:success=" + success);
            dispatchChange(success);
        }

        return success;
    }

    /**
     * 创建索引
     * 
     * @param c JavaBean类
     * @param indexName 索引名称
     * @param fields 在指定列上创建索引
     */
    public void createIndex(Class<?> c, String indexName, String... fields) {
        checkNull(indexName);

        if (fields == null || fields.length == 0)
        {
            throw new DAOException("请指定需要添加索引的字段", new NullPointerException());
        }

        Table table = Table.getTable(c);

        StringBuilder sql = new StringBuilder()
        .append("CREATE INDEX ")
        .append(indexName)
        .append(" ON ")
        .append(table.getTableName())
        .append(" (");
        
        DAOClause.create(fields).appendTo(table, sql);

        sql.append(")");

        execute(sql.toString());
    }

    /**
     * 删除索引
     * 
     * @param indexName 索引名称
     */
    public void deleteIndex(String indexName) {
        checkNull(indexName);

        execute("DROP INDEX " + indexName);
    }

    /**
     * 删除视图
     * 
     * @param viewName 视图名称
     */
    public void deleteView(String viewName) {
        checkNull(viewName);

        execute("DROP VIEW " + viewName);
    }

    /**
     * 创建数据库表
     * 
     * @param c JavaBean类
     */
    public void createTable(Class<?> c) {
        createTable(c, false);
    }

    /**
     * 创建数据库表
     * 
     * @param c JavaBean类
     * @param deleteOldTable 是否删除旧表
     */
    public void createTable(Class<?> c, boolean deleteOldTable) {
        Table table = Table.getTable(c);
        PrimaryKey primaryKey = table.getPrimaryKey();
        Collection<Property> properties = table.getPropertiesWithoutPrimaryKey();

        StringBuilder sql = new StringBuilder(500);
        if (deleteOldTable)
        {
            sql.append("DROP TABLE IF EXISTS ")
            .append(table.getTableName())
            .append(";");
        }

        sql.append("CREATE TABLE IF NOT EXISTS ")
        .append(table.getTableName())
        .append("\n(\n");

        if (primaryKey != null)
        {
            sql.append("    ")
            .append(primaryKey.getColumn())
            .append(" ")
            .append(primaryKey.isAsInteger() ?
                    "INTEGER" : primaryKey.getDataType().getSimpleName())
            .append(" PRIMARY KEY")
            .append(primaryKey.isAutoincrement() ?
                    " AUTOINCREMENT" : "")
            .append(",\n");
        }

        for (Property property : properties)
        {
            sql.append("    ")
            .append(property.getColumn())
            .append(" ")
            .append(property.getDataType().getSimpleName())
            .append(",\n");
        }

        sql.deleteCharAt(sql.length() - 2).append(")");
        execute(sql.toString());
    }

    /**
     * 删除数据库表
     * 
     * @param c JavaBean类
     */
    public void deleteTable(Class<?> c) {
        Table table = Table.getTable(c);
        execute("DROP TABLE IF EXISTS " + table.getTableName());
    }

    /**
     * 重命名数据库表(更新{@link DAOTable#name()}时需同步)
     */
    public void renameTable(String oldName, String newName) {
        StringBuilder sql = new StringBuilder()
        .append("ALTER TABLE ")
        .append(oldName)
        .append(" RENAME TO ")
        .append(newName);

        execute(sql.toString());
    }

    /**
     * 更新数据库表(只支持添加列)
     * 
     * @param c JavaBean类
     */
    public void updateTable(Class<?> c) {
        Table table = Table.getTable(c);
        
        try {
            Cursor cursor = rawQuery("SELECT * FROM " + table.getTableName() + " LIMIT 0", null);
            if (cursor != null)
            {
                try {
                    StringBuilder sql = new StringBuilder(50);
                    for (Property property : table.getPropertiesWithPrimaryKey())
                    {
                        int index = cursor.getColumnIndex(property.getColumn());
                        if (index == -1)
                        {
                            sql .append("ALTER TABLE ")
                                .append(table.getTableName())
                                .append(" ADD COLUMN ")
                                .append(property.getColumn())
                                .append(" ")
                                .append(property.getDataType().getSimpleName())
                                .append(";");
                        }
                    }

                    execute(sql.toString());
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            processException(e);
        }
    }

    /**
     * 更改数据库表结构(备份数据然后重建表)
     * 
     * @param c JavaBean类
     */
    public void alterTable(final Class<?> c) {
        execute(new DAOTransaction() {

            @Override
            public boolean execute(DAOTemplate dao) throws Exception {
                Table table = Table.getTable(c);
                String tempTable = "tempTable";

                dao.renameTable(table.getTableName(), tempTable);
                dao.createTable(c);
                dao.execute("INSERT INTO " + table.getTableName() +
                        " SELECT * FROM " + tempTable);
                dao.execute("DROP TABLE " + tempTable);
                return true;
            }
        });
    }

    /**
     * 清除表中所有数据,并且自增长id还原为 1
     * 
     * @param c JavaBean类
     */
    public void resetTable(Class<?> c) {
        Table table = Table.getTable(c);

        StringBuilder sql = new StringBuilder()
        .append("DELETE FROM ")
        .append(table.getTableName());

        PrimaryKey primaryKey = table.getPrimaryKey();
        if (primaryKey.isAutoincrement())
        {
            sql .append(";UPDATE sqlite_sequence SET seq=0 WHERE name='")
                .append(table.getTableName())
                .append("'")
                .append(";VACUUM");
        }

        execute(sql.toString());
    }

    /**
     * 自动整理数据库（当删除很多条数据后需要对数据库空间进行清理回收）
     */
    public void enableAutoTrim() {
        execute("auto_vacuum pragma");
    }

    /**
     * 检测数据库表是否存在
     * 
     * @param c JavaBean类
     */
    public boolean isTableExist(Class<?> c) {
        return queryCount("SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{ Table.getTable(c).getTableName() }) > 0;
    }

    /**
     * 保存单条数据
     * 
     * @param obj JavaBean对象，映射到数据库的一张表
     * @return 是否保存成功
     */
    public <T> boolean save(T obj) {
        checkNull(obj);

        Table table = Table.getTable(obj.getClass());

        try {
            StringBuilder sql = new StringBuilder()
            .append("INSERT INTO ")
            .append(table.getTableName())
            .append("(");

            int i = 0;

            Collection<Property> properties = table.getPropertiesWithModifiablePrimaryKey();

            for (Property property : properties)
            {
                sql .append(i++ > 0 ? "," : "")
                    .append(property.getColumn());
            }
            
            sql.append(") VALUES (");

            ArrayList<Object> bindArgs = new ArrayList<Object>(properties.size());

            i = 0;

            for (Property property : properties)
            {
                sql.append(i++ > 0 ? ",?" : "?");
                bindArgs.add(property.getValue(obj));
            }

            sql.append(")");

            if (executeInsert(sql.toString(), bindArgs.toArray()) != -1)
            {
                notifyChange(table, DAOListener.INSERT);
                return true;
            }
        } catch (Exception e) {
            processException(e);
        }

        return false;
    }

    /**
     * 保存多条数据（必须是同一类型）
     * 
     * @param obj JavaBean对象，映射到数据库的一张表
     * @return 是否有数据保存
     */
    public <T> boolean save(T... obj) {
        checkNull(obj);
        if (obj.length == 0) return false;

        Table table = Table.getTable(obj.getClass().getComponentType());

        try {
            StringBuilder sql = new StringBuilder(50 + 10 * obj.length)
            .append("INSERT INTO ")
            .append(table.getTableName())
            .append("(");

            StringBuilder values = new StringBuilder(" VALUES ");

            int i = 0;

            Collection<Property> properties = table.getPropertiesWithModifiablePrimaryKey();

            for (Property property : properties)
            {
                sql .append(i++ > 0 ? "," : "")
                    .append(property.getColumn());
            }

            ArrayList<Object> bindArgs = new ArrayList<Object>(properties.size() * obj.length);

            for (Object o : obj)
            {
                i = 0;

                for (Property property : properties)
                {
                    values.append(i++ > 0 ? ",?" : "(?");
                    bindArgs.add(property.getValue(o));
                }

                values.append("),");
            }

            sql.append(")").append(values).deleteCharAt(sql.length() - 1);

            if (executeInsert(sql.toString(), bindArgs.toArray()) != -1)
            {
                notifyChange(table, DAOListener.INSERT);
                return true;
            }
        } catch (Exception e) {
            processException(e);
        }

        return false;
    }

    private long executeInsert(String sql, Object[] bindArgs) {
        if (printLog) LOG_SQL(sql, bindArgs);
        SQLiteStatement statement = getDataBase().compileStatement(sql);
        try {
            if (bindArgs != null && bindArgs.length > 0)
            {
                for (int i = bindArgs.length; i != 0; i--)
                {
                    bindObjectToProgram(statement, i, bindArgs[i - 1]);
                }
            }

            return statement.executeInsert();
        } finally {
            statement.close();
        }
    }

    /**
     * 更新或删除数据
     */
    public <T> DAOEditBuilder<T> edit(Class<T> c) {
        return new DAOEditBuilder<T>(c);
    }

    /**
     * 根据主键删除某条数据
     *
     * @param obj JavaBean对象，映射到数据库的一张表
     * @return 是否删除成功
     */
    public <T> boolean remove(T obj) {
        checkNull(obj);

        @SuppressWarnings("unchecked")
        DAOEditBuilder<T> builder = new DAOEditBuilder<T>((Class<T>) obj.getClass());

        try {
            PrimaryKey primaryKey = builder.table.getPrimaryKey();
            if (primaryKey != null)
            {
                return builder.where(DAOExpression
                    .create(primaryKey.getColumn())
                    .equal(primaryKey.getValue(obj)))
                .delete();
            }
        } catch (Exception e) {
            processException(e);
        }

        return false;
    }

    /**
     * 根据主键更新某一条数据
     *
     * @param obj JavaBean对象，映射到数据库的一张表
     * @param fields 需要修改的字段，不设置则修改所有字段
     * @return 是否更新成功
     */
    public <T> boolean update(T obj, String... fields) {
        checkNull(obj);

        @SuppressWarnings("unchecked")
        DAOEditBuilder<T> builder = new DAOEditBuilder<T>((Class<T>) obj.getClass());

        try {
            PrimaryKey primaryKey = builder.table.getPrimaryKey();
            if (primaryKey != null)
            {
                return builder.where(DAOExpression
                    .create(primaryKey.getColumn())
                    .equal(primaryKey.getValue(obj)))
                .update(obj, fields);
            }
        } catch (Exception e) {
            processException(e);
        }

        return false;
    }

    <T> boolean remove(DAOSQLBuilder<T> builder) {
        Table table = builder.table;

        try {
            StringBuilder sql = new StringBuilder()
            .append("DELETE FROM ")
            .append(table.getTableName());

            LinkedList<Object> bindArgs = new LinkedList<Object>();
            builder.appendWhere(sql, bindArgs);

            if (executeUpdateDelete(sql.toString(), bindArgs.toArray()) > 0)
            {
                notifyChange(table, DAOListener.DELETE);
                return true;
            }
        } catch (Exception e) {
            processException(e);
        }

        return false;
    }

    <T> boolean edit(DAOSQLBuilder<T> builder, T bean, String... fields) {
        checkNull(bean);

        Table table = builder.table;

        try {
            StringBuilder sql = new StringBuilder()
            .append("UPDATE ")
            .append(table.getTableName())
            .append(" SET ");

            ArrayList<Object> bindArgs;

            int i = 0;

            if (fields == null || fields.length == 0)
            {
                Collection<Property> properties = table.getPropertiesWithModifiablePrimaryKey();

                bindArgs = new ArrayList<Object>(properties.size());

                for (Property property : properties)
                {
                    sql .append(i++ > 0 ? "," : "")
                        .append(property.getColumn())
                        .append("=?");
                    bindArgs.add(property.getValue(bean));
                }
            }
            else
            {
                bindArgs = new ArrayList<Object>(fields.length);

                for (String field : fields)
                {
                    Property property = table.getProperty(field);
                    if (property != null)
                    {
                        sql .append(i++ > 0 ? "," : "")
                            .append(property.getColumn())
                            .append("=?");
                        bindArgs.add(property.getValue(bean));
                    }
                }
            }

            builder.appendWhere(sql, bindArgs);

            if (executeUpdateDelete(sql.toString(), bindArgs.toArray()) > 0)
            {
                notifyChange(table, DAOListener.UPDATE);
                return true;
            }
        } catch (Exception e) {
            processException(e);
        }

        return false;
    }

    private int executeUpdateDelete(String sql, Object[] bindArgs) {
        if (printLog) LOG_SQL(sql, bindArgs);
        SQLiteStatement statement = getDataBase().compileStatement(sql);
        try {
            if (bindArgs != null && bindArgs.length > 0)
            {
                for (int i = bindArgs.length; i != 0; i--)
                {
                    bindObjectToProgram(statement, i, bindArgs[i - 1]);
                }
            }

            return statement.executeUpdateDelete();
        } finally {
            statement.close();
        }
    }

    /**
     * 查询数据
     */
    public <T> DAOQueryBuilder<T> find(Class<T> c) {
        return new DAOQueryBuilder<T>(c);
    }

    static void checkNull(Object obj) {
        if (obj != null) return;
        
        String message;
        StackTraceElement stack = LogUtil.getCallerStackFrame();
        if (stack != null)
        {
            message = String.format("Argument passed to %s[%d] cannot be null",
                    stack.getMethodName(), stack.getLineNumber());
        }
        else
        {
            message = "Argument cannot be null";
        }

        throw new DAOException("你故意的吧！", new NullPointerException(message));
    }

    void processException(Exception t) {
        DAOException e = new DAOException(t);
        if (getDataBase().inTransaction())
        {
            throw e;
        }

        LOG_DAOException(e);
    }

    /**
     * 将结果集游标所在记录转换为对象
     */
    public static <T> T convertFromCursor(Cursor cursor, Class<T> c) {
        try {
            return extractFromCursor(cursor, Table.getTable(c), c);
        } catch (Exception e) {
            LOG_DAOException(new DAOException(e));
        }
        
        return null;
    }

    private static <T> T extractFromCursor(Cursor cursor, Table table, Class<T> c)
            throws Exception {
        T o = c.newInstance();
        for (int i = 0, count = cursor.getColumnCount(); i < count; i++)
        {
            String columnName = cursor.getColumnName(i);
            Property property = table.getPropertyByColumn(columnName);
            if (property != null)
            {
                Object value = getCursorValue(cursor, i, property.getDataType());
                if (value != null) property.setValue(o, value);
            }
        }
    
        return o;
    }

    /**
     * Binds the given Object to the given SQLiteProgram using the proper
     * typing. For example, bind numbers as longs/doubles, and everything else
     * as a string by call toString() on it.
     * 
     * @param prog the program to bind the object to
     * @param index the 1-based index to bind at
     * @param value the value to bind
     */
    private static void bindObjectToProgram(SQLiteProgram prog, int index, Object value) {
        if (value == null)
        {
            prog.bindNull(index);
        }
        else if (value instanceof byte[])
        {
            prog.bindBlob(index, (byte[]) value);
        }
        else if (value instanceof Double || value instanceof Float)
        {
            prog.bindDouble(index, ((Number) value).doubleValue());
        }
        else if (value instanceof Number)
        {
            prog.bindLong(index, ((Number) value).longValue());
        }
        else
        {
            prog.bindString(index, value.toString());
        }
    }

    private static Object getCursorValue(Cursor cursor, int columnIndex, Class<?> type) {
        if (cursor.isNull(columnIndex)) return null;

        Object value = null;
        if (type == byte[].class)
        {
            value = cursor.getBlob(columnIndex);
        }
        else if (type == Boolean.class || type == boolean.class)
        {
            value = Boolean.parseBoolean(cursor.getString(columnIndex));
        }
        else if (type == Character.class || type == char.class)
        {
            String s = cursor.getString(columnIndex);
            if (!TextUtils.isEmpty(s))
            {
                value = s.charAt(0);
            }
        }
        else if (type == String.class)
        {
            value = cursor.getString(columnIndex);
        }
        else if (type == Double.class || type == double.class)
        {
            value = cursor.getDouble(columnIndex);
        }
        else if (type == Float.class || type == float.class)
        {
            value = cursor.getFloat(columnIndex);
        }
        else if (type == Byte.class || type == byte.class)
        {
            value = (byte) cursor.getInt(columnIndex);
        }
        else if (type == Integer.class || type == int.class)
        {
            value = cursor.getInt(columnIndex);
        }
        else if (type == Long.class || type == long.class)
        {
            value = cursor.getLong(columnIndex);
        }
        else if (type == Short.class || type == short.class)
        {
            value = cursor.getShort(columnIndex);
        }
        else if (type == Date.class)
        {
            value = Date.valueOf(cursor.getString(columnIndex));
        }
        else if (type == Time.class)
        {
            value = Time.valueOf(cursor.getString(columnIndex));
        }

        return value;
    }

    public static String printCursor(Cursor cursor) {
        return DatabaseUtils.dumpCursorToString(cursor);
    }

    public static String printCursor(Cursor cursor, int position) {
        if (cursor != null && cursor.moveToPosition(position))
        {
            return DatabaseUtils.dumpCurrentRowToString(cursor);
        }
        else
        {
            return "";
        }
    }

    /******************************* 华丽丽的分割线 *******************************/

    private static class Property {

        private final Field field;                    // 对应JavaBean的域

        private final String fieldName;               // JavaBean变量名称

        private final String column;                  // 对应DataBase的列

        public Property(Field field, DAOProperty property) {
            this(field, property.column());
        }

        public Property(Field field, String column) {
            fieldName = (this.field = field).getName();
            this.column = TextUtils.isEmpty(column) ? fieldName : column;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getColumn() {
            return column;
        }

        /**
         * 获取数据类型
         */
        public Class<?> getDataType() {
            return field.getType();
        }

        public Object getValue(Object obj) throws Exception {
            field.setAccessible(true);
            return field.get(obj);
        }

        public void setValue(Object obj, Object value) throws Exception {
            field.setAccessible(true);
            field.set(obj, value);
        }
    }

    private static class PrimaryKey extends Property {

        private final boolean asInteger;

        private final boolean isAutoincrement;

        public PrimaryKey(Field field, DAOPrimaryKey primaryKey) {
            super(field, primaryKey.column());
            Class<?> dataType = getDataType();
            asInteger = dataType == Integer.class || dataType == int.class
                     || dataType == Long.class || dataType == long.class
                     || dataType == Short.class || dataType == short.class
                     || dataType == Byte.class || dataType == byte.class;
            isAutoincrement = primaryKey.autoincrement() && asInteger;
        }

        public boolean isAutoincrement() {
            return isAutoincrement;
        }

        public boolean isAsInteger() {
            return asInteger;
        }
    }

    private static class Table {

        private static final ConcurrentHashMap<String, Table> tables =
                new ConcurrentHashMap<String, Table>(); // 类名为索引

        private final String tableName;

        private PrimaryKey primaryKey;

        private final HashMap<String, Property> propertiesByField =
                new HashMap<String, Property>(); // 域名为索引

        private final HashMap<String, Property> propertiesByColumn =
                new HashMap<String, Property>(); // 列名为索引

        public Table(Class<?> c) {
            tableName = getTableName(c);

            Field[] fields = c.getDeclaredFields();
            for (Field field : fields)
            {
                // 过滤主键
                DAOPrimaryKey primaryKey = field.getAnnotation(DAOPrimaryKey.class);
                if (primaryKey != null && this.primaryKey == null)
                {
                    this.primaryKey = new PrimaryKey(field, primaryKey);
                }
                else
                {
                    DAOProperty property = field.getAnnotation(DAOProperty.class);
                    if (property != null)
                    {
                        Property p = new Property(field, property);
                        propertiesByField.put(p.getFieldName(), p);
                        propertiesByColumn.put(p.getColumn(), p);
                    }
                }
            }
        }

        private static String getTableName(Class<?> c) {
            DAOTable table = c.getAnnotation(DAOTable.class);
            if (table != null)
            {
                String name = table.name();
                if (name != null && name.trim().length() > 0)
                {
                    return name;
                }
            }

            // 当没有注解的时候默认用类的名称作为表名,并把点(.)替换为下划线(_)
            return c.getName().replace('.', '_');
        }

        public static Table getTable(Class<?> c) {
            String name = c.getName();
            Table table = tables.get(name);
            if (table == null)
            {
                tables.putIfAbsent(name, new Table(c));
                table = tables.get(name);
            }

            return table;
        }

        public String getTableName() {
            return tableName;
        }

        public PrimaryKey getPrimaryKey() {
            return primaryKey;
        }

        public Collection<Property> getPropertiesWithoutPrimaryKey() {
            return Collections.unmodifiableCollection(propertiesByField.values());
        }

        public Collection<Property> getPropertiesWithPrimaryKey() {
            Collection<Property> properties = propertiesByField.values();
            if (primaryKey != null)
            {
                ArrayList<Property> list = new ArrayList<Property>(properties.size() + 1);
                list.add(primaryKey);
                list.addAll(properties);
                
                properties = list;
            }

            return Collections.unmodifiableCollection(properties);
        }

        public Collection<Property> getPropertiesWithModifiablePrimaryKey() {
            Collection<Property> properties = propertiesByField.values();
            if (primaryKey != null && !primaryKey.isAutoincrement())
            {
                ArrayList<Property> list = new ArrayList<Property>(properties.size() + 1);
                list.add(primaryKey);
                list.addAll(properties);
                
                properties = list;
            }

            return Collections.unmodifiableCollection(properties);
        }

        public Property getPropertyByField(String fieldName) {
            if (primaryKey != null && primaryKey.getFieldName().equals(fieldName))
            {
                return primaryKey;
            }

            return propertiesByField.get(fieldName);
        }

        public Property getPropertyByColumn(String column) {
            if (primaryKey != null && primaryKey.getColumn().equals(column))
            {
                return primaryKey;
            }

            return propertiesByColumn.get(column);
        }

        public Property getProperty(String name) {
            if (primaryKey != null
            && (primaryKey.getFieldName().equals(name)
            ||  primaryKey.getColumn().equals(name)))
            {
                return primaryKey;
            }

            Property property = propertiesByField.get(name);
            if (property == null)
            {
                property = propertiesByColumn.get(name);
            }

            return property;
        }
    }

    /******************************* 华丽丽的分割线 *******************************/

    /**
     * 数据库操作的最小单元（对应数据库表的列）<br>
     * 封装一些函数操作
     */
    public static final class DAOParam {

        private final String fieldName;         // 默认识别为域名

        private LinkedList<String> format;      // 执行一些函数操作

        private String param;                   // 缓存参数

        /**
         * @param fieldOrColumn 可以识别映射bean的域，也可以直接操作表的列
         */
        public DAOParam(String fieldOrColumn) {
            fieldName = fieldOrColumn;
        }

        private DAOParam addFormat(String s) {
            if (format == null) format = new LinkedList<String>();
            format.add(s);
            return this;
        }

        public DAOParam count() {
            return addFormat("count");
        }

        public DAOParam max() {
            return addFormat("max");
        }

        public DAOParam min() {
            return addFormat("min");
        }

        public DAOParam avg() {
            return addFormat("avg");
        }

        public DAOParam sum() {
            return addFormat("sum");
        }

        public DAOParam abs() {
            return addFormat("abs");
        }

        public DAOParam upper() {
            return addFormat("upper");
        }

        public DAOParam lower() {
            return addFormat("lower");
        }

        public DAOParam length() {
            return addFormat("length");
        }

        String getParam(Table table) {
            if (param == null)
            {
                String column = fieldName;
                Property property = table.getPropertyByField(column);
                if (property != null)
                {
                    column = property.getColumn();
                }

                param = format(column);
            }

            return param;
        }

        private String format(String column) {
            if (format == null) return column;

            StringBuilder sb = new StringBuilder(column);
            for (String s : format)
            {
                sb.insert(0, s + "(").append(")");
            }

            return sb.toString();
        }
    }

    /**
     * 数据库操作语句，可用来指定查询列
     */
    private static class DAOClause {

        private final LinkedList<DAOParam> params
                = new LinkedList<DAOParam>();
        
        private DAOClause() {}

        public void add(DAOParam param) {
            params.add(param);
        }

        public static DAOClause create(String... params) {
            if (params == null || params.length == 0) return null;

            DAOClause clause = new DAOClause();
            for (String param : params)
            {
                clause.add(new DAOParam(param));
            }

            return clause;
        }

        public static DAOClause create(Object... params) {
            if (params == null || params.length == 0) return null;

            DAOClause clause = new DAOClause();
            for (Object param : params)
            {
                if (param instanceof String)
                {
                    clause.add(new DAOParam((String) param));
                }
                else if (param instanceof DAOParam)
                {
                    clause.add((DAOParam) param);
                }
                else
                {
                    throw new DAOException("parameters only allow String or DAOParam",
                            new IllegalArgumentException());
                }
            }

            return clause;
        }

        String[] build(Table table) {
            String[] clause = new String[params.size()];
            for (int i = 0, len = clause.length; i < len; i++)
            {
                clause[i] = params.get(i).getParam(table);
            }

            return clause;
        }

        void appendTo(Table table, StringBuilder sql) {
            boolean firstTime = true;
            for (DAOParam param : params)
            {
                if (firstTime)
                {
                    firstTime = false;
                }
                else
                {
                    sql.append(",");
                }

                sql.append(param.getParam(table));
            }
        }
    }

    /**
     * SQL表达式
     */
    public static class DAOExpression {

        PropertyCondition condition;

        boolean isCombineExpression;

        private DAOExpression() {}

        public static DAOCondition create(String fieldOrColumn) {
            return create(new DAOParam(fieldOrColumn));
        }

        public static DAOCondition create(DAOParam param) {
            DAOExpression expression = new DAOExpression();
            return expression.condition = new PropertyCondition(expression, param);
        }

        public DAOCondition and(String fieldOrColumn) {
            return and(new DAOParam(fieldOrColumn));
        }

        public DAOCondition and(DAOParam param) {
            DAOExpression expression = new DAOExpression();
            return expression.condition = new PropertyCondition(join(expression, " AND "), param);
        }

        public DAOCondition or(String fieldOrColumn) {
            return or(new DAOParam(fieldOrColumn));
        }

        public DAOCondition or(DAOParam param) {
            DAOExpression expression = new DAOExpression();
            return expression.condition = new PropertyCondition(join(expression, " OR "), param);
        }

        DAOExpression join(DAOExpression expression, String op) {
            return new DAOCombineExpression(this).join(expression, op);
        }

        void appendTo(Table table, StringBuilder sql, List<Object> whereArgs) {
            condition.appendTo(table, sql, whereArgs);
        }

        /**
         * 组合表达式，连接多个子句
         */
        private static class DAOCombineExpression extends DAOExpression {

            private final LinkedList<Pair<DAOExpression, String>> children;

            public DAOCombineExpression(DAOExpression expression) {
                isCombineExpression = true;
                children = new LinkedList<Pair<DAOExpression, String>>();
                condition = expression.condition;
            }

            DAOExpression join(DAOExpression expression, String op) {
                children.add(new Pair<DAOExpression, String>(expression, op));
                return this;
            }

            void appendTo(Table table, StringBuilder sql, List<Object> whereArgs) {
                super.appendTo(table, sql, whereArgs);

                for (Pair<DAOExpression, String> child : children)
                {
                    sql.append(child.second);

                    DAOExpression expression = child.first;
                    if (expression.isCombineExpression) sql.append("(");
                    expression.appendTo(table, sql, whereArgs);
                    if (expression.isCombineExpression) sql.append(")");
                }
            }
        }

        /**
         * 条件判断，一般用于where子句
         */
        public interface DAOCondition {

            DAOCondition not();

            DAOExpression equal(Object value);

            DAOExpression like(String value);

            DAOExpression between(Object value1, Object value2);

            DAOExpression in(Object... values);

            DAOExpression greaterThan(Object value);

            DAOExpression lessThan(Object value);

            DAOExpression isNull();
        }

        private static class PropertyCondition implements DAOCondition {

            private final DAOExpression expression;

            private final DAOParam param;

            private String op;

            private Object[] values;

            private boolean notIsCalled;

            public PropertyCondition(DAOExpression expression, DAOParam param) {
                this.expression = expression;
                this.param = param;
            }

            private DAOExpression setup(String op, Object... values) {
                this.op = op;
                this.values = values;
                return expression;
            }

            @Override
            public DAOCondition not() {
                notIsCalled = true;
                return this;
            }

            @Override
            public DAOExpression equal(Object value) {
                return setup(notIsCalled ? "<>?" : "=?", value);
            }

            @Override
            public DAOExpression like(String value) {
                return setup((notIsCalled ? " NOT" : "") + " LIKE ?", value);
            }

            @Override
            public DAOExpression between(Object value1, Object value2) {
                return setup((notIsCalled ? " NOT" : "") + " BETWEEN ? AND ?", value1, value2);
            }

            @Override
            public DAOExpression in(Object... values) {
                StringBuilder sb = new StringBuilder(" IN (");
                for (int i = 0; i < values.length; i++)
                {
                    sb.append(i > 0 ? ",?" : "?");
                }

                return setup((notIsCalled ? " NOT" : "") + sb.append(")"), values);
            }

            @Override
            public DAOExpression greaterThan(Object value) {
                return setup(notIsCalled ? "<=?" : ">?", value);
            }

            @Override
            public DAOExpression lessThan(Object value) {
                return setup(notIsCalled ? ">=?" : "<?", value);
            }

            @Override
            public DAOExpression isNull() {
                return setup(notIsCalled ? " IS NOT NULL" : " IS NULL");
            }

            void appendTo(Table table, StringBuilder sql, List<Object> whereArgs) {
                sql.append(param.getParam(table)).append(op);
                if (values != null)
                {
                    for (Object value : values)
                    {
                        whereArgs.add(value);
                    }
                }
            }
        }
    }

    /**
     * This is a convenient utility that helps build SQL语句
     */
    private static class DAOSQLBuilder<T> {

        final Class<T> c;

        final Table table;

        DAOExpression where;

        DAOSQLBuilder(Class<T> c) {
            table = Table.getTable(this.c = c);
        }

        public DAOSQLBuilder<T> where(DAOExpression expression) {
            where = expression;
            return this;
        }

        void appendWhere(StringBuilder sql, List<Object> args) {
            if (where != null) where.appendTo(table, sql.append(" WHERE "), args);
        }
        
        static String[] convertArgs(List<Object> args) {
            String[] strs = new String[args.size()];
            ListIterator<Object> iter = args.listIterator();
            int index = 0;
            while (iter.hasNext())
            {
                strs[index++] = String.valueOf(iter.next());
            }
            
            return strs;
        }
    }

    public class DAOEditBuilder<T> extends DAOSQLBuilder<T> {

        DAOEditBuilder(Class<T> c) {
            super(c);
        }

        @Override
        public DAOEditBuilder<T> where(DAOExpression expression) {
            super.where(expression);
            return this;
        }

        /**
         * 删除数据
         *
         * @return 是否有数据被删除
         */
        public boolean delete() {
            return remove(this);
        }

        /**
         * 修改数据
         *
         * @param bean JavaBean对象，映射到数据库的一张表
         * @param fields 需要修改的字段，不设置则修改所有字段
         * @return 是否有数据更改
         */
        public boolean update(T bean, String... fields) {
            return edit(this, bean, fields);
        }
    }

    /**
     * This is a convenient utility that helps build 数据库查询语句
     */
    public class DAOQueryBuilder<T> extends DAOSQLBuilder<T> {

        private DAOClause selection;                // 查询指定列

        private boolean isDistinct;                 // 消除重复的记录

        private DAOClause group;                    // 按条件进行分组

        private DAOExpression having;               // 分组上设置条件

        private DAOClause order;                    // 按指定顺序显示

        private boolean orderDesc;                  // 按降序进行排列

        private Page page;                          // 分页工具

        DAOQueryBuilder(Class<T> c) {
            super(c);
        }

        public DAOQueryBuilder<T> select(Object... params) {
            selection = DAOClause.create(params);
            return this;
        }

        public DAOQueryBuilder<T> distinct() {
            isDistinct = true;
            return this;
        }

        @Override
        public DAOQueryBuilder<T> where(DAOExpression expression) {
            super.where(expression);
            return this;
        }

        public DAOQueryBuilder<T> groupBy(Object... params) {
            group = DAOClause.create(params);
            return this;
        }

        public DAOSQLBuilder<T> having(DAOExpression expression) {
            having = expression;
            return this;
        }

        public DAOQueryBuilder<T> orderBy(Object... params) {
            orderDesc = false;
            order = DAOClause.create(params);
            return this;
        }

        public DAOQueryBuilder<T> orderDesc(Object... params) {
            orderDesc = true;
            order = DAOClause.create(params);
            return this;
        }

        /**
         * 使用分页技术
         *
         * @return 需设置分页参数
         */
        public Page usePage() {
            if (page == null) page = new Page(10);
            return page;
        }

        /**
         * 停止使用分页技术
         */
        public void stopPage() {
            page = null;
        }

        private static final int CONSTRAINT_COUNT = 1;

        private static final int CONSTRAINT_LIMIT = 2;

        private final StringBuilder sql = new StringBuilder(120);

        private final LinkedList<Object> args = new LinkedList<Object>();

        private void build(int constraint) {
            StringBuilder sql = this.sql;
            LinkedList<Object> args = this.args;

            appendSelection(sql, constraint);
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
                .append(page.getPageSize())
                .append(",")
                .append(page.getBeginRecord());
            }
        }

        private void appendSelection(StringBuilder sql, int constraint) {
            sql.append("SELECT ");

            if (constraint == CONSTRAINT_COUNT)
            {
                sql.append("COUNT(*)");
            }
            else
            {
                if (isDistinct)
                {
                    sql.append("DISTINCT ");
                }

                if (selection == null)
                {
                    sql.append("*");
                }
                else
                {
                    selection.appendTo(table, sql);
                }
            }

            sql.append(" FROM ").append(table.getTableName());
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
            return queryCount(getSql(), getArgs());
        }

        /**
         * 返回结果集游标
         */
        public Cursor getCursor() {
            build(0);
            return queryCursor(getSql(), getArgs());
        }

        /**
         * 获取数据表里第一条满足条件的数据，如没有则返回Null
         */
        public T get() {
            build(CONSTRAINT_LIMIT);
            try {
                Cursor cursor = rawQuery(getSql(), getArgs());
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
         * 获取满足条件的所有数据
         */
        public T[] getAll() {
            build(0);
            try {
                Cursor cursor = rawQuery(getSql(), getArgs());
                if (cursor != null)
                {
                    try {
                        @SuppressWarnings("unchecked")
                        T[] array = (T[]) Array.newInstance(c, cursor.getCount());
                        int index = 0;

                        while (cursor.moveToNext())
                        {
                            array[index++] = extractFromCursor(cursor, table, c);
                        }

                        return array;
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
         * 创建视图
         *
         * @param viewName 视图名称
         */
        public void createView(String viewName) {
            checkNull(viewName);

            StringBuilder sql = new StringBuilder()
            .append("CREATE VIEW ")
            .append(viewName)
            .append(" AS ");

            build(0);
            execute(sql.append(getSql()).toString(), getArgs());
        }
    }

    static
    {
        if (!ApplicationManager.isDebuggable())
        {
            LogFactory.addLogFile(DAOTemplate.class, "dao.txt");
        }
    }

    private static void LOG_DAOException(DAOException e) {
        log("数据库操作异常", e);
    }

    private static void LOG_SQL(String sql, Object[] bindArgs) {
        if (bindArgs != null)
        {
            StringBuilder sb = new StringBuilder(sql);
            int i = 0, index = 0;

            while ((index = sql.indexOf("?", index)) >= 0)
            {
                String arg = String.valueOf(bindArgs[i++]);
                sb.replace(index, index + 1, arg);
                index += arg.length();
            }
        }

        LOG_SQL(sql);
    }

    private static void LOG_SQL(String sql) {
        log("执行SQL语句", sql);
    }

    private static class DAOException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public DAOException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public DAOException(Throwable throwable) {
            super(throwable);
        }
    }

    /******************************* 华丽丽的分割线 *******************************/

    public static class ProviderTemplate {

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

        private boolean inTransaction;

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
                inTransaction = true;
                batch.applyBatch(this);
                context.getContentResolver().applyBatch(authority, operations);
                success = true;
            } catch (ProviderException e) {
                LOG_ProviderException(e);
            } catch (Exception e) {
                LOG_ProviderException(new ProviderException(e));
            } finally {
                inTransaction = false;
                operations.clear();
                if (printLog) log(LogUtil.getCallerStackFrame(), "批处理任务结束:success=" + success);
            }

            return success;
        }

        /**
         * 执行批处理操作时添加一条指令
         */
        public void addBatch(ContentProviderOperation operation) {
            if (inTransaction) operations.add(operation);
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

                    if (inTransaction)
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

                    if (inTransaction)
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

        <T> boolean remove(Uri url, DAOSQLBuilder<T> builder) {
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
                if (inTransaction)
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

        <T> boolean edit(Uri uri, DAOSQLBuilder<T> builder, T bean, String... fields) {
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

                if (inTransaction)
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

        Cursor query(Uri uri, Table table, String[] projection, String selection, String[] selectionArgs) {
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

        void processException(Exception t) {
            ProviderException e = new ProviderException(t);
            if (inTransaction)
            {
                throw e;
            }

            LOG_ProviderException(e);
        }

        private static void LOG_ProviderException(ProviderException e) {
            log("Provider操作异常", e);
        }
        
        private static class ProviderSQLBuilder<T> extends DAOSQLBuilder<T> {

            final Uri uri;

            ProviderSQLBuilder(Class<T> c, Uri uri) {
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
             *
             * @return 需设置分页参数
             */
            public Page usePage() {
                if (page == null) page = new Page(10);
                return page;
            }

            /**
             * 停止使用分页技术
             */
            public void stopPage() {
                page = null;
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
                    .append(page.getPageSize())
                    .append(",")
                    .append(page.getBeginRecord());
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
            void appendWhere(StringBuilder sql, List<Object> args) {
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
             * 获取满足条件的所有数据
             */
            public T[] getAll() {
                build(0);
                try {
                    Cursor cursor = queryCursor();
                    if (cursor != null)
                    {
                        try {
                            @SuppressWarnings("unchecked")
                            T[] array = (T[]) Array.newInstance(c, cursor.getCount());
                            int index = 0;

                            while (cursor.moveToNext())
                            {
                                array[index++] = extractFromCursor(cursor, table, c);
                            }

                            return array;
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

        private static class ProviderException extends RuntimeException {

            private static final long serialVersionUID = 1L;

            public ProviderException(Throwable throwable) {
                super(throwable);
            }
        }

        static
        {
            if (!ApplicationManager.isDebuggable())
            {
                LogFactory.addLogFile(ProviderTemplate.class, DAOTemplate.class);
            }
        }

        private static String SQL_WHERE_LOG(String sql, Object[] bindArgs) {
            if (TextUtils.isEmpty(sql)) return "";
            if (bindArgs != null)
            {
                StringBuilder sb = new StringBuilder(sql);
                int i = 0, index = 0;

                while ((index = sql.indexOf("?", index)) >= 0)
                {
                    String arg = String.valueOf(bindArgs[i++]);
                    sb.replace(index, index + 1, arg);
                    index += arg.length();
                }
            }

            return " WHERE " + sql;
        }
    }
}