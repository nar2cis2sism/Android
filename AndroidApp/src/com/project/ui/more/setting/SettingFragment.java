package com.project.ui.more.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.daimon.yueba.R;
import com.project.util.LogUploader;

import engine.android.core.ApplicationManager;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.framework.ui.extra.BaseInfoFragment;
import engine.android.util.AndroidUtil;
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
    Button logout;
    
    FastClickCounter counter = new FastClickCounter(7);     // 日志上传后门
//    User user;
//    
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        enableReceiveEvent(AVATAR, EDIT_USER_INFO);
//        addPresenter(new PhotoPresenter(this));
//        user = Util.clone(MySession.getUser());
//    }
    
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
        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        
        // 修改密码
        password = addComponent(root, inflater, 
                R.string.setting_password, NO_TEXT, true);
        // 更换手机号
        phone = addComponent(root, inflater, 
                R.string.setting_phone, NO_TEXT, true);
        // 版本更新
        version = addComponent(root, inflater, 
                R.string.setting_version, NO_TEXT, true);
        // 关于我们
        about = addComponent(root, inflater, 
                R.string.setting_about, NO_TEXT, true);
        about.getConvertView().setOnClickListener(this);
        // 退出登录
        logout = new Button(getContext());
        logout.setText(R.string.setting_logout);
//        logout.setOnClickListener(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = params.rightMargin = AndroidUtil.dp2px(getContext(), 24);
        params.topMargin = AndroidUtil.dp2px(getContext(), 100);
        root.addView(logout, params);

        return root;
    }
    
    @Override
    public void onClick(View v) {
        if (v == password.getConvertView())
        {
        }
        else if (v == phone.getConvertView())
        {
        }
        else if (v == version.getConvertView())
        {
        }
        else if (v == about.getConvertView())
        {
            if (counter.count())
            {
                LogUploader.upload(getContext());
                ApplicationManager.showMessage("日志已上传");
            }
        }
    }
//
//    /******************************* 修改个人信息 *******************************/
//    
//    private void sendEditUserInfoAction(User user, ChangeStatus status) {
//        EditUserInfo action = new EditUserInfo();
//        action.user = user;
//        action.status = status;
//        
//        getBaseActivity().sendHttpRequest(action);
//    }
//    
//    @Override
//    protected void onReceiveSuccess(String action, Object param) {
//        if (AVATAR.equals(action))
//        {
//            // 头像上传成功
//            getBaseActivity().hideProgress();
//            MyApp.showMessage(getString(R.string.toast_upload_avatar_success));
//            setupAvatar();
//        }
//        else if (EDIT_USER_INFO.equals(action))
//        {
//            getBaseActivity().hideProgress();
//            finish();
//        }
//    }
//    
//    @Override
//    protected void onReceiveFailure(String action, int status, Object param) {
//        if (AVATAR.equals(action))
//        {
//            // 头像上传失败
//            getBaseActivity().hideProgress();
//            MyApp.showMessage(getString(R.string.toast_upload_avatar_failure));
//        }
//        else
//        {
//            super.onReceiveFailure(action, status, param);
//        }
//    }
}