package com.project.storage;

import android.content.Context;
import android.content.SharedPreferences;

import engine.android.framework.MyConfiguration.MyConfiguration_SHARED_PREFERENCES;
import engine.android.framework.MyContext;
import engine.android.util.Singleton;

/**
 * 轻量存储
 * 
 * @author Daimon
 */
public class MySharedPreferences implements MyConfiguration_SHARED_PREFERENCES {
    
    private static final Singleton<MySharedPreferences> instance
    = new Singleton<MySharedPreferences>() {
        
        @Override
        protected MySharedPreferences create() {
            return new MySharedPreferences(MyContext.getContext());
        }
    };
    
    public static final MySharedPreferences getInstance() {
        return instance.get();
    }

    /**************************** 华丽丽的分割线 ****************************/
    
    private final SharedPreferences sp;
    
    MySharedPreferences(Context context) {
        sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 
                Context.MODE_PRIVATE);
    }

    /*************************** 是否已显示过引导页 ***************************/
    private static final String IS_GUIDE_SHOWN = "IS_GUIDE_SHOWN";
    
    public void finishGuide() {
        sp.edit()
        .putBoolean(IS_GUIDE_SHOWN, true)
        .commit();
    }
    
    public boolean isGuideShown() {
        return sp.getBoolean(IS_GUIDE_SHOWN, false);
    }

    /**************************** 华丽丽的分割线 ****************************/
    
    public void reset() {
        sp.edit()
        .clear()
        .commit();
    }
}