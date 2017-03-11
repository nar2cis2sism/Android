package com.project.ui.login;

import static com.project.network.Actions.LOGIN;
import static com.project.network.Actions.NAVIGATION;

import android.app.AlertDialog;
import android.app.Dialog;
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

import com.project.R;
import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.network.action.LoginAction;
import com.project.network.action.NavigationAction;
import com.project.ui.main.MainActivity;
import com.project.util.MyValidator;

import engine.android.core.Forelet.ProgressSetting;
import engine.android.core.annotation.InjectView;
import engine.android.core.annotation.OnClick;
import engine.android.framework.ui.BaseFragment;
import engine.android.util.listener.MyTextWatcher;
import engine.android.util.ui.MyValidator.PatternValidation;
import engine.android.util.ui.MyValidator.Validation;
import engine.android.util.ui.UIUtil;
import engine.android.widget.InputBox;
import protocol.java.json.AppUpgradeInfo;

/**
 * 登录界面
 * 
 * @author Daimon
 */
public class LoginFragment extends BaseFragment {
    
    @InjectView(R.id.username)
    InputBox username;                      // 账号输入框

    @InjectView(R.id.password)
    InputBox password;                      // 密码输入框

    @InjectView(R.id.login)
    Button login;                           // 登录按钮

    @InjectView(R.id.register)
    TextView register;                      // 用户注册按钮

    @InjectView(R.id.find_password)
    TextView find_password;                 // 找回密码按钮
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        enableReceiveEvent(NAVIGATION, LOGIN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.setBackgroundResource(R.drawable.login_bg);
        
        return inflater.inflate(R.layout.login_fragment, scrollView, true);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupUsername();
        setupPassword();
        
        if (MyApp.isDebuggable())
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
        
        input.addTextChangedListener(new MyTextWatcher() {
            
            @Override
            protected void changeToEmpty(String before) {
                login.setEnabled(false);
            }
            
            @Override
            protected void changeFromEmpty(String after) {
                login.setEnabled(true);
            }
        });
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
        
        getBaseActivity().bindValidation(input, new Validation<EditText>()
        .addValidation(new PatternValidation<EditText>(MyValidator.VALID), 
                getString(R.string.login_username_validation_empty))
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
        
        getBaseActivity().bindValidation(input, new Validation<EditText>()
        .addValidation(new PatternValidation<EditText>(MyValidator.VALID), 
                getString(R.string.login_password_validation_empty))
        .addValidation(new PatternValidation<EditText>(MyValidator.PASSWORD), 
                getString(R.string.login_password_validation_length)));
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        UIUtil.adjustResize((ScrollView) getView(), username);
    }
    
    @OnClick(R.id.login)
    private void login() {
        if (getBaseActivity().checkNetworkStatus(true)
        &&  getBaseActivity().requestValidation())
        {
            getBaseActivity().showProgress(ProgressSetting.getDefault()
            .setMessage(getString(R.string.login_progress)));
            
            if (MySession.hasNavigation())
            {
                // 已有导航配置
                sendLoginAction();
            }
            else
            {
                sendNavigationAction();
            }
        }
    }

    /******************************* 获取导航 *******************************/
    
    private void sendNavigationAction() {
        NavigationAction action = new NavigationAction();
        
        getBaseActivity().sendHttpRequest(action);
    }

    /******************************* 用户登录 *******************************/
    
    private void sendLoginAction() {
        LoginAction action = new LoginAction(username.input().getText().toString(), 
                                             password.input().getText().toString());
        
        getBaseActivity().sendHttpRequest(action);
    }
    
    @Override
    protected void onReceiveSuccess(String action, Object param) {
        if (NAVIGATION.equals(action))
        {
            if (param instanceof AppUpgradeInfo)
            {
                AppUpgradeInfo info = (AppUpgradeInfo) param;
                if (info.type == 1)
                {
                    // 强制升级，弹窗提醒
                    upgradeApp(info);
                    getBaseActivity().hideProgress();
                    return;
                }
                else
                {
                    // 建议升级，进入主界面后弹窗提醒
                    MySession.setUpgradeInfo(info);
                }
            }
            
            MySession.getNavigation();
            sendLoginAction();
        }
        else if (LOGIN.equals(action))
        {
            startActivity(new Intent(getContext(), MainActivity.class));
            finish();
        }
    }
    
    private void upgradeApp(AppUpgradeInfo info) {
        Dialog dialog = new AlertDialog.Builder(getContext())
        .setTitle(R.string.dialog_upgrade_title)
        .setMessage(info.desc)
        .setPositiveButton(R.string.dialog_upgrade_now, null)
        .setCancelable(false)
        .create();

        getBaseActivity().showDialog("upgrade", dialog);
    }
}