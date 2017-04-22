package engine.android.plugin;

import static engine.android.plugin.PluginEnvironment.log;

import android.app.Application;
import android.os.FileUtils;
import android.util.Singleton;

import java.io.File;
import java.io.InputStream;

import engine.android.core.ApplicationManager;

/**
 * 对外提供的插件访问类
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2014
 */
public final class Plugin {
    
    private static final PluginEnvironment environment = new PluginEnvironment();
    
    /**
     * 初始化，一般在{@link Application#onCreate()}里调用
     */
    public static void init() {
        ApplicationManager.ensureCallMethodOnMainThread();
        try {
            environment.onInit();
            log("初始化完毕");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void ensureInit() {
        if (environment.getActivityThread() == null)
        {
            throw new RuntimeException("You must call Plugin.init() first.");
        }
    }
    
    private static final Singleton<PluginManager> manager
    = new Singleton<PluginManager>() {
        
        @Override
        protected PluginManager create() {
            ensureInit();
            return new PluginManager(environment);
        }
    };
    
    private static PluginManager getManager() {
        return manager.get();
    }
    
    /**
     * 从assets目录中加载插件包
     * 
     * @param assetsPath 插件包在assets目录下的路径
     * @param forceReload 强制覆盖重新加载
     * @return 插件包名(返回Null表示插件未加载成功)
     */
    public static String loadPluginFromAssets(String assetsPath, boolean forceReload) {
        PluginManager manager = getManager();
        
        try {
            String apkName = new File(assetsPath).getName();
            File apkFile = new File(environment.getPluginDir(), apkName);
            if (forceReload || !apkFile.exists())
            {
                InputStream is = null;
                try {
                    is = environment.getApplication().getAssets().open(assetsPath);
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
            
            return manager.loadPlugin(apkFile);
        } catch (Exception e) {
            log(e);
        }
        
        return null;
    }
    
    /**
     * 从APK文件中加载插件
     * 
     * @param apkFile APK文件包
     * @return 插件包名(返回Null表示插件未加载成功)
     */
    public static String loadPluginFromFile(File apkFile) {
        PluginManager manager = getManager();
        
        try {
            File pluginFile = new File(environment.getPluginDir(), apkFile.getName());
            if (!pluginFile.exists() || pluginFile.lastModified() != apkFile.lastModified())
            {
                if (!FileUtils.copyFile(apkFile, pluginFile))
                {
                    throw new Exception("Failed to copy apk file to:" + pluginFile.getPath());
                }
                
                pluginFile.setLastModified(apkFile.lastModified());
            }
            
            return manager.loadPlugin(pluginFile);
        } catch (Exception e) {
            log(e);
        }
        
        return null;
    }

    /**
     * 根据包名获取插件，如插件未成功加载会抛出异常
     */
    public static Plugin getPlugin(String packageName) {
        Plugin plugin = getManager().getPlugin(packageName);
        if (plugin != null)
        {
            return plugin;
        }

        throw new RuntimeException("Plugin is not loaded:" + packageName);
    }
    
    /**
     * 判断插件是否可用
     * 
     * @param packageName 插件的包名
     */
    public static boolean isPluginned(String packageName) {
        return getManager().getPlugin(packageName) != null;
    }
    
    /**
     * 获取宿主环境
     */
    public static Application getHostApplication() {
        ensureInit();
        return environment.getApplication();
    }
    
    /**
     * 打印日志调试
     */
    public static void setDebuggable(boolean debuggable) {
        PluginEnvironment.DEBUGGABLE = debuggable;
    }

    /******************************* 华丽丽的分割线 *******************************/
    
    private final PluginLoader loader;
    
    Plugin(PluginLoader loader) {
        this.loader = loader;
    }
    
    /**
     * 获取插件环境
     */
    public Application getApplication() {
        return loader.app;
    }
    
    @SuppressWarnings("unchecked")
    public <T> Class<T> loadClass(String className) throws Exception {
        return (Class<T>) Class.forName(className, true, getApplication().getClassLoader());
    }
}