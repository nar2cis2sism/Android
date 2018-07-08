package com.project.network.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.project.app.event.Events;

import engine.android.http.HttpConnector;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Events.notifyConnectivityChange(HttpConnector.isAccessible(context));
    }
}