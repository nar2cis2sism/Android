package engine.android.util.image;

import static engine.android.util.ui.RectUtil.getRect;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.view.View;
import android.widget.ImageView.ScaleType;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

/**
 * 图像处理工具类
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public final class ImageUtil {

    /**
     * 获取图像的ARGB信息
     * 
     * @param image 图片
     * @return 信息数组
     */
    private static int[] getRGB(Bitmap image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] a = new int[w * h];
        image.getPixels(a, 0, w, 0, 0, w, h);
        return a;
    }

    /**
     * 根据ARGB信息创建图像
     * 
     * @param a 信息数组
     * @param w 图像的宽
     * @param h 图像的高
     * @return 图像
     */
    private static Bitmap newRGB(int[] a, int w, int h) {
        return Bitmap.createBitmap(a, w, h, Config.ARGB_8888);
    }

    /**
     * 图片缩放
     * 
     * @param w 新的宽
     * @param h 新的高
     */
    public static Bitmap zoom(Bitmap image, int w, int h) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (w <= 0)
        {
            w = width;
        }

        if (h <= 0)
        {
            h = height;
        }

        if (w == width && h == height)
        {
            return image;
        }

        return Bitmap.createScaledBitmap(image, w, h, true);
    }

    /**
     * 图片旋转
     * 
     * @param angle 旋转角度（顺时针为正）
     */
    public static Bitmap rotate(Bitmap image, float angle) {
        if (angle == 0)
        {
            return image;
        }

        Matrix m = new Matrix();
        m.postRotate(angle);
        return getBitmap(image, m);
    }

    /**
     * 图片旋转同时缩放
     * 
     * @param angle 旋转角度（顺时针为正）
     * @param scaleW，scaleH 缩放倍数
     */
    public static Bitmap rotateAndScale(Bitmap image, float angle, float scaleW, float scaleH) {
        Matrix m = new Matrix();
        m.postRotate(angle);
        m.postScale(scaleW, scaleH);
        return getBitmap(image, m);
    }

    /**
     * 图片格式转换
     */
    public static Bitmap drawable2Bitmap(Drawable image) {
        int width = image.getIntrinsicWidth();
        int height = image.getIntrinsicHeight();
        Bitmap b = Bitmap.createBitmap(width, height,
                image.getOpacity() != PixelFormat.OPAQUE ?
                Config.ARGB_8888 : Config.RGB_565);
        Canvas c = new Canvas(b);
        image.setBounds(0, 0, width, height);
        image.draw(c);
        return b;
    }

    /**
     * 视图转换成图片（截屏）
     */
    public static Bitmap view2Bitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(Color.TRANSPARENT);

        if (color != Color.TRANSPARENT)
        {
            v.destroyDrawingCache();
        }

        v.buildDrawingCache();
        Bitmap cache = v.getDrawingCache();
        if (cache == null)
        {
            return null;
        }

        Bitmap image = Bitmap.createBitmap(cache);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return image;
    }

    /**
     * 获取水印图片
     * 
     * @param source 源图片
     * @param image 需要印上去的图片
     * @param x,y 印刷位置
     */
    public static Bitmap getOverlayImage(Bitmap source, Bitmap image, int x, int y) {
        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(),
                Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(source, 0, 0, null);
        canvas.drawBitmap(image, x, y, null);
        return bitmap;
    }

    /**
     * 获取灰度图片
     * 
     * @return 去色后的图片
     */
    public static Bitmap getGrayImage(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Bitmap b = Bitmap.createBitmap(width, height, Config.RGB_565);
        Canvas c = new Canvas(b);

        final Paint p = new Paint();

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);

        p.setColorFilter(new ColorMatrixColorFilter(cm));
        c.drawBitmap(image, 0, 0, p);

        return b;
    }

    /**
     * 获取圆角图片
     * 
     * @param radius 圆角半径
     */
    public static Bitmap getRoundImage(Bitmap image, float radius) {
        int width = image.getWidth();
        int height = image.getHeight();
        Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas c = new Canvas(b);

        final int color = 0xff424242;
        final Paint p = new Paint();
        final Rect r = new Rect(0, 0, width, height);
        final RectF rf = new RectF(r);

        p.setAntiAlias(true);
        c.drawARGB(0, 0, 0, 0);
        p.setColor(color);
        c.drawRoundRect(rf, radius, radius, p);

        /** Daimon:PorterDuffXfermode **/
        // http://blog.csdn.net/q445697127/article/details/7867529
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        c.drawBitmap(image, r, r, p);

        return b;
    }

    /**
     * 获取带倒影的图片
     */
    public static Bitmap getReflectedImage(Bitmap image) {
        final int reflectionGap = 4;
        int width = image.getWidth();
        int height = image.getHeight();

        Matrix m = new Matrix();
        m.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(image, 0,
                height / 2, width, height / 2, m, false);

        Bitmap b = Bitmap.createBitmap(width, height + reflectionGap + height / 2,
                Config.ARGB_8888);

        Canvas c = new Canvas(b);
        c.drawBitmap(image, 0, 0, null);
        Paint p = new Paint();
        p.setColor(Color.TRANSPARENT);
        c.drawRect(0, height, width, height + reflectionGap, p);
        c.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        p = new Paint();
        LinearGradient shader = new LinearGradient(0, height + reflectionGap,
                0, b.getHeight(), 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
        p.setShader(shader);
        p.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        c.drawRect(0, height + reflectionGap, width, b.getHeight(), p);

        return b;
    }

    /**
     * 将图片转换为字节数组
     */
    public static byte[] image2bytes(Bitmap image) {
        if (image == null)
        {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(CompressFormat.PNG, 100, baos);
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /**
     * 获取砖块图片
     * 
     * @param image 原图片
     * @param tileWidth 砖块宽
     * @param tileHeight 砖块高
     * @param index 砖块索引
     */
    public static Bitmap getTileImage(Bitmap image, int tileWidth, int tileHeight, int index) {
        int cols = image.getWidth() / tileWidth;
        int row = index / cols;
        int col = index % cols;
        return clip(image, col * tileWidth, row * tileHeight, tileWidth, tileHeight);
    }

    /**
     * 剪切图片
     * 
     * @param image 原图片
     * @param x,y,w,h 剪切的范围
     * @return 截取的图片
     */
    public static Bitmap clip(Bitmap image, int x, int y, int w, int h) {
        return Bitmap.createBitmap(image, x, y, w, h);
    }

    /**
     * 调节图像的透明度
     * 
     * @param image 图像
     * @param alpha 透明值，用百分比表示
     * @return 调节后的图像
     */
    public static Bitmap changeAlpha(Bitmap image, int alpha) {
        if (alpha == 0)
        {
            // 不透明
            alpha = 0xff;
        }
        else if (alpha == 100)
        {
            // 全透明
            alpha = 0;
        }
        else
        {
            alpha = alpha * 255 / 100;
        }

        alpha = alpha << 24;
        int[] a = getRGB(image);
        for (int i = 0; i < a.length; i++)
        {
            a[i] = a[i] & 0x00ffffff | alpha;
        }

        return newRGB(a, image.getWidth(), image.getHeight());
    }

    /**
     * 通过矩阵变换创建图片
     */
    public static Bitmap getBitmap(Bitmap image, Matrix m) {
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(),
                m, true);
    }

    /**
     * 复制图片
     * 
     * @param image 源图片
     * @param bak 是备份源图片还是创建一个空白的图片
     */
    public static Bitmap copy(Bitmap image, boolean bak) {
        return copy(image, bak, image.getConfig());
    }

    /**
     * 复制图片
     * 
     * @param image 源图片
     * @param bak 是备份源图片还是创建一个空白的图片
     */
    public static Bitmap copy(Bitmap image, boolean bak, Config config) {
        if (config == null)
        {
            config = Config.ARGB_8888;
        }

        Bitmap bitmap;
        if (bak)
        {
            bitmap = image.copy(config, true);
            if (bitmap == null)
            {
                bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), config);
                Canvas c = new Canvas(bitmap);
                c.drawBitmap(image, 0, 0, null);
            }
        }
        else
        {
            bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), config);
        }

        return bitmap;
    }

    /**
     * 绘制带有边框的文字
     * 
     * @param text 文字内容
     * @param canvas 画布
     * @param paint 画笔
     * @param x,y 起始位置
     * @param fontColor 字体颜色
     * @param backgroundColor 边框颜色
     */
    public static void drawTextWithBorder(String text, Canvas canvas, Paint paint,
            int x, int y, int fontColor, int backgroundColor) {
        canvas.save();
        paint.setColor(backgroundColor);
        canvas.drawText(text, x + 1, y, paint);
        canvas.drawText(text, x, y - 1, paint);
        canvas.drawText(text, x, y + 1, paint);
        canvas.drawText(text, x - 1, y, paint);
        paint.setColor(fontColor);
        canvas.drawText(text, x, y, paint);
        canvas.restore();
    }

     /**
     * 绘制砖块图片
     * @param image 原图片
     * @param tileWidth 砖块宽
     * @param tileHeight 砖块高
     * @param index 砖块索引
     * @param x,y 绘制的位置
     */
     public static void drawTile(Canvas c, Bitmap image, int tileWidth, int tileHeight, 
             int index, int x, int y) {
         int cols = image.getWidth() / tileWidth;
         int row = index / cols;
         int col = index % cols;
         c.drawBitmap(image, 
                 getRect(col * tileWidth, row * tileHeight, tileWidth, tileHeight), 
                 getRect(x, y, tileWidth, tileHeight), null);
     }

    /**
     * 调节图像的亮度对比度
     * 
     * @param image 图像
     * @param d 亮度，数值越大越亮，为1.0不变
     * @param a 对比度，0不变
     * @return 调节后的图像
     */
    public static Bitmap changeLight(Bitmap image, double d, int a) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] ab = getRGB(image);
        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {
                int l0 = ab[i * w + j];
                int l1 = (l0 & 0xff000000) >> 24;
                int l2 = (l0 & 0xff0000) >> 16;
                int l3 = (l0 & 0xff00) >> 8;
                int l4 = l0 & 0xff;
                l2 = (int) (l2 * d + a);
                l3 = (int) (l3 * d + a);
                l4 = (int) (l4 * d + a);
                if (l2 > 255) l2 = 255;
                else if (l2 < 0) l2 = 0;
                if (l3 > 255) l3 = 255;
                else if (l3 < 0) l3 = 0;
                if (l4 > 255) l4 = 255;
                else if (l4 < 0) l4 = 0;
                ab[i * w + j] = l1 << 24 | l2 << 16 | l3 << 8 | l4;
            }
        }

        return newRGB(ab, w, h);
    }

    /**
     * 粉笔画
     * 
     * @param image 原图像
     * @return 此图像的粉笔画
     */
    public static Bitmap crayon(Bitmap image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] a = getRGB(image);
        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {
                int k0 = a[i * w + j];
                int k1 = (k0 & 0xff000000) >> 24;
                int k2 = (k0 & 0xff0000) >> 16;
                int k3 = (k0 & 0xff00) >> 8;
                int k4 = k0 & 0xff;
                int l2, l3, l4;
                if (i + 1 == h)
                {
                    l2 = l3 = l4 = 0;
                }
                else
                {
                    int l0 = a[(i + 1) * w + j];
                    l2 = (l0 & 0xff0000) >> 16;
                    l3 = (l0 & 0xff00) >> 8;
                    l4 = l0 & 0xff;
                }

                int m2, m3, m4;
                if (j + 1 == w)
                {
                    m2 = m3 = m4 = 0;
                }
                else
                {
                    int m0 = a[i * w + j + 1];
                    m2 = (m0 & 0xff0000) >> 16;
                    m3 = (m0 & 0xff00) >> 8;
                    m4 = m0 & 0xff;
                }

                k2 = 255 - (int) Math.sqrt(2 * (k2 - l2) * (k2 - l2) + (k2 - m2) * (k2 - m2));
                k3 = 255 - (int) Math.sqrt(2 * (k3 - l3) * (k3 - l3) + (k3 - m3) * (k3 - m3));
                k4 = 255 - (int) Math.sqrt(2 * (k4 - l4) * (k4 - l4) + (k4 - m4) * (k4 - m4));
                a[i * w + j] = k1 << 24 | k2 << 16 | k3 << 8 | k4;
            }
        }

        return newRGB(a, w, h);
    }

    /**
     * 线条
     * 
     * @param image 原图像
     * @return 原图像的线条效果
     */
    public static Bitmap lines(Bitmap image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] a = getRGB(image);
        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {
                int k0 = a[i * w + j];
                int k1 = (k0 & 0xff000000) >> 24;
                int k2 = (k0 & 0xff0000) >> 16;
                int k3 = (k0 & 0xff00) >> 8;
                int k4 = k0 & 0xff;
                int l2, l3, l4;
                if (i + 1 == h)
                {
                    l2 = l3 = l4 = 0;
                }
                else
                {
                    int l0 = a[(i + 1) * w + j];
                    l2 = (l0 & 0xff0000) >> 16;
                    l3 = (l0 & 0xff00) >> 8;
                    l4 = l0 & 0xff;
                }

                int m2, m3, m4;
                if (j + 1 == w)
                {
                    m2 = m3 = m4 = 0;
                }
                else
                {
                    int m0 = a[i * w + j + 1];
                    m2 = (m0 & 0xff0000) >> 16;
                    m3 = (m0 & 0xff00) >> 8;
                    m4 = m0 & 0xff;
                }

                k2 = (int) Math.sqrt(2 * (k2 - l2) * (k2 - l2) + (k2 - m2) * (k2 - m2));
                k3 = (int) Math.sqrt(2 * (k3 - l3) * (k3 - l3) + (k3 - m3) * (k3 - m3));
                k4 = (int) Math.sqrt(2 * (k4 - l4) * (k4 - l4) + (k4 - m4) * (k4 - m4));
                a[i * w + j] = k1 << 24 | k2 << 16 | k3 << 8 | k4;
            }
        }

        return newRGB(a, w, h);
    }

    /**
     * 滤镜
     * 
     * @param image 原图像
     * @return 在上面放一块滤镜的效果，起初为绿色
     */
    public static Bitmap filter(Bitmap image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] ab = getRGB(image);
        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {
                int l0 = ab[i * w + j];
                int l1 = (l0 & 0xff000000) >> 24;
                int l2 = (l0 & 0xff0000) >> 16;
                int l3 = (l0 & 0xff00) >> 8;
                int l4 = l0 & 0xff;
                l2 = (int) (0.29899999999999999 * l2);
                l3 = (int) (0.58699999999999997 * l3);
                l4 = (int) (0.114 * l4);
                ab[i * w + j] = l1 << 24 | l2 << 16 | l3 << 8 | l4;
            }
        }

        return newRGB(ab, w, h);
    }

    /**
     * 镜像
     * 
     * @param image 原图像
     * @param horizontal 水平或垂直方向
     * @return 原图像的镜像
     */
    public static Bitmap mirror(Bitmap image, boolean horizontal) {
        int width = image.getWidth();
        int height = image.getHeight();

        Matrix m = new Matrix();
        if (horizontal)
        {
            m.postScale(-1, 1, width / 2, height / 2);
        }
        else
        {
            m.postScale(1, -1, width / 2, height / 2);
        }

        return getBitmap(image, m);
    }

    /**
     * 底片
     * 
     * @param image 原图像
     * @return 原图像的底片效果
     */
    public static Bitmap negative(Bitmap image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] a = getRGB(image);
        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {
                int l0 = a[i * w + j];
                int l1 = (l0 & 0xff000000) >> 24;
                int l2 = 255 - ((l0 & 0xff0000) >> 16);
                int l3 = 255 - ((l0 & 0xff00) >> 8);
                int l4 = 255 - (l0 & 0xff);
                a[i * w + j] = l1 << 24 | l2 << 16 | l3 << 8 | l4;
            }
        }

        return newRGB(a, w, h);
    }

    /**
     * 怀旧
     * 
     * @param image 原图像
     * @return 原图像的怀旧效果
     */
    public static Bitmap remember(Bitmap image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] a = getRGB(image);
        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {
                int color = a[i * w + j];
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);

                int newR = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int newG = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int newB = (int) (0.272 * r + 0.534 * g + 0.131 * b);
                a[i * w + j] = Color.rgb(newR > 255 ? 255 : newR, newG > 255 ? 255 : newG,
                        newB > 255 ? 255 : newB);
            }
        }

        return newRGB(a, w, h);
    }

    /**
     * 光照
     * 
     * @param image 原图像
     * @param centerX,centerY 光照中心坐标
     * @return 原图像的光照效果
     */
    public static Bitmap sunshine(Bitmap image, int centerX, int centerY) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] a = getRGB(image);

        float strength = 150f; // 光照强度[100,150]
        int radius = Math.min(centerX, centerY);

        for (int i = 1; i < h - 1; i++)
        {
            for (int j = 1; j < w - 1; j++)
            {
                int color = a[i * w + j];
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);

                int newR = r;
                int newG = g;
                int newB = b;

                // 计算当前点到光照中心的距离，平面座标系中求两点之间的距离
                int distance = (int) (Math.pow((centerY - i), 2) + Math.pow(centerX - j, 2));
                if (distance < radius * radius)
                {
                    // 按照距离大小计算增加的光照值
                    int result = (int) (strength * (1.0 - Math.sqrt(distance) / radius));
                    newR = r + result;
                    newG = g + result;
                    newB = b + result;

                    newR = Math.min(255, Math.max(0, newR));
                    newG = Math.min(255, Math.max(0, newG));
                    newB = Math.min(255, Math.max(0, newB));
                }

                a[i * w + j] = Color.rgb(newR, newG, newB);
            }
        }

        return newRGB(a, w, h);
    }

    /**
     * 绘制验证码图片
     * 
     * @param width 验证码图片宽度
     * @param height 验证码图片高度
     * @return 带有验证码的图片
     */
    public static Bitmap getRandomCode(int width, int height) {
        Bitmap image = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        setRandomColor(paint, 200, 250); // 背景颜色要偏淡
        canvas.drawPaint(paint); // 画背景

        setRandomColor(paint, 0, 255); // 边框颜色
        canvas.drawRect(0, 0, width - 1, height - 1, paint); // 画边框

        setRandomColor(paint, 160, 200); // 随机产生8条干扰线，使图象中的认证码不易被其它程序探测到
        Random r = new Random(); // 创建一个随机类
        for (int i = 0; i < 8; i++)
        {
            int x = r.nextInt(width - 1);
            int y = r.nextInt(height - 1);
            int x1 = r.nextInt(width - 1);
            int y1 = r.nextInt(height - 1);
            canvas.drawLine(x, y, x1, y1, paint);
        }

        setRandomColor(paint, 160, 200); // 随机产生100点，使图象中的认证码不易被其它程序探测到
        for (int i = 0; i < 100; i++)
        {
            int x = r.nextInt(width - 1);
            int y = r.nextInt(height - 1);
            canvas.drawPoint(x, y, paint);
        }

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        paint.setTextSize(height / 2); // 设置字体，字体的大小应该根据图片的高度来定

        // 设置备选验证码:包括大小写"a-z"和数字"0-9"
        String s = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String sRand = "";
        // 用随机产生的颜色将验证码绘制到图像中
        // 生成随机颜色(因为是做前景，所以偏深)
        // 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
        int len = 6;// 设置默认生成6个验证码
        int dx = width / (len + 1);// 水平间距
        int dy = (int) ((height - paint.getTextSize()) / 2); // 垂直间隔
        for (int i = 0; i < len; i++)
        {
            setRandomColor(paint, 20, 130);
            String ch = String.valueOf(s.charAt(r.nextInt(s.length())));
            sRand += ch;
            canvas.drawText(ch, dx * (i + 1), (r.nextInt(5) - 2) * i + dy + paint.getTextSize(),
                    paint);
        }

        System.out.println(sRand);
        return image;
    }

    /**
     * 设置随机颜色
     * 
     * @param paint 需要设置的画笔
     * @param a 颜色分量的下限值
     * @param b 颜色分量的上限值
     */
    private static void setRandomColor(Paint paint, int a, int b) {
        Random r = new Random();
        int R = a + r.nextInt(b - a);
        int G = a + r.nextInt(b - a);
        int B = a + r.nextInt(b - a);
        paint.setARGB(0xff, R, G, B);
    }

     /**
     * 绘制渐变色
     * @param x,y,w,h 绘制区域
     * @param startColor,endColor 渐变起始和结束颜色
     * @param horizontal 是否水平渐变
     */
     public static void fillGradualColor(Canvas canvas, int x, int y, int w, int h, 
             int startColor, int endColor, boolean horizontal) {
         Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
         paint.setStyle(Style.FILL);
         int time = horizontal ? w : h;
         int sr = ((startColor & 0xff0000) >> 16) & 0xff;
         int sg = ((startColor & 0xff00) >> 8) & 0xff;
         int sb = startColor & 0xff;
         int er = ((endColor & 0xff0000) >> 16) & 0xff;
         int eg = ((endColor & 0xff00) >> 8) & 0xff;
         int eb = endColor & 0xff;
         int red = er - sr;
         int green = eg - sg;
         int blue = eb - sb;
         for (int i = 0; i <= time; i++)
         {
             int dr = sr + red * i / time;
             int dg = sg + green * i / time;
             int db = sb + blue * i / time;
             if (i == time)
             {
                 dr = (endColor & 0xff0000) >> 16;
                 dg = ((endColor & 0xff00) >> 8);
                 db = endColor & 0xff;
             }

             paint.setARGB(0xff, dr, dg, db);
             if (horizontal)
             {
                 canvas.drawRect(getRect(x + i, y, 1, h), paint);
             }
             else
             {
                 canvas.drawRect(getRect(x, y + i, w, 1), paint);
             }
         }
     }

    /**
     * 绘制旋转图片
     * 
     * @param angle 旋转角度（顺时针为正）
     * @param x,y 旋转轴心（画布上的坐标）
     * @param rx,ry 相对于图片的旋转参考点
     */
    public static void drawRotateImage(Canvas canvas, Bitmap image, float angle, 
            int x, int y, int rx, int ry) {
        Matrix m = new Matrix();
        m.setTranslate(x - rx, y - ry);
        m.preRotate(angle, rx, ry);
        canvas.drawBitmap(image, m, null);
    }

    /**
     * 计算图片所占内存大小
     */
    public static long getImageSize(int width, int height) {
        return width * height * 4;
    }

    /**
     * Returns the number of bytes used to store this bitmap's pixels.
     */
    public static long getImageSize(Bitmap image) {
        return image.getRowBytes() * image.getHeight();
    }

    /**
     * 图片解码（为了节约内存，做了一些处理）
     */
    public static final class ImageDecoder {

        /**
         * 解码固定尺寸的图片
         * 
         * @param pathName 图片文件路径
         * @param width,height 图片按比例缩放最大显示尺寸
         * @param fitXY {@link ScaleType#FIT_XY}
         */
        public static Bitmap decodeFile(String pathName, int width, int height, boolean fitXY) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inJustDecodeBounds = true;              // 设置为true表示我们只读取Bitmap的宽高等信息，不读取像素

            BitmapFactory.decodeFile(pathName, opts);    // 获取尺寸信息

            opts.inSampleSize = calculateInSampleSize(opts, width, height);

            opts.inJustDecodeBounds = false;

            Bitmap image = BitmapFactory.decodeFile(pathName, opts);
            if (fitXY)
            {
                image = ImageUtil.zoom(image, width, height);
            }

            return image;
        }

        /**
         * @see #decodeFile(String, int, int, boolean)
         */
        public static Bitmap decodeResource(Resources res, int id, int width, int height,
                boolean fitXY) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inJustDecodeBounds = true;              // 设置为true表示我们只读取Bitmap的宽高等信息，不读取像素

            BitmapFactory.decodeResource(res, id, opts); // 获取尺寸信息

            opts.inSampleSize = calculateInSampleSize(opts, width, height);

            opts.inJustDecodeBounds = false;

            Bitmap image = BitmapFactory.decodeResource(res, id, opts);
            if (fitXY)
            {
                image = ImageUtil.zoom(image, width, height);
            }

            return image;
        }

        /**
         * @see #decodeFile(String, int, int, boolean)
         */
        public static Bitmap decodeByteArray(byte[] data, int offset, int length, int width,
                int height, boolean fitXY) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inJustDecodeBounds = true;              // 设置为true表示我们只读取Bitmap的宽高等信息，不读取像素

            BitmapFactory.decodeByteArray(data, offset, length, opts); // 获取尺寸信息

            opts.inSampleSize = calculateInSampleSize(opts, width, height);

            opts.inJustDecodeBounds = false;

            Bitmap image = BitmapFactory.decodeByteArray(data, offset, length, opts);
            if (fitXY)
            {
                image = ImageUtil.zoom(image, width, height);
            }

            return image;
        }

        /**
         * @see #decodeFile(String, int, int, boolean, boolean)
         */
        public static Bitmap decodeStream(InputStream is, int width, int height, boolean fitXY) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inJustDecodeBounds = true;              // 设置为true表示我们只读取Bitmap的宽高等信息，不读取像素

            BitmapFactory.decodeStream(is, null, opts);  // 获取尺寸信息

            opts.inSampleSize = calculateInSampleSize(opts, width, height);

            opts.inJustDecodeBounds = false;

            Bitmap image = BitmapFactory.decodeStream(is, null, opts);
            if (fitXY)
            {
                image = ImageUtil.zoom(image, width, height);
            }

            return image;
        }

        /**
         * @see #decodeFile(String, int, int, boolean)
         */
        public static Bitmap decodeFileDescriptor(FileDescriptor fd, int width, int height,
                boolean fitXY) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inJustDecodeBounds = true;              // 设置为true表示我们只读取Bitmap的宽高等信息，不读取像素

            BitmapFactory.decodeFileDescriptor(fd, null, opts); // 获取尺寸信息

            opts.inSampleSize = calculateInSampleSize(opts, width, height);

            opts.inJustDecodeBounds = false;

            Bitmap image = BitmapFactory.decodeFileDescriptor(fd, null, opts);
            if (fitXY)
            {
                image = ImageUtil.zoom(image, width, height);
            }

            return image;
        }

        /**
         * Calculate an inSampleSize for use in a {@link BitmapFactory.Options}
         * object when decoding bitmaps using the decode* methods from
         * {@link BitmapFactory}. This implementation calculates the closest
         * inSampleSize that will result in the final decoded bitmap having a
         * width and height equal to or larger than the requested width and
         * height. This implementation does not ensure a power of 2 is returned
         * for inSampleSize which can be faster when decoding but results in a
         * larger bitmap which isn't as useful for caching purposes.
         */
        private static int calculateInSampleSize(BitmapFactory.Options opts, 
                int width, int height) {
            int w = opts.outWidth;
            int h = opts.outHeight;
            // 计算缩放比例
            int inSampleSize = 1;
            int scaleW = Math.round(w * 1.0f / width);
            int scaleH = Math.round(h * 1.0f / height);
            if (scaleW > 1 && scaleH > 1)
            {
                if (scaleW > scaleH)
                {
                    inSampleSize = scaleH;
                }
                else
                {
                    inSampleSize = scaleW;
                }

                // This offers some additional logic in case the image has a strange
                // aspect ratio. For example, a panorama may have a much larger
                // width than height. In these cases the total pixels might still
                // end up being too large to fit comfortably in memory, so we should
                // be more aggressive with sample down the image.

                final float totalPixels = w * h;

                // Anything more than 2x the requested pixels we'll sample down further.
                final float totalPixelsCap = width * height * 2;

                while (totalPixels / (inSampleSize * inSampleSize) > totalPixelsCap)
                {
                    inSampleSize++;
                }
            }

            return inSampleSize;
        }
    }

    /**
     * HTML图片替换
     */
    public static final ImageGetter imageGetter = new ImageGetter() {

        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = null;
            try {
                drawable = Drawable.createFromStream(new URL(source).openStream(), null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            if (drawable != null)
            {
                drawable.setBounds(0, 0, 
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
            }

            return drawable;
        }
    };
}