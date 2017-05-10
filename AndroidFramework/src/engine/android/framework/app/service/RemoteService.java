package engine.android.framework.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

/**
 * 远程服务的抽象基类，方便子类自定义消息
 * 
 * @author Daimon
 */
public abstract class RemoteService extends Service {
	
	private final Messenger messenger = new Messenger(new RemoteHandler());

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}
	
	public class RemoteHandler extends Handler {
	    
	    @Override
	    public void handleMessage(Message msg) {
	        RemoteService.this.handleMessage(msg);
	    }
	}
	
	protected abstract void handleMessage(Message msg);
}