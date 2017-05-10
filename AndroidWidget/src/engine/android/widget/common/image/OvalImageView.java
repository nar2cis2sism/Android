package engine.android.widget.common.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

/**
 * 内切圆图片
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class OvalImageView extends MaskImageView {

    public OvalImageView(Context context) {
        super(context);
    }

    public OvalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OvalImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public Bitmap createMaskBitmap() {
        int width = getWidth();
        int height = getHeight();

        Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas c = new Canvas(b);

        final int color = 0xff424242;
        final Paint p = new Paint();
        final RectF rf = new RectF(0, 0, width, height);

        p.setAntiAlias(true);
        c.drawARGB(0, 0, 0, 0);
        p.setColor(color);
        c.drawOval(rf, p);

        return b;
    }
}