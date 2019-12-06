package demo.activity;

import engine.android.util.extra.SafeThread;
import engine.android.util.extra.SafeThread.SafeRunnable;
import engine.android.util.manager.MyPowerManager;
import engine.android.util.manager.MyPowerManager.ScreenObserver;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import demo.activity.UninstallActivity.LogWatcher.LogObserver;
import demo.android.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class UninstallActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, UninstallService.class));
    }
    
    @Override
    protected void onDestroy() {
        stopService(new Intent(this, UninstallService.class));
        super.onDestroy();
    }
    
    public static class UninstallService extends Service implements ScreenObserver, LogObserver {
        
        LogWatcher lw;
        MyPowerManager pm;
        
        Handler handler = new Handler() {
            
            public void handleMessage(android.os.Message msg) {
                Intent intent = new Intent(UninstallService.this, UninstallPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            };
        };

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        
        @Override
        public void onCreate() {
            System.out.println("start watch");
            lw = new LogWatcher(this);
            pm = new MyPowerManager(this);
            pm.registerScreenObserver(this);
            if (pm.isScreenOn())
            {
                lw.startWatch();
            }
        }
        
        @Override
        public void onDestroy() {
            System.out.println("stop watch");
            pm.unregisterScreenObserver();
            lw.stopWatch();
        }

        @Override
        public void screenOn() {
            lw.startWatch();
        }

        @Override
        public void screenOff() {
            lw.stopWatch();
        }

        @Override
        public void log(String log) {
            if (startUninstall(log) && log.contains(getPackageName()))
            {
                handler.sendEmptyMessage(0);
            }
        }
        
        /**
         * 通过应用程序管理器点击卸载按钮后
         */
        
        public static boolean startUninstall(String log)
        {
            return log.contains(Intent.ACTION_DELETE);
        }
        
        /**
         * 应用程序被卸载瞬间
         */
        
        public static boolean afterUninstall(String log)
        {
            return log.contains("uninstall") || log.contains("Removing");
        }
    }
    
    public static class UninstallPage extends Activity {
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            ImageView iv = new ImageView(this);
            iv.setScaleType(ScaleType.FIT_START);
            iv.setImageResource(R.drawable.uninstall);
            
            setContentView(iv);
        }
    }
    
    /**
     * 日志监视器
     * @author Daimon
     * @version 3.0
     * @since 11/14/2012
     */

    public static class LogWatcher extends SafeRunnable {
        
        private final SafeThread thread = new SafeThread();
        
        private LogObserver observer;
        
        public LogWatcher(LogObserver observer) {
            this.observer = observer;
        }
        
        /**
         * 开启监测
         */
        
        public void startWatch()
        {
            thread.startThread(this);
        }
        
        /**
         * 停止监测
         */
        
        public void stopWatch()
        {
            thread.forceStopThread();
        }
        
        public static interface LogObserver {
            
            public void log(String log);
            
        }
        
        @Override
        public void run(AtomicBoolean isRunning) {
            Process process = null;
            BufferedReader br = null;
            try {
                //clear logs before
                if (Runtime.getRuntime().exec("logcat -c").waitFor() == 0)
                {
                    //start watch current logs
                    process = Runtime.getRuntime().exec("logcat");
                    
//                    String cmds = "logcat *:e *:i | grep \"(" + android.os.Process.myPid() + ")\"";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            
            if (process != null)
            {
                br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String s;
                try {
                    while (isRunning.get() && (s = br.readLine()) != null)
                    {
                        observer.log(s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                process.destroy();
            }
        }
    }
}