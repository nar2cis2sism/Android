package engine.android.util.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

/**
 * 我的电源管理器<br>
 * 需要声明权限
 * <uses-permission android:name="android.permission.DEVICE_POWER" />
 * <uses-permission android:name="android.permission.WAKE_LOCK" />
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public class MyPowerManager {

    private final Context context;

    private final PowerManager pm;						// 电源管理器

    private WakeLock lock;								// 屏幕锁

    private BroadcastReceiver screenReceiver;           // 屏幕接收器

    public MyPowerManager(Context context) {
        pm = (PowerManager) (this.context = context.getApplicationContext())
                .getSystemService(Context.POWER_SERVICE);
    }

    /**
     * 注册屏幕监测器
     */
    public void registerScreenObserver(final ScreenObserver screenObserver) {
        if (screenReceiver == null)
        {
            screenReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (Intent.ACTION_SCREEN_ON.equals(action))
                    {
                        screenObserver.screenOn();
                    }
                    else if (Intent.ACTION_SCREEN_OFF.equals(action))
                    {
                        screenObserver.screenOff();
                    }
                }
            };

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            context.registerReceiver(screenReceiver, filter);
        }
    }

    /**
     * 取消屏幕监测
     */
    public void unregisterScreenObserver() {
        if (screenReceiver != null)
        {
            context.unregisterReceiver(screenReceiver);
            screenReceiver = null;
        }
    }

    /**
     * 获取屏幕的状态（亮:暗）
     */
    public boolean isScreenOn() {
        return pm.isScreenOn();
    }

    /**
     * 锁定屏幕<br>
     * {@link PowerManager#PARTIAL_WAKE_LOCK} 黑屏<br>
     * {@link PowerManager#SCREEN_DIM_WAKE_LOCK} 屏幕会变暗<br>
     * {@link PowerManager#SCREEN_BRIGHT_WAKE_LOCK} 屏幕一直亮<br>
     * {@link PowerManager#FULL_WAKE_LOCK} 屏幕和键盘一直亮<br>
     * {@link PowerManager#ACQUIRE_CAUSES_WAKEUP} 必须设置这个标志才能真正点亮屏幕
     * {@link PowerManager#ON_AFTER_RELEASE} 解锁后过一会才黑屏
     * 
     * @param state 状态
     * @param time 持续时间（时间到自动释放锁），-1为直到解锁
     */
    public void lock(int state, long time) {
        if (lock == null)
        {
            lock = pm.newWakeLock(state, getClass().getSimpleName());
        }

        if (time == -1)
        {
            lock.acquire();
        }
        else
        {
            lock.acquire(time);
        }
    }

    /**
     * 解锁（锁定之后一定要记得解锁以节省电量）
     */
    public void unlock() {
        if (lock != null)
        {
            if (lock.isHeld())
                lock.release();
            lock = null;
        }
    }

    /**
     * 屏幕监测器
     */
    public static interface ScreenObserver {

        public void screenOn();

        public void screenOff();
    }
}