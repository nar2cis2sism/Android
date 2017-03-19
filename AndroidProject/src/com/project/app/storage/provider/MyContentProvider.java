//package com.project.app.storage.provider;
//
//import static com.zhiku.provider.ProviderContract.AUTHORITY;
//
//import android.content.ContentProvider;
//import android.content.ContentProviderOperation;
//import android.content.ContentProviderResult;
//import android.content.ContentResolver;
//import android.content.ContentUris;
//import android.content.ContentValues;
//import android.content.OperationApplicationException;
//import android.content.UriMatcher;
//import android.database.Cursor;
//import android.database.SQLException;
//import android.database.sqlite.SQLiteDatabase;
//import android.net.Uri;
//import android.provider.BaseColumns;
//import android.text.TextUtils;
//
//import com.zhiku.provider.ProviderContract.Friend;
//import com.zhiku.storage.MyDAOManager;
//
//import engine.android.dao.DAOTemplate;
//
//import java.util.ArrayList;
//
//public class MyContentProvider extends ContentProvider {
//    
//    private static final UriMatcher matcher;
//    
//    private static final String CONTENT_TYPE = 
//            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY;
//    private static final String CONTENT_ITEM_TYPE = 
//            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY;
//    
//    private static final int ITEM = 1;
//    private static final int ITEM_ID = 2;
//    
//    static
//    {
//        // 传入匹配码如果大于0表示匹配根路径或传入-1，即常量UriMatcher.NO_MATCH表示不匹配根路径
//        // addURI()方法是用来增加其他URI匹配路径的：
//        // 第一个参数代表传入标识ContentProvider的AUTHORITY字符串
//        // 第二个参数是要匹配的路径，#代表任意数字，另外还可以用*来匹配任意文本
//        // 第三个参数必须传入一个大于零的匹配码，用于match()方法对相匹配的URI返回相对应的匹配码
//        matcher = new UriMatcher(UriMatcher.NO_MATCH);
//        
//        // TODO register tables
//        registerTable(Friend.TABLE);
//    }
//    
//    private static void registerTable(String table) {
//        matcher.addURI(AUTHORITY, table, ITEM);
//        matcher.addURI(AUTHORITY, table + "/#", ITEM_ID);
//    }
//    
//    private static String getTable(Uri uri, int match) {
//        String table = uri.getPath();
//        if (match == ITEM_ID)
//        {
//            table = table.substring(0, table.lastIndexOf("/"));
//        }
//        
//        if (table.startsWith("/"))
//        {
//            table = table.substring("/".length());
//        }
//        
//        return table;
//    }
//    
//    /**
//     * Defines a handle to the database helper object
//     */
//    private DAOTemplate dao;
//    
//    /**
//     * 每当ContentProvider启动时都会回调onCreate()方法。此方法主要执行一些ContentProvider初始化
//     * 的工作，返回true表示初始化成功，返回false则初始化失败
//     */
//    @Override
//    public boolean onCreate() {
//        dao = MyDAOManager.getDAO();
//        return true;
//    }
//    
//    /**
//     * 查询数据，将数据装入一个Cursor对象并返回
//     */
//    @Override
//    public Cursor query(Uri uri, String[] projection, String selection, 
//            String[] selectionArgs, String sortOrder) {
//        SQLiteDatabase db = dao.getDataBase();
//        Cursor c;
//        switch (matcher.match(uri)) {
//            case ITEM:
//                c = db.query(getTable(uri, ITEM), projection, selection, selectionArgs, 
//                        null, null, sortOrder);
//                break;
//            case ITEM_ID:
//                selection = BaseColumns._ID + "=" + uri.getLastPathSegment() + 
//                    (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
//                c = db.query(getTable(uri, ITEM_ID), projection, selection, selectionArgs, 
//                        null, null, sortOrder);
//                break;
//
//            default:
//                throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//        
//        c.setNotificationUri(getContext().getContentResolver(), uri);
//        return c;
//    }
//    
//    /**
//     * 是用来返回数据的MIME类型的方法。使用UriMatcher对URI进行匹配，并返回相应的MIME类型字符串
//     */
//    @Override
//    public String getType(Uri uri) {
//        switch (matcher.match(uri)) {
//            case ITEM:
//                return CONTENT_TYPE + "/" + getTable(uri, ITEM);
//            case ITEM_ID:
//                return CONTENT_ITEM_TYPE + "/" + getTable(uri, ITEM_ID);
//
//            default:
//                throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//    }
//    
//    /**
//     * 插入数据，返回新插入数据的URI，只接受数据集的URI，即指向表的URI
//     */
//    @Override
//    public Uri insert(Uri uri, ContentValues values) {
//        if (matcher.match(uri) != ITEM)
//        {
//            throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//
//        long rowId = dao.getDataBase().insert(getTable(uri, ITEM), null, values);
//        if (rowId > 0)
//        {
//            Uri data = ContentUris.withAppendedId(uri, rowId);
//            getContext().getContentResolver().notifyChange(data, null);
//            return data;
//        }
//        
//        throw new SQLException("Failed to insert row into " + uri);
//    }
//    
//    /**
//     * 用于数据的删除，返回的是所影响数据的数目，首先利用数据库辅助对象获取一个SQLiteDatabase对象
//     * 然后根据传入URI用UriMatcher进行匹配，对单个数据或数据集进行删除或修改。notifyChange()方法
//     * 用来通知注册在此URI上的观察者（observer）数据发生了改变
//     */
//    @Override
//    public int delete(Uri uri, String selection, String[] selectionArgs) {
//        SQLiteDatabase db = dao.getDataBase();
//        int count;
//        switch (matcher.match(uri)) {
//            case ITEM:
//                count = db.delete(getTable(uri, ITEM), selection, selectionArgs);
//                break;
//            case ITEM_ID:
//                selection = BaseColumns._ID + "=" + uri.getLastPathSegment() + 
//                    (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
//                count = db.delete(getTable(uri, ITEM_ID), selection, selectionArgs);
//                break;
//
//            default:
//                throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//        
//        getContext().getContentResolver().notifyChange(uri, null);
//        return count;
//    }
//    
//    /**
//     * 更新，与删除类方法类似
//     */
//    @Override
//    public int update(Uri uri, ContentValues values, String selection, 
//            String[] selectionArgs) {
//        SQLiteDatabase db = dao.getDataBase();
//        int count;
//        switch (matcher.match(uri)) {
//            case ITEM:
//                count = db.update(getTable(uri, ITEM), values, selection, selectionArgs);
//                break;
//            case ITEM_ID:
//                selection = BaseColumns._ID + "=" + uri.getLastPathSegment() + 
//                    (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
//                count = db.update(getTable(uri, ITEM_ID), values, selection, selectionArgs);
//                break;
//
//            default:
//                throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//        
//        getContext().getContentResolver().notifyChange(uri, null);
//        return count;
//    }
//    
//    @Override
//    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
//            throws OperationApplicationException {
//        SQLiteDatabase db = dao.getDataBase();
//        final int numOperations = operations.size();
//        final ContentProviderResult[] results = new ContentProviderResult[numOperations];
//        
//        db.beginTransaction();
//        try {
//            for (int i = 0; i < numOperations; i++)
//            {
//                results[i] = operations.get(i).apply(this, results, i);
//            }
//            
//            db.setTransactionSuccessful();
//        } finally {
//            db.endTransaction();
//        }
//
//        return results;
//    }
//}