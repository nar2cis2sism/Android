package com.project.ui.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.project.R;
import com.project.action.Actions;
import com.project.http.builder.LoginAction;
import com.project.http.builder.NavigationAction;
import com.project.ui.main.MainActivity;
import com.project.util.MyValidator;

import engine.android.core.Forelet.ProgressSetting;
import engine.android.core.annotation.InjectView;
import engine.android.framework.MyConfiguration.MyConfiguration_APP;
import engine.android.framework.ui.BaseFragment;
import engine.android.util.listener.MyTextWatcher;
import engine.android.util.ui.MyValidator.PatternValidation;
import engine.android.util.ui.MyValidator.Validation;
import engine.android.widget.InputBox;
import protocol.java.json.AppUpgradeInfo;

/**
 * 登录界面
 * 
 * @author Daimon
 */
public class LoginFragment extends BaseFragment implements OnClickListener {

//    @InjectView(R.id.content)
//    LinearLayout content;
    
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        enableReceiveEvent(Actions.NAVIGATION, Actions.LOGIN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {
//        ScrollView scrollView = new ScrollView(getContext());
//        scrollView.setBackgroundResource(R.drawable.login_bg);
        
        return inflater.inflate(R.layout.login_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
//        content.setOnClickListener(this);
        setupUsername();
        setupPassword();
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        find_password.setOnClickListener(this);
        
        setupData();
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
//                    login();
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
    
    private void setupData() {
        if (MyConfiguration_APP.APP_TESTING)
        {
            username.input().setText("18311287987");
            password.input().setText("yanhao");
        }
    }
//    
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        getActivity().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        
//        initKeyboardManager();
//    }
//    
//    private void initKeyboardManager() {
//        final Runnable keyboardChanged = new Runnable() {
//            
//            private int scrollY;
//            
//            @Override
//            public void run() {
//                if (scrollY == 0)
//                {
//                    int[] location = new int[2];
//                    username.getLocationOnScreen(location);
//                    scrollY = location[1];
//                }
//                
//                getView().scrollTo(0, scrollY);
//            }
//        };
//        
//        MyKeyboardManager keyboardManager = new MyKeyboardManager(getView());
//        keyboardManager.setKeyboardListener(new KeyboardListener() {
//            
//            @Override
//            public void keyboardChanged(boolean isKeyboardShown) {
//                if (isKeyboardShown)
//                {
//                    MyApp.getHandler().post(keyboardChanged);
//                }
//            }
//        });
//    }
//
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.content:
//                UIUtil.hideSoftInput(v);
//                break;
            case R.id.login:
                // 登录
                login();
                break;
            case R.id.register:
                // 用户注册
                break;
            case R.id.find_password:
                // 找回密码
                break;
        }
    }
    
    private void login() {
        if (getBaseActivity().checkNetStatus(true)
        &&  getBaseActivity().requestValidation())
        {
            getBaseActivity().showProgress(ProgressSetting.getDefault()
            .setMessage(getString(R.string.login_progress)));
            
//            if (MySession.hasNavigation())
//            {
//                // 已有导航配置
//                sendLoginAction();
//            }
//            else
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
        if (Actions.NAVIGATION.equals(action))
        {
            if (param != null)
            {
                AppUpgradeInfo info = (AppUpgradeInfo) param;
                if (info.type == 1)
                {
                    // 强制升级，弹窗提醒
                    upgradeApp(info);
                    getBaseActivity().hideProgress();
                }
                else
                {
                    // 建议升级，进入主界面后弹窗提醒
//                    MySession.setUpgradeInfo(info);
                }
            }
            
//            MySession.getNavigation();
            sendLoginAction();
        }
        else if (Actions.LOGIN.equals(action))
        {
            startActivity(new Intent(getContext(), MainActivity.class));
            finish();
        }
    }
    
    private void upgradeApp(AppUpgradeInfo info) {
        Dialog dialog = new AlertDialog.Builder(getContext())
        .setTitle(R.string.dialog_upgrade_title)
        .setMessage(info.desc)
        .setPositiveButton(R.string.dialog_upgrade_button1, null)
        .setNegativeButton(R.string.dialog_upgrade_button2, null)
        .create();

        getBaseActivity().showDialog("upgrade", dialog);
    }
}