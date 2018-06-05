package engine.android.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import engine.android.util.file.FileManager;
import engine.android.util.file.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

/**
 * Android系统工具类
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public final class AndroidUtil {

    /**
     * 设置全屏模式
     * 
     * @param noTitle 去掉标题栏
     */
    public static void setFullScreenMode(Activity a, boolean noTitle) {
        a.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (noTitle)
        {
            a.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }

    /**
     * 设置横屏模式
     */
    public static void setLandscapeMode(Activity a) {
        a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * 设置屏幕常亮
     */
    public static void setKeepScreenOn(Activity a) {
        a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 设置背景窗口模糊
     * 
     * @deprecated
     */
    public static void setBackWindowBlur(Activity a) {
        a.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Activity a) {
        // 包括标题栏，不包括状态栏
        View decorView = a.getWindow().getDecorView();
        // 状态栏以下的屏幕区域
        Rect outRect = new Rect();
        decorView.getWindowVisibleDisplayFrame(outRect);

        return outRect.top;
    }

    /**
     * 获取标题栏高度
     */
    public static int getTitleBarHeight(Activity a) {
        // 当前显示的View根（是一个FrameLayout对象，不包括标题栏）
        View root = a.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        // 状态栏以下的屏幕区域
        Rect outRect = new Rect();
        root.getWindowVisibleDisplayFrame(outRect);

        return Math.max(0, outRect.height() - root.getHeight());
    }

    /**
     * 获取ActionBar的高度
     */
    public static int getActionBarHeight(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, outValue, true);
        return context.getResources().getDimensionPixelSize(outValue.resourceId);
    }

    /**
     * 获取背景图
     */
    public static Drawable getWindowBackground(Context context) {
        int[] attrs = { android.R.attr.windowBackground };

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(attrs[0], outValue, true);

        TypedArray a = context.obtainStyledAttributes(outValue.resourceId, attrs);
        Drawable windowBackground = a.getDrawable(0);
        a.recycle();

        return windowBackground;
    }

    /**
     * 获取屏幕分辨率
     */
    public static DisplayMetrics getResolution(Activity a) {
        DisplayMetrics dm = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    /**
     * 获取手机SDK版本号<br>
     * {@link android.os.Build.VERSION_CODES}<br>
     * 3---1.5<br>
     * 4---1.6<br>
     * 5---2.0<br>
     * 7---2.1<br>
     * 8---2.2<br>
     * 10--2.3.3<br>
     * 11---3.0<br>
     * 12---3.1<br>
     * 13---3.2<br>
     * 14---4.0<br>
     * 15---4.0.3<br>
     * 16---4.1.2<br>
     * 17---4.2.2<br>
     * 18---4.3<br>
     * 19---4.4.2<br>
     */
    public static int getVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取应用程序可分配的内存
     * 
     * @return MB
     */
    public static int getAllocatedMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getMemoryClass();
    }

    /**
     * 像素转换
     */
    public static int dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * 像素转换
     */
    public static int px2dp(Context context, float px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * 获取系统权限
     */
    public static boolean getRootPermission(ContextWrapper cw) throws Exception {
        return RootUtil.runRootCommand("chmod 777 " + cw.getPackageCodePath());
    }

    /**
     * 判断本程序是否正处于前台运行<br>
     * 需要声明权限<uses-permission android:name="android.permission.GET_TASKS" />
     */
    public static boolean atForeGround(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && !list.isEmpty())
        {
            RunningTaskInfo task = list.get(0);
            if (task != null
			&& (task.baseActivity != null
			&&  task.baseActivity.getPackageName().equals(context.getPackageName()))
			|| (task.topActivity != null
			&&  task.topActivity.getPackageName().equals(context.getPackageName())))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断本应用程序进程是否还在运行（或被系统杀死）
     */
    public static boolean isAppRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
        if (list != null && !list.isEmpty())
        {
            for (RunningAppProcessInfo process : list)
            {
                if (process.processName.equals(context.getApplicationInfo().processName))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 显示桌面屏幕
     */
    public static void ShowHomeScreen(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 获取设备实际的物理尺寸（例如：2.8、3.7、5.0英寸）
     */
    public static double getScreenSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        double d = Math.sqrt(Math.pow(dm.widthPixels / dm.xdpi, 2)
                 + Math.pow(dm.heightPixels / dm.ydpi, 2));
        return Math.round(d * 10.0) / 10.0;
    }

    /**
     * 判断设备当前是否横屏
     * 
     * @return Null表示无法判断（屏幕宽高相同）
     */
    public static Boolean isLandscape(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_UNDEFINED)
        {
            // 未定义
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            if (width < height)
            {
                // 竖屏
                return false;
            }
            else if (width > height)
            {
                // 横屏
                return true;
            }
            else
            {
                // 屏幕宽高相同
                return null;
            }
        }
        else if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            // 竖屏
            return false;
        }
        else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            // 横屏
            return true;
        }

        return null;
    }

    /**
     * 我们认为长宽比大于4:3的就为宽屏
     */
    public static boolean isWideScreen(int width, int height) {
        int max = width;
        int min = height;
        if (max < min)
        {
            max = max ^ min;
            min = max ^ min;
            max = max ^ min;
        }
        
        return max * 3 > min * 4;
    }

    /**
     * 根据包名启动已安装APK
     */
    public static void startApp(Context context, String packageName) throws Exception {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(packageName, 0);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // context不是activity的时候需要加这一句
        intent.setPackage(pi.packageName);

        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
        if (list != null && !list.isEmpty())
        {
            ResolveInfo ri = list.get(0);
            intent.setComponent(new ComponentName(pi.packageName, ri.activityInfo.name));
        }
        
        context.startActivity(intent);
    }

    /**
     * 判断当前的手机屏幕是否开启了自动旋转这个选项
     */
    public static boolean isAutoRotate(Context context) throws SettingNotFoundException {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION) == 1;
    }

    /**
     * 获取手机所有应用（包括未安装的）
     * 
     * @param includeSystem 是否包含系统预装的应用程序
     */
    public static List<PackageInfo> getAllApps(Context context, boolean includeSystem) {
        List<PackageInfo> list = context.getPackageManager().getInstalledPackages(
                PackageManager.GET_UNINSTALLED_PACKAGES
              | PackageManager.GET_DISABLED_COMPONENTS);
        ListIterator<PackageInfo> iter = list.listIterator();
        while (iter.hasNext())
        {
            PackageInfo pi = iter.next();
            if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
            ||  (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
            {
                // 非系统预装的第三方应用程序
                continue;
            }
            else if (!includeSystem)
            {
                iter.remove();
            }
        }

        return list;
    }
    
    /**
     * 导出应用程序安装包
     * 
     * @param destDir 导出目录
     */
    public static boolean exportApp(Context context, File destDir) throws Exception {
        String apk = context.getPackageCodePath();
        String fileName = FileManager.getFileName(apk);
        File destFile = new File(destDir, fileName);
        
        FileInputStream fis = new FileInputStream(apk);
        try {
            return FileUtils.copyToFile(fis, destFile);
        } finally {
            fis.close();
        }
    }

    /**
     * 判断当前应用是否是系统软件
     */
    public static boolean isSystemApplication(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), 0);
            return ai != null && (ai.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 获取应用的签名
     */
    public static Signature getSignature(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            return pi.signatures[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 解析签名信息
     */
    public static byte[] parseSignature(Signature sign) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bais = new ByteArrayInputStream(sign.toByteArray());
            Certificate cert = certFactory.generateCertificate(bais);
            return cert.getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 根据权限得到Authority
     */
    public static String getAuthorityFromPermission(Context context, String permission) {
        if (permission == null)
        {
            return null;
        }

        List<PackageInfo> list = context.getPackageManager().getInstalledPackages(
                PackageManager.GET_PROVIDERS);
        for (PackageInfo pi : list)
        {
            ProviderInfo[] providers = pi.providers;
            if (providers != null)
            {
                for (ProviderInfo provider : providers)
                {
                    if (permission.equals(provider.readPermission)
                    ||  permission.equals(provider.writePermission))
                    {
                        return provider.authority;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 通过反射获取到sdk隐藏的服务
     * 
     * @param name 服务名称
     */
    public static IBinder getServiceIBinder(String name) throws Exception {
        Method m = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
        IBinder binder = (IBinder) m.invoke(null, name); // 激活服务
        return binder;
    }

    /**
     * 绕过权限访问（用于不同进程间）
     */
    public static <V> V grantPermission(Callable<V> call) {
        long token = Binder.clearCallingIdentity();
        try {
            return call.call();
        } catch (Exception e) {
            return null;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /**
     * 获取字体的高度
     */
    public static float getFontHeight(Paint paint) {
        return paint.descent() - paint.ascent();
    }

    /**
     * 获取字体的行高
     */
    public static float getLineHeight(Paint paint) {
        return paint.getFontSpacing();
    }

    /**
     * 获取应用版本号
     */
    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 获取应用版本名称
     */
    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public static void setupStrictMode() {
        if (getVersion() < 11) return;
        
        // StrictMode.enableDefaults()有bug
        // (android.os.StrictMode$InstanceCountViolation:instance=2;limit=1)
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectCustomSlowCalls()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()
        .penaltyLog()
        .penaltyDeathOnNetwork()
        .penaltyFlashScreen()
        .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedClosableObjects()
        .detectLeakedSqlLiteObjects()
        .penaltyLog()
        .build());
    }
}