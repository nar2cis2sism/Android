package com.project.ui.more.setting;

import static com.project.network.action.Actions.LOGOUT;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.network.action.http.Logout;
import com.project.ui.login.LoginFragment;
import com.project.util.LogUploader;

import engine.android.core.annotation.OnClick;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.framework.ui.extra.BaseInfoFragment;
import engine.android.util.ui.FastClickCounter;
import engine.android.widget.component.TitleBar;

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
//    User user;
//    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableReceiveEvent(LOGOUT);
//        addPresenter(new PhotoPresenter(this));
//        user = Util.clone(MySession.getUser());
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setTitle(R.string.setting_title)
        .setDisplayUpEnabled(true)
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(
                R.layout.setting_fragment, container, false);
        
        // 修改密码
        password = addComponent(root, inflater, 
                R.string.setting_password, NO_TEXT, true);
        // 绑定手机
        phone = addComponent(root, inflater, 
                R.string.setting_phone, NO_TEXT, true);
        // 版本更新
        version = addComponent(root, inflater, 
                R.string.setting_version, NO_TEXT, true);
        version.getConvertView().setOnClickListener(this);
        // 关于我们
        about = addComponent(root, inflater, 
                R.string.setting_about, NO_TEXT, true);
        about.getConvertView().setOnClickListener(this);
        // 退出登录按钮换位
        View logout = root.findViewById(R.id.logout);
        root.removeViewInLayout(logout);
        root.addView(logout);

        return root;
    }
    
    @OnClick(R.id.logout)
    void logout() {
        if (getBaseActivity().checkNetworkStatus(true))
        {
            showProgress(getString(R.string.progress_waiting));
            getBaseActivity().sendHttpRequest(new Logout());
        }
    }
    
    @Override
    public void onClick(View v) {
        if (v == version.getConvertView())
        {
        }
        else if (v == about.getConvertView())
        {
            if (counter.count())
            {
                LogUploader.upload(getContext());
                MyApp.showMessage("日志已上传");
            }
        }
    }
    
    @Override
    protected void onReceiveSuccess(String action, Object param) {
        if (LOGOUT.equals(action))
        {
            // 清除缓存数据
            MySession.setUser(null);
            
            MyApp.getApp().popupAllActivities();
            getBaseActivity().startFragment(LoginFragment.class);
        }
    }
}