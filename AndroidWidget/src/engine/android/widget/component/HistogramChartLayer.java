package engine.android.widget.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

/**
 * 柱状图背景层
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class HistogramChartLayer extends View {

    private int segmentWidth;               // 段落宽度
    private int segmentHeight;              // 段落高度
    
    private int textWidth;                  // 文本宽度
    private int textHeight;                 // 文本高度
    private int horizontalGap = 8;          // 文本与线条的水平间距
    private int verticalOffset;             // 文本与线条的垂直偏移
    
    private int lines = 5;                  // 线条数量
    private int count = 1;                  // 段落数量
    
    private int maxValue = 100;             // 最大数值
    private String format;                  // 格式化显示文本

    private Paint textPaint;                // 文本画笔
    private Paint linePaint;                // 实线画笔
    private Paint dashPaint;                // 虚线画笔
    
    final Path path = new Path();           // 虚线路径

    public HistogramChartLayer(Context context) {
        this(context, null);
    }
    
    public HistogramChartLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Align.RIGHT);
        textPaint.setTextSize(20);
        textPaint.setColor(Color.WHITE);
        
        linePaint = new Paint(textPaint);
        linePaint.setStrokeWidth(2);
        
        dashPaint = new Paint(textPaint);
        dashPaint.setStyle(Style.STROKE);
        dashPaint.setStrokeWidth(1);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
        
        textHeight = textPaint.getFontMetricsInt(null);
        verticalOffset = textHeight / 4;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        textWidth = (int) Math.ceil(textPaint.measureText(getTextValue(maxValue)));
        segmentWidth = (getMeasuredWidth() - textWidth - horizontalGap) / count;
        segmentHeight = (getMeasuredHeight() - textHeight + verticalOffset) / lines;
    }
    
    private String getTextValue(int value) {
        if (TextUtils.isEmpty(format))
        {
            return String.valueOf(value);
        }
        
        return String.format(format, value);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        int value = maxValue / lines;
        int lineX = textWidth + horizontalGap;
        int y = textHeight;
        for (int i = 0; i < lines; i++, y += segmentHeight)
        {
            // 绘制文本
            canvas.drawText(getTextValue(value * (lines - i)), textWidth, y, textPaint);
            // 绘制虚线
            path.reset();
            path.moveTo(lineX, y - verticalOffset);
            path.lineTo(getWidth(), y - verticalOffset);
            canvas.drawPath(path, dashPaint);
        }
        
        // 绘制实线
        y = getHeight();
        canvas.drawLine(lineX, y, getWidth(), y, linePaint);
        
        // 绘制段落
        for (int i = 0; i < count; i++, lineX += segmentWidth)
        {
            canvas.drawLine(lineX, y, lineX, y - 8, linePaint);
        }
        
        lineX = getWidth() - 1;
        canvas.drawLine(lineX, y, lineX, y - 8, linePaint);
    }
    
    public HistogramChartLayer setLines(int lines) {
        if (lines > 0 && lines != this.lines)
        {
            this.lines = lines;
            requestLayout();
        }
        
        return this;
    }
    
    public HistogramChartLayer setCount(int count) {
        if (count > 0 && count != this.count)
        {
            this.count = count;
            requestLayout();
        }
        
        return this;
    }
    
    public HistogramChartLayer setMaxValue(int maxValue) {
        if (maxValue > 0 && maxValue != this.maxValue)
        {
            this.maxValue = maxValue;
            requestLayout();
        }
        
        return this;
    }
    
    public HistogramChartLayer setFormat(String format) {
        if (!TextUtils.equals(format, this.format))
        {
            this.format = format;
            requestLayout();
        }
        
        return this;
    }
}