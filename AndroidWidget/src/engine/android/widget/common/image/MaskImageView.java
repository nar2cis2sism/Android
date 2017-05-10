package engine.android.widget.common.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 自定义遮罩图片
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public abstract class MaskImageView extends ImageView {

    private static final Xfermode MASK_XFERMODE
    = new PorterDuffXfermode(Mode.DST_IN);

    private Bitmap mask;

    private Paint paint;

    public MaskImageView(Context context) {
        super(context);
        init();
    }

    public MaskImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaskImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setFilterBitmap(false);
        paint.setXfermode(MASK_XFERMODE);
    }

    public abstract Bitmap createMaskBitmap();

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();

        if (drawable == null)
        {
            return;
        }

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        if (drawableWidth == 0 || drawableHeight == 0)
        {
            // nothing to draw (empty bounds)
            return;
        }

        int width = getWidth();
        int height = getHeight();

        int saveCount = canvas.saveLayer(0, 0, width, height, null, 31);

        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        if (mask == null || mask.isRecycled())
        {
            mask = createMaskBitmap();
        }

        canvas.drawBitmap(mask, 0, 0, paint);

        canvas.restoreToCount(saveCount);
    }
}