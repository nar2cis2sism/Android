package com.project.ui.login.register;

import static com.project.network.action.Actions.GET_SMS_CODE;
import static com.project.network.action.Actions.REGISTER;

import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.BaseFragment;
import engine.android.util.listener.MyTextWatcher;
import engine.android.util.ui.MyValidator.PatternValidation;
import engine.android.util.ui.MyValidator.Validation;
import engine.android.widget.common.button.CountDownButton;
import engine.android.widget.component.TitleBar;
import engine.android.widget.component.input.InputBox;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.bean.ErrorInfo;
import com.project.network.action.http.GetSmsCode;
import com.project.network.action.http.Register;
import com.project.util.MyValidator;

/**
 * 注册界面
 * 
 * @author Daimon
 */
public class RegisterFragment extends BaseFragment {
    
    @InjectView(R.id.username)
    InputBox username;
    @InjectView(R.id.passcode)
    InputBox passcode;
    @InjectView(R.id.password)
    InputBox password;
    
    @InjectView(R.id.bottom)
    LinearLayout bottom;
    @InjectView(R.id.next)
    Button next;
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(R.string.register_title)
        .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupUsername();
        setupPasscode();
        setupPassword();
        
        if (MyApp.getApp().isDebuggable())
        {
            username.input.setText("18222776787");
            password.input.setText("123456");
        }
    }
    
    private void setupUsername() {
        username.setStyle(InputBox.STYLE_MOBILE);
        // 输入框
        EditText input = username.input;
        input.setHint(R.string.register_username_hint);
        input.addTextChangedListener(new MyTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                username.sms_code.setEnabled(MyValidator.validate(s.toString(), MyValidator.MOBILE_NUMBER));
            }
        });
        // 获取验证码
        CountDownButton sms_code = username.sms_code;
        sms_code.setVisibility(View.VISIBLE);
        sms_code.setEnabled(false);
        sms_code.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (getBaseActivity().checkNetworkStatus(true))
                {
                    showProgress(R.string.progress_waiting);
            
                    GetSmsCode action = new GetSmsCode(username.input.getText().toString());
                    action.duplication = 1;
                    getBaseActivity().sendHttpRequest(action);
                }
            }
        });
    }

    private void setupPasscode() {
        passcode.setStyle(InputBox.STYLE_PASSCODE);
        passcode.input.setHint(R.string.register_passcode_hint);
        passcode.sms_code.setVisibility(View.GONE);
    }
    
    private void setupPassword() {
        password.setStyle(InputBox.STYLE_PASSWORD);
        // 输入框
        EditText input = password.input;
        input.setHint(R.string.register_password_hint);
        
        getBaseActivity().bindValidation(input, new Validation<EditText>()
        .addValidation(new PatternValidation<EditText>(MyValidator.PASSWORD), 
                getString(R.string.register_password_validation_length)));
    }
    
    @OnClick(R.id.next)
    void next() {
        if (getBaseActivity().requestValidation()
        &&  getBaseActivity().checkNetworkStatus(true))
        {
            showProgress(R.string.progress_waiting);
            
            Register action = new Register(
                    username.input.getText().toString(), 
                    password.input.getText().toString());
            action.passport = passcode.input.getText().toString();
            getBaseActivity().sendHttpRequest(action);
        }
    }

    /**
     * 显示“下一步”
     */
    void showNext() {
        username.input.setEnabled(false);
        username.sms_code.start(60, "已发送(%d)");
        
        if (bottom.getVisibility() == View.VISIBLE)
        {
            return;
        }
        
        passcode.setVisibility(View.VISIBLE);
        passcode.startAnimation(AnimationUtils.loadAnimation(
                getContext(), android.R.anim.fade_in));
        
        password.setVisibility(View.VISIBLE);
        password.startAnimation(AnimationUtils.loadAnimation(
                getContext(), android.R.anim.fade_in));
        
        bottom.setVisibility(View.VISIBLE);
        bottom.startAnimation(AnimationUtils.loadAnimation(
                getContext(), R.anim.slide_up_in));
    }
    
    @Override
    protected EventHandler registerEventHandler() {
        return new EventHandler();
    }
    
    private class EventHandler extends BaseActivity.EventHandler {
        
        public EventHandler() {
            super(GET_SMS_CODE, REGISTER);
        }

        @Override
        protected void onReceiveSuccess(String action, Object param) {
            if (GET_SMS_CODE.equals(action))
            {
                hideProgress();
                showNext();
                MyApp.showMessage(getString(R.string.toast_sms_code));
            }
            else if (REGISTER.equals(action))
            {
                finish();
            }
        }
        
        @Override
        protected void onReceiveFailure(String action, int status, Object param) {
            if (param instanceof ErrorInfo)
            {
                ErrorInfo info = (ErrorInfo) param;
                if (info.code == 415)
                {
                    // 数据已存在
                    hideProgress();
                    MyApp.showMessage(getString(R.string.register_username_exist));
                    return;
                }
            }
            
            super.onReceiveFailure(action, status, param);
        }
    }
}