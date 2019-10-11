package com.project.ui.more.setting;

import static com.project.network.action.Actions.LOGOUT;

import engine.android.core.annotation.OnClick;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.fragment.BaseInfoFragment;
import engine.android.util.ui.FastClickUtil.FastClickCounter;
import engine.android.widget.common.text.BadgeView;
import engine.android.widget.component.TitleBar;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.network.action.http.Logout;
import com.project.ui.login.LoginFragment;
import com.project.util.AppUtil;
import com.project.util.LogUploader;

/**
 * 设置界面
 * 
 * @author Daimon
 */
public class SettingFragment extends BaseInfoFragment implements OnClickListener {
    
    ViewHolder password;
    ViewHolder phone;
    ViewHolder version;
    ViewHolder about;
    
    FastClickCounter counter = new FastClickCounter(7);     // 日志上传后门
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(R.string.setting_title)
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(
                R.layout.setting_fragment, container, false);
        // 修改密码
        password = addComponent(root, inflater, R.string.setting_password, NO_TEXT, true);
        password.getConvertView().setOnClickListener(this);
        // 绑定手机
        phone = addComponent(root, inflater, R.string.setting_phone, NO_TEXT, true);
        // 版本更新
        if (MySession.getUpgradeInfo() != null)
        {
            BadgeView badge = new BadgeView(getContext());
            badge.setText("New");
            badge.setPadding(16, 4, 16, 4);
            
            version = addComponent(root, inflater, R.string.setting_version, badge, true);
            version.getConvertView().setOnClickListener(this);
            
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT;
            badge.setLayoutParams(params);
        }
        else
        {
            version = addComponent(root, inflater, R.string.setting_version, NO_TEXT, true);
        }
        // 关于我们
        about = addComponent(root, inflater, R.string.setting_about, NO_TEXT, true);
        about.getConvertView().setOnClickListener(this);
        // 退出登录按钮换位
        View view = root.findViewById(R.id.logout_container);
        root.removeViewInLayout(view);
        root.addView(view);

        return root;
    }
    
    @OnClick(R.id.logout)
    void logout() {
        if (getBaseActivity().checkNetworkStatus(true))
        {
            showProgress(R.string.progress_waiting);
            getBaseActivity().sendHttpRequest(new Logout());
        }
    }
    
    @Override
    public void onClick(View v) {
        if (v == password.getConvertView())
        {
            startFragment(EditPasswordFragment.class);
        }
        else if (v == version.getConvertView())
        {
            AppUtil.upgradeApp(getBaseActivity(), MySession.getUpgradeInfo(), false);
        }
        else if (v == about.getConvertView())
        {
            if (counter.count())
            {
                LogUploader.upload(getContext(), null);
                MyApp.showMessage("日志已上传");
            }
        }
    }
    
    @Override
    protected EventHandler registerEventHandler() {
        return new EventHandler();
    }
    
    private class EventHandler extends BaseActivity.EventHandler {
        
        public EventHandler() {
            super(LOGOUT);
        }

        @Override
        protected void onReceiveSuccess(String action, Object param) {
            // 清除缓存数据
            MySession.setUser(null);
            
            MyApp.getApp().getActivityStack().popupAllActivities();
            startFragment(LoginFragment.class);
        }
    }
}