package demo.prototype;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Daimon
 * @version 4.0
 * @since 5/7/2014
 */

public class CustomView extends View {

    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init() {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        int width = getDesiredSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDesiredSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected int getSuggestedMinimumWidth() {
        // TODO Auto-generated method stub
        return super.getSuggestedMinimumWidth();
    }
    
    @Override
    protected int getSuggestedMinimumHeight() {
        // TODO Auto-generated method stub
        return super.getSuggestedMinimumHeight();
    }
    
    /**
     * @see {@link #getDefaultSize(int, int)}
     */
    
    private int getDesiredSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        case MeasureSpec.AT_MOST:
            // TODO Auto-generated method stub
            result = Math.min(size, specSize);
            break;
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        
        return result;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return super.onTouchEvent(event);
    }
    
    public static abstract class TouchEventDelegate implements OnTouchListener {
        
        protected int lastMotionX;
        protected int lastMotionY;

        public boolean onTouchEvent(MotionEvent event) {
            boolean consumed = false;
            int x = (int) event.getX();
            int y = (int) event.getY();
            
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastMotionX = x;
                lastMotionY = y;
                consumed = handleActionDown(event, x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                consumed = handleActionMove(event, x, y);
                lastMotionX = x;
                lastMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                consumed = handleActionUp(event, x, y);
                break;
            }
            
            return consumed;
        }

        public abstract boolean handleActionDown(MotionEvent event, int x, int y);

        public abstract boolean handleActionMove(MotionEvent event, int x, int y);

        public abstract boolean handleActionUp(MotionEvent event, int x, int y);

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return onTouchEvent(event);
        }
    }
}