package engine.android.framework.ui;

import engine.android.core.Forelet.ProgressSetting;
import engine.android.core.extra.EventBus;
import engine.android.framework.R;
import engine.android.framework.ui.BaseActivity.EventHandler;
import engine.android.framework.ui.activity.SinglePaneActivity;
import engine.android.framework.util.GsonUtil;
import engine.android.util.AndroidUtil;
import engine.android.util.ui.UIUtil;
import engine.android.widget.component.TitleBar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public abstract class BaseFragment extends engine.android.core.BaseFragment {
    
    private BaseActivity baseActivity;
    
    private boolean menuVisible = true;
    
    private Boolean 沉浸式状态栏_深色字体;
    
    public void apply沉浸式状态栏() {
        apply沉浸式状态栏(false);
    }

    public void apply沉浸式状态栏(boolean 深色字体) {
        沉浸式状态栏_深色字体 = 深色字体;
    }
    
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
            if (沉浸式状态栏_深色字体 != null) baseActivity.apply沉浸式状态栏(沉浸式状态栏_深色字体);
        }
    }
    
    protected void setupTitleBar(TitleBar titleBar) {}
    
    public final TitleBar getTitleBar() {
        return baseActivity == null || baseActivity.isFinishing() ? null : baseActivity.getTitleBar();
    }
    
    public final BaseActivity getBaseActivity() {
        return baseActivity;
    }
    
    @Override
    public Context getContext() {
        if (baseActivity != null) return baseActivity;
        return super.getContext();
    }

    /******************************* EventBus *******************************/
    
    private EventHandler handler;

    /**
     * 注册事件处理器
     */
    protected EventHandler registerEventHandler() {
        return null;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if ((handler = registerEventHandler()) != null)
        {
            BaseActivity.registerEventHandler(handler, getBaseActivity());
        }
    }
    
    @Override
    public void onDestroyView() {
        if (handler != null) EventBus.getDefault().unregister(handler);
        super.onDestroyView();
        View focus = getActivity().getCurrentFocus();
        if (focus != null)
        {
            UIUtil.hideSoftInput(focus);
        }
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

    /******************************* 华丽丽的分割线 *******************************/
    
    public TextView newTextAction(CharSequence text, OnClickListener listener) {
        TextView tv = new TextView(getContext());
        int padding = AndroidUtil.dp2px(getContext(), 6);
        tv.setPadding(padding, padding, padding, padding);
        tv.setTextColor(getResources().getColorStateList(R.color.title_bar_action));
        tv.setText(text);
        if (listener != null) tv.setOnClickListener(listener);
        return tv;
    }
    
    public void showProgress(int msgResId) {
        showProgress(getText(msgResId));
    }
    
    public void showProgress(CharSequence message) {
        baseActivity.showProgress(ProgressSetting.getDefault().setMessage(message), 100);
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
}