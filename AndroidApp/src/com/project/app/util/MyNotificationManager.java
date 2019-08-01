package com.project.app.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat.Builder;

import com.daimon.yueba.R;
import com.project.app.MyContext;
import com.project.storage.db.Message;
import com.project.ui.message.conversation.ConversationActivity;
import com.project.ui.message.conversation.ConversationActivity.ConversationParams;

import engine.android.util.extra.Singleton;

/**
 * 通知管理器
 * 
 * @author Daimon
 */
public class MyNotificationManager {
    
    private static int NOTIFICATION_ID = 0;

    private static final Singleton<MyNotificationManager> instance
    = new Singleton<MyNotificationManager>() {

        @Override
        protected MyNotificationManager create() {
            return new MyNotificationManager(MyContext.getContext());
        }
    };

    public static final MyNotificationManager getInstance() {
        return instance.get();
    }

    /******************************** 华丽丽的分割线 ********************************/

    private final Context context;

    private final NotificationManager nm;

    private final Vibrator v;

    private MyNotificationManager(Context context) {
        this.context = context;
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private PendingIntent pendingActivityIntent(Intent intent) {
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void clear() {
        nm.cancelAll();
    }
    
    /**
     * 需要声明权限<uses-permission android:name="android.permission.VIBRATE" />
     * 
     * @param milliseconds 震动时长，0表示取消震动
     */
    public void vibrate(long milliseconds) {
        if (milliseconds > 0)
        {
            v.vibrate(milliseconds);
        }
        else
        {
            v.cancel();
        }
    }

    /******************************** 华丽丽的分割线 ********************************/
    
    private static final int MESSAGE = ++NOTIFICATION_ID;
    
    /**
     * 消息通知
     * 
     * @param msg 最新消息
     * @param count 未读消息数量
     */
    public void notifyMessage(Message msg, int count) {
        Builder builder = new Builder(context)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(msg.content)
        .setNumber(count)
        .setContentIntent(pendingActivityIntent(ConversationActivity
                .buildIntent(context, new ConversationParams(msg.account))));
        
        nm.notify(MESSAGE, builder.build());
    }
    
    public void cancelMessage() {
        nm.cancel(MESSAGE);
    }
}