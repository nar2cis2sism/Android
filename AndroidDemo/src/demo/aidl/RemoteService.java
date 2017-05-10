package demo.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class RemoteService extends Service {
    
    public static final int MSG_REMOTE = 1;
    
    private final Messenger messenger = new Messenger(new RemoteHandler());

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}
	
	public class RemoteHandler extends Handler {
	    
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
                case MSG_REMOTE:
                    System.out.println("remote command");
                    break;
            }
	    }
	}
}