//package demo.activity.effect;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.os.Bundle;
//
//
//import engine.android.game.GameCanvas;
//import engine.android.game.Sprite;
//import engine.android.util.image.ImageUtil;
//import engine.water.WaterRender;
//
//public class WaterActivity extends Activity {
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		
//		setContentView(new WaterCanvas(this));
//	}
//}
//
//class WaterCanvas extends GameCanvas {
//	
//	final String image_water = "water.png";
//
//	public WaterCanvas(Context context) {
//		super(context);
//		
//		showFPS(true);
//	}
//
//	@Override
//	protected void onCreate() {
//		Bitmap image = ImageUtil.zoom(load(image_water), getWidth(), getHeight());
//		WaterRender.setBitmap(image);
//		getContentPane().append(new Water(ImageUtil.copy(image, false)));
//		
//		release(image_water);
//		image.recycle();
//	}
//	
//	class Water extends Sprite {
//
//		public Water(Bitmap image) {
//			super(image);
//		}
//		
//		@Override
//		public void onDraw(Canvas canvas) {
//			WaterRender.render(getImage());
//			super.onDraw(canvas);
//		}
//		
//		@Override
//		protected boolean mousePressed(int x, int y) {
//			WaterRender.drop(x, y, 4, 320);
//			return true;
//		}
//		
//		@Override
//		protected boolean mouseDragged(int x, int y) {
//			WaterRender.flip(x, y, 2, 240);
//			return true;
//		}
//	}
//}