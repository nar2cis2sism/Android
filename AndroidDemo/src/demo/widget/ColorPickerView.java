package demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 颜色选取
 * 
 * @author Daimon
 * @version 3.0
 * @since 11/12/2012
 */

public class ColorPickerView extends View {

    private static final int[] colors = {
        0xFF000000, 0xFFFF0000, 0xFFFFFF00,
        0xFF00FF00, 0xFF00FFFF, 0xFF0000FF,
        0xFFFF00FF, 0xFFFFFFFF, 0xFF000000 };

    private int width, height;

    private int circle_radius = 100;
    private int center_radius = 32;

    private Paint paint;
    private Paint centerPaint;
    
    private final RectF oval = new RectF();

    private OnColorPickerListener listener;

    private boolean isTrackingCenter;
    private boolean isHighlightCenter;

    public ColorPickerView(Context context) {
        super(context);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new SweepGradient(0, 0, colors, null));
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(center_radius);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setStrokeWidth(5);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = circle_radius * 2;
        int w = getSize(size, widthMeasureSpec);
        int h = getSize(size, heightMeasureSpec);
        setMeasuredDimension(width = w, height = h);
        updateRadius(Math.min(w / 2, h / 2));
    }

    private int getSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }

        return result;
    }

    private void updateRadius(int radius) {
        if (radius != circle_radius)
        {
            center_radius = (circle_radius = radius) * 32 / 100;
            paint.setStrokeWidth(center_radius);
            
            float r = circle_radius - paint.getStrokeWidth() * 0.5f;
            oval.set(-r, -r, r, r);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(width / 2, height / 2);

        canvas.drawOval(oval, paint);
        canvas.drawCircle(0, 0, center_radius, centerPaint);

        if (isTrackingCenter)
        {
            int color = centerPaint.getColor();
            centerPaint.setStyle(Style.STROKE);

            if (isHighlightCenter)
            {
                centerPaint.setAlpha(0xFF);
            }
            else
            {
                centerPaint.setAlpha(0x80);
            }

            canvas.drawCircle(0, 0, center_radius + centerPaint.getStrokeWidth(), centerPaint);

            centerPaint.setStyle(Style.FILL);
            centerPaint.setColor(color);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - width / 2;
        float y = event.getY() - height / 2;
        boolean isInCenter = x * x + y * y <= center_radius * center_radius;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTrackingCenter = isInCenter)
                {
                    isHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (isTrackingCenter)
                {
                    if (isHighlightCenter != isInCenter)
                    {
                        isHighlightCenter = isInCenter;
                        invalidate();
                    }
                }
                else
                {
                    double angle = Math.atan2(y, x);
                    // need to turn angle[-PI,PI] into unit[0,1]
                    float unit = (float) (angle / (2 * Math.PI));
                    if (unit < 0)
                    {
                        unit += 1;
                    }

                    centerPaint.setColor(interpColor(colors, unit));
                    if (listener != null)
                    {
                        listener.onColorChanged(centerPaint.getColor());
                    }

                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                if (isTrackingCenter)
                {
                    if (isInCenter && listener != null)
                    {
                        listener.onColorPicked(centerPaint.getColor());
                    }

                    isTrackingCenter = false;
                    invalidate();
                }

                break;
        }

        return true;
    }

    private int interpColor(int colors[], float unit) {
        if (unit <= 0)
        {
            return colors[0];
        }
        else if (unit >= 1)
        {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;

        // now p is just the fractional part [0,1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    private int ave(int c0, int c1, float p) {
        return c0 + Math.round(p * (c1 - c0));
    }

    public void setOnColorPickerListener(OnColorPickerListener listener) {
        this.listener = listener;
    }

    public void setInitialColor(int color) {
        centerPaint.setColor(color);
    }

    /**
     * 颜色选取监听器
     */

    public static interface OnColorPickerListener {

        public void onColorChanged(int color);

        public void onColorPicked(int color);
    }
}