package engine.android.widget.common.chart;

import engine.android.util.AndroidUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * 蜘蛛网状图
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class SpiderChartView extends View {
    
    private int count = 6;                  // 六边形（起始向右顺时针维度）
    private int layer = count - 1;          // 5层蜘蛛网
    
    public int spiderColor                  // 蜘蛛网颜色
    = Color.parseColor("#444444");
    public int radiusColor                  // 蜘蛛丝颜色
    = Color.parseColor("#CCCCCC");
    public int regionColor                  // 覆盖区域颜色
    = Color.parseColor("#801AAF03");
    public int pointColor                   // 小圆点颜色
    = Color.parseColor("#1AAF03");
    public int textColor                    // 文本颜色
    = Color.parseColor("#1AAF03");
    
    private float[] data;                   // 各维度百分比数值
    private String[] title;                 // 各维度显示标题文本
    
    private int minSize;                    // 绘制图形大小
    private float radius;                   // 半径
    private int centerX,centerY;            // 中点坐标
    private double angle;                   // 每条边对应的圆心角
    private int degree;                     // 每个角对应的度数
    
    private Paint linePaint;                // 绘制线条画笔
    private Paint dataPaint;                // 绘制数据画笔
    private Paint textPaint;                // 绘制文本画笔
    
    private final Path path = new Path();   // 绘制路径

    public SpiderChartView(Context context) {
        this(context, null);
    }

    public SpiderChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Style.STROKE);

        dataPaint = new Paint();
        dataPaint.setAntiAlias(true);
        dataPaint.setStyle(Style.FILL_AND_STROKE);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        setTextSize(AndroidUtil.dp2px(getContext(), 12));

        setCount(count);
    }

    public void setCount(int count) {
        if ((this.count = count) != 0)
        {
            angle = Math.PI * 2 / count;
            degree = 360 / count;
            invalidate();
        }
    }

    public void setLayer(int layer) {
        this.layer = layer;
        invalidate();
    }

    /**
     * 设置文本字体大小
     */
    public void setTextSize(float size) {
        textPaint.setTextSize(size);
        invalidate();
    }

    public void setData(float[] data) {
        if ((this.data = data) != null)
        {
            setCount(count = data.length);
        }
    }

    public void setTitle(String[] title) {
        this.title = title;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        minSize = Math.min(width, height);
        if (width != height)
        {
            width = height = Math.max(width, height);
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radius = minSize / 2;
        centerX = w / 2;
        centerY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPolygon(canvas);
        drawRadius(canvas);
        if (data != null) drawRegion(canvas);
        if (title != null) drawText(canvas);
    }

    /**
     * 绘制多边形
     */
    private void drawPolygon(Canvas canvas) {
        linePaint.setColor(spiderColor);
        float r = radius / layer; // 蜘蛛丝之间的间距
        for (int i = 0; i < layer; i++)
        {
            float curR = r * (i + 1); // 当前半径
            path.reset();
            for (int j = 0; j < count; j++)
            {
                if (j == 0)
                {
                    path.moveTo(centerX + curR, centerY);
                }
                else
                {
                    // 根据半径，计算蜘蛛丝上每个点的坐标
                    double angle = this.angle * j;
                    float x = (float) (centerX + curR * Math.cos(angle));
                    float y = (float) (centerY + curR * Math.sin(angle));
                    path.lineTo(x, y);
                }
            }

            path.close(); // 闭合路径
            canvas.drawPath(path, linePaint);
        }
    }

    /**
     * 绘制从中心到末端的直线
     */
    private void drawRadius(Canvas canvas) {
        linePaint.setColor(radiusColor);
        for (int i = 0; i < count; i++)
        {
            double angle = this.angle * i;
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));
            canvas.drawLine(centerX, centerY, x, y, linePaint);
        }
    }

    /**
     * 绘制数值区域
     */
    private void drawRegion(Canvas canvas) {
        dataPaint.setColor(pointColor);
        path.reset();
        for (int i = 0; i < count; i++)
        {
            float curR = radius * data[i] / 100;
            double angle = this.angle * i;
            float x = (float) (centerX + curR * Math.cos(angle));
            float y = (float) (centerY + curR * Math.sin(angle));
            if (i == 0)
            {
                path.moveTo(x, y);
            }
            else
            {
                path.lineTo(x, y);
            }
            // 绘制小圆点
            canvas.drawCircle(x, y, 10, dataPaint);
        }

        path.close();
        // 绘制填充区域
        dataPaint.setColor(regionColor);
        canvas.drawPath(path, dataPaint);
    }

    /**
     * 绘制文本
     */
    private void drawText(Canvas canvas) {
        float fontHeight = AndroidUtil.getFontHeight(textPaint) - AndroidUtil.dp2px(getContext(), 6);
        float curR = radius + fontHeight / 2; // 文本到中心点的距离
        for (int i = 0; i < count; i++)
        {
            double angle = this.angle * i;
            float x = (float) (centerX + curR * Math.cos(angle));
            float y = (float) (centerY + curR * Math.sin(angle));
            int degree = this.degree * i;
            if (degree > 270)
            {
                // 第1象限
                textPaint.setTextAlign(Align.LEFT);
            }
            else if (degree > 180)
            {
                // 第2象限
                textPaint.setTextAlign(Align.RIGHT);
            }
            else
            {
                if (degree > 90)
                {
                    // 第3象限
                    textPaint.setTextAlign(Align.RIGHT);
                }
                else
                {
                    // 第4象限
                    textPaint.setTextAlign(Align.LEFT);
                }

                y += fontHeight;
            }

            if (degree == 0 || degree == 180)
            {
                // 刚好在X坐标轴上
                y -= fontHeight / 2;
            }
            else if (degree == 90 || degree == 270)
            {
                // 刚好在Y坐标轴上
                textPaint.setTextAlign(Align.CENTER);
            }

            canvas.drawText(title[i], x, y, textPaint);
        }
    }
}