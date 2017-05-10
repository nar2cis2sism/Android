package demo.widget;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

public class XYChartView extends View {
    
    /*****各边距*****/
    private final int spaceLeft     = 30;
    private final int spcaeTop      = 20;
    private final int spaceRight    = 20;
    private final int spaceBottom   = 50;
    
    private final DecimalFormat df = new DecimalFormat("0.#");
    
    private int xLength;                    //X轴长度
    private int yLength;                    //Y轴长度
    
    private int xNum;                       //X轴坐标数
    private int yNum;                       //Y轴坐标数
    
    private int segmentX;                   //X轴段落长度
    private int segmentY;                   //Y轴段落长度
    
    private String xUnit;                   //X轴单位
    private String yUnit;                   //Y轴单位
    
    private String[] coordsX;               //X轴坐标段落文本
    private String[] coordsY;               //Y轴坐标段落文本
    
    private float xMax;                     //X轴坐标最大值
    private float yMax;                     //Y轴坐标最大值
    
    private Paint paint = new Paint();
    private int paintColor = Color.BLACK;   //画笔颜色
    
    private Point basePoint;                //坐标原点
    
    private List<XYChartLine> lines         //线条列表
    = new LinkedList<XYChartLine>();
    private int lineNum;                    //线条数量

    public XYChartView(Context context) {
        super(context);
    }

    public XYChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XYChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setCoordsX(int xNum) {
        this.xNum = xNum;
        coordsX = null;
    }
    
    public void setCoordsX(String[] coordsX) {
        xNum = (this.coordsX = coordsX).length;
    }
    
    public void setCoordsY(int yNum) {
        this.yNum = yNum;
        coordsY = null;
    }
    
    public void setCoordsY(String[] coordsY) {
        yNum = (this.coordsY = coordsY).length;
    }
    
    public void setXUnit(String xUnit) {
        this.xUnit = xUnit;
    }
    
    public void setYUnit(String yUnit) {
        this.yUnit = yUnit;
    }
    
    public void setPaintColor(int paintColor) {
        this.paintColor = paintColor;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if ((xLength = w - spaceLeft - spaceRight) <= 0 || (yLength = h - spcaeTop - spaceBottom) <= 0)
        {
            return;
        }

        basePoint = new Point(spaceLeft, spcaeTop + yLength);
        if (xNum == 0 || yNum == 0)
        {
            return;
        }
        
        segmentX = xLength / xNum; xLength = segmentX * xNum;
        segmentY = yLength / yNum; yLength = segmentY * yNum;
        
        if (xMax <= 0 || yMax <= 0)
        {
            return;
        }
        
        xMax = FloatMath.ceil(xMax);
        yMax = FloatMath.ceil(yMax);
        {
            int pow = 10;
            while (xMax / pow >= 10)
            {
                pow *= 10;
            }
            
            pow /= 10;
            int len = (int) (segmentX * xMax / xLength);
            while (len <= 0 || len % pow != 0)
            {
                len++;
            }
            
            xMax = len * xLength / segmentX;
            
            if (coordsX == null)
            {
                coordsX = new String[xNum];
                for (int i = 0; i < xNum; i++)
                {
                    coordsX[i] = String.valueOf(i * len);
                }
            }
        }
        
        {
            int pow = 10;
            while (yMax / pow >= 10)
            {
                pow *= 10;
            }
            
            pow /= 10;
            int len = (int) (segmentY * yMax / yLength);
            while (len <= 0 || len % pow != 0)
            {
                len++;
            }
            
            yMax = len * yLength / segmentY;
            
            if (coordsY == null)
            {
                coordsY = new String[yNum];
                for (int i = 0; i < yNum; i++)
                {
                    coordsY[i] = String.valueOf(i * len);
                }
            }
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (xLength <= 0 || yLength <= 0)
        {
            //无法显示
            return;
        }
        
        paint.reset();
        paint.setColor(paintColor);
        paint.setStrokeWidth(3);
        
        int x = basePoint.x;
        int y = basePoint.y;
        canvas.drawLine(x, y, x + xLength, y, paint);
        canvas.drawLine(x, y, x, y - yLength, paint);
        
        x += xLength;
        drawTriangle(canvas, new Point(x, y), new Point(x - 10, y - 5), new Point(x - 10, y + 5));
        if (!TextUtils.isEmpty(xUnit)) canvas.drawText(xUnit, x - 15, y + 18, paint);
        
        x = basePoint.x;
        y -= yLength;
        drawTriangle(canvas, new Point(x, y), new Point(x - 5, y + 10), new Point(x + 5, y + 10));
        if (!TextUtils.isEmpty(yUnit)) canvas.drawText(yUnit, x + 12, y + 15, paint);
        
        if (xNum == 0 || yNum == 0)
        {
            return;
        }
        
        drawAxis(canvas, segmentX, segmentY);
        
        if (lineNum > 0 && xMax > 0 && yMax > 0)
        {
            int lineX = 0;
            for (int i = 0; i < lineNum; i++)
            {
                drawLine(canvas, segmentX, lines.get(i), lineX);
                lineX += 70;
            }
        }
    }
    
    /**
     * 绘制坐标轴
     */
    
    private void drawAxis(Canvas canvas, int segmentX, int segmentY)
    {
        paint.reset();
        paint.setColor(paintColor);
        paint.setStrokeWidth(3);
        paint.setTextAlign(Align.CENTER);
        
        int x = basePoint.x;
        int y = basePoint.y;
        for (int i = 0; i < xNum; i++)
        {
            canvas.drawLine(x, y - 5, x, y + 5, paint);
            if (coordsX != null) canvas.drawText(coordsX[i], x, y + 5 + 18, paint);
            x += segmentX;
        }

        x = basePoint.x;
        for (int i = 0; i < yNum; i++)
        {
            canvas.drawLine(x - 5, y, x + 5, y, paint);
            if (coordsY != null) canvas.drawText(coordsY[i], x - 18, y + 5, paint);
            y -= segmentY;
        } 
    }

    /**
     * 绘制坐标轴的箭头
     */
    
    private void drawTriangle(Canvas canvas, Point p1, Point p2, Point p3)
    {
        Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.close();
        
        paint.reset();
        paint.setColor(paintColor);
        
        canvas.drawPath(path, paint);
    }
    
    /**
     * 绘制线条
     */
    
    private void drawLine(Canvas canvas, int segmentX, XYChartLine line, int lineX)
    {
        paint.reset();
        paint.setColor(line.color);
        paint.setStrokeWidth(2);

        Paint textPaint = new Paint();
        textPaint.setColor(paintColor);
        
        float x = basePoint.x + lineX;
        float y = basePoint.y;
        canvas.drawLine(x, y + 35, x + 10, y + 35, paint);
        canvas.drawText(line.name, x + 15, y + 40, textPaint);
        
        float[] coordsX = line.coordsX;
        float[] coordsY = line.coordsY;
        
        Path path = new Path();
        
        for (int i = 0; i < coordsX.length; i++)
        {
            x = basePoint.x + xLength * coordsX[i] / xMax;
            y = basePoint.y - yLength * coordsY[i] / yMax;
            canvas.drawCircle(x, y, 4, paint);
            canvas.drawText(df.format(coordsY[i]), x + 5, y - 10, textPaint);
            if (i == 0)
            {
                path.moveTo(x, y);
            }
            else
            {
                path.lineTo(x, y);
            }
        }

        paint.setStyle(Style.STROKE);
        canvas.drawPath(path, paint);
    }
    
    /**
     * 添加线条
     */
    
    public void addLine(String name, float[] coordsX, float[] coordsY)
    {
        if (coordsX.length != coordsY.length)
        {
            throw new IllegalArgumentException();
        }
        
        xMax = Math.max(xMax, findMaxCoords(coordsX));
        yMax = Math.max(yMax, findMaxCoords(coordsY));
        int[] color = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.MAGENTA};
        lines.add(new XYChartLine(name, color[lineNum++ % color.length], coordsX, coordsY));
    }
    
    private float findMaxCoords(float[] coords)
    {
        float max = 0;
        for (int i = 0; i < coords.length; i++)
        {
            max = Math.max(max, coords[i]);
        }
        
        return max;
    }
    
    private static final class XYChartLine {
        
        String name;                    //线条名称
        int color;                      //线条颜色
        float[] coordsX,coordsY;        //线条对应坐标值
        
        public XYChartLine(String name, int color, float[] coordsX, float[] coordsY) {
            this.name = name;
            this.color = color;
            this.coordsX = coordsX;
            this.coordsY = coordsY;
        }
    }
}