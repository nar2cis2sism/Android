package com.project.ui.login;

import static com.project.network.action.Actions.LOGIN;
import static com.project.network.action.Actions.NAVIGATION;

import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.BaseFragment;
import engine.android.util.listener.MyTextWatcher;
import engine.android.util.os.WindowUtil;
import engine.android.util.ui.MyValidator.PatternValidation;
import engine.android.util.ui.UIUtil;
import engine.android.widget.component.input.InputBox;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.network.action.http.Login;
import com.project.network.action.http.Navigation;
import com.project.ui.MainActivity;
import com.project.util.AppUtil;
import com.project.util.MyValidator;

import protocol.http.NavigationData.AppUpgradeInfo;

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
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtil.setFullScreenMode(getBaseActivity().getWindow(), false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupUsername(username);
        setupPassword(password);
        
        if (MyApp.getApp().isDebuggable())
        {
            username.input.setText("18318066253");
            password.input.setText("123456");
        }
    }
    
    private void setupUsername(InputBox username) {
        username.setStyle(InputBox.STYLE_MOBILE);
        username.enableClear();
        // 输入框
        EditText input = username.input;
        input.setCompoundDrawablesWithIntrinsicBounds(R.drawable.login_username, 0, 0, 0);
        input.setHint(R.string.login_username_hint);
        input.addTextChangedListener(new MyTextWatcher() {
            
            @Override
            protected void changeToEmpty(String before) {
                password.input.setText(null);
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
    
    private void setupPassword(InputBox password) {
        password.setStyle(InputBox.STYLE_PASSWORD);
        password.enableClear();
        // 输入框
        EditText input = password.input;
        input.setCompoundDrawablesWithIntrinsicBounds(R.drawable.login_password, 0, 0, 0);
        input.setHint(R.string.login_password_hint);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    UIUtil.hideSoftInput(v);
                    login();
                    return true;
                }
                
                return false;
            }
        });
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getBaseActivity().requestPermission(null, 
                Manifest.permission.WRITE_EXTERNAL_STORAGE, 
                Manifest.permission.ACCESS_FINE_LOCATION);
    }
    
    @OnClick(R.id.login)
    void login() {
        if (getBaseActivity().requestValidation()
        &&  getBaseActivity().checkNetworkStatus(true))
        {
            showProgress(R.string.progress_login);
            if (MySession.hasNavigation())
            {
                // 已有导航配置
                sendLoginAction();
            }
            else
            {
                getBaseActivity().sendHttpRequest(new Navigation());
            }
        }
    }
    
    @OnClick(R.id.register)
    void register() {
        startFragment(RegisterFragment.class);
    }
    
    @OnClick(R.id.find_password)
    void find_password() {
        startFragment(FindPasswordFragment.class);
    }

    /******************************* 用户登录 *******************************/
    
    void sendLoginAction() {
        getBaseActivity().sendHttpRequest(new Login(
                username.input.getText().toString(), 
                password.input.getText().toString()));
    }
    
    @Override
    protected EventHandler registerEventHandler() {
        return new EventHandler();
    }
    
    private class EventHandler extends BaseActivity.EventHandler {
        
        public EventHandler() {
            super(NAVIGATION, LOGIN);
        }

        @Override
        protected void onReceiveSuccess(String action, Object param) {
            if (NAVIGATION.equals(action))
            {
                AppUpgradeInfo info = MySession.getUpgradeInfo();
                if (info != null)
                {
                    if (info.type == 1)
                    {
                        // 强制升级，弹窗提醒
                        AppUtil.upgradeApp(getBaseActivity(), info, true);
                        hideProgress();
                        return;
                    }
                    else
                    {
                        // 建议升级，进入主界面后弹窗提醒
                    }
                }
                
                MySession.gotNavigation();
                sendLoginAction();
            }
            else if (LOGIN.equals(action))
            {
                startActivity(new Intent(getContext(), MainActivity.class));
                finish();
            }
        }
    }
}