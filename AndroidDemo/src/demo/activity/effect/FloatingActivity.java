package demo.activity.effect;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import demo.android.R;
import engine.android.util.AndroidUtil;
import engine.android.util.os.WindowUtil;

public class FloatingActivity extends Activity {
	
	WindowManager wm;
	WindowManager.LayoutParams wl;
	FloatingWindow fw;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TextView tv = new TextView(this);
		tv.setText("悬浮窗口展示");
		tv.setTextSize(20);
		
		setContentView(tv);
		
		wm = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
		wl = new LayoutParams();
		
		//需要声明权限<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
		wl.type = LayoutParams.TYPE_SYSTEM_ALERT;
		wl.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
		wl.format = PixelFormat.TRANSLUCENT;
		
		//以屏幕左上角为原点，设置x、y初始值
		wl.gravity = Gravity.LEFT | Gravity.TOP;
		wl.x = 0;
		wl.y = 0;
		//设置悬浮窗口长宽数据
		wl.width = LayoutParams.WRAP_CONTENT;
		wl.height = LayoutParams.WRAP_CONTENT;
		
		fw = new FloatingWindow(this);
		fw.setImageResource(R.drawable.icon);
		
		wm.addView(fw, wl);
	}
	
	@Override
	protected void onDestroy() {
		wm.removeView(fw);
		super.onDestroy();
	}
	
	class FloatingWindow extends ImageView {
		
		int statusBarHeight = -1;
		
		float x;
		float y;

		public FloatingWindow(Context context) {
			super(context);
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//获取相对View的坐标，即以此View左上角为原点
				x = event.getX();
				y = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				//获取相对屏幕的坐标，即以屏幕左上角为原点
				wl.x = (int) (event.getRawX() - x);
				//除去状态栏的高度
				wl.y = (int) (event.getRawY() - y - (statusBarHeight == -1 ? 
						statusBarHeight = WindowUtil.getStatusBarHeight(getWindow()) : statusBarHeight));
				//更新浮动窗口位置参数
				wm.updateViewLayout(this, wl);
				break;

			default:
				break;
			}
			
			return true;
		}
	}
}