package engine.android.game.animation;

import engine.android.game.AnimationManager.GameAnimation;

/**
 * 透明控制动画
 * 
 * @author Daimon
 * @version N
 * @since 6/7/2012
 */
public class GameAlphaAnimation extends GameAnimation {

    private final float fromAlpha;                      // 起始alpha
    private final float toAlpha;                        // 结束alpha

    private float changeAlpha;                          // 变换alpha

    private float alpha;                                // 当前alpha

    public GameAlphaAnimation(float fromAlpha, float toAlpha) {
        this.fromAlpha = fromAlpha;
        this.toAlpha = toAlpha;
    }

    @Override
    protected long getPeriod(long duration, long interval) {
        long baseTime = getBaseAnimationTime();
        float alpha = toAlpha - fromAlpha;
        if (interval != 0)
        {
            changeAlpha = alpha > 0 ? 0.1f : -0.1f;
            return interval;
        }
        else
        {
            long time = (long) (duration / Math.abs(alpha));

            if (time < baseTime)
            {
                changeAlpha = alpha * (time = baseTime) / duration;
            }
            else
            {
                changeAlpha = alpha > 0 ? 0.1f : -0.1f;
            }

            return time;
        }
    }

    @Override
    protected boolean onAnimation() {
        if (alpha == Float.MIN_VALUE)
        {
            alpha = fromAlpha;
            return false;
        }

        if (isReverse())
        {
            if (isOutOfRange(alpha, fromAlpha, -changeAlpha))
            {
                alpha = fromAlpha;
                return true;
            }
            else
            {
                alpha -= changeAlpha;
            }
        }
        else
        {
            if (alpha == toAlpha)
            {
                alpha = fromAlpha;
            }
            else if (isOutOfRange(alpha, toAlpha, changeAlpha))
            {
                alpha = toAlpha;
                return true;
            }
            else
            {
                alpha += changeAlpha;
            }
        }

        return false;
    }

    @Override
    protected void onAnimationBefore() {
        alpha = Float.MIN_VALUE;
    }

    @Override
    protected void onAnimationAfter() {
        if (fillEnabled)
        {
            if (fillAfter)
            {
                alpha = toAlpha;
            }
            else if (fillBefore)
            {
                alpha = fromAlpha;
            }
        }
    }

    /**
     * 获取变换alpha值
     */
    public float getChangeAlpha() {
        return changeAlpha;
    }

    /**
     * 获取当前alpha值
     */
    public float getAlpha() {
        return alpha;
    }
}