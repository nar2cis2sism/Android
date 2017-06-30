package engine.android.game.animation;

import engine.android.game.GameAnimation;

/**
 * 旋转控制动画
 * 
 * @author Daimon
 * @version 3.0
 * @since 6/7/2012
 */

public class GameRotateAnimation extends GameAnimation {

    private final float fromDegrees;                    // 起始角度
    private final float toDegrees;                      // 结束角度

    private float rotateDegrees;                        // 旋转角度

    private float degrees;                              // 当前角度

    public GameRotateAnimation(float fromDegrees, float toDegrees) {
        this.fromDegrees = fromDegrees;
        this.toDegrees = toDegrees;
    }

    @Override
    protected long getPeriod() {
        long baseTime = getBaseAnimationTime();
        float degrees = toDegrees - fromDegrees;
        if (interval == 0)
        {
            long time = (long) (duration / Math.abs(degrees));

            if (time < baseTime)
            {
                rotateDegrees = degrees * (time = baseTime) / duration;
            }
            else
            {
                rotateDegrees = degrees > 0 ? 1 : -1;
            }

            return time;
        }
        else
        {
            rotateDegrees = degrees > 0 ? 1 : -1;
            return interval;
        }
    }

    @Override
    protected boolean onAnimation() {
        if (degrees == Float.MIN_VALUE)
        {
            degrees = fromDegrees;
            return false;
        }

        if (isReverse())
        {
            if (isOutOfRange(degrees, fromDegrees, -rotateDegrees))
            {
                degrees = fromDegrees;
                return true;
            }
            else
            {
                degrees -= rotateDegrees;
            }
        }
        else
        {
            if (degrees == toDegrees)
            {
                degrees = fromDegrees;
            }
            else if (isOutOfRange(degrees, toDegrees, rotateDegrees))
            {
                degrees = toDegrees;
                return true;
            }
            else
            {
                degrees += rotateDegrees;
            }
        }

        return false;
    }

    @Override
    protected void onAnimationAfter() {
        if (fillEnabled)
        {
            if (fillAfter)
            {
                degrees = toDegrees;
            }
            else if (fillBefore)
            {
                degrees = fromDegrees;
            }
        }
    }

    @Override
    protected void onAnimationBefore() {
        degrees = Float.MIN_VALUE;
    }

    /**
     * 获取旋转角度
     */

    public float getRotateDegrees() {
        return rotateDegrees;
    }

    /**
     * 获取当前角度
     */

    public float getDegrees() {
        return degrees;
    }
}