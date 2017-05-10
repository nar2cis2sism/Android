package engine.android.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import engine.android.game.GameCanvas.TouchEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * 游戏图层
 * 
 * @author Daimon
 * @version 3.0
 * @since 8/13/2012
 */

public abstract class Layer implements EventConstants {

    private static final int ACTION_DOWN        = MotionEvent.ACTION_DOWN;
    private static final int ACTION_UP          = MotionEvent.ACTION_UP;
    private static final int ACTION_MOVE        = MotionEvent.ACTION_MOVE;

    public static final int VISIBLE             = View.VISIBLE;
    public static final int INVISIBLE           = View.INVISIBLE;
    public static final int GONE                = View.GONE;

    int x, y;                                   // 图层的位置
    int width, height;                          // 图层的大小
    private int dRefX, dRefY;                   // 参考坐标（相对坐标）

    int visibility;                             // 可见状态
    private Rect visibleRect;                   // 可见区域（相对坐标）
    private Rect visibleRect_abs;               // 可见区域（绝对坐标）

    Paint paint;                                // 绘制画笔
    private Paint bufferPaint;                  // 画笔缓存

    private List<AppendDrawn> drawn;            // 自定义绘制接口

    protected Layer(int width, int height) {
        setSize(width, height);
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

    /**
     * 设置图层大小
     */

    private void setSize(int width, int height) {
        if (width < 0 || height < 0)
        {
            throw new IllegalArgumentException();
        }

        this.width = width;
        this.height = height;
    }

    /**
     * 图层大小变化
     * 
     * @return 是否和原有大小不一样
     */

    protected boolean sizeChanged(int width, int height) {
        if (!(width == this.width && height == this.height))
        {
            setSize(width, height);
            return true;
        }

        return false;
    }

    /**
     * 设置图层位置
     */

    public Layer setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * 定义参考坐标
     */

    public Layer defineReferencePixel(int x, int y) {
        dRefX = x;
        dRefY = y;
        return this;
    }

    /**
     * 设置参考点位置
     */

    public Layer setRefPixelPosition(int x, int y) {
        // 依据参考点计算图层左上角坐标
        this.x = x - dRefX;
        this.y = y - dRefY;
        return this;
    }

    /**
     * 返回参考点的X坐标
     */

    public final int getRefPixelX() {
        return x + dRefX;
    }

    /**
     * 返回参考点的Y坐标
     */

    public final int getRefPixelY() {
        return y + dRefY;
    }

    /**
     * 移动图层
     * 
     * @param dx 水平移动距离
     * @param dy 垂直移动距离
     */

    public Layer move(int dx, int dy) {
        x += dx;
        y += dy;
        return this;
    }

    /**
     * 移动至右边界
     */

    public Layer moveToRight(int newRight) {
        move(newRight - width - x, 0);
        return this;
    }

    /**
     * 移动至底部
     */

    public Layer moveToBottom(int newBottom) {
        move(0, newBottom - height - y);
        return this;
    }

    /**
     * 移动至水平中心
     */

    public Layer moveToCenterX(int newX) {
        move(newX - width / 2 - x, 0);
        return this;
    }

    /**
     * 移动至垂直中心
     */

    public Layer moveToCenterY(int newY) {
        move(0, newY - height / 2 - y);
        return this;
    }

    public final int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public final int getX() {
        return x;
    }

    public final int getY() {
        return y;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    /**
     * 返回中心X坐标
     * 
     * @return
     */

    public final int centerX() {
        return x + width / 2;
    }

    /**
     * 返回中心Y坐标
     * 
     * @return
     */

    public final int centerY() {
        return y + height / 2;
    }

    /**
     * 返回画笔缓存，所做修改并未直接反映到绘制画笔
     */

    public final Paint getPaint() {
        if (paint == null)
        {
            paint = new Paint();
        }

        if (bufferPaint == null)
        {
            bufferPaint = new Paint(paint);
        }
        else
        {
            bufferPaint.set(paint);
        }

        return bufferPaint;
    }

    /**
     * 设置画笔
     */

    public void setPaint(Paint paint) {
        if (paint == null)
        {
            this.paint = this.bufferPaint = null;
            return;
        }

        if (this.paint == null)
        {
            this.paint = new Paint(paint);
        }
        else
        {
            this.paint.set(paint);
        }
    }

    /**
     * 设置可见区域（相对坐标）
     */

    public void setVisibleBounds(Rect rect) {
        visibleRect = copyRect(visibleRect, rect);
    }

    /**
     * 设置可见区域（相对坐标）
     */

    public void setVisibleBounds(int x, int y, int width, int height) {
        if (width < 0 || height < 0)
        {
            throw new IllegalArgumentException();
        }

        visibleRect = setRect(visibleRect, x, y, width, height);
    }

    /**
     * 返回可见区域（绝对坐标）
     */

    private Rect getVisibleBounds() {
        visibleRect_abs = copyRect(visibleRect_abs, visibleRect);
        visibleRect_abs.offset(x, y);
        return visibleRect_abs;
    }

    /**
     * 取消自定义绘制
     */

    public void removeDrawn(AppendDrawn drawn) {
        if (drawn == null)
        {
            throw new NullPointerException();
        }

        if (this.drawn != null)
        {
            this.drawn.remove(drawn);
        }
    }

    /**
     * 添加自定义绘制
     */

    public void appendDrawn(AppendDrawn drawn) {
        removeDrawn(drawn);
        if (this.drawn == null)
        {
            this.drawn = new LinkedList<AppendDrawn>();
        }

        this.drawn.add(drawn);
    }

    /**
     * 图层绘制（由系统管理）
     */

    void paint(Canvas canvas) {
        if (visibility == View.VISIBLE)
        {
            Rect r = null;
            if (visibleRect != null)
            {
                r = canvas.getClipBounds();
                canvas.clipRect(getVisibleBounds());
            }

            onDraw(canvas);
            if (drawn != null && !drawn.isEmpty())
            {
                for (AppendDrawn d : drawn)
                {
                    d.onDraw(this, canvas);
                }
            }

            if (r != null)
            {
                canvas.clipRect(r, android.graphics.Region.Op.REPLACE);
            }
        }
    }

    /**
     * 图层绘制（由子类实现）
     */

    public abstract void onDraw(Canvas canvas);

    /**
     * 触屏事件分发（由系统管理）
     * 
     * @param event 触屏事件
     * @return 是否中断事件处理
     */

    boolean dispatchTouchEvent(TouchEvent event) {
        if (visibility == View.GONE)
        {
            return false;
        }

        return onTouchEvent(event);
    }

    /**
     * 触屏事件处理（分为低级事件和高级事件分开处理）
     * 
     * @param event 触屏事件
     * @return 是否中断事件处理
     */

    protected boolean onTouchEvent(TouchEvent event) {
        switch (event.getAction()) {
            case ACTION_DOWN:
                return mousePressed((int) event.getTriggerX(), (int) event.getTriggerY());
            case ACTION_UP:
                return mouseReleased((int) event.getTriggerX(), (int) event.getTriggerY());
            case ACTION_MOVE:
                return mouseDragged((int) event.getTriggerX(), (int) event.getTriggerY());

            default:
                // 高级事件
                return onAdvancedTouchEvent(event);
        }
    }

    /**
     * 鼠标按下
     * 
     * @param x,y 按下时的坐标
     * @return 是否中断事件处理
     */

    protected boolean mousePressed(int x, int y) {
        return false;
    }

    /**
     * 鼠标松开
     * 
     * @param x,y 松开时的坐标
     * @return 是否中断事件处理
     */

    protected boolean mouseReleased(int x, int y) {
        return false;
    }

    /**
     * 鼠标拖曳
     * 
     * @param x,y 实时坐标
     * @return 是否中断事件处理
     */

    protected boolean mouseDragged(int x, int y) {
        return false;
    }

    /**
     * 高级触屏事件处理<br>
     * 需要设置游戏属性
     * 
     * @param event 触屏事件
     * @return 是否中断事件处理
     */

    protected boolean onAdvancedTouchEvent(TouchEvent event) {
        return false;
    }

    @Override
    public String toString() {
        return setRect(null, x, y, width, height).toString();
    }

    /**
     * 自定义绘制接口
     */

    public static interface AppendDrawn {

        /**
         * 自定义绘制方法
         * 
         * @param source 绘制图层
         * @param canvas 绘制画布
         */

        public void onDraw(Layer source, Canvas canvas);

    }
}