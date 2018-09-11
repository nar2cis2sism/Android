package com.project.app.event;

import com.project.ui.MainActivity;

import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;

/**
 * 全局事件
 * 
 * @author Daimon
 */
public final class Events {

    /******************************* 网络切换 *******************************/
    public static final String CONNECTIVITY_CHANGE = "CONNECTIVITY_CHANGE";
    
    /**
     * @param noNetwork True:网络不可用
     */
    public static void notifyConnectivityChange(boolean noNetwork) {
        EventBus.getDefault().post(new Event(CONNECTIVITY_CHANGE, 0, noNetwork));
    }

    /******************************* 主界面标签徽章 *******************************/
    public static final String MAIN_TAB_BADGE = "MAIN_TAB_BADGE";
    public static final int STATUS_SHOW = 1;
    public static final int STATUS_HIDE = 2;

    /**
     * @param tag 主界面标签tag。例如{@link MainActivity#TAB_TAG_MESSAGE}
     * @param shown 显示/隐藏徽章效果
     */
    public static void notifyMainTabBadge(String tag, boolean shown) {
        EventBus.getDefault().post(new Event(MAIN_TAB_BADGE, shown ? STATUS_SHOW : STATUS_HIDE, tag));
    }
}