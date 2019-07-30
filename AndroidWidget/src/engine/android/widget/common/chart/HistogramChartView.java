package engine.android.widget.common.chart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.TypedValue;
import android.view.View;

import engine.android.widget.R;

/**
 * 柱状图
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class HistogramChartView extends View {
    
    private int chartWidth;                 // 柱状图的宽度
    private int chartHeight = 100;          // 柱状图的高度
    private int chartColor;                 // 柱状图的颜色
    
    private int maxSize = 100;              // 柱状图的最大值
    private int size;                       // 柱状图当前数值
    private boolean showSize;               // 是否显示数值
    private String unit = "%";              // 柱状图显示的单位
    
    private boolean lowerSizeMode;          // 显示模式，数值越低，颜色越偏向红色，反之亦然
    private boolean animationMode;          // 动画模式，是否显示动画加载效果
    
    private Paint textPaint;
    private Paint chartPaint;
    
    private int centerX;
    private int startY;
    private int endY;

    public HistogramChartView(Context context) {
        this(context, null);
    }
    
    public HistogramChartView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.HistogramChartView, 0);
    }

    public HistogramChartView(Context context, int defStyleRes) {
        this(context, null, R.attr.HistogramChartView, defStyleRes);
    }

    private HistogramChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.HistogramChartView, defStyleAttr, defStyleRes);
        
        int textSize = a.getDimensionPixelSize(R.styleable.HistogramChartView_android_textSize, 18);
        int textColor = a.getColor(R.styleable.HistogramChartView_android_textColor, 0);
        chartWidth = a.getDimensionPixelSize(R.styleable.HistogramChartView_chartWidth, 30);
        chartColor = a.getColor(R.styleable.HistogramChartView_chartColor, -1);
        showSize = a.getBoolean(R.styleable.HistogramChartView_showSize, true);
        int mode = a.getInt(R.styleable.HistogramChartView_mode, 0);
        lowerSizeMode = (mode & 1) != 0;
        animationMode = (mode & 2) != 0;
        
        a.recycle();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        chartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
        
        centerX = width / 2;
        endY = height - (int) textPaint.getTextSize() - 5;
        setSize(size);
    }
    
    private int measureWidth(int widthMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    
        int width;
        if (widthMode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
        }
        else
        {
            int maxWidth = (int) FloatMath.ceil(Math.max(
                    textPaint.measureText(String.valueOf(maxSize)), 
                    textPaint.measureText(unit)));
            width = Math.max(chartWidth, maxWidth);
            
            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());
            
            if (widthMode == MeasureSpec.AT_MOST && width > widthSize)
            {
                width = widthSize;
            }
        }
        
        return width;
    }
    
    private int measureHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    
        int height;
        int maxHeight = (int) FloatMath.ceil(textPaint.getTextSize() * 2);
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
        
        return height;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (size > 0)
        {
            canvas.drawRect(centerX - chartWidth / 2, startY, centerX + chartWidth / 2, endY, chartPaint);
        }
        
        if (showSize)
        {
            canvas.drawText(String.valueOf(size), centerX, startY - 5, textPaint);
        }
        
        canvas.drawText(unit, centerX, getHeight() - 2, textPaint);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setAnimationMode(animationMode);
    }
    
    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setTextSize(int unit, float size) {
        setRawTextSize(TypedValue.applyDimension(
                unit, size, getResources().getDisplayMetrics()));
    }

    private void setRawTextSize(float size) {
        if (size != textPaint.getTextSize())
        {
            textPaint.setTextSize(size);
            requestLayout();
        }
    }
    
    public void setTextColor(int color) {
        if (color != textPaint.getColor())
        {
            textPaint.setColor(color);
            invalidate();
        }
    }
    
    public void setChartWidth(int chartWidth) {
        if (chartWidth != this.chartWidth)
        {
            this.chartWidth = chartWidth;
            requestLayout();
        }
    }
    
    public void setChartColor(int chartColor) {
        if (chartColor != this.chartColor)
        {
            this.chartColor = chartColor;
            setSize(size);
        }
    }
    
    public void setShowSize(boolean showSize) {
        if (showSize != this.showSize)
        {
            this.showSize = showSize;
            invalidate();            
        }
    }
    
    public int getSize() {
        return size;
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
        
        int percent = ((this.size = size) * 100 / maxSize);
        startY = endY - chartHeight * percent / 100;
        
        setupChartColor(percent);
        invalidate();
        return this;
    }
    
    private void setupChartColor(int percent) {
        if (chartColor != -1)
        {
            chartPaint.setColor(chartColor);
            return;
        }
        
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
    }
    
    public void addSize(int progress) {
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
        if (this.animationMode = animationMode)
        {
            // 柱状图缓慢增高动画效果
            final int sizeAnim = size;
            size = 0;
            post(new Runnable() {
                
                @Override
                public void run() {
                    if (size < sizeAnim)
                    {
                        addSize(1);
                        postDelayed(this, 15);
                    }
                }
            });
        }
        
        return this;
    }
}