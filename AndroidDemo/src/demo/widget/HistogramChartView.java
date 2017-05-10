package demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

public class HistogramChartView extends View {
    
    //柱状图的宽度
    private static final int chartWidth = 30;
    //柱状图的高度
    private int chartHeight = 100;
    
    private int maxSize = 100;
    //柱状图显示的数值
    private int size;
    //柱状图显示的单位
    private String unit = "%";
    
    private Paint sizePaint;
    private Paint unitPaint;
    private Paint chartPaint;
    
    private int centerX;
    private int startY;
    private int endY;
    
    //显示模式，默认为false，数值越高，颜色越偏向红色
    private boolean lowerSizeMode;
    //动画模式，是否显示动画加载效果
    private boolean animationMode;

    public HistogramChartView(Context context) {
        super(context);
        init();
    }
    
    public HistogramChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HistogramChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
        sizePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sizePaint.setARGB(255, 99, 66, 0);
        sizePaint.setTextAlign(Align.CENTER);
        sizePaint.setTextSize(18);
        unitPaint = new Paint(sizePaint);
        unitPaint.setARGB(255, 66, 66, 66);
        chartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    
        int width;
        if (widthMode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
        }
        else
        {
            int maxWidth = (int) FloatMath.ceil(Math.max(sizePaint.measureText(String.valueOf(maxSize)), unitPaint.measureText(unit)));
            width = Math.max(chartWidth, maxWidth);
            
            //Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());
            
            if (widthMode == MeasureSpec.AT_MOST && width > widthSize)
            {
                width = widthSize;
            }
        }
        
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    
        int height;
        int maxHeight = (int) FloatMath.ceil(sizePaint.getTextSize() + unitPaint.getTextSize());
        boolean recalculate = false;
        if (heightMode == MeasureSpec.EXACTLY)
        {
            height = heightSize;
            recalculate = true;
        }
        else
        {
            height = chartHeight + maxHeight + 10;
            
            if (height < getSuggestedMinimumHeight())
            {
                height = getSuggestedMinimumHeight();
                recalculate = true;
            }
            
            if (heightMode == MeasureSpec.AT_MOST && height > heightSize)
            {
                height = heightSize;
                recalculate = true;
            }
        }
        
        if (recalculate)
        {
            chartHeight = height - maxHeight - 10;
        }
    
        setMeasuredDimension(width, height);
        centerX = width / 2;
        endY = height - (int) unitPaint.getTextSize() - 5;
        setSize(size);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (size > 0)
        {
            canvas.drawRect(centerX - chartWidth / 2, startY, centerX + chartWidth / 2, endY, chartPaint);
        }
        
        canvas.drawText(String.valueOf(size), centerX, startY - 5, sizePaint);
        canvas.drawText(unit, centerX, getHeight() - 2, unitPaint);
    }
    
    public HistogramChartView setSize(int size) {
        if (size < 0)
        {
            size = 0;
        }
        else if (size > maxSize)
        {
            size = maxSize;
        }
        
        int percent = (int) ((this.size = size) * 100f / maxSize);
        startY = endY - chartHeight * percent / 100;
        chartPaint.setARGB(255, 110, 210, 60);
        if (lowerSizeMode)
        {
            if (percent <= 30)
            {
                chartPaint.setARGB(255, 255 - percent, percent, 20);
            }
            else if (percent <= 50)
            {
                chartPaint.setARGB(255, 200, 200, 60);
            }
        }
        else
        {
            if (percent >= 80)
            {
                chartPaint.setARGB(255, (percent < 100) ? (110 + percent + 45) : 255, (percent < 100) ? 210 - (percent + 45) : 0, 20);
            }
            else if (percent >= 50)
            {
                chartPaint.setARGB(255, 200, 200, 60);
            }
        }
        
        invalidate();
        return this;
    }
    
    public void addSize(int progress)
    {
        setSize(size + progress);
    }
    
    public HistogramChartView setMaxSize(int maxSize) {
        if (maxSize > 0 && maxSize != this.maxSize)
        {
            this.maxSize = maxSize;
            requestLayout();
            setSize(0);
        }
        
        return this;
    }
    
    public HistogramChartView setUnit(String unit) {
        if (unit != null && !unit.equals(this.unit))
        {
            this.unit = unit;
            requestLayout();
        }
        
        return this;
    }
    
    public HistogramChartView setLowerSizeMode(boolean lowerSizeMode) {
        if (lowerSizeMode != this.lowerSizeMode)
        {
            this.lowerSizeMode = lowerSizeMode;
            setSize(size);
        }
        
        return this;
    }
    
    public HistogramChartView setAnimationMode(boolean animationMode) {
        if (animationMode != this.animationMode)
        {
            if (this.animationMode = animationMode)
            {
                //柱状图缓慢增高动画效果
                final int sizeAnim = size;
                size = 0;
                new Handler() {
                    public void handleMessage(android.os.Message msg) {
                        if (size < sizeAnim)
                        {
                            addSize(1);
                            sendEmptyMessageDelayed(0, 15);
                        }
                    };
                }.sendEmptyMessage(0);
            }
        }
        
        return this;
    }
}