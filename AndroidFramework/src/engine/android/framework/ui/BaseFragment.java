package engine.android.framework.ui;

import android.app.Activity;

import engine.android.framework.network.event.Event;
import engine.android.framework.network.event.EventCallback;
import engine.android.framework.network.event.EventHandler;
import engine.android.framework.network.event.EventObserver;
import engine.android.widget.TitleBar;

public abstract class BaseFragment extends engine.android.core.BaseFragment implements EventHandler {
    
    private BaseActivity baseActivity;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        if (activity instanceof BaseActivity)
        {
            baseActivity = (BaseActivity) activity;
        }
    }
    
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

    /******************************* EventBus *******************************/
    
    private boolean isReceiveEventEnabled;
    
    /**
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
        getView().post(new Runnable() {
            
            @Override
            public void run() {
                onReceive(event.action, event.status, event.param);
            }
        });
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
    
    @Override
    public void onDestroy() {
        if (isReceiveEventEnabled)
        {
            EventObserver.getDefault().unregister(this);
        }
        
        super.onDestroy();
    }
}