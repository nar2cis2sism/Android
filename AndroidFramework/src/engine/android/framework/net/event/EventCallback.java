package engine.android.framework.net.event;

import engine.android.framework.net.MyNetConnectionStatus;

/**
 * 网络事件回调
 * 
 * @author Daimon
 */
public interface EventCallback extends MyNetConnectionStatus {

    void call(String action, int status, Object param);
}