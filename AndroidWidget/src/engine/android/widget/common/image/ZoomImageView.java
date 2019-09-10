package engine.android.widget.common.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Observable;
import java.util.Observer;

/**
 * 图片缩放控件
 * 
 * @author Daimon
 * @since 12/15/2013
 */
public class ZoomImageView extends View implements Observer {

    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);            // 图片绘制画笔

    private final Rect src = new Rect();                                        // 图片裁剪区域

    private final Rect dst = new Rect();                                        // 绘制目标区域

    private final OnTouchEvent touchEvent = new OnTouchEvent();                 // 缩放监听器

    private Bitmap image;                                                       // 缩放图片

    private float aspect_quotient;                                              // 纵横商(Aspect ratio content) / (Aspect ratio view)

    private ZoomState state;                                                    // 当前缩放状态

    public ZoomImageView(Context context) {
        super(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 获取缩放状态以便控制缩放参数
     */
    public ZoomState getZoomState() {
        if (state == null)
        {
            state = new ZoomState();
            state.addObserver(this);
        }

        return state;
    }

    /**
     * 设置缩放图片
     */
    public void setZoomImage(Bitmap image) {
        this.image = image;
        calculateAspectQuotient();
        invalidate();
    }

    /**
     * 计算纵横商
     */
    private void calculateAspectQuotient() {
        if (image != null)
        {
            aspect_quotient = ((float) image.getWidth() / image.getHeight())
                            / ((float) getWidth() / getHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        calculateAspectQuotient();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (image != null)
        {
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            ZoomState state = getZoomState();
            float panX = state.getPanX();
            float panY = state.getPanY();
            float zoomX = state.getZoomX(aspect_quotient) * viewWidth / imageWidth;
            float zoomY = state.getZoomY(aspect_quotient) * viewHeight / imageHeight;

            src.left = (int) (panX * imageWidth - viewWidth / (zoomX * 2));
            src.top = (int) (panY * imageHeight - viewHeight / (zoomX * 2));
            src.right = (int) (src.left + viewWidth / zoomX);
            src.bottom = (int) (src.top + viewHeight / zoomY);

            dst.left = getLeft();
            dst.top = getTop();
            dst.right = getRight();
            dst.bottom = getBottom();

            // Adjust source rectangle so that it fits within the source image.
            if (src.left < 0)
            {
                dst.left += -src.left * zoomX;
                src.left = 0;
            }

            if (src.top < 0)
            {
                dst.top += -src.top * zoomY;
                src.top = 0;
            }

            if (src.right > imageWidth)
            {
                dst.right -= (src.right - imageWidth) * zoomX;
                src.right = imageWidth;
            }

            if (src.bottom > imageHeight)
            {
                dst.bottom -= (src.bottom - imageHeight) * zoomY;
                src.bottom = imageHeight;
            }

            canvas.drawBitmap(image, src, dst, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchEvent.onTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (state != null)
        {
            state.deleteObservers();
            state = null;
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        invalidate();
    }

    /**
     * Daimon:多点触控
     */
    private class OnTouchEvent {

        private float lastMotionX;
        private float lastMotionY;

        private float gap;

        @SuppressWarnings("deprecation")
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            int count = event.getPointerCount();
            if (count == 1)
            {
                // 单点移动
                float x = event.getX();
                float y = event.getY();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        lastMotionX = x;
                        lastMotionY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = (x - lastMotionX) / getWidth();
                        float dy = (y - lastMotionY) / getHeight();
                        state.setPanX(state.getPanX() - dx / state.getZoom());
                        state.setPanY(state.getPanY() - dy / state.getZoom());
                        state.notifyDataChanged();
                        lastMotionX = x;
                        lastMotionY = y;
                        break;
                }
            }
            else if (count == 2)
            {
                // 多点缩放
                float x0 = event.getX(event.getPointerId(0));
                float y0 = event.getY(event.getPointerId(0));
                float x1 = event.getX(event.getPointerId(1));
                float y1 = event.getY(event.getPointerId(1));

                float gap = getGap(x0, y0, x1, y1);
                switch (action) {
                    case MotionEvent.ACTION_POINTER_1_DOWN:
                    case MotionEvent.ACTION_POINTER_2_DOWN:
                        this.gap = gap;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        state.setZoom(state.getZoom() * (float) Math.pow(5, gap / this.gap - 1));
                        state.notifyDataChanged();
                        this.gap = gap;
                        break;
                    case MotionEvent.ACTION_POINTER_1_UP:
                        lastMotionX = x1;
                        lastMotionY = y1;
                        break;
                    case MotionEvent.ACTION_POINTER_2_UP:
                        lastMotionX = x0;
                        lastMotionY = y0;
                        break;
                }
            }

            return true;
        }

        private float getGap(float x0, float y0, float x1, float y1) {
            return (float) Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
        }
    }

    /**
     * 缩放状态，控制缩放参数<br>
     * 使用观察者模式，通知视图更新<br>
     * Clients that modify ZoomState should call {@link #notifyDataChanged()}
     * 
     * Daimon:Observable
     */
    public static class ZoomState extends Observable {

        private float zoom;                     // 缩放比例(A value of 1.0 means the content fits the view)

        private float panX,panY;                // 缩放窗口中心位置坐标(relative to the content)

        float maxZoom,minZoom;                  // 缩放范围控制

        ZoomState() {
            zoom = 1.0f;
            panX = panY = 0.5f;
            maxZoom = 2.0f;
            minZoom = 0.5f;
        }

        public void notifyDataChanged() {
            notifyObservers();
        }

        public float getZoom() {
            return zoom;
        }

        public void setZoom(float zoom) {
            if (this.zoom != (zoom = Math.min(maxZoom, Math.max(minZoom, zoom))))
            {
                this.zoom = zoom;
                setChanged();
            }
        }

        public void setZoomRange(float maxZoom, float minZoom) {
            this.maxZoom = maxZoom;
            this.minZoom = minZoom;
            setZoom(zoom);
        }

        public float getPanX() {
            return panX;
        }

        public void setPanX(float panX) {
            if (this.panX != panX)
            {
                this.panX = panX;
                setChanged();
            }
        }

        public float getPanY() {
            return panY;
        }

        public void setPanY(float panY) {
            if (this.panY != panY)
            {
                this.panY = panY;
                setChanged();
            }
        }

        /**
         * 计算水平方向上的缩放比例
         */
        float getZoomX(float aspect_quotient) {
            return Math.min(zoom, zoom * aspect_quotient);
        }

        /**
         * 计算垂直方向上的缩放比例
         */
        float getZoomY(float aspect_quotient) {
            return Math.min(zoom, zoom / aspect_quotient);
        }
    }
}