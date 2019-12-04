package demo.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.WindowManager;

import demo.android.R;

public class NotificationActivity extends Activity {
	
	private NotificationManager nm;						//通知管理器
	
	/**需要声明权限<uses-permission android:name="android.permission.VIBRATE" />**/
	private Vibrator v;									//振动设备
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		//新建通知（包括三个属性）
		//图标，图标后面的文字，以及Notification时间部分显示出来的时间，通常使用当前时间
        Notification n = new Notification(R.drawable.icon, null, System.currentTimeMillis());
        //FLAG_AUTO_CANCEL说明Notification点击一次就消失
        n.flags = Notification.FLAG_AUTO_CANCEL;
        
        Intent intent = new Intent(this, IntentActivity.class);
        //FLAG_ACTIVITY_CLEAR_TOP， FLAG_ACTIVITY_NEW_TASK这两个FLAG
        //表示优先寻找已经打开的应用，如果应用没有打开那么启动它
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        //设置点击通知时的事件处理，第二个参数是PendingIntent的id，如果id相同会被认为是一个
        //FLAG_UPDATE_CURRENT是指后来的PendingIntent会更新前面的
        PendingIntent pi = PendingIntent.getActivity(
        		this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
//        n.setLatestEventInfo(this, "标题", "内容", pi);
        nm.notify(0, n);
        //振动2秒
        v.vibrate(2000);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
    	{
			//心跳频率
    		long[] pattern = {800, 50, 800, 30};//off/on/off/on
    		v.vibrate(pattern, 2);//从pattern[2]开始重复
    	}
    	
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onDestroy() {
    	if (nm != null)
    	{
    		nm.cancelAll();
    	}
    	
		if (v != null)
    	{
    		v.cancel();
    	}
    	
		super.onDestroy();
	}
}