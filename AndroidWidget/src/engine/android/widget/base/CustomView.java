package engine.android.widget.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 自定义View模板
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class CustomView extends View {

    public CustomView(Context context) {
        super(context);
        init(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    protected void init(Context context) {}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDesiredSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDesiredSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    /**
     * @see {@link #getDefaultSize(int, int)}
     */
    public static int getDesiredSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                result = Math.min(size, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }

        return result;
    }

    public static abstract class TouchEventDelegate implements OnTouchListener {

        protected int lastMotionX;
        protected int lastMotionY;

        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_CANCEL)
            {
                handleActionCancel(event);
                return true;
            }

            int x = (int) event.getX();
            int y = (int) event.getY();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    return handleActionDown(event, lastMotionX = x, lastMotionY = y);
                case MotionEvent.ACTION_MOVE:
                    handleActionMove(event, x, y);
                    lastMotionX = x;
                    lastMotionY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    handleActionUp(event, x, y);
                    break;
            }

            return true;
        }

        public abstract boolean handleActionDown(MotionEvent event, int x, int y);

        public void handleActionMove(MotionEvent event, int x, int y) {}

        public void handleActionUp(MotionEvent event, int x, int y) {}

        public void handleActionCancel(MotionEvent event) {}

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return onTouchEvent(event);
        }
    }
}