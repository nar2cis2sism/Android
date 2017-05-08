package engine.android.aidl;

import engine.android.aidl.Event;

interface ICallback {

    void handle(in Event event);
}