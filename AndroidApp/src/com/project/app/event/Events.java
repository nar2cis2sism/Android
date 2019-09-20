package com.project.app.event;

import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;

import com.project.ui.MainActivity;

/**
 * 全局事件
 * 
 * @author Daimon
 */
public class Events extends engine.android.framework.app.event.Events {

    /******************************* 主界面标签徽章 *******************************/
    public static final String MAIN_TAB_BADGE = "MAIN_TAB_BADGE";

    /**
     * @param tag 主界面标签tag。例如{@link MainActivity#TAB_TAG_MESSAGE}
     * @param shown 显示/隐藏徽章效果
     */
    public static void notifyMainTabBadge(String tag, boolean shown) {
        EventBus.getDefault().post(new Event(MAIN_TAB_BADGE, shown ? 1 : 0, tag));
    }
}