//package demo.activity.example;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.RectF;
//import android.graphics.Typeface;
//import android.os.Bundle;
//
//import engine.android.game.GameCanvas;
//
///**
// * 闪烁心形界面
// * @author yanhao
// * @version 1.0
// */
//
//public class HeartActivity extends Activity {
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		
//		setContentView(new HeartCanvas(this));
//	}
//}
//
//class HeartCanvas extends GameCanvas {
//
//	public HeartCanvas(Context context) {
//		super(context);
//		
//		showFPS(true);
//		setFPS(10);
//		supportScreenAutoAdapt(true, FULLSCREEN, 320, 480);
//	}
//
//	@Override
//	protected void onCreate() {
//		getContentPane().append(new Heart());
//	}
//	
//	class Heart extends Area {
//		
//		int count;
//
//		public Heart() {
//			super(320, 480);
//			
//			Paint paint = getPaint();
//			paint.setAntiAlias(true);
//			paint.setTextSize(32);
//			paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
//            setPaint(paint);
//		}
//		
//		@Override
//		public void onDraw(Canvas canvas) {
//			Paint paint = getPaint();
//			switch (count++ % 6) {
//			case 0:
//				paint.setColor(Color.BLUE);
//				break;
//			case 1:
//				paint.setColor(Color.GREEN);
//				break;
//			case 2:
//				paint.setColor(Color.RED);
//				break;
//			case 3:
//				paint.setColor(Color.YELLOW);
//				break;
//			case 4:
//				paint.setColor(Color.argb(255, 255, 181, 216));
//				break;
//			case 5:
//				paint.setColor(Color.argb(255, 0, 255, 255));
//				break;
//
//			default:
//				paint.setColor(Color.WHITE);
//				break;
//			}
//			
//			double x,y,r;
//			for (int i = 0; i <= 90; i++)
//			{
//				for (int j = 0; j <= 90; j++)
//				{
//					r = Math.PI / 45 * i * (1 - Math.sin(Math.PI / 45 * j)) * 20;
//					x = r * Math.cos(Math.PI / 45 * j) * Math.sin(Math.PI / 45 * i) + 320 / 2;
//					y = -r * Math.sin(Math.PI / 45 * j) + 400 / 4;
//					canvas.drawPoint((float) x, (float) y, paint);
//				}
//			}
//			
//			RectF rect = new RectF(60, 400, 260, 405);
//			canvas.drawRoundRect(rect, 1, 1, paint);
//			canvas.drawText("Loving You", 75, 400, paint);
//		}
//	}
//}