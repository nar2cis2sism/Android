package engine.android.plugin.proxy;

import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Intent;

import java.lang.reflect.Method;

import engine.android.plugin.PluginEnvironment;
import engine.android.plugin.proxy.component.PluginService;
import engine.android.plugin.util.PluginProxy;

public class ActivityManagerService extends PluginProxy<IActivityManager> {
    
    private final PluginEnvironment environment;

    public ActivityManagerService(IActivityManager obj, PluginEnvironment environment) {
        super(obj);
        this.environment = environment;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        String name = method.getName();
        if ("startActivity".equals(name))
        {
            startActivity(args);
        }
        else if ("startActivities".equals(name))
        {
            startActivities(args);
        }
        else if ("startService".equals(name))
        {
            ComponentName cn = startService(args);
            if (isPluginComponent(cn))
            {
                super.invoke(proxy, method, args);
                return cn;
            }
        }
        else if ("stopService".equals(name))
        {
            ComponentName cn = stopService(args);
            if (isPluginComponent(cn))
            {
                return PluginService.stopService(cn);
            }
        }
        else if ("bindService".equals(name))
        {
            bindService(args);
        }
        else if ("registerReceiver".equals(name))
        {
            registerReceiver(args);
        }
        
        return super.invoke(proxy, method, args);
    }
    
    private void startActivity(Object[] args) {
        Intent intent = (Intent) args[1];
        String resolvedType = (String) args[2];
        
        environment.interceptActivityIntent(intent, resolvedType);
    }
    
    private void startActivities(Object[] args) {
        Intent[] intents = (Intent[]) args[1];
        String[] resolvedTypes = (String[]) args[2];
        if (intents.length != resolvedTypes.length)
        {
            throw new IllegalArgumentException("intents are length different than resolvedTypes");
        }
        
        for (int i = 0; i < intents.length; i++)
        {
            environment.interceptActivityIntent(intents[i], resolvedTypes[i]);
        }
    }
    
    private ComponentName startService(Object[] args) {
        Intent service = (Intent) args[1];
        String resolvedType = (String) args[2];
        
        return environment.interceptServiceIntent(service, resolvedType);
    }
    
    private ComponentName stopService(Object[] args) {
        Intent service = (Intent) args[1];
        String resolvedType = (String) args[2];

        return environment.interceptServiceIntent(service, resolvedType);
    }
    
    private void bindService(Object[] args) {
        Intent intent = (Intent) args[2];
        String resolvedType = (String) args[3];

        environment.interceptServiceIntent(intent, resolvedType);
    }

    private void registerReceiver(Object[] args) {
        String callerPackage = (String) args[1];
        
        args[1] = environment.interceptReceiverPackage(callerPackage);
    }
    
    private boolean isPluginComponent(ComponentName name) {
        return name != null && !name.getPackageName().equals(environment.getPackage());
    }
}