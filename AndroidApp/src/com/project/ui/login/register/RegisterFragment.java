package com.project.ui.login.register;

import static com.project.network.action.Actions.GET_SMS_CODE;
import static com.project.network.action.Actions.REGISTER;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.bean.ErrorInfo;
import com.project.network.action.http.GetSmsCode;
import com.project.network.action.http.Register;
import com.project.util.AppUtil;
import com.project.util.MyValidator;

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
        registerEventHandler(new EventHandler());
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
            username.input().setText("18222776787");
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
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                smsCode.setEnabled(MyValidator.validate(s.toString(), MyValidator.MOBILE_NUMBER));
            }
        });
        // 获取验证码
        smsCode = new SmsCodeButton(getContext());
        smsCode.setEnabled(false);
        smsCode.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (getBaseActivity().checkNetworkStatus(true))
                {
                    showProgress(getString(R.string.progress_waiting));
            
                    GetSmsCode action = new GetSmsCode(username.input().getText().toString());
                    action.duplication = 1;
                    getBaseActivity().sendHttpRequest(action);
                }
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
        input.setKeyListener(AppUtil.passwordKeyListener);
        input.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        
        // 密码显示
        ImageView eye = new ImageView(getContext());
        eye.setImageResource(R.drawable.register_eye);
        eye.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        break;
                }
                
                return true;
            }
        });
        password.place(eye);
        
        getBaseActivity().bindValidation(input, new Validation<EditText>()
        .addValidation(new PatternValidation<EditText>(MyValidator.PASSWORD), 
                getString(R.string.register_password_validation_length)));
    }
    
    @OnClick(R.id.next)
    void next() {
        if (getBaseActivity().checkNetworkStatus(true)
        &&  getBaseActivity().requestValidation())
        {
            showProgress(getString(R.string.progress_waiting));
            
            Register action = new Register(
                    username.input().getText().toString(), 
                    password.input().getText().toString());
            action.passport = passcode.input().getText().toString();
            getBaseActivity().sendHttpRequest(action);
        }
    }

    /**
     * 显示“下一步”
     */
    void showNext() {
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
                getContext(), R.anim.slide_up_in));
    }
    
    private class EventHandler extends engine.android.framework.ui.BaseActivity.EventHandler {
        
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