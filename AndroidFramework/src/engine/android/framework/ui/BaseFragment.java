package engine.android.framework.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.TextView;

import engine.android.core.Forelet.ProgressSetting;
import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;
import engine.android.framework.R;
import engine.android.framework.network.ConnectionStatus;
import engine.android.framework.ui.extra.SinglePaneActivity;
import engine.android.framework.util.GsonUtil;
import engine.android.util.Util;
import engine.android.widget.component.TitleBar;

public abstract class BaseFragment extends engine.android.core.BaseFragment {
    
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
    public void onDetach() {
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
    
    private EventHandler handler;
    
    public static class EventHandler implements engine.android.core.extra.EventBus.EventHandler {
        
        private final String[] actions;
        
        private BaseActivity baseActivity;
        
        public EventHandler(String... actions) {
            this.actions = actions;
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
            if (baseActivity == null) return;
            hideProgress();
            showErrorDialog(param);
        }
        
        protected void hideProgress() {
            baseActivity.hideProgress();
        }
        
        protected void showErrorDialog(Object error) {
            Dialog dialog = new AlertDialog.Builder(baseActivity)
            .setTitle(R.string.dialog_error_title)
            .setMessage(Util.getString(error, null))
            .setPositiveButton(android.R.string.ok, null)
            .create();
        
            baseActivity.showDialog("dialog_error", dialog);
        }
    }
    
    /**
     * 注册事件处理器<br>
     * Call it in {@link #onCreate(android.os.Bundle)}
     */
    public final void registerEventHandler(EventHandler handler) {
        if (handler != null && this.handler == null)
        {
            String[] actions = handler.actions;
            if (actions != null && actions.length > 0)
            {
                (this.handler = handler).baseActivity = getBaseActivity();
                for (String action : actions)
                {
                    EventBus.getDefault().register(action, handler);
                }
            }
        }
    }
    
    @Override
    public void onDestroy() {
        if (handler != null) EventBus.getDefault().unregister(handler);
        super.onDestroy();
    }
    
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

    /**
     * Provide a convenient way to start fragment wrapped in {@link SinglePaneActivity}
     * (需要在Manifest中注册)
     */
    public void startFragment(Class<? extends Fragment> fragmentCls) {
        startFragment(fragmentCls, null);
    }
    
    /**
     * Provide a convenient way to start fragment wrapped in {@link SinglePaneActivity}
     * (需要在Manifest中注册)
     */
    public void startFragment(Class<? extends Fragment> fragmentCls, Bundle args) {
        startActivity(SinglePaneActivity.buildIntent(getContext(), fragmentCls, args));
    }

    /******************************* 华丽丽的分割线 *******************************/
    
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