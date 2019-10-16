package com.project.ui.login;

import static com.project.network.action.Actions.GET_SMS_CODE;
import static com.project.network.action.Actions.REGISTER;

import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.BaseFragment;
import engine.android.util.listener.MyTextWatcher;
import engine.android.util.ui.MyPasswordTransformationMethod;
import engine.android.util.ui.MyValidator.PatternValidation;
import engine.android.util.ui.MyValidator.Validation;
import engine.android.util.ui.NoUnderlineURL;
import engine.android.widget.common.button.CountDownButton;
import engine.android.widget.component.TitleBar;
import engine.android.widget.component.input.InputBox;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

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
    
    @InjectView(R.id.content)
    ViewGroup content;
    @InjectView(R.id.passcode)
    InputBox passcode;
    @InjectView(R.id.password)
    InputBox password;
    @InjectView(R.id.license)
    CheckBox license;
    
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
        
        setupUsername(username);
        setupPasscode(passcode);
        setupPassword(password);
        setupLicense(license);
        
        if (MyApp.getApp().isDebuggable())
        {
            username.input.setText("18222776787");
            passcode.input.setText("1234");
            password.input.setText("123456");
        }
    }
    
    private void setupUsername(final InputBox username) {
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

    private void setupPasscode(InputBox passcode) {
        passcode.setStyle(InputBox.STYLE_PASSCODE);
        passcode.sms_code.setVisibility(View.GONE);
        // 输入框
        EditText input = passcode.input;
        input.setHint(R.string.register_passcode_hint);
        // 字体示例
        input.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "fonts/font.ttc"));
    }
    
    private void setupPassword(InputBox password) {
        password.setStyle(InputBox.STYLE_PASSWORD);
        // 输入框
        EditText input = password.input;
        input.setHint(R.string.register_password_hint);
        // 密码符号示例
        input.setTransformationMethod(MyPasswordTransformationMethod.getInstance());
        
        getBaseActivity().bindValidation(input, new Validation<EditText>()
        .addValidation(new PatternValidation<EditText>(MyValidator.PASSWORD), 
                getString(R.string.register_password_validation_length)));
    }
    
    private void setupLicense(CheckBox license) {
        license.setMovementMethod(LinkMovementMethod.getInstance());
        NoUnderlineURL.replace(license);
        license.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                next.setEnabled(isChecked);
            }
        });
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
        
        if (content.getVisibility() == View.VISIBLE)
        {
            return;
        }
        
        content.setVisibility(View.VISIBLE);
        content.startAnimation(AnimationUtils.loadAnimation(
                getContext(), android.R.anim.fade_in));
        
        next.setVisibility(View.VISIBLE);
        next.startAnimation(AnimationUtils.loadAnimation(
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