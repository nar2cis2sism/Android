package engine.android.framework.app.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import engine.android.core.util.LogFactory.LOG;

/**
 * 远程服务的调用实现
 * 
 * @author Daimon
 */
public class RemoteServiceBinder<Service extends RemoteService> {
    
    private final Context context;
    
    private Messenger messenger;
    private final ServiceConnection conn = new ServiceConnection() {
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            try {
                messenger = new Messenger(binder);
                LOG.log(name.flattenToShortString());
            } catch (Exception e) {
                LOG.log(e);
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            messenger = null;
            LOG.log(name.flattenToShortString());
        }
    };
    private Intent serviceIntent;
    
    public RemoteServiceBinder(Context context) {
        this.context = context;
    }
    
    public void bindService(Class<Service> service) {
        if (serviceIntent == null)
        {
            serviceIntent = new Intent(context, service);
            context.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    }
    
    public void bindAndStartService(Class<Service> service) {
        if (serviceIntent == null)
        {
            serviceIntent = new Intent(context, service);
            context.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
            context.startService(serviceIntent);
        }
    }
    
    public void unbindService() {
        if (serviceIntent != null)
        {
            context.unbindService(conn);
            serviceIntent = null;
        }
    }
    
    public void unbindAndStopService() {
        if (serviceIntent != null)
        {
            context.unbindService(conn);
            context.stopService(serviceIntent);
            serviceIntent = null;
        }
    }
    
    public final void sendMessage(Message message) throws Exception {
        messenger.send(message);
    }
}