package com.project.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.project.app.MyContext;
import com.project.storage.db.Friend;

import java.io.File;
import java.io.FileOutputStream;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOExpression;
import engine.android.dao.DAOTemplate.DBUpdateListener;
import engine.android.util.extra.Singleton;
import engine.android.util.file.FileManager;
import engine.android.util.io.IOUtil;
import engine.android.util.manager.SDCardManager;

/**
 * 数据库管理器
 * 
 * @author Daimon
 */
public class MyDAOManager implements DBUpdateListener {

    /******************************* 数据库配置 *******************************/
    
    private static final String DB_NAME = "project.db";
    private static final int DB_VERSION = 1;
    
    private static final Singleton<MyDAOManager> instance
    = new Singleton<MyDAOManager>() {
        
        @Override
        protected MyDAOManager create() {
            return new MyDAOManager(MyContext.getContext());
        }
    };
    
    public static final DAOTemplate getDAO() {
        return instance.get().dao;
    }
    
    /**
     * 加载第三方数据库
     * 
     * @param assetsPath assets目录下的数据库路径
     */
    public static SQLiteDatabase loadAssetsDB(Context context, String assetsPath) {
        File db_file = new File(SDCardManager.openSDCardAppDir(context), assetsPath);
        
        if (!db_file.exists())
        {
            FileManager.createFileIfNecessary(db_file);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(db_file);
                IOUtil.writeStream(context.getAssets().open(assetsPath), fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtil.closeSilently(fos);
            }
        }
        
        return SQLiteDatabase.openOrCreateDatabase(db_file, null);
    }

    /**************************** 华丽丽的分割线 ****************************/
    
    final DAOTemplate dao;
    
    MyDAOManager(Context context) {
        dao = new DAOTemplate(context, DB_NAME, DB_VERSION, this);
    }

    /**
     * Creates the data repository. This is called when the provider 
     * attempts to open the repository and SQLite reports that it doesn't exist
     */
    @Override
    public void onCreate(DAOTemplate dao) {
        dao.createTable(Friend.class);
    }

    @Override
    public void onUpdate(DAOTemplate dao, int oldVersion, int newVersion) {}
    
    public static class BaseDAO {
        
        protected static final DAOTemplate dao = getDAO();
        
        public static <T> T findItemByProperty(Class<T> cls, String property, Object value) {
            return dao.find(cls).where(DAOExpression.create(property).equal(value)).get();
        }
    }
}