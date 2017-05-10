package engine.android.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

/**
 * 游戏精灵
 * 
 * @author Daimon
 * @version 3.0
 * @since 5/11/2012
 */

public class Sprite extends Layer {

    private String name;                            // 名称标识，可以没有

    Bitmap image;                                   // 精灵图片

    private Rect collisionRect;                     // 碰撞判定区域（相对坐标）
    private Rect collisionRect_abs;                 // 碰撞判定区域（绝对坐标）

    private Object tag;                             // 属性标签

    Sprite(int width, int height) {
        super(width, height);
        initCollisionRectBounds();
    }

    public Sprite(Bitmap image) {
        this(image.getWidth(), image.getHeight());
        this.image = image;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (image != null)
        {
            canvas.drawBitmap(image, x, y, paint);
        }
    }

    public final Bitmap getImage() {
        return image;
    }

    /**
     * 更换图片
     */

    public void setImage(Bitmap image) {
        sizeChanged(image.getWidth(), image.getHeight());
        this.image = image;
    }

    @Override
    protected boolean sizeChanged(int width, int height) {
        int w = this.width;
        int h = this.height;
        if (super.sizeChanged(width, height))
        {
            collisionRect.right += width - w;
            collisionRect.bottom += height - h;
            return true;
        }

        return false;
    }

    /**
     * 初始化碰撞区域
     */

    private void initCollisionRectBounds() {
        collisionRect = setRect(collisionRect, 0, 0, width, height);
    }

    /**
     * 定义碰撞区域（相对坐标）
     * 
     * @param x,y,width,height
     */

    public void defineCollisionRectangle(int x, int y, int width, int height) {
        if (width < 0 || height < 0)
        {
            throw new IllegalArgumentException();
        }

        collisionRect = setRect(collisionRect, x, y, width, height);
    }

    /**
     * 返回碰撞区域（绝对坐标）
     * 
     * @return
     */

    private Rect getCollisionRect() {
        collisionRect_abs = copyRect(collisionRect_abs, collisionRect);
        collisionRect_abs.offset(x, y);
        return collisionRect_abs;
    }

    /**
     * 碰撞检测
     * 
     * @param x,y 碰撞位置
     * @param pixelLevel true为像素检测,false为矩形检测
     */

    public final boolean collidesWith(int x, int y, boolean pixelLevel) {
        if (visibility == View.GONE)
        {
            return false;
        }

        boolean collides = getCollisionRect().contains(x, y);
        if (collides && pixelLevel)
        {
            return doPixelCollision(x - this.x, y - this.y);
        }

        return collides;
    }

    /**
     * 碰撞检测
     * 
     * @param s 碰撞精灵
     * @param pixelLevel true为像素检测,false为矩形检测
     */

    public final boolean collidesWith(Sprite s, boolean pixelLevel) {
        if (visibility == View.GONE || s.visibility == View.GONE)
        {
            return false;
        }

        Rect rect = new Rect();
        // 判断是否有交集
        boolean collides = rect.setIntersect(getCollisionRect(), s.getCollisionRect());
        if (collides && pixelLevel)
        {
            int x1 = rect.left - x;
            int y1 = rect.top - y;
            int x2 = rect.left - s.x;
            int y2 = rect.top - s.y;
            for (int i = 0, w = rect.width(); i <= w; i++)
            {
                for (int j = 0, h = rect.height(); j <= h; j++)
                {
                    if (doPixelCollision(x1 + i, y1 + j) && s.doPixelCollision(x2 + i, y2 + j))
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        return collides;
    }

    /**
     * 碰撞检测
     * 
     * @param t 碰撞地图
     * @param pixelLevel true为像素检测,false为矩形检测
     */

    public final boolean collidesWith(TiledLayer t, boolean pixelLevel) {
        if (visibility == View.GONE || t.visibility == View.GONE)
        {
            return false;
        }

        Rect sr = getCollisionRect();
        Rect tr = setRect(null, t.x, t.y, t.width, t.height);

        // 判断是否有交集
        if (!Rect.intersects(sr, tr))
        {
            return false;
        }

        int tW = t.getCellWidth();
        int tH = t.getCellHeight();

        int startCol = sr.left <= tr.left ? 0 : (sr.left - tr.left) / tW;
        int startRow = sr.top <= tr.top ? 0 : (sr.top - tr.top) / tH;

        int cols = t.getCols();
        int rows = t.getRows();

        int endCol = sr.right < tr.right ? (sr.right - 1 - tr.left) / tW : cols - 1;
        int endRow = sr.bottom < tr.bottom ? (sr.bottom - 1 - tr.top) / tH : rows - 1;

        for (int row = startRow; row <= endRow; row++)
        {
            for (int col = startCol; col <= endCol; col++)
            {
                int tileIndex = t.getCell(col, row);
                if (tileIndex != 0)
                {
                    if (pixelLevel)
                    {
                        Rect rect = new Rect();
                        Rect cr = setRect(null, t.x + startCol * tW, t.y + startRow * tH, tW, tH);
                        // 判断是否有交集（必须有）
                        boolean collides = rect.setIntersect(sr, cr);
                        if (collides)
                        {
                            int x1 = rect.left - x;
                            int y1 = rect.top - y;
                            int x2 = rect.left - t.x;
                            int y2 = rect.top - t.y;
                            for (int i = 0, w = rect.width(); i <= w; i++)
                            {
                                for (int j = 0, h = rect.height(); j <= h; j++)
                                {
                                    if (doPixelCollision(x1 + i, y1 + j)
                                    && t.doPixelCollision(x2 - cr.left + i, 
                                                          y2 - cr.top + j, 
                                                          tileIndex))
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 像素检测
     * 
     * @param x,y 相对坐标
     */

    protected boolean doPixelCollision(int x, int y) {
        return image != null
            && x >= 0
            && y >= 0
            && x < image.getWidth()
            && y < image.getHeight()
            && (image.getPixel(x, y) & 0xff000000) != 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return String.format("Name:%s,Tag:%s ", name, tag)
                + super.toString()
                + " CollisionRect" + getCollisionRect().toShortString();
    }
}