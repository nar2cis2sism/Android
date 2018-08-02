package com.project.ui.login.register;

import static com.project.network.action.Actions.GET_SMS_CODE;
import static com.project.network.action.Actions.REGISTER;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.network.action.http.GetSmsCode;
import com.project.network.action.http.Register;
import com.project.util.MyValidator;

import engine.android.core.Forelet.ProgressSetting;
import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.framework.ui.BaseFragment;
import engine.android.framework.ui.widget.SmsCodeButton;
import engine.android.util.ui.MyValidator.PatternValidation;
import engine.android.util.ui.MyValidator.Validation;
import engine.android.widget.component.InputBox;
import engine.android.widget.component.TitleBar;

/**
 * 注册界面
 * 
 * @author Daimon
 */
public class RegisterFragment extends BaseFragment {
    
    @InjectView(R.id.username)
    InputBox username;
    SmsCodeButton smsCode;
    
    @InjectView(R.id.passcode)
    InputBox passcode;

    @InjectView(R.id.password)
    InputBox password;
    
    @InjectView(R.id.bottom)
    LinearLayout bottom;

    @InjectView(R.id.next)
    Button next;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        enableReceiveEvent(GET_SMS_CODE, REGISTER);
    }
    
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
            username.input().setText("18318066253");
            password.input().setText("password");
        }
    }
    
    private void setupUsername() {
        // 输入框
        final EditText input = username.input();
        input.setHint(R.string.register_username_hint);
        input.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        input.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                smsCode.setEnabled(MyValidator.validate(
                        s.toString(), MyValidator.MOBILE_NUMBER));
            }
        });
        // 获取验证码
        smsCode = new SmsCodeButton(getContext());
        smsCode.setEnabled(false);
        smsCode.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                getSmsCode();
            }
        });
        username.place(smsCode);
    }
    
    private void setupPasscode() {
        passcode.input().setHint(R.string.register_passcode_hint);
    }
    
    private void setupPassword() {
        // 输入框
        final EditText input = password.input();
        input.setHint(R.string.register_password_hint);
        input.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        
        getBaseActivity().bindValidation(input, new Validation<EditText>()
        .addValidation(new PatternValidation<EditText>(MyValidator.VALID), 
                getString(R.string.login_password_validation_empty))
        .addValidation(new PatternValidation<EditText>(MyValidator.LOGIN_PASSWORD), 
                getString(R.string.login_password_validation_length)));
    }
    
    void getSmsCode() {
        if (getBaseActivity().checkNetworkStatus(true))
        {
            sendGetSmsCodeAction();
            showNext();
        }
    }
    
    /**
     * 显示“下一步”
     */
    private void showNext() {
        username.input().setEnabled(false);
        smsCode.n秒后可重新获取验证码(60);
        
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
                getContext(), R.anim.slide_up));
    }
    
    @OnClick(R.id.next)
    void next() {
        if (getBaseActivity().checkNetworkStatus(true)
        &&  getBaseActivity().requestValidation())
        {
            getBaseActivity().showProgress(ProgressSetting.getDefault()
            .setMessage(getString(R.string.progress_waiting)));
            
            sendRegisterAction();
        }
    }

    /******************************* 获取手机验证码 *******************************/
    
    private void sendGetSmsCodeAction() {
        GetSmsCode action = new GetSmsCode(username.input().getText().toString());
        action.type = 1;
        
        getBaseActivity().sendHttpRequest(action);
    }

    /******************************* 用户注册 *******************************/
    
    private void sendRegisterAction() {
        Register action = new Register(
                username.input().getText().toString(), 
                password.input().getText().toString());
        action.passport = passcode.input().getText().toString();
        
        getBaseActivity().sendHttpRequest(action);
    }
    
    @Override
    protected void onReceiveSuccess(String action, Object param) {
        if (REGISTER.equals(action))
        {
            getBaseActivity().hideProgress();
            finish();
        }
    }
}