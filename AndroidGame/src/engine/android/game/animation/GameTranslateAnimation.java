package engine.android.game.animation;

import engine.android.game.AnimationManager.GameAnimation;

/**
 * 位移控制动画
 * 
 * @author Daimon
 * @version N
 * @since 6/7/2012
 */
public class GameTranslateAnimation extends GameAnimation {

    private final int fromX, fromY;                     // 起始位置
    private final int toX, toY;                         // 结束位置

    private int distanceX;                              // 水平移动距离
    private int distanceY;                              // 垂直移动距离

    private int x, y;                                   // 当前位置

    public GameTranslateAnimation(int fromX, int fromY, int toX, int toY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    @Override
    protected long getPeriod(long duration, long interval) {
        long baseTime = getBaseAnimationTime();
        int x = toX - fromX;
        int y = toY - fromY;
        if (interval == 0)
        {
            long time;
            int ax = Math.abs(x);
            int ay = Math.abs(y);
            if (x == 0)
            {
                time = duration / ay;
                if (time < baseTime)
                {
                    distanceY = (int) (y * (time = baseTime) / duration);
                }
                else
                {
                    distanceY = y > 0 ? 1 : -1;
                }
            }
            else if (y == 0)
            {
                time = duration / ax;
                if (time < baseTime)
                {
                    distanceX = (int) (x * (time = baseTime) / duration);
                }
                else
                {
                    distanceX = x > 0 ? 1 : -1;
                }
            }
            else
            {
                long timeX = duration / ax;
                long timeY = duration / ay;
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
                    distanceX = (int) (x * (time = baseTime) / duration);
                    distanceY = (int) (y * (time = baseTime) / duration);
                }
                else
                {
                    distanceX = (int) ((x > 0 ? 1 : -1) * (time / timeX));
                    distanceY = (int) ((y > 0 ? 1 : -1) * (time / timeY));
                }
            }

            return time;
        }
        else
        {
            if (x != 0)
            {
                distanceX = x > 0 ? 1 : -1;
            }

            if (y != 0)
            {
                distanceY = y > 0 ? 1 : -1;
            }

            return interval;
        }
    }

    @Override
    protected boolean onAnimation() {
        if (x == Integer.MIN_VALUE && y == Integer.MIN_VALUE)
        {
            x = fromX;
            y = fromY;
            return false;
        }

        if (isReverse())
        {
            if (isOutOfRange(x, fromX, -distanceX) || isOutOfRange(y, fromY, -distanceY))
            {
                x = fromX;
                y = fromY;
                return true;
            }
            else
            {
                x -= distanceX;
                y -= distanceY;
            }
        }
        else
        {
            if (x == toX && y == toY)
            {
                x = fromX;
                y = fromY;
            }
            else if (isOutOfRange(x, toX, distanceX) || isOutOfRange(y, toY, distanceY))
            {
                x = toX;
                y = toY;
                return true;
            }
            else
            {
                x += distanceX;
                y += distanceY;
            }
        }

        return false;
    }

    @Override
    protected void onAnimationBefore() {
        x = y = Integer.MIN_VALUE;
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

    /**
     * 获取当前水平坐标
     */
    public int getX() {
        return x;
    }

    /**
     * 获取水平移动距离
     */
    public int getDistanceX() {
        return distanceX;
    }

    /**
     * 获取当前垂直坐标
     */
    public int getY() {
        return y;
    }

    /**
     * 获取垂直移动距离
     */
    public int getDistanceY() {
        return distanceY;
    }
}