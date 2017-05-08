package engine.android.aidl;

import engine.android.aidl.ICallback;
import engine.android.aidl.Action;

interface IAidl {

    void register(in ICallback callback);
    
    void unregister();

    void sendAction(in Action action);

    void cancelAction(in String action);
}