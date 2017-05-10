package demo.widget;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import engine.android.util.image.ImageUtil;

/**
 * 我的画廊
 * @author yanhao
 * @version 1.0
 */

public class MyGallery extends Gallery implements OnItemClickListener {
	
	private int maxRotationAngle = 60;				//最大旋转角度
	
	private int maxZoom = -120;						//最大缩放角度
	
	private boolean alphaMode;						//透明模式开关
	
	private Camera camera = new Camera();
	
	private int center;								//画廊的水平中点
	
	public MyGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		setStaticTransformationsEnabled(true);
		setOnItemClickListener(this);
	}
	
	/**
	 * 获取视图的水平中点
	 * @param view
	 * @return
	 */
	
	private int getCenterOfView(View view)
	{
		return view.getLeft() + view.getWidth() / 2;
	}
	
	/**
	 * 获取画廊的水平中点
	 * @return
	 */
	
	private int getCenter()
	{
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		center = getCenter();
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		final int childCenter = getCenterOfView(child);
        final int childWidth = child.getWidth();
        int rotationAngle = 0;

        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX);
        
        if (childCenter != center)
        {
        	rotationAngle = ((center - childCenter) * maxRotationAngle / childWidth);
        	if (Math.abs(rotationAngle) > maxRotationAngle)
        	{
        		rotationAngle = rotationAngle < 0 ? -maxRotationAngle : maxRotationAngle;
        	}
        }

    	transform(child, t, rotationAngle);
        return true;
	}
	
	/**
	 * 图片转换（3D旋转效果）
	 * @param child
	 * @param t
	 * @param rotationAngle
	 */
	
	private void transform(View child, Transformation t, int rotationAngle)
	{
		camera.save();
		
		final Matrix m = t.getMatrix();
		final int width = child.getLayoutParams().width;
        final int height = child.getLayoutParams().height;
        final int rotation = Math.abs(rotationAngle);

        //在Z轴上正向移动camera的视角，实际效果为放大图片。
        //如果在Y轴上移动，则图片上下移动；X轴上对应图片左右移动。
        camera.translate(0.0f, 0.0f, 100.0f);

        // As the angle of the view gets less, zoom in
        if (rotation <= maxRotationAngle)
        {
            float zoomAmount = (float) (maxZoom + (rotation * 1.5));
            camera.translate(0.0f, 0.0f, zoomAmount);
            if (alphaMode)
            {
            	((ImageView) child).setAlpha((int) (255 - rotation * 2.5));
            }
        }

        //在Y轴上旋转，对应图片竖向向里翻转。
        //如果在X轴上旋转，则对应图片横向向里翻转。
        camera.rotateY(rotationAngle);
        
        camera.getMatrix(m);
        m.preTranslate(-(width / 2), -(height / 2));
        m.postTranslate((width / 2), (height / 2));
        camera.restore();
	}
	
	/** 实现反弹的效果 **/
	private float x;
	private float endX;
	private int selectedIndex;
	private float width;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			x = event.getX();
			endX = 0;
			selectedIndex = getSelectedItemPosition();
			if (selectedIndex == 0)
			{
				width = getWidth() - x;
			}
			else if (selectedIndex == getCount() - 1)
			{
				width = x;
			}
			
			break;
		case MotionEvent.ACTION_MOVE:
			if (selectedIndex == 0 || selectedIndex == getCount() - 1)
			{
				float x = event.getX();
				endX = x - this.x;
				if (selectedIndex == 0)
				{
					if (endX > 0 && endX < getWidth() && x > this.x - width)
					{
						Animation anim = new TranslateAnimation(endX, endX, 0, 0);
						anim.setDuration(25);
						anim.setFillAfter(true);
						startAnimation(anim);
					}
					else
					{
						endX = 0;
						selectedIndex = getSelectedItemPosition();
					}
				}
				else
				{
					if (endX < 0 && endX > -getWidth() * 2 && x < this.x + width)
					{
						Animation anim = new TranslateAnimation(endX, endX, 0, 0);
						anim.setDuration(25);
						anim.setFillAfter(true);
						startAnimation(anim);
					}
					else
					{
						endX = 0;
						selectedIndex = getSelectedItemPosition();
					}
				}
			}
			
			break;
		case MotionEvent.ACTION_UP:
			if (selectedIndex == 0 || selectedIndex == getCount() - 1)
			{
				int index = getSelectedItemPosition();
				if (index == 0 || index == getCount() - 1)
				{
					if (endX != 0)
					{
						Animation anim = new TranslateAnimation(endX, 0, 0, 0);
						anim.setDuration(300);
						anim.setFillAfter(true);
						startAnimation(anim);
					}
				}
			}
			
			break;

		default:
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	/**
	 * 画廊图片适配器
	 * @author yanhao
	 * @version 1.0
	 */

	public static class ImageAdapter extends BaseAdapter {
		
		private Context context;
		
		private int[] ids;								//图片资源ID数组
		
		private ImageView[] images;						//图片视图数组
		
		public ImageAdapter(Context context, int[] ids) {
			this.context = context;
			this.ids = ids;
			images = new ImageView[ids.length];
			createReflectedImages();
		}
		
		/**
		 * 创建倒影图片
		 */
		
		private void createReflectedImages()
		{
			for (int i = 0, len = ids.length; i < len; i++)
			{
				ImageView iv = new ImageView(context);
				iv.setImageBitmap(ImageUtil.getReflectedImage
						(BitmapFactory.decodeResource(context.getResources(), ids[i])));
				iv.setLayoutParams(new Gallery.LayoutParams(180, 240));
				images[i] = iv;
			}
		}

		@Override
		public int getCount() {
			return ids.length;
		}

		@Override
		public Object getItem(int position) {
			return ids[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return images[position];
		}
	}

	public void setMaxRotationAngle(int maxRotationAngle) {
		this.maxRotationAngle = maxRotationAngle;
	}

	public void setMaxZoom(int maxZoom) {
		this.maxZoom = maxZoom;
	}
	
	public void setAlphaMode(boolean alphaMode) {
		this.alphaMode = alphaMode;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position == getSelectedItemPosition())
		{
			//处理点击动作
		}
	}
}