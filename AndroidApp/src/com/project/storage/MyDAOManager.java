package com.project.storage;

import android.content.Context;

import com.project.app.MyContext;
import com.project.storage.db.Friend;
import com.project.storage.db.Message;
import com.project.storage.db.User;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOExpression;
import engine.android.dao.DAOTemplate.DBUpdateListener;
import engine.android.util.extra.Singleton;

/**
 * 数据库管理器
 * 
 * @author Daimon
 */
public class MyDAOManager implements DBUpdateListener {

    /******************************* 数据库配置 *******************************/
    
    private static final String DB_NAME = "app.db";
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

    /**************************** 华丽丽的分割线 ****************************/
    
    private final DAOTemplate dao;
    
    private MyDAOManager(Context context) {
        dao = new DAOTemplate(context, DB_NAME, DB_VERSION, this);
    }

    /**
     * Creates the data repository. This is called when the provider 
     * attempts to open the repository and SQLite reports that it doesn't exist
     */
    @Override
    public void onCreate(DAOTemplate dao) {
        dao.createTable(User.class);
        dao.createTable(Friend.class);
        dao.createTable(Message.class);
    }

    @Override
    public void onUpdate(DAOTemplate dao, int oldVersion, int newVersion) {}
    
    public static class BaseDAO {
        
        protected static final DAOTemplate dao = getDAO();
        
        public static <T> T findItemByProperty(Class<T> cls, String property, Object value) {
            return dao.find(cls).where(DAOExpression.create(property).eq(value)).get();
        }
    }
}