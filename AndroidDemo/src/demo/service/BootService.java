package demo.service;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * 开机启动后台服务
 * @author yanhao
 * @version 1.0
 */

public class BootService extends Service {
	
	private MediaRecorder r;										//录音机
	private boolean isRecording;									//是否正在录音

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//监听电话状态
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(new PhoneStateListener(){
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE://电话空闲
					//电话挂断时录音完毕
					if (r != null && isRecording)
					{
						r.stop();
						r.release();
						r = null;
						isRecording = false;
					}
					
					break;
				case TelephonyManager.CALL_STATE_RINGING://电话响铃（有电话来）
					
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK://电话接起
					//开始录音
					record(incomingNumber);
					isRecording = true;
					break;

				default:
					break;
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	/**
	 * 录音
	 * @param incomingNumber 来电号码
	 */
	
	private void record(String incomingNumber)
	{
		try {
			//创建临时存储文件
			File file = File.createTempFile("phoneRecord/" + incomingNumber, null);
			//打开录音机
			MediaRecorder r = new MediaRecorder();
			r.setAudioSource(MediaRecorder.AudioSource.MIC);
			r.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			r.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			r.setOutputFile(file.getAbsolutePath());
			r.prepare();
			r.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}