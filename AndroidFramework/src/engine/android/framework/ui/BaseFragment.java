package engine.android.framework.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import java.util.Collection;
import java.util.LinkedList;

import engine.android.framework.ui.PresentManager.BasePresenter;

public abstract class BaseFragment extends NetworkFragment {
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getPresenters().onViewCreated(view, savedInstanceState);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPresenters().onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }
    
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    
    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }
    
    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
    }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    /******************************* Presenter *******************************/
    
    private PresentManager presenterManager;
    
    private final Presenters presenters = new Presenters();
    
    private boolean addIsCalled;

    /**
     * Must keep empty constructor of presenterCls for the instantiation.
     */
    public <P extends Presenter> P addPresenter(Class<P> presenterCls) {
        if (presenterManager == null) presenterManager = new PresentManager();
        P p = presenterManager.addPresenter(presenterCls, this);
        p.onCreate(getContext());
        addIsCalled = true;
        return p;
    }
    
    public <P extends Presenter> void addPresenter(P p) {
        if (presenterManager == null) presenterManager = new PresentManager();
        presenterManager.addPresenter(p, this);
        p.onCreate(getContext());
        addIsCalled = true;
    }
    
    public <P extends Presenter> P getPresenter(Class<P> presenterCls) {
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
    
    public static abstract class Presenter extends BasePresenter<BaseFragment> {
        
        public void onCreate(Context context) {}
        
        public void onViewCreated(View view, Bundle savedInstanceState) {}
        
        public void onActivityCreated(Bundle savedInstanceState) {}
    }
    
    private static class Presenters extends Presenter {
        
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
        
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onViewCreated(view, savedInstanceState);
        }
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onActivityCreated(savedInstanceState);
        }
    }
}