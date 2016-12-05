package engine.android.framework.ui;

import android.app.Activity;

import engine.android.framework.net.event.Event;
import engine.android.framework.net.event.EventHandler;
import engine.android.framework.net.event.EventObserver;
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
        getBaseActivity().runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                onReceive(event.action, event.status, event.param);
            }
        });
    }
    
    protected void onReceive(String action, int status, Object param) {
        if (getBaseActivity().isReceiveSuccess(status, param))
        {
            onReceiveSuccess(action, param);
        }
    }
    
    protected void onReceiveSuccess(String action, Object param) {}
    
    @Override
    public void onDestroy() {
        if (isReceiveEventEnabled)
        {
            EventObserver.getDefault().unregister(this);
        }
        
        super.onDestroy();
    }
}