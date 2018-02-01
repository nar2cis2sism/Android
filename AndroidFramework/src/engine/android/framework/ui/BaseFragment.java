package engine.android.framework.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;
import engine.android.core.extra.EventBus.EventHandler;
import engine.android.framework.R;
import engine.android.framework.network.ConnectionStatus;
import engine.android.util.Util;
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
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (menuVisible) setupTitleBar();
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void setMenuVisibility(boolean menuVisible) {
        if (this.menuVisible = menuVisible) setupTitleBar();
        super.setMenuVisibility(menuVisible);
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
        isReceiveEventEnabled = true;
        for (String action : actions)
        {
            EventBus.getDefault().register(action, this);
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
        if (baseActivity != null)
        {
            baseActivity.hideProgress();
            showErrorDialog(baseActivity, param);
        }
    }
    
    protected void showErrorDialog(BaseActivity baseActivity, Object error) {
        Dialog dialog = new AlertDialog.Builder(baseActivity)
        .setTitle(R.string.dialog_error_title)
        .setMessage(Util.getString(error, null))
        .setPositiveButton(R.string.ok, null)
        .create();
    
        baseActivity.showDialog("dialog_error", dialog);
    }
    
    @Override
    public void onDestroy() {
        if (isReceiveEventEnabled) EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}