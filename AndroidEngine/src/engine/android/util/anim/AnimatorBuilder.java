package engine.android.util.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.renderscript.Float2;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import java.util.LinkedList;

/**
 * 方便构造动画
 * 
 * @author Daimon
 * @since 1/28/2019
 */
public class AnimatorBuilder {
    
    private final ObjectAnimator anim = new ObjectAnimator();
    
    private final LinkedList<PropertyValuesHolder> values = new LinkedList<PropertyValuesHolder>();
    
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

    /**
     * 贝塞尔曲线动画
     *
     * @param control 控制点
     */
    public AnimatorBuilder bezier(float fromX, float toX, float fromY, float toY, Float2 control) {
        if (control == null) control = new Float2((toX - fromX) / 4 + fromX, toY);
        values.add(PropertyValuesHolder.ofObject("x", new BezierEvaluator(control.x), fromX, toX));
        values.add(PropertyValuesHolder.ofObject("y", new BezierEvaluator(control.y), fromY, toY));
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

    private static class BezierEvaluator implements TypeEvaluator<Number> {

        private final float control;

        public BezierEvaluator(float control) {
            this.control = control;
        }

        @Override
        public Number evaluate(float fraction, Number startValue, Number endValue) {
            float t = fraction;
            return ((1 - t) * (1 - t) * startValue.floatValue()
                 + 2 * t * (1 - t) * control + t * t * endValue.floatValue());
        }
    }
    
    public abstract static class AnimationListenerAdapter implements AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {}

        @Override
        public void onAnimationRepeat(Animation animation) {}
    }
    
    /**
     * 方便构造动画集
     */
    public static class AnimatorSetBuilder {

        private final AnimatorSet set = new AnimatorSet();

        private Builder builder;

        public AnimatorSetBuilder play(Animator anim) {
            if (builder == null)
            {
                builder = set.play(anim);
            }
            else
            {
                builder.with(anim);
            }

            return this;
        }

        public AnimatorSetBuilder before(Animator anim) {
            if (builder != null)
            {
                builder.before(anim);
            }

            return this;
        }

        public AnimatorSetBuilder before(long delay) {
            // setup dummy ValueAnimator just to run the clock
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(delay);
            before(anim);
            return this;
        }

        public AnimatorSetBuilder after(Animator anim) {
            if (builder != null)
            {
                builder.after(anim);
            }

            return this;
        }

        public AnimatorSetBuilder after(long delay) {
            if (builder != null)
            {
                builder.after(delay);
            }

            return this;
        }

        public AnimatorSet build() {
            return set;
        }

        public void start() {
            set.start();
        }
    }
}