package engine.android.widget.common.list;

import engine.android.widget.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

/**
 * 自定义画廊
 * 
 * @author Daimon
 * @since 6/6/2014
 * 
 * Daimon:Camera
 */
@SuppressWarnings("deprecation")
public class MyGallery extends Gallery {
    
    private int maxDegree = 60;                     // 最大旋转角度
    private int maxZoom = 0;                        // 最大缩放角度（正数表示缩小，反之亦然）
    
    private boolean enableRotate;                   // 旋转开关
    private boolean enableZoom;                     // 缩放开关
    private boolean enableAlpha;                    // 透明开关
    
    private final Camera camera = new Camera();
    
    private int centerX;                            // 画廊的水平中点

    public MyGallery(Context context) {
        this(context, null);
    }

    public MyGallery(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyGallery);
        
        maxDegree = a.getInteger(R.styleable.MyGallery_Gallery_maxDegree, maxDegree);
        maxZoom = a.getInteger(R.styleable.MyGallery_Gallery_maxZoom, maxZoom);
        setStyle(a.getInt(R.styleable.MyGallery_Gallery_style, 0));
        
        a.recycle();
        
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setStaticTransformationsEnabled(true);
    }
    
    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }
    
    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }
    
    public void setStyle(int style) {
        enableRotate = (style & 1) != 0;
        enableZoom = (style & 2) != 0;
        enableAlpha = (style & 4) != 0;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // 获取画廊的水平中点
        centerX = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        int childWidth = child.getWidth();
        int childCenterX = getCenterXOfView(child);
        int degree = 0;
        
        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX);
        
        if (childCenterX != centerX)
        {
            degree = (centerX - childCenterX) * maxDegree / childWidth;
            if (Math.abs(degree) > maxDegree)
            {
                degree = degree < 0 ? -maxDegree : maxDegree;
            }
        }
        
        transform(child, t, degree);
        return true;
    }
    
    /**
     * 获取视图的水平中点
     */
    private int getCenterXOfView(View view) {
        return view.getLeft() + view.getWidth() / 2;
    }

    /**
     * 视图转换（3D旋转效果）
     */
    private void transform(View child, Transformation t, int degree) {
        camera.save();
        
        Matrix m = t.getMatrix();
        int halfWidth = child.getWidth() / 2;
        int halfHeight = child.getHeight() / 2;

        int rotation = Math.abs(degree);
        // 在Z轴上正向移动camera的视角，实际效果为放大图片
        // 如果在Y轴上移动，则图片上下移动；X轴上对应图片左右移动
        if (enableZoom)
        {
            camera.translate(0, 0, maxZoom + rotation * 1.5f);
        }
        
        if (enableAlpha)
        {
            child.setAlpha(1 - rotation * 2.5f / 255);
        }
        // 在Y轴上旋转，对应图片竖向向里翻转
        // 如果在X轴上旋转，则对应图片横向向里翻转
        if (enableRotate)
        {
            camera.rotateY(degree);
        }
        
        camera.getMatrix(m);
        m.preTranslate(-halfWidth, -halfHeight);
        m.postTranslate(halfWidth, halfHeight);
        camera.restore();
    }
}