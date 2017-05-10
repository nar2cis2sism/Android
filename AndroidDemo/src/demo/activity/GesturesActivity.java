package demo.activity;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import demo.android.R;

import java.util.ArrayList;

public class GesturesActivity extends Activity {
	
	private GestureLibrary library;								//手势库
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gestures);
		
		//加载手势库
		library = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!library.load())
		{
			finish();
		}
		
		GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
		gestures.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);//设置笔划类型（支持多笔划）
		gestures.addOnGesturePerformedListener(new OnGesturePerformedListener(){

			@Override
			public void onGesturePerformed(GestureOverlayView overlay,
					Gesture gesture) {
				//从手势库识别手势
				ArrayList<Prediction> predictions = library.recognize(gesture);
				// We want at least one prediction
				if (predictions.size() > 0) {
					Prediction prediction = predictions.get(0);
					// We want at least some confidence in the result
					if (prediction.score > 1.0) {
						// Show the spell
						Toast.makeText(GesturesActivity.this, prediction.name, Toast.LENGTH_SHORT).show();
					}
				}
			}});
		gestures.addOnGestureListener(new OnGestureListener(){

			@Override
			public void onGesture(GestureOverlayView overlay, MotionEvent event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGestureCancelled(GestureOverlayView overlay,
					MotionEvent event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGestureEnded(GestureOverlayView overlay,
					MotionEvent event) {
				//获得刚绘制的手势对象
				Gesture g = overlay.getGesture();
				System.out.println(g.getStrokesCount());//如果设置了多笔划需要判断当前笔划数
				//转换为一张图片
				g.toBitmap(128, 128, 8, 0xffffff00);
				//保存手势
//				library.addGesture("name", g);
//				library.save();
			}

			@Override
			public void onGestureStarted(GestureOverlayView overlay,
					MotionEvent event) {
				// TODO Auto-generated method stub
				
			}});
	}
}