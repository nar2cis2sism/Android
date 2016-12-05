package engine.android.framework.storage;

import android.content.Context;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DBUpdateListener;
import engine.android.framework.MyConfiguration.MyConfiguration_DB;
import engine.android.framework.MyContext;
import engine.android.util.Singleton;

/**
 * 数据库管理器
 * 
 * @author Daimon
 */
public class MyDAOManager implements MyConfiguration_DB, DBUpdateListener {
    
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
//        dao.createTable(Friend.class);
//        
//        if (MyConfiguration_APP.APP_TESTING)
//        {
//            DAOTestData.initTestData(dao);
//        }
    }

    @Override
    public void onUpdate(DAOTemplate dao, int oldVersion, int newVersion) {
        
    }
}