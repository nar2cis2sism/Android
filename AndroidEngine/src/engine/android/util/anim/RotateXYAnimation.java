package engine.android.util.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * 华为手机rotationX,rotationY动画失效（View莫名消失）的替代方案
 * 
 * @author Daimon
 * @since 8/27/2019
 */
public class RotateXYAnimation extends Animation {

    private int centerX, centerY;
    
    private final Camera camera = new Camera();
    
    private float startDegree, rotation;

    private boolean rotateY;

    public RotateXYAnimation(float startDegree, float rotation) {
        this.startDegree = startDegree;
        this.rotation = rotation;
        setFillAfter(true);
        setInterpolator(new LinearInterpolator());
    }
    
    public RotateXYAnimation setRotateY(boolean rotateY) {
        this.rotateY = rotateY;
        return this;
    }

    @Override
    public void initialize(int width, int height, int parentWidth,
            int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        centerX = width / 2;
        centerY = height / 2;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        camera.save();
        
        final Matrix m = t.getMatrix();
        // 设置camera的位置
        camera.setLocation(0, 0, 180);
        // 在Y轴上旋转，对应图片竖向向里翻转。
        // 如果在X轴上旋转，则对应图片横向向里翻转。
        float rotation = this.rotation * interpolatedTime + startDegree;
        if (rotateY)
        {
            camera.rotateY(rotation);
        }
        else
        {
            camera.rotateX(rotation);
        }
        
        camera.getMatrix(m);
        m.preTranslate(-centerX, -centerY);
        m.postTranslate(centerX, centerY);
        camera.restore();
    }
}