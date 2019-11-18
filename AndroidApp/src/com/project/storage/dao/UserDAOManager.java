package com.project.storage.dao;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOExpression;
import engine.android.dao.DAOTemplate.DBUpdateListener;

import android.content.Context;

import com.project.app.MyContext;
import com.project.storage.db.Friend;
import com.project.storage.db.Message;

/**
 * 账号关联数据库管理
 * 
 * @author Daimon
 */
public class UserDAOManager implements DBUpdateListener {

    /******************************* 数据库配置 *******************************/
    
    private static final String DB_NAME = "user_";
    private static final int DB_VERSION = 1;
    
    /**
     * 切换账号时调用
     */
    public static synchronized void changeUser(String username, boolean reset) {
        if (BaseDAO.dao != null) BaseDAO.dao.close();
        BaseDAO.dao = new UserDAOManager(MyContext.getContext(), username).dao;
        if (reset) BaseDAO.dao.deleteSelf();
        BaseDAO.dao.getDataBase();
    }

    /**************************** 华丽丽的分割线 ****************************/
    
    private final DAOTemplate dao;
    
    private UserDAOManager(Context context, String username) {
        dao = new DAOTemplate(context, DB_NAME + username, DB_VERSION, this);
    }

    /**
     * Creates the data repository. This is called when the provider 
     * attempts to open the repository and SQLite reports that it doesn't exist
     */
    @Override
    public void onCreate(DAOTemplate dao) {
        dao.createTable(Friend.class, false);
        dao.createTable(Message.class, false);
    }

    @Override
    public void onUpdate(DAOTemplate dao, int oldVersion, int newVersion) {}
    
    public static class BaseDAO {
        
        public static DAOTemplate dao;
        
        public static <T> T findItemByProperty(Class<T> cls, String property, Object value) {
            return dao.find(cls).where(DAOExpression.create(property).eq(value)).get();
        }
    }
}