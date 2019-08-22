package engine.android.util.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Home事件监听器
 * 
 * @author Daimon
 * @since 3/1/2013
 */
public class HomeWatcher {

    private final Context mContext;
    private final IntentFilter mFilter;

    private HomeListener mListener;
    private HomeReceiver mRecevier;

    public HomeWatcher(Context context) {
        mContext = context.getApplicationContext();
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }

    /**
     * 设置Home键监听器
     */
    public void setHomeListener(HomeListener listener) {
        mListener = listener;
        mRecevier = new HomeReceiver();
    }

    /**
     * 开始监听，注册广播
     */
    public void startWatch() {
        if (mRecevier == null)
        {
            throw new NullPointerException("请先设置监听器");
        }

        mContext.registerReceiver(mRecevier, mFilter);
    }

    /**
     * 停止监听，注销广播
     */
    public void stopWatch() {
        if (mRecevier != null)
        {
            mContext.unregisterReceiver(mRecevier);
        }
    }

    public interface HomeListener {

        void onHomePressed();

        void onHomeLongPressed();
    }

    private class HomeReceiver extends BroadcastReceiver {

        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";

        private static final String SYSTEM_DIALOG_REASON_KEY_HOME = "homekey";
        private static final String SYSTEM_DIALOG_REASON_KEY_RECENT_APPS = "recentapps";

        @SuppressWarnings("unused")
        private static final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction()))
            {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null && mListener != null)
                {
                    if (reason.equals(SYSTEM_DIALOG_REASON_KEY_HOME))
                    {
                        // 点击Home键
                        mListener.onHomePressed();
                    }
                    else if (reason.equals(SYSTEM_DIALOG_REASON_KEY_RECENT_APPS))
                    {
                        // 长按Home键
                        mListener.onHomeLongPressed();
                    }
                }
            }
        }
    }
}