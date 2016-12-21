package engine.android.plugin.util;

import android.app.ActivityThread;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.FileUtils;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Provide a mechanism to access Apk file.
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2012
 */
public class ApkLoader {
    
    private static final String APK_SUFFIX  = ".apk";
    private static final String ODEX_SUFFIX = ".odex";
    
    private final File apkFile;                             // APK文件
    private final File cacheDir;                            // 加载APK所生成的临时文件存放目录
    
    private final String apkPath;
    private final String optimizedDirectory;
    private final String optimizedDexPath;
    private final String libraryPath;
    
    private DexClassLoader dexLoader;
    private Resources resources;
    private DexFile dexFile;
    
    public ApkLoader(File apkFile) {
        if (!apkFile.exists())
        {
            throw new IllegalAccessError("File is not found.");
        }
        
        String path = apkFile.getPath();
        if (!path.endsWith(APK_SUFFIX))
        {
            throw new IllegalArgumentException("Only Apk file is allowed.");
        }
        
        this.apkFile = apkFile;
        String apkName = getApkName(apkPath = path);
        cacheDir = new File(apkFile.getParent(), apkName);
        optimizedDirectory = cacheDir.getPath();
        optimizedDexPath = optimizedDirectory + File.separatorChar + apkName + ODEX_SUFFIX;
        libraryPath = optimizedDirectory + File.separatorChar + "library";
    }
    
    public DexClassLoader getClassLoader() throws Exception {
        return getClassLoader(ClassLoader.getSystemClassLoader());
    }
    
    public DexClassLoader getClassLoader(ClassLoader parent) throws Exception {
        if (dexLoader != null)
        {
            return dexLoader;
        }
        
        if (cacheDir.mkdirs())
        {
            loadLibrary();
        }
        
        return dexLoader = new DexClassLoader(apkPath, optimizedDirectory, libraryPath, parent);
    }
    
    private void loadLibrary() throws Exception {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(apkFile));
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null)
            {
                String name = ze.getName();
                if (name.startsWith("lib/") && name.endsWith(".so"))
                {
                    File libFile = new File(libraryPath, name.substring(name.lastIndexOf("/")));
                    File parent = libFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs())
                    {
                        throw new Exception(String.format("Cannot create parent dir[%s]", parent));
                    }
                    
                    FileUtils.copyToFile(zis, libFile);
                }
            }
        } finally {
            if (zis != null)
            {
                zis.close();
            }
        }
    }
    
    public Resources getResources() throws Exception {
        if (resources != null)
        {
            return resources;
        }

        Resources res = ActivityThread.currentApplication().getResources();
        AssetManager am = new AssetManager();
        am.addAssetPath(apkPath);
        return resources = new Resources(am, res.getDisplayMetrics(), 
                res.getConfiguration(), res.getCompatibilityInfo());
    }
    
    public DexFile getDexFile() throws Exception {
        if (dexFile != null)
        {
            return dexFile;
        }
        
        return dexFile = DexFile.loadDex(apkPath, optimizedDexPath, 0);
    }
    
    public String getApkPath() {
        return apkPath;
    }
    
    public File getCacheDir() {
        return cacheDir;
    }
    
    /**
     * 从assets目录中加载APK包
     * 
     * @param assetsPath APK包在assets目录下的路径
     */
    public static ApkLoader loadApkFromAssets(String assetsPath) throws Exception {
        String apkName = new File(assetsPath).getName();
        File apkDir = ActivityThread.currentApplication().getDir("apk", 0);
        File apkFile = new File(apkDir, apkName);
        
        InputStream is = null;
        try {
            is = ActivityThread.currentApplication().getAssets().open(assetsPath);
            if (!FileUtils.copyToFile(is, apkFile))
            {
                throw new Exception("Failed to load apk to:" + apkFile.getPath());
            }
        } finally {
            if (is != null)
            {
                is.close();
            }
        }
        
        return new ApkLoader(apkFile);
    }

    /******************************* 华丽丽的分割线 *******************************/
    
    private static ReflectObject currentActivityThreadRef;
    private static ReflectObject AppBindDataRef;
    private static ReflectObject LoadedApkRef;
    
    private static Configuration configuration;
    private static CompatibilityInfo compatibilityInfo;
    
    public static ReflectObject getActivityThreadRef() throws Exception {
        if (currentActivityThreadRef == null)
        {
            currentActivityThreadRef = new ReflectObject(ActivityThread.currentActivityThread());
        }
        
        return currentActivityThreadRef;
    }
    
    public static ReflectObject getAppBindDataRef() throws Exception {
        if (AppBindDataRef == null)
        {
            AppBindDataRef = new ReflectObject(getActivityThreadRef().get("mBoundApplication"));
        }
        
        return AppBindDataRef;
    }
    
    public static ReflectObject getLoadedApkRef() throws Exception {
        if (LoadedApkRef == null)
        {
            LoadedApkRef = new ReflectObject(ActivityThread.currentApplication().mLoadedApk);
        }
        
        return LoadedApkRef;
    }
    
    public static Configuration getConfiguration() throws Exception {
        if (configuration == null)
        {
            configuration = (Configuration) getAppBindDataRef().get("config");
        }
        
        return configuration;
    }
    
    public static CompatibilityInfo getCompatibilityInfo() throws Exception {
        if (compatibilityInfo == null)
        {
            compatibilityInfo = (CompatibilityInfo) getAppBindDataRef().get("compatInfo");
        }
        
        return compatibilityInfo;
    }
    
    public static String getApkName(String apkPath) {
        if (apkPath == null)
        {
            return null;
        }
        
        int sidx = apkPath.lastIndexOf("/");
        int eidx = apkPath.lastIndexOf(".");
        if (eidx == -1)
        {
            eidx = apkPath.length();
        }
        else if (eidx == 0)
        {
            return null;
        }
        
        return apkPath.substring(sidx + 1, eidx);
    }
}