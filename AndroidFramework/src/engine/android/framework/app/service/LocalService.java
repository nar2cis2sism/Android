package engine.android.framework.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * 本地服务的抽象基类，方便子类自定义函数
 * 
 * @author Daimon
 */
public abstract class LocalService extends Service {
	
	private final IBinder binder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class LocalBinder extends Binder {
	    
	    public LocalService getService() {
	        return LocalService.this;
	    }
	}
}