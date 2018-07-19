package com.project.network.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.project.app.event.Events;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Events.notifyConnectivityChange(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false));
    }
}