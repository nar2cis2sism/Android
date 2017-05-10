package demo.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import demo.activity.MainActivity;

/**
 * 开机启动广播接收器
 * @author yanhao
 * @version 1.0
 */

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//启动服务
//		context.startService(new Intent(context, BootService.class));
		//启动程序
		intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//一定要加这一句啊
		context.startActivity(intent);
		System.out.println("开机自动启动");
	}
}