package engine.android.util.manager;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;

/**
 * 我的锁屏管理器<br>
 * 需要声明权限<uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2012
 */
@SuppressWarnings("deprecation")
public class MyKeyguardManager {

    private final KeyguardManager km;                   // 锁屏管理器

    private KeyguardLock lock;                          // 键盘锁

    public MyKeyguardManager(Context context) {
        km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    /**
     * 隐藏锁屏界面
     */
    public void hide() {
        if (lock == null)
        {
            lock = km.newKeyguardLock(getClass().getSimpleName());
        }

        lock.disableKeyguard();
    }

    /**
     * 重新显示锁屏界面
     */
    public void show() {
        if (lock != null)
        {
            lock.reenableKeyguard();
            lock = null;
        }
    }

    /**
     * 屏幕是否锁住
     */
    public boolean isLock() {
        return km.inKeyguardRestrictedInputMode();
    }
}