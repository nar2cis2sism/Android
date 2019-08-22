package engine.android.util.service;

import static engine.android.core.util.LogFactory.LOG.log;

import engine.android.util.service.LocalService.LocalBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * 本地服务的调用实现
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class LocalServiceBinder<Service extends LocalService> {
    
    private final Context context;
    
    private Service service;
    private final ServiceConnection conn = new ServiceConnection() {
        
        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            try {
                service = (Service) ((LocalBinder) binder).getService();
                log(name.flattenToShortString());
            } catch (Exception e) {
                log(e);
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
            log(name.flattenToShortString());
        }
    };
    private Intent serviceIntent;
    
    public LocalServiceBinder(Context context) {
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
    
    public final Service getService() {
        return service;
    }
}