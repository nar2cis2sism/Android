package engine.android.plugin.proxy.component;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.LoadedApk;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import engine.android.plugin.Plugin;
import engine.android.plugin.PluginLog;
import engine.android.plugin.util.ReflectObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class PluginService extends Service {
    
    private static WeakReference<PluginService> instance;
    
    private final HashMap<ComponentName, Service> serviceMap
    = new HashMap<ComponentName, Service>();
    
    @Override
    public void onCreate() {
        instance = new WeakReference<PluginService>(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Service service = retrieveService(intent, true);
        if (service != null)
        {
            return service.onStartCommand(intent, flags, startId);
        }
        
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        Service service = retrieveService(intent, true);
        if (service != null)
        {
            service.onStart(intent, startId);
        }
    }
    
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Service service = retrieveService(rootIntent, false);
        if (service != null)
        {
            service.onTaskRemoved(rootIntent);
        }
    }

    private Service retrieveService(Intent intent, boolean createIfNeeded) {
        ComponentName component = Plugin.handleIntent(intent);
        if (component == null)
        {
            return null;
        }
        
        Service service = serviceMap.get(component);
        if (service == null && createIfNeeded)
        {
            try {
                service = handleCreateService(component);
                serviceMap.put(component, service);
            } catch (Exception e) {
                throw new RuntimeException(
                    "Unable to create service " + component
                    + ": " + e.toString(), e);
            }
        }
        
        return service;
    }
    
    private Service handleCreateService(ComponentName component) throws Exception {
        LoadedApk packageInfo = Plugin.getPluginLoader(component.getPackageName())
                .getLoadedApk();

        String name = component.getClassName();
        java.lang.ClassLoader cl = packageInfo.getClassLoader();
        Service service = (Service) cl.loadClass(name).newInstance();
        
        ActivityThread currentActivityThread = ActivityThread.currentActivityThread();
        Context context = createPackageContext(component.getPackageName(), Context.CONTEXT_INCLUDE_CODE);
        ReflectObject ContextImplRef = new ReflectObject(context);
        ContextImplRef.invoke(ContextImplRef.getMethod(
                "setOuterContext", Context.class), service);

        Application app = packageInfo.makeApplication(false, 
                currentActivityThread.getInstrumentation());
        service.attach(context, currentActivityThread, name, null, app,
                ActivityManagerNative.getDefault());
        service.onCreate();
        return service;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // 由于需要之前的数据作为索引获取ServiceConnection，这里不能修改Intent的值
        Intent copyIntent = new Intent(intent);
        Service service = retrieveService(copyIntent, true);
        if (service != null)
        {
            return service.onBind(copyIntent);
        }
        
        return null;
    }
    
    @Override
    public void onRebind(Intent intent) {
        // 由于需要之前的数据作为索引获取ServiceConnection，这里不能修改Intent的值
        Intent copyIntent = new Intent(intent);
        Service service = retrieveService(copyIntent, false);
        if (service != null)
        {
            service.onRebind(copyIntent);
        }
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        // 由于需要之前的数据作为索引获取ServiceConnection，这里不能修改Intent的值
        Intent copyIntent = new Intent(intent);
        Service service = retrieveService(copyIntent, false);
        if (service != null)
        {
            return service.onUnbind(copyIntent);
        }
        
        return false;
    }
    
    @Override
    public void onDestroy() {
        instance = null;
    }
    
    public static int stopService(ComponentName component) {
        int res = 0;
        if (instance != null)
        {
            PluginService service = instance.get();
            if (service != null)
            {
                try {
                    res = service.handleStopService(component) ? 1 : 0;
                } catch (Exception e) {
                    PluginLog.log(e);
                }
            }
        }
        
        return res;
    }
    
    private boolean handleStopService(ComponentName component) throws Exception {
        boolean handle = false;
        
        Service service = serviceMap.remove(component);
        if (service != null)
        {
            service.onDestroy();

            Plugin.getPluginLoader(component.getPackageName()).getLoadedApk()
            .removeContextRegistrations(service, component.getClassName(), "Service");
            
            handle = true;
        }
        
        if (serviceMap.isEmpty())
        {
            // 如果没有活动的service，那么代理service也没有存在的必要了
            stopSelf();
        }
        
        return handle;
    }
}