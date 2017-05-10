package com.project.ui.module.launch;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;

import com.project.MySession;
import com.project.ui.BaseActivity;

import demo.android.R;
import engine.android.core.util.extra.SplashScreen;
import engine.android.core.util.extra.SplashScreen.SplashCallbacks;
import engine.android.core.util.extra.SplashScreen.SplashLoading;

/**
 * 启动界面
 * 
 * @author Daimon
 */

public class LaunchActivity extends BaseActivity implements SplashCallbacks {

    private SplashScreen splash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MySession.initialized())
        {
            onSplashFinished();
            return;
        }

        splash = new SplashScreen(this, new SplashLoading() {

            @Override
            public void loadInBackground() {
                // TODO 初始化
            }
        });
        splash.setDuration(500);
        splash.display();
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
        // 启动主界面
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