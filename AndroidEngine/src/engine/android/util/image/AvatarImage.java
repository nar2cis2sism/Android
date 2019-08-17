package engine.android.util.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 圆形头像图片
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class AvatarImage {

    private static final int DEFAULT_BG_COLOR = 0xff424242;

    private static final int DEFAULT_TEXT_AVATAR_SIZE = 50;

    private final Bitmap avatar;

    public AvatarImage(Bitmap image) {
        this(image, 0);
    }

    public AvatarImage(Bitmap image, int size) {
        avatar = createAvatarBitmap(image, size);
    }

    public AvatarImage(String text) {
        this(text, DEFAULT_BG_COLOR);
    }

    public AvatarImage(String text, int bgColor) {
        this(text, DEFAULT_TEXT_AVATAR_SIZE, bgColor);
    }

    public AvatarImage(String text, int size, int bgColor) {
        avatar = createAvatarBitmap(text, size, bgColor);
    }

    public Bitmap get() {
        return avatar;
    }

    private static Bitmap createAvatarBitmap(Bitmap image, int size) {
        if (image == null)
        {
            return null;
        }
        
        int width = image.getWidth();
        int height = image.getHeight();
        int imageSize = Math.min(width, height);
        if (size == 0)
        {
            size = imageSize;
        }

        Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas c = new Canvas(b);

        Rect dst = new Rect(0, 0, size, size);
        RectF rf = new RectF(dst);
        Rect src = new Rect(0, 0, imageSize, imageSize);
        src.offsetTo((width - imageSize) / 2, (height - imageSize) / 2);

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(0xff424242);
        c.drawARGB(0, 0, 0, 0);
        c.drawOval(rf, p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        c.drawBitmap(image, src, dst, p);

        return b;
    }

    private static Bitmap createAvatarBitmap(String text, int size, int bgColor) {
        if (text == null)
        {
            return null;
        }

        int width = size;
        int height = size;

        Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas c = new Canvas(b);

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(bgColor);
        c.drawARGB(0, 0, 0, 0);
        c.drawOval(new RectF(0, 0, width, height), p);
        
        if (text.trim().length() > 0)
        {
            p.setColor(Color.WHITE);
            p.setTextAlign(Align.CENTER);
            p.setTextSize(size / 2);

            Rect bounds = new Rect();
            p.getTextBounds(text, 0, text.length(), bounds);

            c.drawText(text, width / 2, (height + bounds.height() - 3) / 2, p);
        }
        
        return b;
    }
}