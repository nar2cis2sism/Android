package engine.android.plugin;

import android.app.ActivityThread;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageUserState;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Process;
import android.os.UserHandle;

import engine.android.plugin.proxy.component.PluginActivity;
import engine.android.plugin.proxy.component.PluginService;

public class Plugin {
    
    private static final Application app = ActivityThread.currentApplication();
    
    private static final UserHandle user
    = new UserHandle(UserHandle.getUserId(Process.myUid()));
    
    private static final PackageUserState state = new PackageUserState();
    
    private static final String PACKAGE = app.getPackageName();
    
    private static final ComponentName PLUGIN_ACTIVITY
    = new ComponentName(PACKAGE, PluginActivity.class.getName());
    
    private static final ComponentName PLUGIN_SERVICE
    = new ComponentName(PACKAGE, PluginService.class.getName());
    
    /**
     * <P>Type: ComponentName</P>
     */
    private static final String EXTRA_COMPONENT_NAME = "plugin_component";

    // The flags that are set for all calls we make to the package manager.
    static final int STOCK_PM_FLAGS = PackageManager.GET_SHARED_LIBRARY_FILES;
    
    public static Context getContext() {
        return app;
    }
    
    public static String getPackage() {
        return PACKAGE;
    }
    
    public static int getUserId() {
        return user.getIdentifier();
    }
    
    public static PackageUserState getUserState() {
        return state;
    }
    
    public static void interceptActivityIntent(Intent intent, String resolvedType) {
        interceptIntent(intent, resolvedType, true);
    }
    
    public static ComponentName interceptServiceIntent(Intent service, String resolvedType) {
        return interceptIntent(service, resolvedType, false);
    }
    
    private static ComponentName interceptIntent(Intent intent, String resolvedType,
            boolean isActivityOrService) {
        PluginLog.log(PluginLog.getCallerStackFrame(), 
                "before:" + intent);
        
        ComponentName component = intent.getComponent();
        if (component == null)
        {
            if (isActivityOrService)
            {
                resolveActivity(intent, resolvedType);
            }
            else
            {
                resolveService(intent, resolvedType);
            }
            
            component = intent.getComponent();
            PluginLog.log(PluginLog.getCallerStackFrame(), 
                    "resolve:" + intent);
        }
        
        interceptIntent(intent, resolvedType, isActivityOrService
                ? PLUGIN_ACTIVITY : PLUGIN_SERVICE);
        
        PluginLog.log(PluginLog.getCallerStackFrame(), 
                "after:" + intent);
        return component;
    }
    
    private static ActivityInfo resolveActivity(Intent intent, String resolvedType) {
        // Collect information about the target of the Intent.
        ResolveInfo rInfo = PluginManager.getPackageManager().resolveIntent(
                intent, 
                resolvedType, 
                STOCK_PM_FLAGS, 
                getUserId());
        ActivityInfo aInfo = rInfo != null ? rInfo.activityInfo : null;
        if (aInfo != null)
        {
            // Store the found target back into the intent, because now that
            // we have it we never want to do this again.  For example, if the
            // user navigates back to this point in the history, we should
            // always restart the exact same activity.
            intent.setComponent(new ComponentName(
                    aInfo.applicationInfo.packageName, aInfo.name));
        }
        
        return aInfo;
    }
    
    private static ServiceInfo resolveService(Intent service, String resolvedType) {
        // Collect information about the target of the Intent.
        ResolveInfo rInfo = PluginManager.getPackageManager().resolveService(
                service, 
                resolvedType, 
                STOCK_PM_FLAGS, 
                getUserId());
        ServiceInfo sInfo = rInfo != null ? rInfo.serviceInfo : null;
        if (sInfo != null)
        {
            service.setComponent(new ComponentName(
                    sInfo.applicationInfo.packageName, sInfo.name));
        }
        
        return sInfo;
    }
    
    private static void interceptIntent(Intent intent, String resolvedType, 
            ComponentName proxyComponent) {
        ComponentName component = intent.getComponent();
        if (component != null)
        {
            String pkg = component.getPackageName();
            if (!PACKAGE.equals(pkg)
            &&  PluginManager.getInstance().isPluginned(pkg))
            {
                // 如果启动其它包中的组件，将其替换成代理组件
                intent.setComponent(proxyComponent);
                // 同时将真正需要启动的组件名称作为参数传进去
                intent.putExtra(EXTRA_COMPONENT_NAME, component);
            }
        }
    }
    
    public static String interceptReceiverPackage(String callerPackage) {
        return PACKAGE;
    }

    public static ComponentName handleIntent(Intent intent) {
        PluginLog.log(PluginLog.getCallerStackFrame(), 
                "handleIntent before:" + intent);
        ComponentName component = null;
        if (intent != null && intent.hasExtra(EXTRA_COMPONENT_NAME))
        {
            component = intent.getParcelableExtra(EXTRA_COMPONENT_NAME);
            intent.removeExtra(EXTRA_COMPONENT_NAME);
            // 解析出真正的组件进行替换
            intent.setComponent(component);
        }
    
        PluginLog.log(PluginLog.getCallerStackFrame(), 
                "handleIntent after:" + intent);
        return component;
    }
    
    public static ActivityInfo resolveActivity(ComponentName component) {
        return PluginManager.getPackageManager().getActivityInfo(
                component, STOCK_PM_FLAGS, getUserId());
    }

    public static PluginLoader getPluginLoader(String packageName) throws Exception {
        PluginLoader loader = PluginManager.getInstance().getPluginLoader(packageName);
        if (loader == null || !loader.isPluginned())
        {
            throw new Exception("Plugin package is not loaded:" + packageName);
        }
        
        return loader;
    }
}