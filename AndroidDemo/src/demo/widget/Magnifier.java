package demo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.view.View;

/**
 * 放大镜效果实现
 * @author Daimon
 * @version 3.0
 * @since 10/25/2012
 */

public class Magnifier extends View {
    
    private static final int SIZE = 200;            //默认放大镜尺寸（直径）
    private static final int LEVEL = 2;             //默认放大倍数
    
    private int circleX,circleY;                    //放大镜的圆心坐标
    private int radius;                             //放大镜的半径
    private float scale;                            //放大倍数
    
    private Path path = new Path();
    private Matrix m = new Matrix();
    private Paint paint = new Paint();
    
    private Bitmap image;
    
    private int darkGlassColor;
    private int brightGlassColor;
    private int lightInGlassColor;
    
    private Path path1 = new Path();
    private Path path2 = new Path();
    private Path path3 = new Path();

    public Magnifier(Context context) {
        super(context);
        
        darkGlassColor = Color.argb(50, 100, 100, 100);
        brightGlassColor = Color.argb(50, 140, 140, 140);
        lightInGlassColor = Color.argb(100, 255, 255, 255);
        
        setRadius(SIZE / 2);
        setScaleLevel(LEVEL);
    }
    
    /**
     * 设置放大屏幕图片
     */
    
    public void setScreenImage(Bitmap image)
    {
        this.image = image;
        invalidate();
    }
    
    /**
     * 设置放大镜左上角位置
     */
    
    public void setLocation(int x, int y)
    {
        circleX = x + radius;
        circleY = y + radius;
        invalidate();
    }
    
    public int getLocationX()
    {
        return circleX - radius;
    }
    
    public int getLocationY()
    {
        return circleY - radius;
    }
    
    /**
     * 设置放大镜圆心坐标
     */
    
    public void setCirclePosition(int x, int y)
    {
        circleX = x;
        circleY = y;
        invalidate();
    }
    
    public int getCirclePositionX()
    {
        return circleX;
    }
    
    public int getCirclePositionY()
    {
        return circleY;
    }
    
    /**
     * 移动放大镜位置
     */
    
    public void move(int dx, int dy)
    {
        circleX += dx;
        circleY += dy;
        invalidate();
    }
    
    /**
     * 设置放大镜尺寸
     * @param radius 半径
     */
    
    public void setRadius(int radius)
    {
        this.radius = radius;
        
        path.rewind();
        path.addCircle(radius, radius, radius, Direction.CW);
        
        int size = radius * 2;
        
        RectF circle1 = new RectF(0, 0, size, size);
        RectF circle2 = new RectF(size / 5, size / 5, size * 11 / 10, size * 11 / 10);
        RectF circle3 = new RectF(size / 5, size / 5, size * 3 / 10, size * 3 / 10);

        path1.rewind();
        path1.addOval(circle1, Direction.CW);
        path2.rewind();
        path2.addOval(circle2, Direction.CW);
        path3.rewind();
        path3.addOval(circle3, Direction.CW);
        
        invalidate();
    }
    
    public int getRadius()
    {
        return radius;
    }
    
    /**
     * 设置放大倍数
     */
    
    public void setScaleLevel(float scale)
    {
        this.scale = scale;
        m.setScale(scale, scale);
        invalidate();
    }
    
    public float getScaleLevel()
    {
        return scale;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (image != null)
        {
            canvas.save();
            canvas.translate(circleX - radius, circleY - radius);
            canvas.clipPath(path);
            
            canvas.translate(radius - circleX * scale, radius - circleY * scale);
            canvas.drawBitmap(image, m, null);
            canvas.restore();
        }

        canvas.translate(circleX - radius, circleY - radius);
        paint.setColor(darkGlassColor);
        canvas.clipPath(path1, Op.REPLACE);
        canvas.clipPath(path2, Op.DIFFERENCE);
        canvas.drawPaint(paint);

        paint.setColor(brightGlassColor);
        canvas.clipPath(path1, Op.REPLACE);
        canvas.clipPath(path2, Op.INTERSECT);
        canvas.drawPaint(paint);

        paint.setColor(lightInGlassColor);
        canvas.clipPath(path3, Op.REPLACE);
        canvas.drawPaint(paint);
    }
    
    public boolean isTouchToCircle(int x, int y)
    {
        return Math.pow(x - circleX, 2) + Math.pow(y - circleY, 2) < Math.pow(radius, 2);
    }
}