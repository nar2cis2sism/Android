package engine.android.framework.ui;

import android.app.Activity;
import android.os.Bundle;

import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;
import engine.android.core.extra.EventBus.EventHandler;
import engine.android.framework.network.ConnectionStatus;
import engine.android.widget.TitleBar;

public abstract class BaseFragment extends engine.android.core.BaseFragment implements EventHandler {
    
    private BaseActivity baseActivity;
    
    private Boolean menuVisible;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        if (activity instanceof BaseActivity)
        {
            baseActivity = (BaseActivity) activity;
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (menuVisible == null) setupTitleBar();
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        
        if ((this.menuVisible = menuVisible))
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
        return baseActivity != null ? baseActivity.getTitleBar() : null;
    }
    
    public final BaseActivity getBaseActivity() {
        return baseActivity;
    }

    /******************************* EventBus *******************************/
    
    private boolean isReceiveEventEnabled;
    
    /**
     * 允许接收事件回调<br>
     * Call it in {@link #onCreate(android.os.Bundle)}
     */
    protected final void enableReceiveEvent(String... actions) {
        if (isReceiveEventEnabled = true)
        {
            for (String action : actions)
            {
                EventBus.getDefault().register(action, this);
            }
        }
    }

    @Override
    public void handleEvent(Event event) {
        onReceive(event.action, event.status, event.param);
    }
    
    protected void onReceive(String action, int status, Object param) {
        if (status == ConnectionStatus.SUCCESS)
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
        if (baseActivity != null) baseActivity.onReceiveFailure(action, status, param);
    }
    
    @Override
    public void onDestroy() {
        if (isReceiveEventEnabled) EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}