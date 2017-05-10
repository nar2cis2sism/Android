package demo.aidl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import demo.aidl.LocalService.LocalBinder;


public class LocalBindActivity extends Activity {
	
	private LocalService mService;								//本地调用接口
	private ServiceConnection conn = new ServiceConnection() {	//本地调用连接

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
		    LocalBinder binder = (LocalBinder) service;
		    mService = binder.getService();
			System.out.println("bind success");
			
			mService.execute();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		    mService = null;
			System.out.println("bind fail");
		}};
	
	@Override
	protected void onStart() {
	    super.onStart();
	    
        bindService(new Intent(this, LocalService.class), conn, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
	    super.onStop();
	    
	    unbindService(conn);
	}
}