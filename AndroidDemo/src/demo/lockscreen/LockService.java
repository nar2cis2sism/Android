package demo.lockscreen;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import demo.activity.IntentActivity;
import demo.android.R;
import engine.android.util.manager.MyKeyguardManager;

public class LockService extends Service {
	
	private MyKeyguardManager km;
	
	//屏幕变亮的广播,我们要隐藏默认的锁屏界面
	private BroadcastReceiver screenOn = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()))
			{
				km.hide();
				
				intent = new Intent(context, LockActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//一定要加这一句啊
				context.startActivity(intent);
			}
		}
	};
	
	@Override
	public void onCreate() {
		km = new MyKeyguardManager(this);
		registerReceiver(screenOn, new IntentFilter(Intent.ACTION_SCREEN_ON));
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    //无效啊
	    
	    //新建通知（包括三个属性）
        //图标，图标后面的文字，以及Notification时间部分显示出来的时间，通常使用当前时间
        Notification n = new Notification(R.drawable.icon, "lock", System.currentTimeMillis());
        //FLAG_AUTO_CANCEL说明Notification点击一次就消失
        n.flags = Notification.FLAG_AUTO_CANCEL;
        
        intent = new Intent(this, IntentActivity.class);
        //FLAG_ACTIVITY_CLEAR_TOP， FLAG_ACTIVITY_NEW_TASK这两个FLAG
        //表示优先寻找已经打开的应用，如果应用没有打开那么启动它
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        //设置点击通知时的事件处理，第二个参数是PendingIntent的id，如果id相同会被认为是一个
        //FLAG_UPDATE_CURRENT是指后来的PendingIntent会更新前面的
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        n.setLatestEventInfo(this, "标题", "内容", pi);
        startForeground(/*must not be 0*/1, n);
	    
	    return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		km.show();
		unregisterReceiver(screenOn);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}