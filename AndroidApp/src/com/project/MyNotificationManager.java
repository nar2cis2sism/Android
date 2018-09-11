package com.project;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat.Builder;

import com.daimon.yueba.R;
import com.project.app.MyContext;
import com.project.storage.db.Message;
import com.project.ui.message.conversation.ConversationActivity;
import com.project.ui.message.conversation.ConversationPresenter.ConversationParams;

import engine.android.util.extra.Singleton;

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

    private MyNotificationManager(Context context) {
        nm = (NotificationManager) (this.context = context).getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private PendingIntent pendingActivityIntent(Intent intent) {
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void clear() {
        nm.cancelAll();
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
        ConversationParams params = new ConversationParams();
        params.title = "";
        params.account = msg.account;
        
        Builder builder = new Builder(context)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(msg.content)
        .setNumber(count)
        .setContentIntent(pendingActivityIntent(ConversationActivity.buildIntent(context, params)));
        
        nm.notify(MESSAGE, builder.build());
    }
    
    public void cancelMessage() {
        nm.cancel(MESSAGE);
    }
}