package engine.android.plugin;

import android.content.pm.PackageParser;

import java.io.File;
import java.util.HashMap;

/**
 * 插件管理器，负责插件的安装及存储
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2014
 */
public class PluginManager {
    
    private final PluginEnvironment environment;
    
    private final HashMap<String, Plugin> pluginsByFile
    = new HashMap<String, Plugin>();
    private final HashMap<String, Plugin> pluginsByPackage
    = new HashMap<String, Plugin>();
    
    PluginManager(PluginEnvironment environment) {
        this.environment = environment;
    }
    
    public Plugin loadPlugin(File apkFile) throws Exception {
        environment.prepare();
        
        String apkName = apkFile.getName();
        Plugin plugin = pluginsByFile.get(apkName);
        if (plugin != null)
        {
            return plugin;
        }
        
        try {
            PackageParser.Package pkg = environment.pm.scanPackage(apkFile);
            String packageName = pkg.packageName;
            
            plugin = pluginsByPackage.get(packageName);
            if (plugin != null)
            {
                return plugin;
            }
            
            PluginLoader loader = new PluginLoader(environment, apkFile, pkg);
            loader.plugin();
            
            plugin = new Plugin(loader);
            pluginsByFile.put(apkName, plugin);
            pluginsByPackage.put(packageName, plugin);
            
            return plugin;
        } catch (Exception e) {
            throw new Exception("Failed to load plugin:" + apkFile.getPath(), e);
        }
    }

    public Plugin getPlugin(String packageName) {
        return pluginsByPackage.get(packageName);
    }
}