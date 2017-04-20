package engine.android.plugin;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageUserState;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Process;
import android.os.UserHandle;
import android.util.Singleton;

import engine.android.core.ApplicationManager;
import engine.android.core.util.LogFactory;
import engine.android.core.util.LogFactory.LOG;
import engine.android.plugin.proxy.ActivityManagerService;
import engine.android.plugin.proxy.PackageManagerService;
import engine.android.plugin.proxy.PluginHandlerCallback;
import engine.android.plugin.proxy.component.PluginActivity;
import engine.android.plugin.proxy.component.PluginService;
import engine.android.plugin.util.ApkLoader;
import engine.android.plugin.util.PluginProxy;
import engine.android.util.ReflectObject;

/**
 * Provide global environment for all framework.
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2014
 */
public class PluginEnvironment {
    
    /**
     * <P>Type: ComponentName</P>
     */
    private static final String EXTRA_COMPONENT_NAME = "plugin_component";

    // The flags that are set for all calls we make to the package manager.
    public final int STOCK_PM_FLAGS = PackageManager.GET_SHARED_LIBRARY_FILES;
    
    public ActivityThread activityThread;
    private Application app;
    private UserHandle user;
    private PackageUserState state;
    public String PACKAGE;
    private ComponentName PLUGIN_ACTIVITY;
    private ComponentName PLUGIN_SERVICE;
    
    public PackageManagerService pm;
    public ActivityManagerService am;
    public Handler h;
    
    private boolean prepared;
    
    PluginEnvironment() {}
    
    public void onInit() throws Exception {
        activityThread = ActivityThread.currentActivityThread();
        app = ActivityThread.currentApplication();
        user = new UserHandle(UserHandle.getUserId(Process.myUid()));
        state = new PackageUserState();
        PACKAGE = app.getPackageName();
        PLUGIN_ACTIVITY = new ComponentName(PACKAGE, PluginActivity.class.getName());
        PLUGIN_SERVICE = new ComponentName(PACKAGE, PluginService.class.getName());
        
        pm = new PackageManagerService(ActivityThread.getPackageManager(), this);
        am = new ActivityManagerService(ActivityManagerNative.getDefault(), this);
        h = (Handler) ApkLoader.getActivityThreadRef().invoke("getHandler");
    }
    
    public void prepare() throws Exception {
        if (!prepared)
        {
            hookPackageManager();
            hookActivityManager();
            hookHandler();
            prepared = true;
            log("插件框架准备", "成功");
        }
    }
    
    /**
     * 替换PackageManagerService实现访问插件包的信息
     */
    private void hookPackageManager() throws Exception {
        ReflectObject.setStatic(ReflectObject.getField(ActivityThread.class, "sPackageManager"), 
                PluginProxy.getProxy(IPackageManager.class, pm));
    }

    /**
     * 启动一个未在AndroidManifest.xml文件中注册的组件是不可能的，因为系统会在PackageManagerService中查询，
     * 而这个类是通过Binder机制远程调用的，客户端无法访问。
     * 要实现这个功能，只能绕过系统的这套检测机制，通过阅读源码，我发现组件真正的启动是交给{@link IActivityManager}处理的，
     * 那么这里我将它替换成自己的实现，这样就可以进行拦截，并伪装成注册过的代理组件来欺骗系统以达到目的。
     */
    private void hookActivityManager() throws Exception {
        ReflectObject.setStatic(ReflectObject.getField(ActivityManagerNative.class, "gDefault"),
                new Singleton<IActivityManager>() {

            @Override
            protected IActivityManager create() {
                return PluginProxy.getProxy(IActivityManager.class, am);
            }
        });
    }
    
    /**
     * 我们知道，动态加载的组件本身是不具有生命周期的，所以一般插件化的方式是启动一个代理组件，通过反射的方式来操纵，
     * 但是这种方式会降低效率，而且很费劲。
     * 通过阅读源码，我发现系统回调事件处理的入口为{@link ActivityThread.H}类，那么这里我将其替换成自己的实现，
     * 这样就可以在代理组件启动之前将其拦截，并可以提取出真实的组件信息
     */
    private void hookHandler() throws Exception {
        // 由于H是内部类，无法继承，所以添加一个回调来进行拦截
        ReflectObject hRef = new ReflectObject(h);
        hRef.set("mCallback", new PluginHandlerCallback(this));
    }
    
    static
    {
        if (!ApplicationManager.isDebuggable())
        {
            LogFactory.addLogFile(PluginEnvironment.class, "plugin.txt");
        }
    }
    
    static boolean DEBUGGABLE = true;
    
    public static void log(Object message) {
        if (DEBUGGABLE) LOG.log("插件", message);
    }
    
    public static void log(String tag, Object message) {
        if (DEBUGGABLE) LOG.log(tag, message);
    }
    
    public Context getContext() {
        return app;
    }
    
    public int getUserId() {
        return user.getIdentifier();
    }
    
    public PackageUserState getState() {
        return state;
    }
    
    public ComponentName interceptActivityIntent(Intent intent, String resolvedType) {
        return interceptIntent(intent, resolvedType, true);
    }
    
    public ComponentName interceptServiceIntent(Intent service, String resolvedType) {
        return interceptIntent(service, resolvedType, false);
    }
    
    private ComponentName interceptIntent(Intent intent, String resolvedType,
            boolean isActivityOrService) {
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
        }
        
        interceptIntent(intent, resolvedType, isActivityOrService
                ? PLUGIN_ACTIVITY : PLUGIN_SERVICE);
        return component;
    }
    
    private ActivityInfo resolveActivity(Intent intent, String resolvedType) {
        // Collect information about the target of the Intent.
        ResolveInfo rInfo = pm.resolveIntent(
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
                    aInfo.packageName, aInfo.name));
        }
        
        return aInfo;
    }
    
    private ServiceInfo resolveService(Intent service, String resolvedType) {
        // Collect information about the target of the Intent.
        ResolveInfo rInfo = pm.resolveService(
                service, 
                resolvedType, 
                STOCK_PM_FLAGS, 
                getUserId());
        ServiceInfo sInfo = rInfo != null ? rInfo.serviceInfo : null;
        if (sInfo != null)
        {
            service.setComponent(new ComponentName(
                    sInfo.packageName, sInfo.name));
        }
        
        return sInfo;
    }
    
    private void interceptIntent(Intent intent, String resolvedType, 
            ComponentName proxyComponent) {
        ComponentName component = intent.getComponent();
        if (component != null)
        {
            String pkg = component.getPackageName();
            if (!PACKAGE.equals(pkg)
            &&  PluginMagic.isPluginned(pkg))
            {
                // 如果启动其它包中的组件，将其替换成代理组件
                intent.setComponent(proxyComponent);
                // 同时将真正需要启动的组件名称作为参数传进去
                intent.putExtra(EXTRA_COMPONENT_NAME, component);
            }
        }
    }
    
    public String interceptReceiverPackage(String callerPackage) {
        return PACKAGE;
    }

    public ComponentName handleIntent(Intent intent) {
        ComponentName component = null;
        if (intent != null && intent.hasExtra(EXTRA_COMPONENT_NAME))
        {
            component = intent.getParcelableExtra(EXTRA_COMPONENT_NAME);
            intent.removeExtra(EXTRA_COMPONENT_NAME);
            // 解析出真正的组件进行替换
            intent.setComponent(component);
        }
    
        return component;
    }
    
    public ActivityInfo resolveActivity(ComponentName component) {
        return pm.getActivityInfo(component, STOCK_PM_FLAGS, getUserId());
    }
}