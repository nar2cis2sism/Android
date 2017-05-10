package demo.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import demo.android.R;

public class MediaActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media);
		
		final VideoView v = (VideoView) findViewById(R.id.VideoView);
		//播放本地视频（存于raw文件夹下）
		//android.resource://是固定的，demo.android是我的包名
		v.setVideoPath("android.resource://demo.android/" + R.raw.video);
		v.setMediaController(new MediaController(this));
		v.setOnPreparedListener(new OnPreparedListener(){

			@Override
			public void onPrepared(MediaPlayer mp) {
				//资源加载完毕后开始播放
				v.start();
			}});
		v.setOnCompletionListener(new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer mp) {
				//重复播放
				v.start();
			}});
	}
}