package engine.android.game;

import static engine.android.util.api.RectUtil.copyRect;
import static engine.android.util.api.RectUtil.setRect;

import engine.android.game.GameCanvas.TouchEvent;
import engine.android.game.LayerManager.Layer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * 图层管理器
 * 
 * @author Daimon
 * @since 8/13/2012
 */
public class LayerManager {

    private final GameCanvas gc;                                // 游戏画布

    private final Point position = new Point();                 // 画布位置

    private final List<Layer> layers = new Box<Layer>();        // 图层容器

    private int viewX, viewY, viewWidth, viewHeight;            // 可视区域

    LayerManager(GameCanvas gc) {
        this.gc = gc;
        setViewWindow(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * 设置画布位置
     */
    public void setPosition(int x, int y) {
        position.set(x, y);
    }

    /**
     * 返回画布位置
     */
    public Point getPosition() {
        return position;
    }

    /**
     * 移动画布位置
     */
    public void translate(int dx, int dy) {
        position.offset(dx, dy);
    }

    /**
     * 添加图层
     */
    public void append(Layer l) {
        layers.add(l);
    }

    /**
     * 插入图层
     */
    public void insert(Layer l, int index) {
        layers.add(index, l);
    }

    /**
     * 获取图层
     * 
     * @param index 图层索引
     */
    public Layer getLayerAt(int index) {
        return layers.get(index);
    }

    /**
     * 获取图层数量
     */
    public int getSize() {
        return layers.size();
    }

    /**
     * 移除图层
     */
    public void remove(Layer l) {
        layers.remove(l);
    }

    /**
     * 清空图层
     */
    public void clear() {
        layers.clear();
    }

    /**
     * 画布渲染
     */
    void render(Canvas canvas) {
        canvas.save();

        canvas.clipRect(viewX, viewY, viewX + viewWidth, viewY + viewHeight);
        canvas.translate(position.x, position.y);
        if (gc.isScreenAutoAdapt)
        {
            canvas.scale(gc.scaleX, gc.scaleY);
        }

        for (Layer l : layers)
        {
            l.paint(canvas);
        }

        canvas.restore();
    }

    /**
     * 触屏事件处理
     * 
     * @param event 触屏事件
     */
    void touchAction(TouchEvent event) {
        event.move(-position.x, -position.y);
        for (ListIterator<Layer> iter = layers.listIterator(layers.size()); iter.hasPrevious();)
        {
            if (iter.previous().dispatchTouchEvent(event))
            {
                return;
            }
        }
    }

    /**
     * 设置窗口显示区域（相对于手机屏幕）
     */
    public void setViewWindow(int x, int y, int width, int height) {
        if (width < 0 || height < 0)
        {
            throw new IllegalArgumentException();
        }

        viewX = x;
        viewY = y;
        viewWidth = width;
        viewHeight = height;
    }

    /**
     * 返回画布的宽度，代替{@link GameCanvas#getWidth()}
     */
    public int getWidth() {
        return gc.width;
    }

    /**
     * 返回画布的高度，代替{@link GameCanvas#getHeight()}
     */
    public int getHeight() {
        return gc.height;
    }
    
    /**
     * 返回画布的缩放宽度（实际绘制时的宽度）
     */
    public float getScaledWidth() {
        float width = getWidth();
        if (gc.isSupportAutoAdapt)
        {
            width *= gc.scaleX;
        }
        
        return width;
    }
    
    /**
     * 返回画布的缩放高度（实际绘制时的高度）
     */
    public float getScaledHeight() {
        float height = getHeight();
        if (gc.isSupportAutoAdapt)
        {
            height *= gc.scaleY;
        }
        
        return height;
    }
    
    /**
     * 游戏图层
     */
    public static abstract class Layer implements EventConstants {

        public static final int VISIBLE             = View.VISIBLE;
        public static final int INVISIBLE           = View.INVISIBLE;
        public static final int GONE                = View.GONE;

        protected int x, y;                         // 图层的位置（左上角的坐标值）
        protected int width, height;                // 图层的大小
        private int dRefX, dRefY;                   // 参考坐标（相对于图层左上角的坐标）

        protected int visibility;                   // 可见状态
        private Rect visibleRect;                   // 可见区域（相对坐标）
        private Rect visibleRect_abs;               // 可见区域（绝对坐标）

        protected Paint paint;                      // 绘制画笔
        private Paint bufferPaint;                  // 画笔缓存

        private LinkedList<AppendDrawn> drawn;      // 自定义绘制接口

        protected Layer(int width, int height) {
            setSize(width, height);
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
            if (width == this.width && height == this.height)
            {
                return false;
            }

            setSize(width, height);
            return true;
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
         */
        public final int getCenterX() {
            return x + width / 2;
        }

        /**
         * 返回中心Y坐标
         */
        public final int getCenterY() {
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
         * 添加自定义绘制
         */
        public void appendDrawn(AppendDrawn drawn) {
            if (this.drawn == null)
            {
                this.drawn = new LinkedList<AppendDrawn>();
            }

            this.drawn.add(drawn);
        }

        /**
         * 取消自定义绘制
         */
        public void removeDrawn(AppendDrawn drawn) {
            if (this.drawn != null)
            {
                this.drawn.remove(drawn);
            }
        }

        /**
         * 图层绘制（由系统管理）
         */
        public final void paint(Canvas canvas) {
            if (visibility != VISIBLE) return;
            
            Rect r = null;
            if (visibleRect != null)
            {
                r = canvas.getClipBounds();
                canvas.clipRect(getVisibleBounds());
            }
            
            Integer saveCount = null;
            if (mTransformationInfo != null)
            {
                saveCount = canvas.save();
                mTransformationInfo.transform(canvas);
                getPaint(); paint.setAlpha((int) (0xff * mTransformationInfo.mAlpha));
            }

            onDraw(canvas);
            if (drawn != null && !drawn.isEmpty())
            {
                for (AppendDrawn d : drawn)
                {
                    d.onDraw(this, canvas);
                }
            }
            
            if (saveCount != null)
            {
                canvas.restoreToCount(saveCount);
            }

            if (r != null)
            {
                canvas.clipRect(r, android.graphics.Region.Op.REPLACE);
            }
        }

        /**
         * 图层绘制（由子类实现）
         */
        protected abstract void onDraw(Canvas canvas);

        /**
         * 触屏事件分发（由系统管理）
         * 
         * @param event 触屏事件
         * @return 是否中断事件处理
         */
        public final boolean dispatchTouchEvent(TouchEvent event) {
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
        public interface AppendDrawn {

            /**
             * 自定义绘制方法
             * 
             * @param source 绘制图层
             * @param canvas 绘制画布
             */
            void onDraw(Layer source, Canvas canvas);
        }

        /******************************* 属性动画 *******************************/

        private TransformationInfo mTransformationInfo;

        public float getTranslationX() {
            return mTransformationInfo != null ? mTransformationInfo.mTranslationX : 0;
        }

        public void setTranslationX(float translationX) {
            ensureTransformationInfo().mTranslationX = translationX;
        }

        public float getTranslationY() {
            return mTransformationInfo != null ? mTransformationInfo.mTranslationY : 0;
        }

        public void setTranslationY(float translationY) {
            ensureTransformationInfo().mTranslationY = translationY;
        }

        public float getRotation() {
            return mTransformationInfo != null ? mTransformationInfo.mRotation : 0;
        }

        public void setRotation(float rotation) {
            ensureTransformationInfo().mRotation = rotation;
        }

        public float getScaleX() {
            return mTransformationInfo != null ? mTransformationInfo.mScaleX : 1;
        }

        public void setScaleX(float scaleX) {
            ensureTransformationInfo().mScaleX = scaleX;
        }

        public float getScaleY() {
            return mTransformationInfo != null ? mTransformationInfo.mScaleY : 1;
        }

        public void setScaleY(float scaleY) {
            ensureTransformationInfo().mScaleY = scaleY;
        }

        public float getPivotX() {
            return mTransformationInfo != null ? mTransformationInfo.mPivotX : 0;
        }

        public void setPivotX(float pivotX) {
            ensureTransformationInfo().mPivotX = pivotX;
        }

        public float getPivotY() {
            return mTransformationInfo != null ? mTransformationInfo.mPivotY : 0;
        }

        public void setPivotY(float pivotY) {
            ensureTransformationInfo().mPivotY = pivotY;
        }

        public float getAlpha() {
            return mTransformationInfo != null ? mTransformationInfo.mAlpha : 1;
        }

        public void setAlpha(float alpha) {
            ensureTransformationInfo().mAlpha = alpha;
        }

        private TransformationInfo ensureTransformationInfo() {
            if (mTransformationInfo == null) mTransformationInfo = new TransformationInfo(this);
            return mTransformationInfo;
        }
    }
}

class TransformationInfo {
    
    private final Layer layer;
    /**
     * These prev values are used to recalculate a centered pivot point when necessary. The
     * pivot point is only used in matrix operations (when rotation, scale, or translation are
     * set), so thes values are only used then as well.
     */
    private int mPrevWidth = -1;
    private int mPrevHeight = -1;
    boolean mPivotExplicitlySet;
    /**
     * The degrees rotation around the pivot point.
     */
    float mRotation = 0f;
    /**
     * The amount of translation of the object away from its left property (post-layout).
     */
    float mTranslationX = 0f;
    /**
     * The amount of translation of the object away from its top property (post-layout).
     */
    float mTranslationY = 0f;
    /**
     * The amount of scale in the x direction around the pivot point. A
     * value of 1 means no scaling is applied.
     */
    float mScaleX = 1f;
    /**
     * The amount of scale in the y direction around the pivot point. A
     * value of 1 means no scaling is applied.
     */
    float mScaleY = 1f;
    
    float mPivotX = 0f;
    float mPivotY = 0f;
    /**
     * The opacity of the Layer. This is a value from 0 to 1, where 0 means
     * completely transparent and 1 means completely opaque.
     */
    float mAlpha = 1f;
    
    public TransformationInfo(Layer layer) {
        this.layer = layer;
    }

    public void transform(Canvas canvas) {
        // Figure out if we need to update the pivot point
        if (!mPivotExplicitlySet)
        {
            if (layer.width != mPrevWidth || layer.height != mPrevHeight)
            {
                mPivotX = layer.x + (mPrevWidth = layer.width) / 2f;
                mPivotY = layer.y + (mPrevHeight = layer.height) / 2f;
            }
        }
        
        canvas.translate(mTranslationX, mTranslationY);
        canvas.rotate(mRotation, mPivotX, mPivotY);
        canvas.scale(mScaleX, mScaleY, mPivotX, mPivotY);
    }
}