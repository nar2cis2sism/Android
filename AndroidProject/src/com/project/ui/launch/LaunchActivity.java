package com.project.ui.launch;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;

import com.project.MyInitial;
import com.project.MySession;
import com.project.R;
import com.project.storage.MySharedPreferences;
import com.project.ui.login.LoginFragment;

import engine.android.core.extra.SplashScreen;
import engine.android.core.extra.SplashScreen.SplashCallback;
import engine.android.core.extra.SplashScreen.SplashLoading;
import engine.android.framework.ui.BaseActivity;
import engine.android.framework.ui.extra.SinglePaneActivity;

/**
 * 启动界面
 * 
 * @author Daimon
 */
public class LaunchActivity extends BaseActivity implements SplashCallback, SplashLoading {
    
    private SplashScreen splash;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
    public void onSplashDisplayed() {
        View view = new View(this);
        view.setBackgroundResource(R.drawable.splash);
        
        view.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                splash.finish();
                return true;
            }
        });
        
        setContentView(view, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onSplashFinished() {
        if (MySharedPreferences.getInstance().isGuideShown())
        {
//            if (false)
//            {
//                // 有缓存
//                startActivity(new Intent(this, MainActivity.class));
//            }
//            else
            {
                startActivity(SinglePaneActivity.buildIntent(
                        this, LoginFragment.class, null));
            }
        }
        else
        {
            // 显示引导页
            startActivity(SinglePaneActivity.buildIntent(
                    this, GuideFragment.class, null));
        }
        
        finish();
    
    }

    @Override
    public void loadInBackground() {
        MyInitial.init(this);
    }
    
    @Override
    public void onBackPressed() {
        if (splash != null)
        {
            splash.cancel();
        }
        
        super.onBackPressed();
    }
}