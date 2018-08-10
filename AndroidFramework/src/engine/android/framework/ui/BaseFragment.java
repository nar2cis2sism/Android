package engine.android.framework.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.TextView;

import engine.android.core.Forelet.OnBackListener;
import engine.android.core.Forelet.ProgressSetting;
import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;
import engine.android.core.extra.EventBus.EventHandler;
import engine.android.framework.R;
import engine.android.framework.network.ConnectionStatus;
import engine.android.framework.util.GsonUtil;
import engine.android.util.Util;
import engine.android.widget.component.TitleBar;

public abstract class BaseFragment extends engine.android.core.BaseFragment implements EventHandler {
    
    private BaseActivity baseActivity;
    
    private OnBackListener onBackListener;
    
    private boolean menuVisible = true;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        if (activity instanceof BaseActivity)
        {
            baseActivity = (BaseActivity) activity;
            if (this instanceof OnBackListener)
            {
                baseActivity.addOnBackListener(onBackListener = (OnBackListener) this);
            }
        }
    }
    
    @Override
    public void onDetach() {
        if (onBackListener != null) baseActivity.removeOnBackListener(onBackListener);
        baseActivity = null;
        super.onDetach();
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
        return baseActivity == null || baseActivity.isFinishing() ? null : baseActivity.getTitleBar();
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
        .setPositiveButton(android.R.string.ok, null)
        .create();
    
        baseActivity.showDialog("dialog_error", dialog);
    }
    
    @Override
    public void onDestroy() {
        if (isReceiveEventEnabled) EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /******************************* 华丽丽的分割线 *******************************/
    
    /**
     * 传递参数构造器
     */
    public static final class ParamsBuilder {
        
        private static final String EXTRA_PARAMS = "params";
        
        public static <Params> Bundle build(Params params) {
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_PARAMS, GsonUtil.toJson(params));
            return bundle;
        }
        
        public static <Params> Params parse(Bundle bundle, Class<Params> cls) {
            if (bundle != null && bundle.containsKey(EXTRA_PARAMS))
            {
                return GsonUtil.parseJson(bundle.getString(EXTRA_PARAMS), cls);
            }
            
            return null;
        }
    }
    
    protected void showProgress(CharSequence message) {
        baseActivity.showProgress(ProgressSetting.getDefault().setMessage(message));
    }
    
    protected TextView newTextAction(CharSequence text, OnClickListener listener) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(getResources().getColorStateList(R.color.title_bar_action));
        if (listener != null) tv.setOnClickListener(listener);
        return tv;
    }
}