package demo.activity.effect;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import demo.android.R;
import demo.prototype.CustomView.TouchEventDelegate;
import demo.widget.Magnifier;
import engine.android.util.image.ImageUtil;

public class MagnifierActivity extends Activity implements Runnable {
    
    int width,height;           //屏幕尺寸
    
    int x,y;                    //放大镜位置
    
    Magnifier magnifier;
    
    boolean autoMove = true;
    
    Handler handler = new Handler() {
        
        private int moveX = 1, moveY = 1;
        
        @Override
        public void handleMessage(Message msg) {
            x += moveX;
            y += moveY;
            if (x <= 0) moveX = 1;
            if (y <= 0) moveY = 1;
            if (x + magnifier.getRadius() * 2 >= width)  moveX = -1;
            if (y + magnifier.getRadius() * 2 >= height) moveY = -1;
            
            magnifier.setLocation(x, y);
            
            if (autoMove)
            {
                sendEmptyMessageDelayed(0, 10);
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.magnifier);
        iv.setScaleType(ScaleType.FIT_XY);
        setContentView(iv);
        
        new Handler().postDelayed(this, 100);
    }
    
    @Override
    protected void onDestroy() {
        stopAutoMove();
        super.onDestroy();
    }
    
    private void stopAutoMove()
    {
        autoMove = false;
        handler.removeMessages(0);
    }

    @Override
    public void run() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        width = dm.widthPixels;
        height = dm.heightPixels;
        int circleX = width / 2;
        int circleY = height / 2;
        
        magnifier = new Magnifier(this);
        magnifier.setCirclePosition(circleX, circleY);
        magnifier.setScreenImage(ImageUtil.view2Bitmap(getWindow().getDecorView()));
        magnifier.setOnTouchListener(touchEvent);
        addContentView(magnifier, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        x = magnifier.getLocationX();
        y = magnifier.getLocationY();
        
        handler.sendEmptyMessage(0);
    }
    
    private TouchEventDelegate touchEvent = new TouchEventDelegate() {
        
        private boolean isPressed;
        
        @Override
        public boolean handleActionUp(MotionEvent event, int x, int y) {
            if (isPressed)
            {
                isPressed = false;
                return true;
            }
            
            return false;
        }
        
        @Override
        public boolean handleActionMove(MotionEvent event, int x, int y) {
            if (isPressed)
            {
                magnifier.setLocation(
                        MagnifierActivity.this.x += x - lastMotionX, 
                        MagnifierActivity.this.y += y - lastMotionY);
                return true;
            }
            
            return false;
        }
        
        @Override
        public boolean handleActionDown(MotionEvent event, int x, int y) {
            if (magnifier.isTouchToCircle(x, y))
            {
                stopAutoMove();
                
                isPressed = true;
                return true;
            }
            else if (!autoMove)
            {
                autoMove = true;
                handler.sendEmptyMessage(0);
                return true;
            }
            
            return false;
        }
    };
}