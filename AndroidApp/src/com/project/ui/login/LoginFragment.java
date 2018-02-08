package com.project.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.ui.MainActivity;
import com.project.util.MyValidator;

import engine.android.core.Forelet.ProgressSetting;
import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.framework.ui.BaseFragment;
import engine.android.util.listener.MyTextWatcher;
import engine.android.util.ui.MyValidator.PatternValidation;
import engine.android.util.ui.UIUtil;
import engine.android.widget.InputBox;

/**
 * 登录界面
 * 
 * @author Daimon
 */
public class LoginFragment extends BaseFragment {
    
    @InjectView(R.id.username)
    InputBox username;

    @InjectView(R.id.password)
    InputBox password;

    @InjectView(R.id.login)
    Button login;

    @InjectView(R.id.register)
    TextView register;

    @InjectView(R.id.find_password)
    TextView find_password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.setBackgroundResource(R.drawable.login_bg);
        
        return inflater.inflate(R.layout.login_fragment, scrollView);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupUsername();
        setupPassword();
        UIUtil.adjustResize((ScrollView) getView(), username);
        
        if (MyApp.getApp().isDebuggable())
        {
            username.input().setText("18311287987");
            password.input().setText("yanhao");
        }
    }
    
    private void setupUsername() {
        username.enableClear();
        // 输入框
        final EditText input = username.input();
        input.setCompoundDrawablesWithIntrinsicBounds(R.drawable.login_username, 0, 0, 0);
        input.setHint(R.string.login_username_hint);
        input.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        input.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT)
                {
                    password.requestFocus();
                    return true;
                }
                
                return false;
            }
        });
        input.addTextChangedListener(new MyTextWatcher() {
            
            @Override
            protected void changeToEmpty(String before) {
                password.input().setText(null);
                login.setEnabled(false);
            }
            
            @Override
            protected void changeFromEmpty(String after) {
                login.setEnabled(true);
            }
        });
        
        getBaseActivity().bindValidation(input, new MyValidator.Validation<EditText>()
        .addValidation(new PatternValidation<EditText>(MyValidator.MOBILE_NUMBER), 
                getString(R.string.login_username_validation_mobile)));
    }
    
    private void setupPassword() {
        password.enableClear();
        // 输入框
        final EditText input = password.input();
        input.setCompoundDrawablesWithIntrinsicBounds(R.drawable.login_password, 0, 0, 0);
        input.setHint(R.string.login_password_hint);
        input.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        
        input.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    login();
                    return true;
                }
                
                return false;
            }
        });
        
        getBaseActivity().bindValidation(input, new MyValidator.Validation<EditText>()
        .addValidation(new PatternValidation<EditText>(MyValidator.VALID), 
                getString(R.string.login_password_validation_empty))
        .addValidation(new PatternValidation<EditText>(MyValidator.LOGIN_PASSWORD), 
                getString(R.string.login_password_validation_length)));
    }
    
    @OnClick(R.id.login)
    void login() {
        if (getBaseActivity().checkNetworkStatus(true)
        &&  getBaseActivity().requestValidation())
        {
            getBaseActivity().showProgress(ProgressSetting.getDefault()
            .setMessage(getString(R.string.login_progress)));
            
            startActivity(new Intent(getContext(), MainActivity.class));
            finish();
        }
    }
}