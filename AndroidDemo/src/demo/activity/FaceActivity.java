package demo.activity;

import engine.android.util.image.ImageSize;
import engine.android.util.image.ImageUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Bundle;
import android.view.Display;
import android.view.View;

import demo.android.R;

public class FaceActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new FaceView(this));
	}
	
	class FaceView extends View {
		
		final int max_face_num = 5;
		
		int find_face_num;
		
		Face[] faces;
		
		Bitmap image;
		
		Paint paint;

		public FaceView(Context context) {
			super(context);
			
			faces = new Face[max_face_num];
			
			Display dis = getWindowManager().getDefaultDisplay();
    		int width = dis.getWidth();
    		int height = dis.getHeight();
    		
    		Options opts = new Options();
    		opts.inPreferredConfig = Bitmap.Config.RGB_565; // 只支持这种位图
    		image = BitmapFactory.decodeResource(getResources(), R.drawable.img0200, opts);
            
            ImageSize size = new ImageSize();
            size.setAspectRatio(image.getWidth(), image.getHeight());
            size.setWidth(width);
    		
    		image = ImageUtil.zoom(image, size.getWidth(), size.getHeight());
			
			paint = new Paint();
			paint.setColor(Color.GREEN);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(3);
			
			FaceDetector fd = new FaceDetector(image.getWidth(), image.getHeight(), max_face_num);
			find_face_num = fd.findFaces(image, faces);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(image, 0, 0, null);
			
			for (int i = 0; i < find_face_num; i++)
			{
				Face face = faces[i];
				PointF mid_point = new PointF();
				face.getMidPoint(mid_point);
				float distance = face.eyesDistance();
				canvas.drawRect(mid_point.x - distance,
								mid_point.y - distance,
								mid_point.x + distance,
								mid_point.y + distance, 
								paint);
			}
		}
	}
}