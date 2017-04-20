package engine.android.plugin;

import android.content.Context;
import android.os.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

public class PluginManager {
    
    private final PluginEnvironment environment;

    // This is where all plugin package goes.
    private final File pluginDir;

    // This is where all plugin application persistent data goes.
    private final File pluginDataDir;
    
    private final HashMap<String, PluginLoader> loadersByFile
    = new HashMap<String, PluginLoader>();
    private final HashMap<String, PluginLoader> loadersByPackage
    = new HashMap<String, PluginLoader>();
    
    PluginManager(PluginEnvironment environment) {
        this.environment = environment;
        pluginDir = environment.getContext().getDir("plugin", Context.MODE_PRIVATE);
        pluginDataDir = new File(pluginDir, "data");
    }
    
    /**
     * 从assets目录中加载插件包
     * 
     * @param assetsPath 插件包在assets目录下的路径
     * @param forceReload 强制覆盖重新加载
     * @return 插件包名
     */
    public String loadPluginFromAssets(String assetsPath, boolean forceReload) throws Exception {
        String apkName = new File(assetsPath).getName();
        File apkFile = new File(pluginDir, apkName);
        if (forceReload || !apkFile.exists())
        {
            InputStream is = null;
            try {
                is = environment.getContext().getAssets().open(assetsPath);
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
    public String loadPluginFromFile(File apkFile) throws Exception {
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
        environment.prepare();
        String apkName = apkFile.getName();
        PluginLoader loader = loadersByFile.get(apkName);
        if (loader == null)
        {
            loader = new PluginLoader(environment, apkFile, environment.pm.scanPackage(apkFile));
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