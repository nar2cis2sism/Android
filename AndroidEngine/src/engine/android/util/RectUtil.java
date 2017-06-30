package engine.android.util;

import android.graphics.Rect;

/**
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class RectUtil {

    public static Rect getRect(int x, int y, int w, int h) {
        return new Rect(x, y, x + w, y + h);
    }

    public static void keepRectInBounds(Rect rect, Rect bounds) {
        if (rect.width() > bounds.width() || rect.height() > bounds.height())
        {
            return;
        }

        if (rect.left < bounds.left)
        {
            rect.offset(bounds.left - rect.left, 0);
        }
        else if (rect.right > bounds.right)
        {
            rect.offset(bounds.right - rect.right, 0);
        }

        if (rect.top < bounds.top)
        {
            rect.offset(0, bounds.top - rect.top);
        }
        else if (rect.bottom > bounds.bottom)
        {
            rect.offset(0, bounds.bottom - rect.bottom);
        }
    }

    public static Rect transformRect(Rect A_coordinate, Rect B_coordinate, Rect A) {
        Rect B = new Rect(0, 0,
                B_coordinate.width() * A.width() / A_coordinate.width(),
                B_coordinate.height() * A.height() / A_coordinate.height());
        B.offsetTo(B_coordinate.width() * (A.centerX() - A_coordinate.centerX()) 
                 / A_coordinate.width() + B_coordinate.centerX() - B.width() / 2,
                   B_coordinate.height() * (A.centerY() - A_coordinate.centerY()) 
                 / A_coordinate.height() + B_coordinate.centerY() - B.height() / 2);
        return B;
    }

    public static Rect rotateRect(Rect rect, int angle) {
        angle = Math.abs(angle) % 360;
        if (angle == 90 || angle == 270)
        {
            int width = rect.width();
            int height = rect.height();
            return getRect(rect.centerX() - height / 2, rect.centerY() - width / 2, height, width);
        }

        return rect;
    }

    /**
     * 设置矩形区域
     * 
     * @param rect 需要设置的矩形，可以为Null（返回一个新建的矩形）
     * @param x,y 矩形位置
     * @param w,h 矩形大小
     */
    public static Rect setRect(Rect rect, int x, int y, int w, int h) {
        if (rect == null)
        {
            rect = new Rect(x, y, x + w, y + h);
        }
        else
        {
            rect.set(x, y, x + w, y + h);
        }

        return rect;
    }

    /**
     * 复制矩形区域
     * 
     * @param rect 需要设置的矩形，可以为Null（返回一个新建的矩形）
     * @param copy 需要复制的矩形
     */
    public static Rect copyRect(Rect rect, Rect copy) {
        if (copy == null)
        {
            return null;
        }

        if (rect == null)
        {
            rect = new Rect(copy);
        }
        else
        {
            rect.set(copy);
        }

        return rect;
    }
}