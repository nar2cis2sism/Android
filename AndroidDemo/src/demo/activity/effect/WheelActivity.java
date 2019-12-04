//package demo.activity.effect;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Paint.Style;
//import android.graphics.Path;
//import android.graphics.Point;
//import android.graphics.RadialGradient;
//import android.graphics.RectF;
//import android.graphics.Shader.TileMode;
//import android.os.Bundle;
//import android.util.AttributeSet;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnLongClickListener;
//import android.widget.ImageButton;
//
//import demo.android.R;
//import engine.android.game.Area;
//import engine.android.game.GameCanvas;
//
//public class WheelActivity extends Activity {
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		
//		setContentView(R.layout.wheel);
//		
//		final WheelCanvas wheel_view = (WheelCanvas) findViewById(R.id.wheel_view);
//		
//		ImageButton wheel_button = (ImageButton) findViewById(R.id.wheel_button);
//		wheel_button.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				wheel_view.wheel.rotate(20);
//			}
//		});
//		wheel_button.setOnLongClickListener(new OnLongClickListener() {
//			
//			@Override
//			public boolean onLongClick(View v) {
//				wheel_view.wheel.rotate(-20);
//				return true;
//			}
//		});
//	}
//}
//
//class WheelCanvas extends GameCanvas {
//	
//	Wheel wheel;
//
//	public WheelCanvas(Context context, AttributeSet attrs) {
//		super(context, attrs);
//
//		showFPS(true);
//		supportAdvancedTouchEvents();
//	}
//
//	@Override
//	protected void onCreate() {
//		setBackgroundColor(0xff969696);
//		getContentPane().append(wheel = 
//				new Wheel(getContentPane().getWidth() / 2, getContentPane().getHeight() / 2, 120)
//				.setItem(new String[]{"美餐一顿", "一起购物", "运动", "唱歌", "看电影"}, null));
//	}
//	
//	class Wheel extends Area {
//		
//		private static final float MaxSpeed = 90;
//		
//		private float radius;					//圆半径
//		private float ringRadius = 15;			//圆环半径
//		private float startAngle;				//开始角度
//		private float speed;					//速度
//		private float acceleration;				//加速度
//		private int itemCount;					//选项数量
//		private int[] itemColor;				//选项颜色
//		private String[] itemTitle;				//选项标题
//		private float[] itemRatio;				//选项比例
//		
//		private Path path = new Path();			//标题绘制路径
//		private boolean rotate;					//旋转开关
//		private boolean clockwise;				//是否顺时针旋转
//		
//		private Paint ringPaint;
//		private Paint pointPaint;
//		
//		public Wheel(int centerX, int centerY, int radius) {
//			super(radius * 2, radius * 2);
//			setPosition(centerX, centerY);
//			defineReferencePixel(radius, radius);
//			
//			this.radius = radius;
//			itemColor = new int[]{
//					0xFFFFFFFF,//白色
//					0xFFB0E0E6,//粉蓝色
//					0xFF444444,//深灰色
//					0xFF008B8B,//暗青色
//					0xFFFFA500,//橙色
//					0xFF7FFF00,//黄绿色
//					0xFFF08080,//亮珊瑚色
//					0xFFB0C4DE //亮钢兰色
//			};
//			
//			Paint paint = getPaint();
//			paint.setAntiAlias(true);
//			paint.setTextSize(18);
//			setPaint(paint);
//			
//			ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//			ringPaint.setStyle(Style.STROKE);
//			ringPaint.setStrokeWidth(ringRadius);
//			//环形渐变
//			RadialGradient radialGradient = new RadialGradient(getX(), getY(), radius + ringRadius, 
//					new int[]{Color.GREEN, Color.GRAY, Color.MAGENTA, Color.YELLOW, Color.BLACK}, 
//					null, TileMode.MIRROR);
//			ringPaint.setShader(radialGradient);
//			
//			pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//			pointPaint.setStrokeWidth(5);
//			pointPaint.setColor(Color.RED);
//		}
//		
//		/**
//		 * 设置转盘选项（最多支持8个）
//		 * @param itemTitle 选项标题
//		 * @param itemRatio 选项所占百分比，Null平均分配
//		 * @return
//		 */
//		
//		public Wheel setItem(String[] itemTitle, float[] itemRatio)
//		{
//			int count = itemTitle.length;
//			if (count > itemColor.length)
//			{
//				throw new ArrayIndexOutOfBoundsException(itemColor.length);
//			}
//			
//			itemCount = count;
//			this.itemTitle = itemTitle;
//			if (itemRatio == null)
//			{
//				this.itemRatio = itemRatio = new float[count];
//				float ratio = 100f / count;
//				float sum = 0;
//				for (int i = 0; i < count - 1; i++)
//				{
//					itemRatio[i] = ratio;
//					sum += ratio;
//				}
//				
//				itemRatio[count - 1] = 100 - sum;
//			}
//			else
//			{
//				if (itemRatio.length != count)
//				{
//					throw new IllegalArgumentException();
//				}
//				
//				float sum = 0;
//				for (int i = 0; i < count; i++)
//				{
//					sum += itemRatio[i];
//				}
//				
//				if (sum != 100)
//				{
//					throw new IllegalArgumentException();
//				}
//				
//				this.itemRatio = itemRatio;
//			}
//			
//			return this;
//		}
//		
//		/**
//		 * 旋转转盘
//		 * @param speed 旋转速度（内含旋转方向），0为停止旋转
//		 */
//		
//		public synchronized void rotate(float speed)
//		{
//			if (speed != 0)
//			{
//				if (rotate)
//				{
//					speed = (clockwise ? this.speed : -this.speed) + speed;
//				}
//				
//				if (speed > MaxSpeed)
//				{
//					speed = MaxSpeed;
//				}
//				else if (speed < -MaxSpeed)
//				{
//					speed = -MaxSpeed;
//				}
//				
//				clockwise = speed > 0;
//				acceleration = (this.speed = Math.abs(speed)) / 100;
//				rotate = true;
//			}
//			else
//			{
//				rotate = false;
//			}
//		}
//		
//		/**
//		 * 判断转盘是否正在旋转
//		 */
//		
//		public boolean isRotating()
//		{
//			return rotate;
//		}
//		
//		@Override
//		public void onDraw(Canvas canvas) {
//			int x = getX();
//			int y = getY();
//			//绘制圆环
//			canvas.drawCircle(x, y, radius, ringPaint);
//			
//			RectF rectF = new RectF(x - radius, y - radius, x + radius, y + radius);
//			//绘制每个区域的颜色块
//			drawItem(canvas, rectF);
//			//绘制中间的红色指示器
//			canvas.drawLine(x, y + radius / 5, x, y - radius / 2, pointPaint);
//			canvas.drawCircle(x, y, 6, pointPaint);
//			
//			if (rotate)
//			{
//				if (clockwise)
//				{
//					startAngle += speed;
//				}
//				else
//				{
//					startAngle -= speed;
//				}
//
//				speed -= acceleration;
//				if (speed <= 0)
//				{
//					rotate = false;
//				}
//			}
//			else
//			{
//				if (startAngle < -360 || startAngle > 360)
//				{
//					startAngle %= 360;
//				}
//			}
//		}
//		
//		private void drawItem(Canvas canvas, RectF rectF)
//		{
//			Paint paint = getPaint();
//			float startAngle = this.startAngle;
//			float sweepAngle = 0;
//			for (int i = 0; i < itemCount; i++)
//			{
//				paint.setColor(itemColor[i]);
//				sweepAngle = 360 * itemRatio[i] / 100;
//				canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
//				//绘制对应的标题
//				path.rewind();
//				path.addArc(rectF, startAngle, sweepAngle);
//				canvas.drawTextOnPath(itemTitle[i], path, 5, -10, paint);
//				
//				startAngle += sweepAngle;
//			}
//		}
//		
//		private boolean isTouch;
//		private Point mousePressed = new Point();
//		private float rotateAngle;
//		
//		@Override
//		protected boolean mousePressed(int x, int y) {
//			if (contains(x, y) && !isRotating())
//			{
//				isTouch = true;
//				mousePressed.set(x, y);
//				rotateAngle = startAngle;
//				return true;
//			}
//			
//			return false;
//		}
//		
//		@Override
//		protected boolean mouseDragged(int x, int y) {
//			if (contains(x, y))
//			{
//				if (isTouch)
//				{
//					double startAngle = computeAngleFromCentre(mousePressed.x, mousePressed.y);
//					double endAngle = computeAngleFromCentre(x, y);
//					this.startAngle = rotateAngle + (float) (startAngle - endAngle);
//					return true;
//				}
//			}
//			else
//			{
//				isTouch = false;
//			}
//			
//			return false;
//		}
//		
//		@Override
//		protected boolean mouseReleased(int x, int y) {
//			isTouch = false;
//			return false;
//		}
//		
//		@Override
//		protected boolean onAdvancedTouchEvent(TouchEvent event) {
//			int x = (int) event.getDownX();
//			int y = (int) event.getDownY();
//			if (event.getAction() == ACTION_LONG_PRESS && contains(x, y))
//			{
//				rotate(0);
//				return true;
//			}
//			
//			if (event.getAction() == ACTION_FLING && contains(x, y))
//			{
//				if (event.isFlingHorizontal())
//				{
//					if (y > getY())
//					{
//						rotate(-event.getFlingVelocityX() * MaxSpeed / event.getMaxVelocity());
//					}
//					else if (y < getY())
//					{
//						rotate(event.getFlingVelocityX() * MaxSpeed / event.getMaxVelocity());
//					}
//				}
//				
//				if (event.isFlingVertical())
//				{
//					if (x > getX())
//					{
//						rotate(event.getFlingVelocityY() * MaxSpeed / event.getMaxVelocity());
//					}
//					else if (x < getX())
//					{
//						rotate(-event.getFlingVelocityY() * MaxSpeed / event.getMaxVelocity());
//					}
//				}
//				
//				return true;
//			}
//			
//			return false;
//		}
//		
//		private boolean contains(int x, int y)
//		{
//			return collidesWith(x, y, false) && Math.abs(x - getX()) < radius && Math.abs(y - getY()) < radius;
//		}
//
//		private double computeAngleFromCentre(int x, int y)
//		{
//			return Math.atan2(x - getX(), y - getY()) * 180 / Math.PI;
//		}
//	}
//}