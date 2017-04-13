package engine.android.framework.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Collection;
import java.util.LinkedList;

import engine.android.framework.network.event.Event;
import engine.android.framework.network.event.EventObserver;
import engine.android.framework.network.event.EventObserver.EventCallback;
import engine.android.framework.network.event.EventObserver.EventHandler;
import engine.android.framework.ui.util.PresentManager;
import engine.android.framework.ui.util.PresentManager.BasePresenter;
import engine.android.widget.TitleBar;

public abstract class BaseFragment extends engine.android.core.BaseFragment implements EventHandler {
    
    private BaseActivity baseActivity;
    
    private boolean menuVisible = true;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        if (activity instanceof BaseActivity)
        {
            baseActivity = (BaseActivity) activity;
        }
        
        if (menuVisible) setupTitleBar();
    }
    
    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        
        if ((this.menuVisible = menuVisible) && getActivity() != null)
        {
            setupTitleBar();
        }
    }
    
    private void setupTitleBar() {
        TitleBar titleBar = getTitleBar();
        if (titleBar != null)
        {
            setupTitleBar(titleBar.reset());
        }
    }
    
    protected void setupTitleBar(TitleBar titleBar) {}
    
    public final TitleBar getTitleBar() {
        if (baseActivity != null)
        {
            return baseActivity.getTitleBar();
        }
        
        return null;
    }
    
    public final BaseActivity getBaseActivity() {
        return baseActivity;
    }

    /******************************* Presenter *******************************/
    
    private PresentManager presenterManager;
    
    private final Presenters presenters = new Presenters();
    
    private boolean addIsCalled;

    /**
     * Must keep empty constructor of presenterCls for the instantiation.
     */
    public <P extends Presenter<C>, C extends BaseFragment> P addPresenter(Class<P> presenterCls) {
        if (presenterManager == null) presenterManager = new PresentManager();
        P p = presenterManager.addPresenter(presenterCls);
        p.setCallbacks(this).onCreate(getContext());
        addIsCalled = true;
        return p;
    }
    
    public <P extends Presenter<C>, C extends BaseFragment> void addPresenter(P p) {
        if (presenterManager == null) presenterManager = new PresentManager();
        presenterManager.addPresenter(p);
        p.setCallbacks(this).onCreate(getContext());
        addIsCalled = true;
    }
    
    public <P extends Presenter<C>, C extends BaseFragment> P getPresenter(Class<P> presenterCls) {
        if (presenterManager == null) return null;
        return presenterManager.getPresenter(presenterCls);
    }
    
    private Presenters getPresenters() {
        if (addIsCalled)
        {
            addIsCalled = false;
            presenters.set(presenterManager.getPresenters());
        }
        
        return presenters;
    }
    
    public static abstract class Presenter<C extends BaseFragment> extends BasePresenter<C> {
        
        protected void onCreate(Context context) {}
        
        protected void onActivityCreated(Bundle savedInstanceState) {}
        
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {}
        
        protected void onStart() {}
        
        protected void onSaveInstanceState(Bundle outState) {}
        
        protected void onStop() {}
        
        protected void onDestroy() {}
        
        @SuppressWarnings("unchecked")
        @Override
        protected final Presenter<C> setCallbacks(BaseFragment callbacks) {
            super.setCallbacks((C) callbacks);
            return this;
        }
    }

    @SuppressWarnings("rawtypes")
    private static class Presenters {
        
        private LinkedList<Presenter> presenters;
        
        public void set(Collection<BasePresenter<?>> collection) {
            if (presenters == null)
            {
                presenters = new LinkedList<Presenter>();
            }
            else
            {
                presenters.clear();
            }
            
            for (BasePresenter<?> p : collection)
            {
                if (p instanceof Presenter)
                {
                    presenters.add((Presenter) p);
                }
            }
        }
        
        public void onActivityCreated(Bundle savedInstanceState) {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onActivityCreated(savedInstanceState);
        }
        
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onActivityResult(requestCode, resultCode, data);
        }
        
        public void onStart() {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onStart();
        }
        
        public void onSaveInstanceState(Bundle outState) {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onSaveInstanceState(outState);
        }
        
        public void onStop() {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onStop();
        }
        
        public void onDestroy() {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onDestroy();
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPresenters().onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getPresenters().onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        getPresenters().onStart();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getPresenters().onSaveInstanceState(outState);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        getPresenters().onStop();
    }
    
    @Override
    public void onDestroy() {
        unregisterEvent();
        getPresenters().onDestroy();
        super.onDestroy();
    }

    /******************************* EventBus *******************************/
    
    private boolean isReceiveEventEnabled;
    
    /**
     * 允许接收事件回调<br>
     * Call it in {@link #onCreate(android.os.Bundle)}
     */
    protected void enableReceiveEvent(String... actions) {
        if (isReceiveEventEnabled = true)
        {
            for (String action : actions)
            {
                EventObserver.getDefault().register(action, this);
            }
        }
    }

    @Override
    public void handleEvent(final Event event) {
        onReceive(event.action, event.status, event.param);
    }
    
    private void onReceive(String action, int status, Object param) {
        if (status == EventCallback.SUCCESS)
        {
            onReceiveSuccess(action, param);
        }
        else
        {
            onReceiveFailure(action, status, param);
        }
    }
    
    protected void onReceiveSuccess(String action, Object param) {}
    
    protected void onReceiveFailure(String action, int status, Object param) {
        baseActivity.hideProgress();
        baseActivity.showErrorDialog(param);
    }
    
    private void unregisterEvent() {
        if (isReceiveEventEnabled)
        {
            EventObserver.getDefault().unregister(this);
        }
    }
}