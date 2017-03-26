package engine.android.framework.ui;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解耦设计
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class PresentManager {
    
    private Map<Class<? extends BasePresenter<?>>, BasePresenter<?>> map;
    
    public <P extends BasePresenter<C>, C> P addPresenter(Class<P> presenterCls) {
        return addPresenter(presenterCls, null);
    }
    
    /**
     * Must keep empty constructor of presenterCls for the instantiation.
     */
    public <P extends BasePresenter<C>, C> P addPresenter(Class<P> presenterCls, C callbacks) {
        try {
            P p = presenterCls.newInstance();
            if (map == null) map = new LinkedHashMap<Class<? extends BasePresenter<?>>, BasePresenter<?>>();
            map.put(presenterCls, p.setCallbacks(callbacks));
            return p;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public <P extends BasePresenter<C>, C> void addPresenter(P presenter) {
        addPresenter(presenter, null);
    }
    
    @SuppressWarnings("unchecked")
    public <P extends BasePresenter<C>, C> void addPresenter(P presenter, C callbacks) {
        if (map == null) map = new LinkedHashMap<Class<? extends BasePresenter<?>>, BasePresenter<?>>();
        map.put((Class<P>) presenter.getClass(), presenter.setCallbacks(callbacks));
    }
    
    public <P extends BasePresenter<C>, C> P getPresenter(Class<P> presenterCls) {
        if (map == null) return null;
        return presenterCls.cast(map.get(presenterCls));
    }
    
    public Collection<BasePresenter<?>> getPresenters() {
        if (map == null) return null;
        return map.values();
    }
    
    public static abstract class BasePresenter<Callbacks> {

        private Callbacks mCallbacks;

        /**
         * Must keep empty constructor for the instantiation.
         */
        public BasePresenter() {}

        public Callbacks getCallbacks() {
            return mCallbacks;
        }
        
        BasePresenter<Callbacks> setCallbacks(Callbacks callbacks) {
            mCallbacks = callbacks;
            return this;
        }
    }
}