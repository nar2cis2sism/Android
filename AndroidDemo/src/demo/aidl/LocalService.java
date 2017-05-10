package demo.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class LocalService extends Service {
	
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
	
	public void execute() {}
}