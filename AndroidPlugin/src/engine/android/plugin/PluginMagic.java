package engine.android.plugin;

import android.app.Application;
import android.util.Singleton;

import engine.android.core.ApplicationManager;

/**
 * 对外提供的插件访问类
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2014
 */
public final class PluginMagic {
    
    private static final PluginEnvironment environment = new PluginEnvironment();
    
    /**
     * 初始化，一般在{@link Application#onCreate()}里调用
     */
    public static void init() {
        ApplicationManager.ensureCallMethodOnMainThread();
        try {
            environment.onInit();
            environment.log("插件框架初始化", "成功");
        } catch (Exception e) {
            environment.log("插件框架初始化", e);
        }
    }
    
    private static final Singleton<PluginManager> manager
    = new Singleton<PluginManager>() {
        
        @Override
        protected PluginManager create() {
            return new PluginManager(environment);
        }
    };
    
    /**
     * 获取插件管理器加载插件
     */
    public static PluginManager getManager() {
        return manager.get();
    }

    public static PluginLoader getLoader(String packageName) throws Exception {
        PluginLoader loader = getManager().getPluginLoader(packageName);
        if (loader == null || !loader.isPluginned())
        {
            throw new Exception("Plugin is not loaded:" + packageName);
        }
        
        return loader;
    }
    
    /**
     * 判断插件是否可用
     * 
     * @param packageName 插件的包名
     */
    public static boolean isPluginned(String packageName) {
        PluginLoader loader = getManager().getPluginLoader(packageName);
        return loader != null && loader.isPluginned();
    }
    
    /**
     * 打印日志调试
     */
    public static void setDebuggable(boolean debuggable) {
        environment.DEBUGGABLE = debuggable;
    }
}