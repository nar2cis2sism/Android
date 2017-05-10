package demo.provider;

import java.util.ArrayList;
import java.util.Random;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class MyContentProvider extends ContentProvider {
	
	public static final String DB_NAME = "MyContentProvider";
	public static final int DB_VERSION = 1;
	
    public static final String AUTHORITY = MyContentProvider.class.getName();
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    
    private static final UriMatcher matcher;
    
    private static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY;
    private static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY;
    
    private static final int ITEM = 1;
    private static final int ITEM_ID = 2;
    
    static
    {
    	//传入匹配码如果大于0表示匹配根路径或传入-1，即常量UriMatcher.NO_MATCH表示不匹配根路径
        //addURI()方法是用来增加其他URI匹配路径的：
        //第一个参数代表传入标识ContentProvider的AUTHORITY字符串
        //第二个参数是要匹配的路径，#代表任意数字，另外还可以用*来匹配任意文本
        //第三个参数必须传入一个大于零的匹配码，用于match()方法对相匹配的URI返回相对应的匹配码
    	matcher = new UriMatcher(UriMatcher.NO_MATCH);
    	
    	// TODO register tables
    	matcher.addURI(AUTHORITY, Groups.TABLE, ITEM);
    	matcher.addURI(AUTHORITY, Groups.TABLE + "/#", ITEM_ID);
    	
    	matcher.addURI(AUTHORITY, Contacts.TABLE, ITEM);
    	matcher.addURI(AUTHORITY, Contacts.TABLE + "/#", ITEM_ID);
    }
    
    private static String getTable(Uri uri, int match)
    {
    	String table = uri.getPath();
    	if (match == ITEM_ID)
    	{
    		table = table.substring(0, table.lastIndexOf("/"));
    	}
    	
    	if (table.startsWith("/"))
    	{
    		table = table.substring("/".length());
    	}
    	
    	return table;
    }
    
    private SQLiteOpenHelper dao;					//Defines a handle to the database helper object
    
    /**
     * 每当ContentProvider启动时都会回调onCreate()方法。此方法主要执行一些ContentProvider初始化
     * 的工作，返回true表示初始化成功，返回false则初始化失败
     */

	@Override
	public boolean onCreate() {
		dao = new MyDataBase(getContext());
		return true;
	}
	
	/**
	 * 是用来返回数据的MIME类型的方法。使用UriMatcher对URI进行匹配，并返回相应的MIME类型字符串
	 */
	
	@Override
	public String getType(Uri uri) {
		switch (matcher.match(uri)) {
		case ITEM:
			return CONTENT_TYPE + "/" + getTable(uri, ITEM);
		case ITEM_ID:
			return CONTENT_ITEM_TYPE + "/" + getTable(uri, ITEM_ID);
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/**
	 * 插入数据，返回新插入数据的URI，只接受数据集的URI，即指向表的URI
	 */

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (matcher.match(uri) != ITEM)
		{
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = dao.getWritableDatabase();
		long rowId;
		rowId = db.insert(getTable(uri, ITEM), null, values);
		if (rowId > 0)
		{
			Uri data = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(data, null);
			return data;
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}
	
	/**
	 * 用于数据的删除，返回的是所影响数据的数目，首先利用数据库辅助对象获取一个SQLiteDatabase对象
	 * 然后根据传入URI用UriMatcher进行匹配，对单个数据或数据集进行删除或修改。notifyChange()方法
	 * 用来通知注册在此URI上的观察者（observer）数据发生了改变
	 */

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dao.getWritableDatabase();
		int count;
		switch (matcher.match(uri)) {
		case ITEM:
			count = db.delete(getTable(uri, ITEM), selection, selectionArgs);
			break;
		case ITEM_ID:
			selection = BaseColumns._ID + "=" + uri.getLastPathSegment() + 
				(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
			count = db.delete(getTable(uri, ITEM_ID), selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	/**
	 * 更新，与删除类方法类似
	 */

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dao.getWritableDatabase();
		int count;
		switch (matcher.match(uri)) {
		case ITEM:
			count = db.update(getTable(uri, ITEM), values, selection, selectionArgs);
			break;
		case ITEM_ID:
			selection = BaseColumns._ID + "=" + uri.getLastPathSegment() + 
				(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
			count = db.update(getTable(uri, ITEM_ID), values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	/**
	 * 查询数据，将数据装入一个Cursor对象并返回
	 */
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dao.getWritableDatabase();
		Cursor c;
		switch (matcher.match(uri)) {
		case ITEM:
			c = db.query(getTable(uri, ITEM), projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case ITEM_ID:
			selection = BaseColumns._ID + "=" + uri.getLastPathSegment() + 
				(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
			c = db.query(getTable(uri, ITEM_ID), projection, selection, selectionArgs, null, null, sortOrder);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
	        throws OperationApplicationException {
        SQLiteDatabase db = dao.getWritableDatabase();
        final int numOperations = operations.size();
        final ContentProviderResult[] results = new ContentProviderResult[numOperations];

        db.beginTransaction();
        try {
            for (int i = 0; i < numOperations; i++)
            {
                results[i] = operations.get(i).apply(this, results, i);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return results;
	}

	/**
	 * Helper class that actually creates and manages the provider's underlying data repository
	 */
	
	protected static final class MyDataBase extends SQLiteOpenHelper {
		
		/**
		 * Instantiates an open helper for the provider's SQLite data repository
		 * Do not do database creation and upgrade here
		 */

		public MyDataBase(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		/**
		 * Creates the data repository. This is called when the provider attempts to open the
		 * repository and SQLite reports that it doesn't exist
		 */

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Creates the main table
			execSQL(db, Groups.SQL_CREATE);
			execSQL(db, Contacts.SQL_CREATE);
			
			// TODO initial data
			init(db);
		}
		
		private void init(SQLiteDatabase db)
		{
			//创建临时的组
			String[] groups = {"亲人","朋友","同学","同事","android开发组","老乡","篮球俱乐部"};
			for (int i = 0; i < groups.length; i++)
			{
				ContentValues values = new ContentValues();
				values.put(Groups.GROUP_NAME, groups[i]);
				db.insert(Groups.TABLE, null, values);
			}
			
			//创建临时的联系人
			String[] contacts = {
					"android",
					"google",
					"windows mobile",
					"microsoft",
					"symbian",
					"nokia",
					"bada",
					"sumsung",
					"IBM",
					"QQ"
			};
			Random random = new Random();
			for (int i = 0; i < contacts.length; i++)
			{
				ContentValues values = new ContentValues();
				values.put(Contacts.NAME, contacts[i]);
				values.put(Contacts.PHONE, "15927614509");
				values.put(Contacts.GROUP_NAME, groups[random.nextInt(groups.length)]);
				values.put(Contacts.BIRTHDAY, "1986-11-03");
				values.put(Contacts.ADDRESS, "杭州");
				values.put(Contacts.EMAIL, "lhb@163.com");
				values.put(Contacts.DESCRIPTION, "this is a scroll text,you can move cursor to here move it...");
				db.insert(Contacts.TABLE, null, values);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Upgrades the main table
			execSQL(db, Groups.SQL_DELETE);
			execSQL(db, Contacts.SQL_DELETE);
			
			onCreate(db);
		}
		
		private void execSQL(SQLiteDatabase db, String sql)
		{
			System.out.println(sql);
			db.execSQL(sql);
		}
	}
	
	/**
     * Contains helper classes used to create or manage {@link android.content.Intent Intents}
     * that involve contacts.
     */
    public static final class Intents {
    	
    	/**
         * Convenience class that contains string constants used
         * to create contact {@link android.content.Intent Intents}.
         */
    	public static final class Insert {
    
            /** The action code to use when adding a contact */
            public static final String ACTION = Intent.ACTION_INSERT;
    
            /** The type code to use when adding a contact */
            public static final String Type = AUTHORITY + "/android.insert";
    	}
    	
    	/**
         * Convenience class that contains string constants used
         * to edit contact {@link android.content.Intent Intents}.
         */
    	public static final class Edit {
    
            /** The action code to use when editing a contact */
            public static final String ACTION = Intent.ACTION_EDIT;
    
            /** The type code to use when editing a contact */
            public static final String Type = AUTHORITY + "/android.edit";
    	}
    }

    /**
	 * @see Groups
	 */

	protected interface GroupsColumns {
		
		/**
		 * 组名
         * <P>Type: TEXT</P>
		 */
        public static final String GROUP_NAME = "group_name";
		
	}
	
	public static final class Groups implements BaseColumns, GroupsColumns {
		
		public static final String TABLE = "groups";
		
		private Groups() {}
		
		/**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE);
        
        public static final String SQL_DELETE = 
        		"drop table if exists " + TABLE;
        
        public static final String SQL_CREATE = 
        		"create table if not exists " + TABLE + "\n(\n" + 
        		String.format("    %s integer primary key autoincrement,\n", _ID) +
        		"    " + GROUP_NAME + " text\n)";
	}
	
	/**
	 * @see Contacts
	 */

	protected interface ContactsColumns {
		
		/**
		 * 姓名
         * <P>Type: TEXT</P>
		 */
        public static final String NAME = "name";
		
		/**
		 * 头像
         * <P>Type: BLOB</P>
		 */
        public static final String HEAD_ICON = "head_icon";
		
		/**
		 * 电话
         * <P>Type: TEXT</P>
		 */
        public static final String PHONE = "phone";
		
		/**
		 * 生日
         * <P>Type: TEXT</P>
		 */
        public static final String BIRTHDAY = "birthday";
		
		/**
		 * 地址
         * <P>Type: TEXT</P>
		 */
        public static final String ADDRESS = "address";
		
		/**
		 * 邮箱
         * <P>Type: TEXT</P>
		 */
        public static final String EMAIL = "email";
		
		/**
		 * 好友描述
         * <P>Type: TEXT</P>
		 */
        public static final String DESCRIPTION = "description";
	}
	
	public static final class Contacts implements BaseColumns, ContactsColumns, GroupsColumns {
		
		public static final String TABLE = "contacts";
		
		private Contacts() {}
		
		/**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE);
        
        public static final String SQL_DELETE = 
        		"drop table if exists " + TABLE;
        
        public static final String SQL_CREATE = 
        		"create table if not exists " + TABLE + "\n(\n" + 
        		String.format("    %s integer primary key autoincrement,\n", _ID) +
        		"    " + NAME + " text,\n" +
        		"    " + HEAD_ICON + " blob,\n" +
        		"    " + PHONE + " text,\n" +
        		"    " + GROUP_NAME + " text,\n" +
        		"    " + BIRTHDAY + " text,\n" +
        		"    " + ADDRESS + " text,\n" +
        		"    " + EMAIL + " text,\n" +
        		"    " + DESCRIPTION + " text\n)";
	}
}