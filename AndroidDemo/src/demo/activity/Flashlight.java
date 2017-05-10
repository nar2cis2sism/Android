package demo.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import demo.android.R;

public class Flashlight extends Activity {

	private static final int MINIMUM_BACKLIGHT = 10;
	private static final int MAXIMUM_BACKLIGHT = 255;

	private static final int MODE_FLASHLIGHT = 0;
	private static final int MODE_FLASH = 1;
	private static final int MODE_SOS = 2;
	
	private PowerManager.WakeLock mWakeLock;
	
	private int mOldBrightness;
	private View mWindow;
	
	/**
	 * 0: flashlight
	 * 1: flash
	 * 2: sos
	 */
	private int mCurrentMode = -1;
	
	/**
	 * background color
	 * 
	 * 1: white
	 * 0: black
	 */
	private int mFlag;
	
	// -----------------------------------
	private FlashHandler mFlashHandler;
	
	// -----------------------------------
	private SOSHandler mSOSHandler;
	private int mSOSIdx;
	private int[] mSOSCode = new int[] {
			300,
			300,
			300,	// ...	S
			300,
			300,
			//
			900,
			//
			900,
			300,
			900,	// ---	O
			300,
			900,
			//
			900,
			//
			300,
			300,
			300,	// ...	S
			300,
			300,
			300,
			//
			2100
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullscreen();
		setContentView(R.layout.flashlight);
		
		mOldBrightness = getOldBrightness();
		
		
		mWindow = findViewById(R.id.window);
		
		// default is flashlight mode
		modeFlashlight();
		
		mWakeLock = getWakeLock();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		
		menu.add(0, MODE_FLASHLIGHT, 0, "Flashlight");
		
		menu.add(0, MODE_FLASH, 0, "Flash");
		
		menu.add(0, MODE_SOS, 0, "SOS");
		
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		mCurrentMode = item.getItemId();
		
		switch (item.getItemId()) {
		case MODE_FLASHLIGHT:
			modeFlashlight();
			return true;
		case MODE_FLASH:
			modeFlash();
			return true;
		case MODE_SOS:
			modeSOS();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStop() {
		restoreBrightness();
		mWakeLock.release();
		
		super.onStop();
	}

	public void modeFlashlight() {
		mFlag = 1;
		mWindow.setBackgroundColor(Color.WHITE);
		setBrightness(MAXIMUM_BACKLIGHT);
	}
	
	// ---------------------------------------------------------------------------- Flash
	public void modeFlash() {
		mFlag = 1;
		mFlashHandler = new FlashHandler();
		updateFlashUI();
	}
	
	private void updateFlashUI() {
		
		if (mCurrentMode != MODE_FLASH) {
			return;
		}
		
		mFlashHandler.sleep(500);
		
		if (mFlag == 1) {
			mWindow.setBackgroundColor(Color.BLACK);
			mFlag = 0;
		} else {
			mWindow.setBackgroundColor(Color.WHITE);
			mFlag = 1;
		}
	}
	
	// ---------------------------------------------------------------------------- SOS Morse Code
	public void modeSOS() {
		mFlag = 1;
		mSOSIdx = 0;
		mSOSHandler = new SOSHandler();
		updateSOSUI();
	}
	
	private void updateSOSUI() {
		if (mCurrentMode != MODE_SOS) {
			return;
		}
		
		mSOSHandler.sleep(mSOSCode[mSOSIdx]);
		
		if (mSOSIdx + 1 == mSOSCode.length) {
			mSOSIdx = 0;
			mFlag = 1;
		} else {
			mSOSIdx++;
		}
		
		if (mFlag == 1) {
			mWindow.setBackgroundColor(Color.BLACK);
			mFlag = 0;
		} else {
			mWindow.setBackgroundColor(Color.WHITE);
			mFlag = 1;
		}
	}
	
	/**
	 * 全屏运行
	 */
	private void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window win = getWindow();
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	private void restoreBrightness() {
		setBrightness(mOldBrightness);
	}
	
	private PowerManager.WakeLock getWakeLock() {
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		PowerManager.WakeLock w = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Flashlight");
		w.acquire();
		
		return w;
	}
	
	/**
	 * 取得当前用户自定义的屏幕亮度
	 */
	private int getOldBrightness() {
		int brightness;
		
		try {
			brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException snfe) {
			brightness = MAXIMUM_BACKLIGHT;
		}
		
		return brightness;
	}
	
	/**
	 * 设置屏幕亮度
	 */
	private void setBrightness(int brightness) {
//		IHardwareService hardware = IHardwareService.Stub.asInterface(ServiceManager.getService("hardware"));
//		if (hardware != null) {
//			try {
//				hardware.setScreenBacklight(brightness);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//		}
		
//		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);//此方法设置无效
		if (brightness < MINIMUM_BACKLIGHT)
		{
			brightness = MINIMUM_BACKLIGHT;
		}
		
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.screenBrightness = brightness * 1.0f / 255;
		getWindow().setAttributes(params);
	}
	
	class FlashHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Flashlight.this.updateFlashUI();
		}
		
		public void sleep(long delayMillis) {
			removeMessages(0);
			
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	}
	
	class SOSHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Flashlight.this.updateSOSUI();
		}
		
		public void sleep(long delayMillis) {
			removeMessages(0);
			
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	}
}