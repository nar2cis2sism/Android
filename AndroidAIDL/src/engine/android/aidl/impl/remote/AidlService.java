package engine.android.aidl.impl.remote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import engine.android.aidl.Action;
import engine.android.aidl.Action.ActionParam;
import engine.android.aidl.Event;
import engine.android.aidl.IAidl;
import engine.android.aidl.ICallback;

public class AidlService extends Service {
    
    private final ActionManager actionManager = new ActionManager();

    private ICallback iCallback;

    private final IAidl.Stub stub = new IAidl.Stub() {      // 实现AIDL接口中各个方法

        @Override
        public void register(ICallback callback) throws RemoteException {
            iCallback = callback;
        }

        @Override
        public void unregister() throws RemoteException {
            iCallback = null;
        }

        @Override
        public void sendAction(Action action) throws RemoteException {
            actionManager.sendAction(action);
        }

        @Override
        public void cancelAction(String action) throws RemoteException {
            actionManager.cancelAction(action);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // 返回AIDL接口实例化对象
        return stub;
    }

    private interface ActionCancellation {

        void cancel();
    }

    public interface ActionCallable<Param extends ActionParam> extends ActionCancellation {

        void call(Param param);
    }

    public void registerAction(String action, ActionCallable<? extends ActionParam> callable) {
        actionManager.registerAction(action, callable);
    }

    public void notifyCallback(Event event) {
        actionManager.notifyCallback(event, iCallback);
    }
}