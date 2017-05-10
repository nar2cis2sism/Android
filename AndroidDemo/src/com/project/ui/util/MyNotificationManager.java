package com.project.ui.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.project.MyContext;
import com.project.ui.module.launch.LaunchActivity;

import demo.android.R;
import engine.android.util.Singleton;

public class MyNotificationManager {

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

    private static final String EXTRA_INTENT = "intent";

    private final Context context;

    private final NotificationManager nm;

    MyNotificationManager(Context context) {
        nm = (NotificationManager) (this.context = context.getApplicationContext())
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private PendingIntent generateContentIntent(Intent intent) {
        intent = new Intent(context, LaunchActivity.class).putExtra(EXTRA_INTENT, intent);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void handle(Context context, Intent intent) {
        intent = intent.getParcelableExtra(EXTRA_INTENT);

        if (intent != null)
        {
            context.startActivity(intent);
        }
    }

    public void cancel(int id) {
        nm.cancel(id);
    }

    public void clear() {
        nm.cancelAll();
    }

    /******************************** 华丽丽的分割线 ********************************/

    public static final int NOTIFICATION_ID = 1;

    public void notify(Object[] objs) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Title")
                .setContentText("text")
                .setNumber(objs.length)
                .setAutoCancel(true);

        builder.setContentIntent(generateContentIntent(null));

        nm.notify(NOTIFICATION_ID, builder.build());
    }
}