package demo.widget;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

/**
 * 3D旋转控件
 * @author Daimon
 * @version 4.0
 * @since 10/9/2012
 */

public class Rotate3DView extends FrameLayout {
    
    private View view_last;
    private View view_main;
    private View view_next;
    
    private Rotate3DAnimation r3d_last;
    private Rotate3DAnimation r3d_main;
    private Rotate3DAnimation r3d_next;
    
    private int centerX,centerY;
    private float perDegree;
    private int currentChild;
    
    private VelocityTracker vt;
    private int lastMotionX;
    private float degree;

    public Rotate3DView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Rotate3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Rotate3DView(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        centerX = dm.widthPixels / 2;
        centerY = dm.heightPixels / 2;
        perDegree = 90f / dm.widthPixels;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setCurrentChild(0);
    }
    
    public void setCurrentChild(int currentChild) {
        this.currentChild = currentChild;
        setupView();
        resetViewVisible();
    }
    
    public int getCurrentChild() {
        return currentChild;
    }
    
    private void setupView() {
        int last = currentChild - 1;
        if (last < 0)
        {
            last = getChildCount() - 1;
        }
        
        int next = currentChild + 1;
        if (next >= getChildCount())
        {
            next = 0;
        }

        view_last = getChildAt(last);
        view_main = getChildAt(currentChild);
        view_next = getChildAt(next);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (vt == null)
        {
            vt = VelocityTracker.obtain();
        }
        
        vt.addMovement(event);
        
        int x = (int) event.getX();
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            lastMotionX = x;
            break;
        case MotionEvent.ACTION_MOVE:
            int dx = x - lastMotionX;
            if (dx != 0)
            {
                doRotate(dx);
                if (degree > 90)
                {
                    degree = 0;
                    break;
                }
            }
            else
            {
                return false;
            }
            
            lastMotionX = x;
            break;
        case MotionEvent.ACTION_UP:
            vt.computeCurrentVelocity(1000);
            float vx = vt.getXVelocity();
            if (vx > 500 || vx < -500)
            {
                endRotateByVelocity();
            }
            else
            {
                endRotate();
            }
            
            releaseVelocityTracker();
            break;
        case MotionEvent.ACTION_CANCEL:
            releaseVelocityTracker();
            break;
            
        default:
            return false;
        }
        
        return true;
    }
    
    private void doRotate(int dx) {
        view_last.setVisibility(View.VISIBLE);
        view_main.setVisibility(View.VISIBLE);
        view_next.setVisibility(View.VISIBLE);
        
        float d = degree;
        degree += perDegree * dx;
        r3d_last = new Rotate3DAnimation(-90 + d, -90 + degree, centerX, centerY);
        r3d_last.setFillAfter(true);
        r3d_main = new Rotate3DAnimation(d , degree, centerX, centerY);
        r3d_main.setFillAfter(true);
        r3d_next = new Rotate3DAnimation(90 + d, 90 + degree, centerX, centerY);
        r3d_next.setFillAfter(true);
        
        view_last.startAnimation(r3d_last);
        view_main.startAnimation(r3d_main);
        view_next.startAnimation(r3d_next);
    }
    
    private void endRotateByVelocity() {
        if (degree > 0)
        {
            r3d_last = new Rotate3DAnimation(-90 + degree, 0, centerX, centerY);
            r3d_last.setDuration(300);
            r3d_main = new Rotate3DAnimation(degree , 90, centerX, centerY);
            r3d_main.setDuration(300);
            
            view_last.startAnimation(r3d_last);
            view_main.startAnimation(r3d_main);
            
            if (--currentChild < 0)
            {
                currentChild = getChildCount() - 1;
            }
            
            setupView();
        }
        else if (degree < 0)
        {
            r3d_main = new Rotate3DAnimation(degree , -90, centerX, centerY);
            r3d_main.setDuration(300);
            r3d_next = new Rotate3DAnimation(90 + degree, 0, centerX, centerY);
            r3d_next.setDuration(300);
            
            view_main.startAnimation(r3d_main);
            view_next.startAnimation(r3d_next);
            
            currentChild = ++currentChild % getChildCount();
            setupView();
        }
        
        resetViewVisible();
        degree = 0;
    }
    
    private void endRotate() {
        if (degree > 45)
        {
            r3d_last = new Rotate3DAnimation(-90 + degree, 0, centerX, centerY);
            r3d_last.setDuration(300);
            r3d_main = new Rotate3DAnimation(degree , 90, centerX, centerY);
            r3d_main.setDuration(300);
            
            view_last.startAnimation(r3d_last);
            view_main.startAnimation(r3d_main);
            
            if (--currentChild < 0)
            {
                currentChild = getChildCount() - 1;
            }
            
            setupView();
        }
        else if (degree < -45)
        {
            r3d_main = new Rotate3DAnimation(degree , -90, centerX, centerY);
            r3d_main.setDuration(300);
            r3d_next = new Rotate3DAnimation(90 + degree, 0, centerX, centerY);
            r3d_next.setDuration(300);
            
            view_main.startAnimation(r3d_main);
            view_next.startAnimation(r3d_next);
            
            currentChild = ++currentChild % getChildCount();
            setupView();
        }
        else
        {
            r3d_last = new Rotate3DAnimation(-90 + degree, -90, centerX, centerY);
            r3d_last.setDuration(300);
            r3d_main = new Rotate3DAnimation(degree , 0, centerX, centerY);
            r3d_main.setDuration(300);
            r3d_next = new Rotate3DAnimation(90 + degree, 90, centerX, centerY);
            r3d_next.setDuration(300);
            
            view_last.startAnimation(r3d_last);
            view_main.startAnimation(r3d_main);
            view_next.startAnimation(r3d_next);
        }
        
        resetViewVisible();
        degree = 0;
    }
        
    private void resetViewVisible() {
        view_last.setVisibility(View.GONE);
        view_main.setVisibility(View.VISIBLE);
        view_next.setVisibility(View.GONE);
    }

    private void releaseVelocityTracker() {
        if (vt != null)
        {
            vt.clear();
            vt.recycle();
            vt = null;
        }
    }
    
    /**
     * 3D旋转动画
     */

    public static final class Rotate3DAnimation extends Animation {
        
        private float fromDegree,toDegree;                  //旋转角度
        
        private float centerX,centerY;                      //旋转中心坐标
        
        private Camera c;                                   //照相机
        
        public Rotate3DAnimation(float fromDegree, float toDegree, float centerX, float centerY) {
            this.fromDegree = fromDegree;
            this.toDegree = toDegree;
            this.centerX = centerX;
            this.centerY = centerY;
        }
        
        @Override
        public void initialize(int width, int height, int parentWidth,
                int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            c = new Camera();
        }
        
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float degree = fromDegree + (toDegree - fromDegree) * interpolatedTime;
            Matrix m = t.getMatrix();
            if (degree <= -76)
            {
                degree = -90;
                c.save();
                c.rotateY(degree);
                c.getMatrix(m);
                c.restore();
            }
            else if (degree >= 76)
            {
                degree = 90;
                c.save();
                c.rotateY(degree);
                c.getMatrix(m);
                c.restore();
            }
            else
            {
                c.save();
                c.translate(0, 0, centerX);
                c.rotateY(degree);
                c.translate(0, 0, -centerX);
                c.getMatrix(m);
                c.restore();
            }
            
            m.preTranslate(-centerX, -centerY);
            m.postTranslate(centerX, centerY);
        }
    }
}