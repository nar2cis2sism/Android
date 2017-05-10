package com.android.aidl.impl;

import static engine.android.core.util.LogFactory.LOG.log;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.aidl.Action;
import com.android.aidl.Event;
import com.android.aidl.IAidl;
import com.android.aidl.ICallback;

import de.greenrobot.event.EventBus;
import engine.android.core.ApplicationManager;
import engine.android.util.SyncLock;

public class AidlServiceBinder {

    private static final Intent AIDL = new Intent(IAidl.class.getName());

    private boolean isServiceConnected;

    private final SyncLock actionLock = new SyncLock();

    private final CallbackDispatcher callback = new CallbackDispatcher();

    private IAidl aidl;                                                 // 远程调用接口

    private final ServiceConnection conn = new ServiceConnection() {    // 远程调用连接

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceConnected = false;
            aidl = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            aidl = IAidl.Stub.asInterface(binder);
            isServiceConnected = true;

            try {
                aidl.register(ICallback.Stub.asInterface(callback.stub));
            } catch (RemoteException e) {
                log("Register AIDL callback", e);
            }

            actionLock.unlock();
        }
    };

    /**
     * 绑定AIDL，启动远程连接，连接成功后才能调用接口，如服务没启动则自动创建
     */

    public void bindService(Context context) {
        context.bindService(AIDL, conn, Context.BIND_AUTO_CREATE);
    }

    public void unbindAndStopService(Context context) {
        context.unbindService(conn);
        context.stopService(AIDL);
    }

    public void sendAction(Action action) {
        if (!isServiceConnected)
        {
            log("sendAction:" + action, "AIDL连接未建立");
            actionLock.lock();
        }

        try {
            aidl.sendAction(action);
        } catch (RemoteException e) {
            log("sendAction:" + action, e);
        }
    }

    public void cancelAction(String action) {
        if (!isServiceConnected)
        {
            log("cancelAction:" + action, "AIDL连接未建立");
            actionLock.lock();
        }

        try {
            aidl.cancelAction(action);
        } catch (RemoteException e) {
            log("cancelAction:" + action, e);
        }
    }

    private static class CallbackDispatcher {

        final ICallback.Stub stub = new ICallback.Stub() {

            @Override
            public void handle(Event event) throws RemoteException {
                log(ApplicationManager.getCurrentStackFrame(),
                        event.action + "|" + event.status + "|" + event.param);
                EventBus.getDefault().post(event);
            }
        };
    }
}