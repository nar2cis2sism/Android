package com.project.app.util;

import android.content.Context;

import com.daimon.yueba.R;
import com.project.app.MyContext;

import engine.android.util.extra.Singleton;
import engine.android.util.manager.SoundPlayer;

/**
 * 音效管理器
 * 
 * @author Daimon
 */
public class MySoundPlayer extends SoundPlayer {
    
    private static final Singleton<MySoundPlayer> instance
    = new Singleton<MySoundPlayer>() {

        @Override
        protected MySoundPlayer create() {
            return new MySoundPlayer(MyContext.getContext());
        }
    };

    public static final MySoundPlayer getInstance() {
        return instance.get();
    }

    /******************************** 华丽丽的分割线 ********************************/
    
    private boolean turnOff;

    private MySoundPlayer(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        load(R.raw.click, true);
    }
    
    public void load(int raw) {
        load(raw, true);
    }
    
    public void load(int raw, boolean load) {
        if (load)
        {
            load(raw, raw);
        }
        else
        {
            unload(raw);
        }
    }
    
    public void play(int raw) {
        play(raw, 0);
    }
    
    @Override
    public int play(int id, int loop) {
        if (turnOff)
        {
            return 0;
        }
        
        return super.play(id, loop);
    }
    
    /**
     * 开关控制
     */
    public void switcher(boolean turnOn) {
        turnOff = !turnOn;
    }

    /******************************** 华丽丽的分割线 ********************************/
    
    /**
     * 点击事件音效
     */
    public void soundClick() {
        play(R.raw.click);
    }
}