package engine.android.framework.ui;

import engine.android.core.Injector;
import engine.android.core.extra.EventBus;
import engine.android.framework.R;
import engine.android.framework.ui.BaseActivity.EventHandler;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

public abstract class BaseDialog extends Dialog {
    
    private BaseActivity baseActivity;

    public BaseDialog(Context context) {
        super(context, R.style.Theme_Dialog);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setup();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setup();
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        setup();
    }
    
    private void setup() {
        Injector.inject(this);
        setupParams(getWindow().getAttributes());
    }

    protected void setupParams(WindowManager.LayoutParams params) {}
    
    public final BaseActivity getBaseActivity() {
        if (baseActivity == null)
        {
            Activity activity = getOwnerActivity();
            if (activity instanceof BaseActivity)
            {
                baseActivity = (BaseActivity) activity;
            }
        }
        
        return baseActivity;
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
    public void onAttachedToWindow() {
        if ((handler = registerEventHandler()) != null)
        {
            BaseActivity.registerEventHandler(handler, getBaseActivity());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        if (handler != null) EventBus.getDefault().unregister(handler);
        super.onDetachedFromWindow();
    }
}