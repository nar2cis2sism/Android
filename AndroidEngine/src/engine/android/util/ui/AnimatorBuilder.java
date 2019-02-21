package engine.android.util.ui;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import java.util.LinkedList;
import java.util.List;

/**
 * 方便构造动画
 * 
 * @author Daimon
 * @version N
 * @since 1/28/2019
 */
public class AnimatorBuilder {
    
    private final ObjectAnimator anim = new ObjectAnimator();
    
    private final List<PropertyValuesHolder> values = new LinkedList<PropertyValuesHolder>();
    
    public AnimatorBuilder(View target, long duration) {
        anim.setTarget(target);
        anim.setDuration(duration);
    }
    
    /**
     * 位移动画
     */
    public AnimatorBuilder translate(float fromX, float toX, float fromY, float toY) {
        values.add(PropertyValuesHolder.ofFloat("translationX", fromX, toX));
        values.add(PropertyValuesHolder.ofFloat("translationY", fromY, toY));
        return this;
    }

    /**
     * 旋转动画
     */
    public AnimatorBuilder rotate(float startDegree, float rotation) {
        values.add(PropertyValuesHolder.ofFloat("rotation", startDegree, startDegree + rotation));
        return this;
    }

    /**
     * 缩放动画
     */
    public AnimatorBuilder scale(float fromScale, float toScale) {
        values.add(PropertyValuesHolder.ofFloat("scaleX", fromScale, toScale));
        values.add(PropertyValuesHolder.ofFloat("scaleY", fromScale, toScale));
        return this;
    }

    /**
     * 透明动画
     */
    public AnimatorBuilder alpha(float fromAlpha, float toAlpha) {
        values.add(PropertyValuesHolder.ofFloat("alpha", fromAlpha, toAlpha));
        return this;
    }

    public AnimatorBuilder property(PropertyValuesHolder holder) {
        values.add(holder);
        return this;
    }
    
    public ObjectAnimator build() {
        anim.setValues(values.toArray(new PropertyValuesHolder[values.size()]));
        return anim;
    }
    
    public abstract static class AnimationListenerAdapter implements AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {}

        @Override
        public void onAnimationRepeat(Animation animation) {}
    }
}