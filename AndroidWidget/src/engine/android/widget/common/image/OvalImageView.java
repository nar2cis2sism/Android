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
 * @since 6/6/2014
 */
public class OvalImageView extends MaskImageView {

    public OvalImageView(Context context) {
        super(context);
    }

    public OvalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public Bitmap createMaskBitmap() {
        int width = getWidth();
        int height = getHeight();

        Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas c = new Canvas(b);

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(0xff424242);
        
        c.drawARGB(0, 0, 0, 0);
        c.drawOval(new RectF(0, 0, width, height), p);

        return b;
    }
}