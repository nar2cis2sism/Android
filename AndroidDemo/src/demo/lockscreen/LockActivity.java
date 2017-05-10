package demo.lockscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import demo.android.R;
import demo.lockscreen.LockScreen.UnlockListener;

public class LockActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lock);
		
		LockScreen lock = (LockScreen) findViewById(R.id.lock);
		lock.setUnlockListener(new UnlockListener() {
			
			@Override
			public void unlock() {
				finish();
			}
		});
		
		startService(new Intent(this, LockService.class));
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}
}