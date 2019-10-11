package com.project.ui.login;

import engine.android.widget.component.TitleBar;

import android.os.Bundle;
import android.view.View;

import com.daimon.yueba.R;
import com.project.app.MyApp;

/**
 * 找回密码
 * 
 * @author Daimon
 */
public class FindPasswordFragment extends RegisterFragment {
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(R.string.find_password_title)
        .show();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        license.setVisibility(View.INVISIBLE);
        
        if (MyApp.getApp().isDebuggable())
        {
            username.input.setText("18318066253");
            password.input.setText("123456");
        }
    }
    
    void next() {
        if (getBaseActivity().requestValidation()
        &&  getBaseActivity().checkNetworkStatus(true))
        {
            showProgress(R.string.progress_waiting);
            
//            ResetPassword action = new ResetPassword(
//                    username.input.getText().toString(), 
//                    password.input.getText().toString());
//            action.passport = passcode.input.getText().toString();
//            getBaseActivity().sendHttpRequest(action);
        }
    }
}