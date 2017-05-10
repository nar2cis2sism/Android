package demo.activity.effect;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import demo.android.R;
import demo.lyric.LyricLoader;
import demo.lyric.LyricView;

import java.io.IOException;

public class LyricActivity extends Activity {
	
	LyricView lyric;
	Button play;
	
	MediaPlayer player;
	Handler handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			lyric.setLyricTime(player.getCurrentPosition());
			lyric.invalidate();
			sendEmptyMessageDelayed(0, 100);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lyric);
		
		lyric = (LyricView) findViewById(R.id.lyric);
		play = (Button) findViewById(R.id.play);
		
		try {
			LyricLoader loader = new LyricLoader();
			loader.load(getAssets().open("lyric/My heart will go on.lrc"));
			//设置歌词资源
			lyric.setLyricList(loader.getLyricList());
			play.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					play();
				}});
		} catch (IOException e) {
			e.printStackTrace();
			lyric.append("歌词加载失败");
			play.setEnabled(false);
		}
		
		player = new MediaPlayer();
		try {
			AssetFileDescriptor fd = getAssets().openFd("lyric/My heart will go on.mp3");
			player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
			player.prepare();
			player.setLooping(true);
		} catch (IOException e) {
			e.printStackTrace();
			lyric.append("\n歌曲加载失败");
			play.setEnabled(false);
		}
	}
	
	@Override
	protected void onDestroy() {
		handler.removeMessages(0);
		player.stop();
		player.release();
		super.onDestroy();
	}
	
	private void play()
	{
		if (!player.isPlaying())
		{
			player.start();
			
			handler.sendEmptyMessage(0);
		}
	}
}