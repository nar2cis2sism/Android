package demo.android.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

public class ViewAnimationUtil {
    
    public static enum AnimationType {
        
        ANIMATION_ROTATE_UP,                //向上翻转
        ANIMATION_ROTATE_DOWN               //向下翻转
    }
    
    private static class BaseAnimatorListenerAdapter extends AnimatorListenerAdapter {
        
        protected final View viewIn, viewOut;
        protected final long duration;
        
        public BaseAnimatorListenerAdapter(View in, View out, long duration) {
            viewIn = in;
            viewOut = out;
            this.duration = duration;
        }
    }
    
    private static class RotateAnimatorListenerAdapter extends BaseAnimatorListenerAdapter {
        
        private final String rotateValue;
        private final float from, to;
        
        public RotateAnimatorListenerAdapter(View in, View out, long duration, 
                String rotateValue, float from, float to) {
            super(in, out, duration);
            
            this.rotateValue = rotateValue;
            float angle = to - from;
            this.from = to - 180;
            this.to = this.from + angle;
        }
        
        @Override
        public void onAnimationEnd(Animator animation) {
            viewOut.setVisibility(View.GONE);
            viewIn.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(viewIn, rotateValue, from, to).setDuration(duration).start();
        }
    }
    
    public static void startAnimation(AnimationType type, View in, View out, long duration) {
        switch (type) {
            case ANIMATION_ROTATE_UP:
                rotate(in, out, duration, "rotationX", 0, -90);
                break;
            case ANIMATION_ROTATE_DOWN:
                rotate(in, out, duration, "rotationX", 0, 90);
                break;

            default:
                throw new IllegalArgumentException();
        }
    }
    
    private static void rotate(View in, View out, long duration, 
            String rotateValue, float from, float to) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(out, rotateValue, from, to).setDuration(duration /= 2);
        anim.addListener(new RotateAnimatorListenerAdapter(in, out, duration, rotateValue, from, to));
        anim.start();
    }
}