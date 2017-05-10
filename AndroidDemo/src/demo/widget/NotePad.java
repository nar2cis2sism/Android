package demo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 记事本
 * @author Daimon
 * @version 3.0
 * @since 11/13/2012
 */

public class NotePad extends View {
    
    private Bitmap image;
    private Canvas canvas;
    
    private Path path;
    private Paint pathPaint;

    private OnNotePadListener listener;

    public NotePad(Context context) {
        super(context);
        init();
    }

    public NotePad(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NotePad(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init()
    {
        path = new Path();
        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setDither(true);
        pathPaint.setColor(Color.BLACK);
        pathPaint.setStyle(Style.STROKE);
        pathPaint.setStrokeWidth(14);
        pathPaint.setStrokeJoin(Join.ROUND);
        pathPaint.setStrokeCap(Cap.ROUND);
    }
    
    public void setNotePaint(Paint paint)
    {
        if (paint != null)
        {
            pathPaint = paint;
        }
    }
    
    public Paint getNotePaint()
    {
        return pathPaint;
    }
    
    public void setOnNotePadListener(OnNotePadListener listener)
    {
        this.listener = listener;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        image = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(image);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, pathPaint);
    }

    private static final float TOUCH_TOLERANCE = 4;
    private float lastX,lastY;
    
    private void mousePressed(float x, float y)
    {
        path.moveTo(lastX = x, lastY = y);
    }
    
    private void mouseMoved(float x, float y)
    {
        float dx = Math.abs(x - lastX);
        float dy = Math.abs(y - lastY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
        {
            path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2);
            lastX = x;
            lastY = y;
        }
    }
    
    private void mouseReleased(float x, float y)
    {
        path.lineTo(x, y);
    }
    
    private Handler handler = new Handler(){
        
        public void handleMessage(android.os.Message msg) {
            //commit the path to our canvas
            canvas.drawPath(path, pathPaint);
            path.reset();
            invalidate();
            
            if (listener != null)
            {
                listener.onNoteToBitmap(image);
            }

            canvas.setBitmap(image = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Config.ARGB_8888));
        };
    };
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            handler.removeMessages(0);
            mousePressed(x, y);
            invalidate();
            break;
        case MotionEvent.ACTION_MOVE:
            mouseMoved(x, y);
            invalidate();
            break;
        case MotionEvent.ACTION_UP:
            mouseReleased(x, y);
            invalidate();
            handler.sendEmptyMessageDelayed(0, 500);
            break;
        }
        
        return true;
    }
    
    /**
     * 记事本监听器
     * @author Daimon
     * @version 3.0
     * @since 11/13/2012
     */
    
    public static interface OnNotePadListener {
        
        public void onNoteToBitmap(Bitmap note);
    }
}