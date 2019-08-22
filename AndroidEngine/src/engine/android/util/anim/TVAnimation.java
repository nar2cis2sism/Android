package engine.android.util.anim;

import android.graphics.Matrix;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 类似于电视开关的效果
 * 
 * @author Daimon
 * @since 8/20/2012
 */
public class TVAnimation extends Animation {

    private int centerX, centerY;

    private final boolean TVOff;

    /**
     * @param TVOff 电视关闭
     */
    public TVAnimation(boolean TVOff) {
        this.TVOff = TVOff;
        setDuration(500);
        setFillAfter(true);
        setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    public void initialize(int width, int height, int parentWidth,
            int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        centerX = width / 2;
        centerY = height / 2;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        Matrix m = t.getMatrix();
        if (!TVOff)
        {
            interpolatedTime = 1 - interpolatedTime;
        }

        if (interpolatedTime < 0.8f)
        {
            m.preScale(1 + 0.625f * interpolatedTime, 1 - interpolatedTime / 0.8f + 0.01f, 
                    centerX, centerY);
        }
        else
        {
            m.preScale(7.5f * (1 - interpolatedTime), 0.01f, centerX, centerY);
        }
    }
}