package engine.android.core;

import static engine.android.core.util.LogFactory.LogUtil.getCallerStackFrame;
import static engine.android.core.util.LogFactory.LogUtil.getClassAndMethod;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Process;
import android.widget.Toast;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import engine.android.core.util.LogFactory.LOG;

/**
 * 应用程序管理器<p>
 * 功能：活动堆栈管理，程序退出，异常处理<br>
 * Note：由于多进程或者插件化模式下Application会启动多次，需特别注意一些逻辑的处理
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public abstract class ApplicationManager extends Application implements UncaughtExceptionHandler {
    
    private static ApplicationManager instance;             // 应用主程序管理器实例
    
    private static UncaughtExceptionHandler ueh;            // 异常处理器

    private final Session session;                          // 程序会话（储存全局属性）

    private final ActivityStack stack;                      // 活动堆栈管理
    
    private boolean isMainApp;                              // 我们以第一次加载此类作为主程序根据

    public ApplicationManager() {
        session = new Session();
        registerActivityLifecycleCallbacks(stack = new ActivityStack(this));
        if (isMainApp = instance == null)
        {
            // 主程序设置一次就行了
            instance = this;
            ueh = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }
    
    /**
     * 供三方调用初始化
     */
    public final void init(Application app) {
        attachBaseContext(app.getBaseContext());
        app.registerActivityLifecycleCallbacks(stack);
    }
    
    /**
     * 获取应用主程序管理器
     */
    public static final ApplicationManager getMainApplication() {
        if (instance != null) return instance;
        throw new RuntimeException("ApplicationManager.init() is not called.");
    }

    /**
     * Provide a mechanism to process unfinished events before quit, run in
     * background thread.
     */
    protected void doExit() {};

    /**
     * 获取程序会话
     */
    public final Session getSession() {
        return session;
    }

    /**
     * 获取程序启动时间
     */
    public final long getLaunchTime() {
        return session.launchTime;
    }

    /**
     * 判断当前应用是否处于debug状态
     */
    public static final boolean isDebuggable(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * 判断当前运行线程是否为主（UI）线程
     */
    public static final boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * 确保在主线程调用，否则抛出异常
     */
    public static final void ensureCallMethodOnMainThread() {
        if (!isMainThread())
        {
            throw new RuntimeException(String.format("You must call %s in main thread.", 
                    getClassAndMethod(getCallerStackFrame())));
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
    public final void exit() {
        ensureCallMethodOnMainThread();
        stack.exit();
    }

    private class ExitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            stack.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
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
            if (isMainApp)
            {
                Process.killProcess(Process.myPid());
                System.exit(0);
            }
        }
    }

    /**
     * 显示消息提示（不推荐使用）
     * 
     * @param message 提示信息
     */
    public static void showMessage(Object message) {
        Toast.makeText(getMainApplication(), String.valueOf(message), Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取当前运行程序的进程名称
     */
    public static final String getProcessName() {
        int pid = Process.myPid();

        ActivityManager am = (ActivityManager) getMainApplication().getSystemService(
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

        return getMainApplication().getApplicationInfo().processName;
    }

    @Override
    public final void uncaughtException(Thread thread, Throwable ex) {
        LOG.log("程序出错--" + thread, ex);
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

    private static class ActivityStack implements ActivityLifecycleCallbacks {
        
        private final ApplicationManager am;

        private final LinkedList<ActivityReference> history     // 活动历史堆栈
        = new LinkedList<ActivityReference>();

        private ExitTask exitTask;                              // 后台退出异步任务
        
        public ActivityStack(ApplicationManager am) {
            this.am = am;
        }

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
        }

        /**
         * 活动出栈
         */
        private void popupActivity(Activity activity) {
            if (history.isEmpty())
            {
                return;
            }

            Iterator<ActivityReference> iter = history.iterator();
            while (iter.hasNext())
            {
                if (iter.next().get() == activity)
                {
                    iter.remove();
                    break;
                }
            }
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

        public void exit() {
            if (exitTask == null)
            {
                // 后台退出任务
                (exitTask = am.new ExitTask()).execute();
            }
        }

        private static class ActivityReference extends WeakReference<Activity> {

            public ActivityReference(Activity r) {
                super(r);
            }
        }
    }
}