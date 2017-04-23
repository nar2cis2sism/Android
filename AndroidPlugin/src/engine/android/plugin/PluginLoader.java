package engine.android.plugin;

import android.app.ActivityThread;
import android.app.Application;
import android.app.LoadedApk;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import engine.android.plugin.proxy.PackageManagerService;
import engine.android.plugin.util.ApkLoader;
import engine.android.util.ReflectObject;

/**
 * Provide a mechanism to load plugin.
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2014
 */
public class PluginLoader extends ApkLoader {
    
    private final PackageParser.Package pkg;    // 这个是用来解析APK包的信息
    private LoadedApk loadedApk;                // 这个是用来加载APK包的资源
    
    private boolean isPluginned;

    PluginLoader(File apkFile, PackageParser.Package pkg) {
        super(apkFile);
        this.pkg = pkg;
    }
    
    /**
     * 装载插件
     */
    void plugin() {
        if (isPluginned)
        {
            return;
        }
        
        try {
            injectPackage();
            Application app = initApplication();
            registerReceiver();
            installProvider();
            // 需手动调用
            app.onCreate();
            
            isPluginned = true;
        } catch (Throwable e) {
            PluginLog.log(e);
            isPluginned = false;
        }
    }
    
    private void injectPackage() throws Exception {
        // 通过这种方式将插件包注入到ActivityThread的mPackages属性中，后续通过包名就可以正确加载插件包的资源
        loadedApk = ActivityThread.currentActivityThread().getPackageInfoNoCheck(
                PackageParser.generateApplicationInfo(
                        pkg, Plugin.STOCK_PM_FLAGS, Plugin.getUserState()),
                getCompatibilityInfo());

        // 替换组件类加载器为DexClassLoader，使动态加载代码具有组件生命周期
        ReflectObject loadedApkRef = new ReflectObject(loadedApk);
        loadedApkRef.set("mClassLoader", getClassLoader());
    }
    
    private Application initApplication() {
        // 必须实例化application,因为在receiver和provider中可能会用到
        return loadedApk.makeApplication(false, null);
    }
    
    private void registerReceiver() throws Exception {
        // 对于插件中定义的广播接收器我们采取动态注册的方式处理
        java.lang.ClassLoader cl = loadedApk.getClassLoader();
        for (PackageParser.Activity a : pkg.receivers)
        {
            BroadcastReceiver receiver = (BroadcastReceiver) 
                    cl.loadClass(a.className).newInstance();
            for (IntentFilter filter : a.intents)
            {
                Plugin.getContext().registerReceiver(receiver, filter);
            }
        }
    }
    
    private void installProvider() throws Exception {
        // 同样的进行动态安装
        ArrayList<ProviderInfo> list = new ArrayList<ProviderInfo>(3);
        for (PackageParser.Provider p : pkg.providers)
        {
            ProviderInfo info = PackageParser.generateProviderInfo(
                    p, 
                    Plugin.STOCK_PM_FLAGS | PackageManager.GET_URI_PERMISSION_PATTERNS,
                    Plugin.getUserState(), Plugin.getUserId());
            if (info != null)
            {
                list.add(info);
            }
        }
        
        if (list.isEmpty())
        {
            return;
        }
        
        Collections.sort(list, PackageManagerService.mProviderInitOrderSorter);
        
        getActivityThreadRef().invoke(
                getActivityThreadRef().getMethod("installContentProviders", 
                Context.class, List.class), 
                Plugin.getContext(), list);
    }
    
    boolean isPluginned() {
        return isPluginned;
    }
    
    PackageParser.Package getPackageInfo() {
        return pkg;
    }
    
    public LoadedApk getLoadedApk() {
        return loadedApk;
    }
}