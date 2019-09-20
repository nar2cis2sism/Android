package engine.android.framework.app.event;

import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;

/**
 * 全局事件
 * 
 * @author Daimon
 */
public class Events {

    /******************************* 网络切换 *******************************/
    public static final String CONNECTIVITY_CHANGE = "CONNECTIVITY_CHANGE";
    
    /**
     * @param noNetwork True:网络不可用
     */
    public static void notifyConnectivityChange(boolean noNetwork) {
        EventBus.getDefault().post(new Event(CONNECTIVITY_CHANGE, 0, noNetwork));
    }
}