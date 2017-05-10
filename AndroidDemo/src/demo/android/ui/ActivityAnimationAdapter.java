package demo.android.ui;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import demo.android.ui.util.AnimationEffect;


public class ActivityAnimationAdapter implements ActivityLifecycleCallbacks {
    
    private final AnimationEffect mAnimationEffect;
    
    private ComponentName mComponentName;
    
    public ActivityAnimationAdapter(AnimationEffect animationEffect) {
        mAnimationEffect = animationEffect;
    }
    
    public void startActivity(Activity activity, Intent intent) {
        mAnimationEffect.prepareBitmap(activity);
        
        mComponentName = intent.getComponent();
        assert mComponentName != null;
        
        activity.getApplication().registerActivityLifecycleCallbacks(this);
        
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (isDestActivity(activity))
        {
            mAnimationEffect.prepareTopView(activity);
            mAnimationEffect.animate(activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (isDestActivity(activity))
        {
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            mAnimationEffect.cancel(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // TODO Auto-generated method stub
        
    }
    
    private boolean isDestActivity(Activity activity) {
        return mComponentName.equals(activity.getComponentName());
    }
}