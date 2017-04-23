package engine.android.plugin;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.os.FileUtils;
import android.os.Handler;
import android.util.Singleton;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import engine.android.plugin.proxy.ActivityManagerService;
import engine.android.plugin.proxy.PackageManagerService;
import engine.android.plugin.proxy.PluginHandlerCallback;
import engine.android.plugin.util.PluginProxy;
import engine.android.util.ReflectObject;

public class PluginManager {
    
    private static final Singleton<PluginManager> instance
    = new Singleton<PluginManager>() {
        
        @Override
        protected PluginManager create() {
            return new PluginManager(Plugin.getContext());
        }
    };
    
    public static final PluginManager getInstance() {
        if (!init)
        {
            throw new RuntimeException("You should call init() first.");
        }
        
        return instance.get();
    }
    
    private static PackageManagerService pm;
    private static ActivityManagerService am;
    private static Handler h;
    
    private static boolean init;
    
    public static final boolean init() {
        try {
            hookPackageManager();
            hookActivityManager();
            hookHandler();
            
            return init = true;
        } catch (Throwable e) {
            PluginLog.log(e);
        }
        
        return false;
    }
    
    /**
     * 替换PackageManagerService实现访问插件包的信息
     */
    private static void hookPackageManager() throws Exception {
        pm = new PackageManagerService(ActivityThread.getPackageManager());
        ReflectObject.setStatic(ReflectObject.getField(ActivityThread.class, "sPackageManager"), 
                PluginProxy.getProxy(IPackageManager.class, pm));
    }

    /**
     * 启动一个未在AndroidManifest.xml文件中注册的组件是不可能的，因为系统会在PackageManagerService中查询，
     * 而这个类是通过Binder机制远程调用的，客户端无法访问。
     * 要实现这个功能，只能绕过系统的这套检测机制，通过阅读源码，我发现组件真正的启动是交给{@link IActivityManager}处理的，
     * 那么这里我将它替换成自己的实现，这样就可以进行拦截，并伪装成注册过的代理组件来欺骗系统以达到目的。
     */
    private static void hookActivityManager() throws Exception {
        am = new ActivityManagerService(ActivityManagerNative.getDefault());
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
     * 而通过上面的处理，系统能够启动我们注册的代理组件，但是这种方式会降低效率，而且很费劲。
     * 通过阅读源码，我发现系统回调事件处理的入口为{@link ActivityThread.H}类，那么这里我将其替换成自己的实现，
     * 这样就可以在代理组件启动之前将其拦截，并可以提取出真实的组件信息
     */
    private static void hookHandler() throws Exception {
        h = (Handler) PluginLoader.getActivityThreadRef().invoke("getHandler");
        // 由于H是内部类，无法继承，所以添加一个回调来进行拦截
        ReflectObject hRef = new ReflectObject(h);
        hRef.set("mCallback", new PluginHandlerCallback());
    }
    
    static PackageManagerService getPackageManager() {
        return pm;
    }

    /******************************* 华丽丽的分割线 *******************************/
    
    private final Context context;

    // This is where all plugin package goes.
    private final File pluginDir;

    // This is where all plugin application persistent data goes.
    private final File pluginDataDir;
    
    private final HashMap<String, PluginLoader> loadersByFile
    = new HashMap<String, PluginLoader>();
    private final HashMap<String, PluginLoader> loadersByPackage
    = new HashMap<String, PluginLoader>();
    
    private PluginManager(Context context) {
        pluginDir = (this.context = context).getDir("plugin", Context.MODE_PRIVATE);
        pluginDataDir = new File(pluginDir, "data");
    }
    
    /**
     * 从assets目录中加载插件包
     * 
     * @param assetsPath 插件包在assets目录下的路径
     * @param forceReload 强制覆盖重新加载
     * @return 插件包名
     */
    public String loadPluginFromAssets(String assetsPath, boolean forceReload)
            throws Exception {
        String apkName = new File(assetsPath).getName();
        File apkFile = new File(pluginDir, apkName);
        if (forceReload || !apkFile.exists())
        {
            InputStream is = null;
            try {
                is = context.getAssets().open(assetsPath);
                if (!FileUtils.copyToFile(is, apkFile))
                {
                    throw new Exception("Failed to copy assets file to:" + apkFile.getPath());
                }
            } finally {
                if (is != null)
                {
                    is.close();
                }
            }
        }
        
        return loadPlugin(apkFile);
    }
    
    /**
     * 从APK包中加载插件
     * 
     * @param apkFile APK文件包
     * @return 插件包名
     */
    public String loadPluginFromFile(File apkFile)
            throws Exception {
        File pluginFile = new File(pluginDir, apkFile.getName());
        if (!pluginFile.exists() || pluginFile.lastModified() != apkFile.lastModified())
        {
            if (!FileUtils.copyFile(apkFile, pluginFile))
            {
                throw new Exception("Failed to copy apk file to:" + pluginFile.getPath());
            }
            
            pluginFile.setLastModified(apkFile.lastModified());
        }
        
        return loadPlugin(pluginFile);
    }
    
    private String loadPlugin(File apkFile) throws Exception {
        String apkName = apkFile.getName();
        PluginLoader loader = loadersByFile.get(apkName);
        if (loader == null)
        {
            loader = new PluginLoader(apkFile, pm.scanPackage(apkFile));
            loadersByFile.put(apkName, loader);
        }
    
        String packageName = loader.getPackageInfo().packageName;
        if (loader.isPluginned())
        {
            return packageName;
        }
        
        loader.plugin();
        if (!loader.isPluginned())
        {
            throw new Exception("Failed to load plugin:" + apkFile.getPath());
        }
    
        loadersByPackage.put(packageName, loader);
        return packageName;
    }

    public File getPluginDir() {
        return pluginDir;
    }
    
    public File getPluginDataDir() {
        return pluginDataDir;
    }

    PluginLoader getPluginLoader(String packageName) {
        return loadersByPackage.get(packageName);
    }
    
    public boolean isPluginned(String packageName) {
        PluginLoader loader = getPluginLoader(packageName);
        return loader != null && loader.isPluginned();
    }
}