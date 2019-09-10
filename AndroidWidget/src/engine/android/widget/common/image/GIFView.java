package engine.android.widget.common.image;

import engine.android.util.io.IOUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Movie;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Gif播放控件
 * 
 * @author Daimon
 * @since 5/7/2014
 */
public class GIFView extends ImageView {

    private Movie movie;
    private int duration;

    private long startTime;
    private boolean isPlaying = true;

    private float scaleX = 1,scaleY = 1;

    public GIFView(Context context) {
        this(context, null);
    }

    public GIFView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void setImageResource(int resId) {
        try {
            movie = initMovie(getResources().getMovie(resId));
        } catch (Exception e) {
            e.printStackTrace();
            movie = null;
        }

        super.setImageResource(resId);
    }

    @Override
    public void setImageURI(Uri uri) {
        if (uri != null)
        {
            InputStream is = null;
            try {
                is = getContext().getContentResolver().openInputStream(uri);
                movie = initMovie(Movie.decodeStream(is));
            } catch (Exception e) {
                e.printStackTrace();
                movie = null;
            } finally {
                IOUtil.closeSilently(is);
            }
        }

        super.setImageURI(uri);
    }

    private Movie initMovie(Movie movie) throws Exception {
        if ((duration = movie.duration()) <= 0)
        {
            throw new Exception("解析Movie失败:duration=" + duration);
        }

        int width = movie.width();
        int height = movie.height();
        if (width == 0 || height == 0)
        {
            throw new Exception(String.format("解析Movie失败:size=%d*%d", width, height));
        }

        return movie;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (movie != null && getScaleType() == ScaleType.FIT_XY)
        {
            scaleX = getMeasuredWidth() * 1.0f / movie.width();
            scaleY = getMeasuredHeight() * 1.0f / movie.height();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (movie != null)
        {
            if (!isPlaying)
            {
                return;
            }

            long time = SystemClock.uptimeMillis();
            if (startTime == 0)
            {
                // first time
                startTime = time;
            }

            movie.setTime((int) ((time - startTime) % duration));
            drawMovie(canvas, movie);
            invalidate();
        }
        else
        {
            super.onDraw(canvas);
        }
    }

    private void drawMovie(Canvas canvas, Movie movie) {
        Matrix matrix = getImageMatrix();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (matrix == null && paddingLeft == 0 && paddingTop == 0)
        {
            movie.draw(canvas, 0, 0);
        }
        else
        {
            int saveCount = canvas.getSaveCount();
            canvas.save();

            canvas.translate(paddingLeft, paddingTop);
            if (matrix != null)
            {
                if (getScaleType() == ScaleType.FIT_XY)
                {
                    matrix.setScale(scaleX, scaleY);
                }

                canvas.concat(matrix);
            }

            movie.draw(canvas, 0, 0);
            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE)
        {
            start();
        }
        else
        {
            stop();
        }
    }

    public void start() {
        isPlaying = true;
        invalidate();
    }

    public void stop() {
        isPlaying = false;
        startTime = 0;
    }
}