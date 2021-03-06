package com.project.ui.launch;

import engine.android.core.extra.SplashScreen;
import engine.android.core.extra.SplashScreen.SplashCallback;
import engine.android.core.extra.SplashScreen.SplashLoading;
import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.activity.SinglePaneActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;

import com.daimon.yueba.R;
import com.project.app.MyInitial;
import com.project.app.MySession;
import com.project.storage.MySharedPreferences;
import com.project.ui.MainActivity;
import com.project.ui.login.LoginFragment;

/**
 * 启动界面
 * 
 * @author Daimon
 */
public class LaunchActivity extends BaseActivity implements SplashCallback, SplashLoading {
    
    SplashScreen splash;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // App安装完成后每次点击桌面图标应用都重新启动
        if (!isTaskRoot())
        {
            finish();
            return;
        }
        
        if (MySession.initialized())
        {
            onSplashFinished();
            return;
        }
        
        splash = new SplashScreen(this, this);
        splash.setDuration(1500);
        splash.start();
    }
    
    @Override
    public void onBackPressed() {
        if (splash != null) splash.cancel();
        super.onBackPressed();
    }

    @Override
    public void onSplashDisplayed() {
        View view = new View(this);
        view.setBackgroundResource(R.drawable.splash);
        view.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                splash.finish();
                return false;
            }
        });
        
        setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onSplashFinished() {
        if (MySharedPreferences.getInstance().isGuideShown())
        {
            if (MySession.getUser() != null)
            {
                // 有缓存
                startActivity(new Intent(this, MainActivity.class));
            }
            else
            {
                startActivity(SinglePaneActivity.buildIntent(this, LoginFragment.class, null));
            }
        }
        else
        {
            // 显示引导页
            startActivity(SinglePaneActivity.buildIntent(this, GuideFragment.class, null));
        }
        
        finish();
    }

    @Override
    public void loadInBackground() {
        MyInitial.init();
        MySession.initialize();
    }
}