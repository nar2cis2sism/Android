package engine.android.core;

import static engine.android.core.util.LogFactory.LOG.log;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Process;
import android.widget.Toast;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import engine.android.core.Forelet.FragmentTransaction;
import engine.android.core.Forelet.Task;

/**
 * 应用程序管理器<p>
 * 功能：活动堆栈管理，程序退出，异常处理
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class ApplicationManager extends Application
implements UncaughtExceptionHandler {

    private static Session session;                         // 程序会话（储存全局属性）

    private static ApplicationManager instance;             // 应用程序管理器实例

    private final ActivityStack stack;                      // 活动堆栈管理

    private final UncaughtExceptionHandler ueh;             // 异常处理器

    private static boolean isDebuggable;                    // Debug模式

    private static ExitTask exitTask;                       // 后台退出异步任务

    public ApplicationManager() {
        session = new Session();
        instance = this;
        registerActivityLifecycleCallbacks(stack = new ActivityStack());
        ueh = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        isDebuggable = isDebuggable(base);
    }
    
    /**
     * 供三方调用初始化
     */
    public static final void init(Application app) {
        ApplicationManager am = new ApplicationManager();
        am.attachBaseContext(app.getBaseContext());
        app.registerActivityLifecycleCallbacks(am.stack);
    }

    /**
     * Provide a mechanism to process unfinished events before quit, run in
     * background thread
     */
    protected void doExit() {};

    /**
     * 获取程序会话
     */
    public static final Session getSession() {
        return session;
    }

    /**
     * 获取应用程序管理器
     */
    public static final ApplicationManager getApplicationManager() {
        if (instance != null) return instance;
        throw new RuntimeException("ApplicationManager.init() is not called.");
    }

    /**
     * 是否调试模式
     */
    public static boolean isDebuggable() {
        return isDebuggable;
    }

    /**
     * 判断当前应用是否处于debug状态
     */
    private static boolean isDebuggable(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * 判断当前运行线程是否为主（UI）线程
     */
    public static final boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    static void ensureCallMethodOnMainThread() {
        if (!isMainThread())
        {
            throw new RuntimeException("You must call this method in main thread.");
        }
    }

    /**
     * 返回栈顶的活动(might be null)
     */
    public final Activity currentActivity() {
        ensureCallMethodOnMainThread();
        return stack.currentActivity();
    }

    /**
     * 移除栈中所有的活动
     */
    public final void popupAllActivities() {
        ensureCallMethodOnMainThread();
        stack.clear();
    }

    /**
     * 程序退出
     */
    public void exit() {
        ensureCallMethodOnMainThread();
        if (exitTask == null)
        {
            // 后台退出任务
            (exitTask = new ExitTask()).execute();
        }
    }

    private class ExitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            stack.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            log((StackTraceElement) null, null);
            log((StackTraceElement) null, "程序退出");

            try {
                Thread.sleep(1500);

                doExit();
            } catch (InterruptedException e) {
                // Cancelled
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }

    /**
     * 显示消息提示（不推荐使用）
     * 
     * @param message 提示信息
     */
    public static void showMessage(Object message) {
        Toast.makeText(instance, String.valueOf(message), Toast.LENGTH_SHORT).show();
    }

    /**
     * 加载图片
     * 
     * @param resourceId 图片资源ID
     */
    public static Bitmap loadImage(int resourceId) {
        return BitmapFactory.decodeResource(instance.getResources(), resourceId);
    }

    /**
     * 获取当前运行程序的进程名称
     */
    public static final String getProcessName() {
        int pid = Process.myPid();

        ActivityManager am = (ActivityManager) instance.getSystemService(
                Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
        if (list != null && !list.isEmpty())
        {
            for (RunningAppProcessInfo process : list)
            {
                if (process.pid == pid)
                {
                    return process.processName;
                }
            }
        }

        return instance.getApplicationInfo().processName;
    }

    @Override
    public final void uncaughtException(Thread thread, Throwable ex) {
        log("程序出错--" + thread, ex);
        if (!handleException(ex) && ueh != null)
        {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            ueh.uncaughtException(thread, ex);
        }
    }

    /**
     * 子类可自定义异常处理
     * 
     * @return 异常是否已处理
     */
    protected boolean handleException(Throwable ex) {
        return false;
    }

    /**
     * 设置是否跟踪打印Activity堆栈状态(默认开启)
     * 
     * @param open 开关
     */
    public static final void traceActivityStack(boolean open) {
        ensureCallMethodOnMainThread();
        ActivityStack.traceActivityStack = open;
    }

    private static class ActivityStack implements ActivityLifecycleCallbacks {

        public static boolean traceActivityStack = true;

        private final LinkedList<ActivityReference> history
        = new LinkedList<ActivityReference>();                  // 活动历史堆栈

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            if (exitTask != null)
            {
                exitTask.cancel(true);
                exitTask = null;
            }

            pushActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {}

        @Override
        public void onActivityResumed(Activity activity) {}

        @Override
        public void onActivityPaused(Activity activity) {}

        @Override
        public void onActivityStopped(Activity activity) {}

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

        @Override
        public void onActivityDestroyed(Activity activity) {
            popupActivity(activity);
        }

        /**
         * 活动入栈
         */
        private void pushActivity(Activity activity) {
            history.addFirst(new ActivityReference(activity));
            if (traceActivityStack)
                log("活动入栈", "[" + activity.getTaskId() + "]" + activity);
        }

        /**
         * 活动出栈
         */
        private void popupActivity(Activity activity) {
            if (history.isEmpty())
            {
                return;
            }

            Activity a;
            Iterator<ActivityReference> iter = history.iterator();
            while (iter.hasNext())
            {
                a = iter.next().get();
                if (a == activity)
                {
                    iter.remove();
                    break;
                }
            }

            if (traceActivityStack)
                log("活动出栈", "[" + activity.getTaskId() + "]" + activity);
        }

        public Activity currentActivity() {
            Activity a;
            while (!history.isEmpty())
            {
                if ((a = history.getFirst().get()) == null)
                {
                    history.removeFirst();
                    continue;
                }
                else
                {
                    return a;
                }
            }

            return null;
        }

        public void clear() {
            if (history.isEmpty())
            {
                return;
            }

            Activity a;
            for (ActivityReference r : history)
            {
                if ((a = r.get()) != null)
                {
                    a.finish();
                }
            }

            history.clear();
        }

        private static class ActivityReference extends WeakReference<Activity> {

            public ActivityReference(Activity r) {
                super(r);
            }
        }
    }
}

class SavedInstance {
    
    private static final WeakHashMap<Bundle, SavedInstance> savedInstanceMap
    = new WeakHashMap<Bundle, SavedInstance>();
    
    public static void save(Bundle bundle, SavedInstance savedInstance) {
        savedInstanceMap.put(bundle, savedInstance);
    }
    
    public static SavedInstance restore(Bundle bundle) {
        return savedInstanceMap.get(bundle);
    }
    
    public Task task;
    
    public Object progress;
    
    public FragmentTransaction transaction;
    
    public final HashMap<String, Object> savedMap = new HashMap<String, Object>();
}