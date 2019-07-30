package engine.android.widget.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * 计时进度条
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class TimerProgress extends ProgressBar {

    private Paint paint;
    private int strokenWidth = 13;              // 环形线条宽度
    private int center;                         // 中心点
    private int radius;                         // 内环半径
    private final RectF arc = new RectF();      // 内环矩形

    public TimerProgress(Context context) {
        this(context, null);
    }

    public TimerProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(strokenWidth);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int size = Math.max(width, height);
        // 保证偶数
        if (size % 2 != 0)
        {
            size += 1;
        }
        
        setMeasuredDimension(size, size);
        
        radius = (center = size / 2) - strokenWidth;
        arc.set(strokenWidth, strokenWidth, size - strokenWidth, size - strokenWidth);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(getResources().getColor(android.R.color.white));
        canvas.drawCircle(center, center, radius, paint);
        
        float percent = getProgress() * 1.0f / getMax();
        // 计算角度
        float angle = percent * 360;
        // 计算色值
        paint.setColor(getColor(percent));
        canvas.drawArc(arc, -90, -angle, false, paint);
    }
    
    private int getColor(float percent) {
        return percent > 0.5f ? Color.parseColor("#20b99a") : Color.parseColor("#f04844");
    }
}