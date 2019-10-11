package com.project.ui.more.setting;

import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.framework.ui.BaseFragment;
import engine.android.util.listener.MyTextWatcher;
import engine.android.widget.component.TitleBar;
import engine.android.widget.component.input.InputBox;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.daimon.yueba.R;
import com.project.util.MyValidator;

/**
 * 修改密码
 * 
 * @author Daimon
 */
public class EditPasswordFragment extends BaseFragment {
    
    @InjectView(R.id.old_password)
    InputBox old_password;
    @InjectView(R.id.password)
    InputBox password;
    @InjectView(R.id.copy)
    InputBox copy;
    
    @InjectView(R.id.tip)
    TextView tip;
    
    @InjectView(R.id.ok)
    Button ok;
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(R.string.edit_password_title)
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_password_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupPassword(old_password, R.string.edit_password_old_hint, null);
        setupPassword(password, R.string.edit_password_new_hint, new MyTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                ok.setEnabled(MyValidator.validate(s.toString(), MyValidator.PASSWORD));
            }
        });
        setupPassword(copy, R.string.edit_password_copy_hint, null);
    }
    
    private void setupPassword(InputBox password, int hintRes, TextWatcher watcher) {
        password.setStyle(InputBox.STYLE_PASSWORD);
        password.enableClear();
        // 输入框
        EditText input = password.input;
        input.setHint(hintRes);
        if (watcher != null) input.addTextChangedListener(watcher);
    }
    
    @OnClick(R.id.ok)
    void ok() {
        String password = this.password.input.getText().toString();
        String copy = this.copy.input.getText().toString();
        if (!TextUtils.equals(password, copy))
        {
            showTip(true);
            return;
        }

        showTip(false);
        if (getBaseActivity().checkNetworkStatus(true))
        {
            showProgress(R.string.progress_waiting);
//            EditLoginPassword action = new EditLoginPassword(password);
//            String old_password = this.old_password.getInput().getText().toString();
//            action.oldPassword = AppUtil.encryptPassword(old_password);
//            getBaseActivity().sendHttpRequest(action);
        }
    }

    private void showTip(boolean shown) {
        tip.setVisibility(shown ? View.VISIBLE : View.INVISIBLE);
    }
//    
//    @Override
//    protected EventHandler registerEventHandler() {
//        return new EventHandler();
//    }
//    
//    private class EventHandler extends BaseActivity.EventHandler {
//        
//        public EventHandler() {
//            super(LOGOUT);
//        }
//
//        @Override
//        protected void onReceiveSuccess(String action, Object param) {
//            // 清除缓存数据
//            MySession.setUser(null);
//            
//            MyApp.getApp().getActivityStack().popupAllActivities();
//            startFragment(LoginFragment.class);
//        }
//    }
}