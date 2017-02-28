package engine.android.framework.network.event;

import engine.android.framework.network.ConnectionStatus;

/**
 * 网络事件回调
 * 
 * @author Daimon
 */
public interface EventCallback extends ConnectionStatus {

    void call(String action, int status, Object param);
}