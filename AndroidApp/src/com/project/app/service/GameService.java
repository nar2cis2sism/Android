//package com.project.app.service;
//
//import android.app.Activity;
//import android.app.Application.ActivityLifecycleCallbacks;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//
//import com.tongxuezhan.tongxue.R;
//import com.project.app.MyApp;
//import com.project.app.util.MySoundPlayer;
//
//import engine.android.util.AndroidUtil;
//import engine.android.util.manager.MyPowerManager;
//import engine.android.util.service.LocalService;
//
//public class GameService extends GameMediaService {}
//
///**
// * 游戏媒体控制器<p>
// * 功能：游戏背景音乐
// */
//class GameMediaService extends LocalService implements ActivityLifecycleCallbacks {
//    
//    private MediaPlayer mediaPlayer;
//    private MyPowerManager pm;
//    
//    private boolean turnOff;
//    
//    @Override
//    public void onCreate() {
//        MyApp.getApp().registerActivityLifecycleCallbacks(this);
//        mediaPlayer = MediaPlayer.create(this, R.raw.game_bg);
//        mediaPlayer.setLooping(true);
//        mediaPlayer.start();
//        pm = new MyPowerManager(this);
//    }
//    
//    @Override
//    public void onDestroy() {
//        MyApp.getApp().unregisterActivityLifecycleCallbacks(this);
//        mediaPlayer.stop();
//        mediaPlayer.release();
//    }
//
//    @Override
//    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
//
//    @Override
//    public void onActivityStarted(Activity activity) {
//        if (turnOff || mediaPlayer.isPlaying())
//        {
//            return;
//        }
//        
//        mediaPlayer.start();
//        MySoundPlayer.getInstance().switcher(true);
//    }
//
//    @Override
//    public void onActivityResumed(Activity activity) {}
//
//    @Override
//    public void onActivityPaused(Activity activity) {}
//
//    @Override
//    public void onActivityStopped(Activity activity) {
//        if (turnOff || (AndroidUtil.atForeGround(this) && pm.isScreenOn()))
//        {
//            return;
//        }
//        
//        mediaPlayer.pause();
//        MySoundPlayer.getInstance().switcher(false);
//    }
//
//    @Override
//    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
//
//    @Override
//    public void onActivityDestroyed(Activity activity) {}
//    
//    /**
//     * 手动播放开关
//     */
//    public void play(boolean turnOn) {
//        if (turnOff = !turnOn)
//        {
//            mediaPlayer.pause();
//        }
//        else
//        {
//            mediaPlayer.start();
//        }
//    }
//}