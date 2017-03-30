package engine.android.dao.util;

import android.content.Context;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOListener;

public abstract class JavaBeanLoader<D> extends engine.android.core.extra.JavaBeanLoader<D> {

    protected final DAOTemplate dao;

    public JavaBeanLoader(Context context, DAOTemplate dao) {
        super(context);
        this.dao = dao;
    }
    
    /**
     * 监听数据库表的变动
     */
    protected <T> void listen(Class<T> dbClass) {
        setDataChangeObserver(new DBChangeObserver<T>(dbClass));
    }
    
    protected class DBChangeObserver<T> extends DataChangeObserver implements DAOListener {
        
        private final Class<T> cls;
        
        public DBChangeObserver(Class<T> dbClass) {
            cls = dbClass;
        }
        
        @Override
        public void registerObserver(Context context) {
            dao.registerListener(cls, this);
        }
        
        @Override
        public void unregisterObserver(Context context) {
            dao.unregisterListener(cls, this);
        }

        @Override
        public void onChange() {
            refresh();
        }
    }
}