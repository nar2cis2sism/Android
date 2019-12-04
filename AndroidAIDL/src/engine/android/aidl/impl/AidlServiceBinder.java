package engine.android.aidl.impl;

import static engine.android.core.util.LogFactory.LOG.log;

import engine.android.aidl.Action;
import engine.android.aidl.Event;
import engine.android.aidl.IAidl;
import engine.android.aidl.ICallback;
import engine.android.aidl.impl.remote.AidlService;
import engine.android.core.extra.EventBus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class AidlServiceBinder<Service extends AidlService> {
    
    private final Context context;

    private final ICallback.Stub stub = new ICallback.Stub() {

        @Override
        public void handle(Event event) throws RemoteException {
            log(event.action + "|" + event.status + "|" + event.param);
            EventBus.getDefault().post(event);
        }
    };
    
    private IAidl aidl;
    private final ServiceConnection conn = new ServiceConnection() {
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            try {
                aidl = IAidl.Stub.asInterface(binder);
                log(name.flattenToShortString());
                
                aidl.register(ICallback.Stub.asInterface(stub));
            } catch (Exception e) {
                log(e);
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (aidl != null)
            {
                try {
                    aidl.unregister();
                } catch (RemoteException e) {
                    log(e);
                }
                
                aidl = null;
            }
            
            log(name.flattenToShortString());
        }
    };
    private Intent serviceIntent;
    
    public AidlServiceBinder(Context context) {
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

    public void sendAction(Action action) {
        if (aidl == null)
        {
            log("sendAction:" + action, "Service未连接");
            return;
        }
    
        try {
            aidl.sendAction(action);
        } catch (Exception e) {
            log("sendAction:" + action, e);
        }
    }

    public void cancelAction(String action) {
        if (aidl == null)
        {
            log("cancelAction:" + action, "Service未连接");
            return;
        }
        
        try {
            aidl.cancelAction(action);
        } catch (Exception e) {
            log("cancelAction:" + action, e);
        }
    }
}