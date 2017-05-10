package demo.j2se.shell;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import dalvik.system.DexClassLoader;

import engine.android.util.ReflectObject;
import engine.android.util.file.FileManager;
import engine.android.util.io.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ApkLoader {
    
    private final File apkFile;
    
    private String dexPath;
    private String optimizedDirectory;
    private String libraryPath;
    
    private ReflectObject ref_currentActivityThread;
    private ReflectObject ref_LoadedApk;
    private boolean isConfigured;
    
    public ApkLoader(File apkFile) {
        if (!apkFile.exists())
        {
            throw new RuntimeException("Apk file is not found.");
        }
        
        this.apkFile = apkFile;
        
        initConfig();
    }
    
    private void initConfig() {
        dexPath = apkFile.getPath();
        optimizedDirectory = apkFile.getParent();
        libraryPath = new File(optimizedDirectory, "library").getPath();
    }
    
    private void ensureConfig() throws Exception {
        if (isConfigured)
        {
            return;
        }
        
        /*ActivityThread*/ Object currentActivityThread = ReflectObject.invokeStatic(
                Class.forName("android.app.ActivityThread"), "currentActivityThread");
        ref_currentActivityThread = new ReflectObject(currentActivityThread);

        /*AppBindData*/ Object mBoundApplication = ref_currentActivityThread.get("mBoundApplication");
        ReflectObject ref_mBoundApplication = new ReflectObject(mBoundApplication);
        
        /*LoadedApk*/ Object LoadedApk = ref_mBoundApplication.get("info");
        ref_LoadedApk = new ReflectObject(LoadedApk);
        
        isConfigured = true;
    }
    
    public void configClassLoader() throws Exception {
        ensureConfig();
        
        readLibrary();
        //替换组件类加载器为DexClassLoader，使动态加载代码具有组件生命周期
        DexClassLoader dexLoader = new DexClassLoader(dexPath, optimizedDirectory, libraryPath, 
                (ClassLoader) ref_LoadedApk.get("mClassLoader"));
        ref_LoadedApk.set("mClassLoader", dexLoader);
    }
    
    private void readLibrary() throws Exception {
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
                    FileManager.writeFile(libFile, IOUtil.readStream(zis), false);
                }
            }
        } finally {
            if (zis != null)
            {
                zis.close();
            }
        }
    }
    
    public void configResourceLoader() throws Exception {
        ensureConfig();
        
        //替换资源
        ref_LoadedApk.set("mResources", null);
        ref_LoadedApk.set("mResDir", dexPath);
    }
    
    @SuppressLint("NewApi")
    public void restoreResourceLoader(Context context) throws Exception {
        ensureConfig();
        
        ref_LoadedApk.set("mResources", null);
        ref_LoadedApk.set("mResDir", context.getPackageCodePath());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void configApplication(String applicationClassName) throws Exception {
        ensureConfig();
        
        //如果源应用配置有Application对象，则替换为源应用Application，以便不影响源程序逻辑
        Application mInitialApplication = (Application) ref_currentActivityThread.get("mInitialApplication");
        List<Application> mAllApplications = (List<Application>) ref_currentActivityThread.get("mAllApplications");
        mAllApplications.remove(mInitialApplication);
        ref_LoadedApk.set("mApplication", null);
        
        ApplicationInfo mApplicationInfo = (ApplicationInfo) ref_LoadedApk.get("mApplicationInfo");
        mApplicationInfo.className = applicationClassName;
        
        Application app = (Application) ref_LoadedApk.invoke(
                ref_LoadedApk.getMethod("makeApplication", boolean.class, Instrumentation.class), 
                false, null);
        ref_currentActivityThread.set("mInitialApplication", app);
        
        Map mProviderMap = (Map) ref_currentActivityThread.get("mProviderMap");
        Iterator iter = mProviderMap.values().iterator();
        while (iter.hasNext())
        {
            ReflectObject ref_ProviderClientRecord = new ReflectObject(iter.next());
            ReflectObject ref_mLocalProvider = new ReflectObject(ref_ProviderClientRecord.get("mLocalProvider"));
            ref_mLocalProvider.set("mContext", app);
        }
        
        app.onCreate();
    }
    
    public String getDexPath() {
        return dexPath;
    }
    
    public String getLibraryPath() {
        return libraryPath;
    }
    
    public String getOptimizedDirectory() {
        return optimizedDirectory;
    }
}