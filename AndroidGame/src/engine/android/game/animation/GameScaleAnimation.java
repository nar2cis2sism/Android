package engine.android.game.animation;

import engine.android.game.AnimationManager.GameAnimation;

/**
 * 缩放控制动画
 * 
 * @author Daimon
 * @version N
 * @since 6/7/2012
 */
public class GameScaleAnimation extends GameAnimation {

    private final float fromX, fromY;                   // 缩放起始值
    private final float toX, toY;                       // 缩放结束值

    private float scaleX;                               // 水平缩放值
    private float scaleY;                               // 垂直缩放值

    private float x, y;                                 // 当前缩放值

    public GameScaleAnimation(float fromX, float toX, float fromY, float toY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    @Override
    protected long getPeriod(long duration, long interval) {
        long baseTime = getBaseAnimationTime();
        float x = toX - fromX;
        float y = toY - fromY;
        if (interval == 0)
        {
            long time;
            float ax = Math.abs(x);
            float ay = Math.abs(y);
            if (x == 0)
            {
                time = (long) (duration / ay);
                if (time < baseTime)
                {
                    scaleY = y * (time = baseTime) / duration;
                }
                else
                {
                    scaleY = y > 0 ? 0.1f : -0.1f;
                }
            }
            else if (y == 0)
            {
                time = (long) (duration / ax);
                if (time < baseTime)
                {
                    scaleX = x * (time = baseTime) / duration;
                }
                else
                {
                    scaleX = x > 0 ? 0.1f : -0.1f;
                }
            }
            else
            {
                long timeX = (long) (duration / ax);
                long timeY = (long) (duration / ay);
                if (timeX % timeY == 0)
                {
                    time = timeX;
                }
                else if (timeY % timeX == 0)
                {
                    time = timeY;
                }
                else
                {
                    time = timeX * timeY;
                }

                if (time < baseTime)
                {
                    scaleX = x * (time = baseTime) / duration;
                    scaleY = y * (time = baseTime) / duration;
                }
                else
                {
                    scaleX = (x > 0 ? 0.1f : -0.1f) * (time / timeX);
                    scaleY = (y > 0 ? 0.1f : -0.1f) * (time / timeY);
                }
            }

            return time;
        }
        else
        {
            scaleX = x > 0 ? 0.1f : -0.1f;
            scaleY = y > 0 ? 0.1f : -0.1f;
            return interval;
        }
    }

    @Override
    protected boolean onAnimation() {
        if (x == Float.MIN_VALUE && y == Float.MIN_VALUE)
        {
            x = fromX;
            y = fromY;
            return false;
        }

        if (isReverse())
        {
            if (isOutOfRange(x, fromX, -scaleX) || isOutOfRange(y, fromY, -scaleY))
            {
                x = fromX;
                y = fromY;
                return true;
            }
            else
            {
                x -= scaleX;
                y -= scaleY;
            }
        }
        else
        {
            if (x == toX && y == toY)
            {
                x = fromX;
                y = fromY;
            }
            else if (isOutOfRange(x, toX, scaleX) || isOutOfRange(y, toY, scaleY))
            {
                x = toX;
                y = toY;
                return true;
            }
            else
            {
                x += scaleX;
                y += scaleY;
            }
        }

        return false;
    }

    @Override
    protected void onAnimationBefore() {
        x = y = Float.MIN_VALUE;
    }

    @Override
    protected void onAnimationAfter() {
        if (fillEnabled)
        {
            if (fillAfter)
            {
                x = toX;
                y = toY;
            }
            else if (fillBefore)
            {
                x = fromX;
                y = fromY;
            }
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }
}