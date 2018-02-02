package engine.android.plugin;

import android.app.Application;
import android.content.Context;
import android.os.FileUtils;
import android.util.Singleton;

import engine.android.core.ApplicationManager;
import engine.android.util.extra.MyThreadFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 对外提供的插件访问类
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2014
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class Plugin {
    
    private static final PluginEnvironment environment = new PluginEnvironment();
    
    private static final Singleton<PluginManager> manager
    = new Singleton<PluginManager>() {
        
        @Override
        protected PluginManager create() {
            ensureInit();
            return new PluginManager(environment);
        }
    };
    
    /**
     * 初始化，一般在{@link Application#onCreate()}里调用
     */
    public static void init() {
        ApplicationManager.ensureCallMethodOnMainThread();
        try {
            environment.onInit();
            environment.log("初始化完毕");
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
    
    private static PluginManager getManager() {
        return manager.get();
    }
    
    /**
     * 从assets目录中加载插件包
     * 
     * @param assetsPath 插件包在assets目录下的路径
     * @return Null表示插件未加载成功
     */
    public static Plugin loadPluginFromAssets(String assetsPath) {
        PluginManager manager = getManager();
        
        try {
            String apkName = new File(assetsPath).getName();
            File apkFile = new File(environment.getPluginDir(), apkName);
            if (!apkFile.exists())
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
            environment.log(e);
        }
        
        return null;
    }
    
    /**
     * 从APK文件中加载插件
     * 
     * @param apkFile APK文件包
     * @return Null表示插件未加载成功
     */
    public static Plugin loadPluginFromFile(File apkFile) {
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
            environment.log(e);
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
    
    public <T> Class<T> loadClass(String className) throws Exception {
        return (Class<T>) Class.forName(className, true, getApplication().getClassLoader());
    }

    /******************************* 插件之间通讯机制 *******************************/
    
    private static final ExecutorService executor
    = Executors.newCachedThreadPool(new MyThreadFactory("插件通讯"));
    
    private HashMap<String, PluginAction> actionMap;
    
    void registerAction(String action, PluginAction call) {
        if (actionMap == null) actionMap = new HashMap<String, PluginAction>();
        actionMap.put(action, call);
    }
    
    public <IN, OUT> OUT callAction(String action, IN param) throws Exception {
        if (actionMap != null)
        {
            PluginAction call = actionMap.get(action);
            if (call != null)
            {
                return (OUT) call.doAction(param);
            }
        }
        
        throw new Exception("No Action:" + action + " is registered.");
    }
    
    public <IN, OUT> void callActionAsync(String action, IN param, Callback<OUT> callback) {
        executor.execute(new ActionRunnable<IN, OUT>(action, param, callback));
    }
    
    private class ActionRunnable<IN, OUT> implements Runnable {
        
        private final String action;
        private final IN param;
        private final Callback<OUT> callback;
        
        public ActionRunnable(String action, IN param, Callback<OUT> callback) {
            this.action = action;
            this.param = param;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                callback.doResult((OUT) callAction(action, param));
            } catch (Exception e) {
                callback.doError(e);
            }
        }
    }
    
    public interface PluginAction<IN, OUT> {
        
        OUT doAction(IN param) throws Exception;
    }
    
    public interface Callback<OUT> {
        
        void doResult(OUT result);
        
        void doError(Exception e);
    }
    
    public static <IN, OUT> void registerAction(Context context, String action, PluginAction<IN, OUT> call) {
        getPlugin(context.getPackageName()).registerAction(action, call);
    }
}