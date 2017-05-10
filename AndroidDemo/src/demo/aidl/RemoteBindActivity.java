package demo.aidl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class RemoteBindActivity extends Activity {
	
	private Messenger messenger;								//远程调用接口
	private ServiceConnection conn = new ServiceConnection(){	//远程调用连接

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
		    messenger = new Messenger(service);
			System.out.println("bind success");
			
			try {
                messenger.send(Message.obtain(null, RemoteService.MSG_REMOTE));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		    messenger = null;
			System.out.println("bind fail");
		}};
	
	@Override
	protected void onStart() {
	    super.onStart();
	    
        bindService(new Intent(this, RemoteService.class), conn, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
	    super.onStop();
	    
	    unbindService(conn);
	}
}