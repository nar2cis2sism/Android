package com.project.app.event;

import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;

/**
 * 全局事件
 * 
 * @author Daimon
 */
public final class Events {

    /** 网络切换 **/
    public static final String CONNECTIVITY_CHANGE = "CONNECTIVITY_CHANGE";
    
    private static final int STATUS_NONE = 0;
    
    /**
     * @param noNetwork True:网络不可用
     */
    public static void notifyConnectivityChange(boolean noNetwork) {
        EventBus.getDefault().post(new Event(CONNECTIVITY_CHANGE, STATUS_NONE, noNetwork));
    }
}