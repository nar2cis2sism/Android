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
        try {
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
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        return super.invoke(proxy, method, args);
    }
    
    private int findArg(Object[] args, Class<?> argType) {
        for (int i = 0; i < args.length; i++)
        {
            if (args[i] != null && args[i].getClass() == argType)
            {
                return i;
            }
        }
        
        return -1;
    }
    
    private void startActivity(Object[] args) {
        int index = findArg(args, Intent.class);
        
        Intent intent = (Intent) args[index];
        String resolvedType = (String) args[index + 1];
        
        environment.interceptActivityIntent(intent, resolvedType);
    }
    
    private void startActivities(Object[] args) {
        int index = findArg(args, Intent[].class);
        
        Intent[] intents = (Intent[]) args[index];
        String[] resolvedTypes = (String[]) args[index + 1];
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
        int index = findArg(args, Intent.class);
        
        Intent service = (Intent) args[index];
        String resolvedType = (String) args[index + 1];
        
        return environment.interceptServiceIntent(service, resolvedType);
    }
    
    private ComponentName stopService(Object[] args) {
        int index = findArg(args, Intent.class);
        
        Intent service = (Intent) args[index];
        String resolvedType = (String) args[index + 1];

        return environment.interceptServiceIntent(service, resolvedType);
    }
    
    private void bindService(Object[] args) {
        int index = findArg(args, Intent.class);

        Intent service = (Intent) args[index];
        String resolvedType = (String) args[index + 1];

        environment.interceptServiceIntent(service, resolvedType);
    }

    private void registerReceiver(Object[] args) {
        int index = findArg(args, String.class);
        
        String callerPackage = (String) args[index];
        
        args[index] = environment.interceptReceiverPackage(callerPackage);
    }
    
    private boolean isPluginComponent(ComponentName name) {
        return name != null && !name.getPackageName().equals(environment.getPackage());
    }
}