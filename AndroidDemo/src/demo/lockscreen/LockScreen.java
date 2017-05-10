package demo.lockscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import demo.android.R;

public class LockScreen extends RelativeLayout {
	
	private static final int DURATION = 20;
	private static final float SPEED = 0.7f;
	
	private ImageView slider;
	
	private AnimationDrawable arrow_anim;
	
	private Bitmap slider_drag;
	
	private int downX;
	private int realX;
	private int unlockX;
	private Rect rect = new Rect();
	private boolean slide;
	
	private Handler handler = new Handler();
	
	private UnlockListener listener;

	public LockScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		slider_drag = BitmapFactory.decodeResource(getResources(), R.drawable.slider_drag);
	}
	
	public void setUnlockListener(UnlockListener listener)
	{
		this.listener = listener;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		slider = (ImageView) findViewById(R.id.slider);
		
		ImageView arrow = (ImageView) findViewById(R.id.arrow);
		arrow_anim = (AnimationDrawable) arrow.getBackground();
	}
	
	@Override
	protected void onAttachedToWindow() {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				arrow_anim.start();
			}
		});
		
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		arrow_anim.stop();
		super.onDetachedFromWindow();
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		
		slider.getHitRect(rect);
		unlockX = getRight() - slider_drag.getWidth();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (slide)
		{
			//draw slider
			int x = realX - downX;
			int y = slider.getTop();
			canvas.drawBitmap(slider_drag, x < 0 ? 0 : x, y, null);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			slide = rect.contains(x, y);
			if (slide)
			{
				slider.setVisibility(INVISIBLE);
				downX = x;
				realX = x;
			}
			
			return slide;
		case MotionEvent.ACTION_MOVE:
			if (slide)
			{
				realX = x;
				if (handleUnlock())
				{
					unlock();
				}
				else
				{
					invalidate();
				}
			}
			
			return slide;
		case MotionEvent.ACTION_UP:
			if (slide)
			{
				realX = x;
				if (handleUnlock())
				{
					unlock();
				}
				else
				{
					handler.postDelayed(resetRunnable, DURATION);
				}
			}
			
			return slide;

		default:
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	private boolean handleUnlock()
	{
		return realX - downX >= unlockX;
	}
	
	private void unlock()
	{
		Toast.makeText(getContext(), "解锁成功", Toast.LENGTH_LONG).show();
		reset();
		if (listener != null)
		{
			listener.unlock();
		}
	}
	
	private void reset()
	{
		slide = false;
		slider.setVisibility(VISIBLE);
		invalidate();
	}
	
	private Runnable resetRunnable = new Runnable() {
		
		@Override
		public void run() {
			realX -= SPEED * DURATION;
			if (realX <= downX)
			{
				reset();
			}
			else
			{
				invalidate();
				handler.postDelayed(this, DURATION);
			}
		}
	};
	
	public interface UnlockListener {
		public void unlock();
	}
}