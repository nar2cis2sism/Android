package com.project.app.bean;

import engine.android.core.util.CalendarFormat;
import engine.android.core.util.CalendarFormat.DateRange;
import engine.android.core.util.CalendarFormat.DateRange.DateRangeLookUp;

import com.project.storage.db.Message;

public class MessageItem {
    
    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    
    public final Message message;
    
    public final String time;
    
    public MessageItem(Message message) {
        this.message = message;
        time = formatTime(DateRangeLookUp.getDefault().lookup(message.creationTime), message.creationTime);
    }
    
    private static String formatTime(DateRange range, long time) {
        return range != null ? range.format(time)
             : CalendarFormat.format(CalendarFormat.getCalendar(time));
    }
    
    public boolean inFiveMinutes(MessageItem item) {
        return Math.abs(message.creationTime - item.message.creationTime) <= FIVE_MINUTES;
    }
    
    /**
     * 消息发送中
     */
    public boolean isSending() {
        return message.sendStatus == 0;
    }
    
    /**
     * 消息发送失败
     */
    public boolean isSendFail() {
        return message.sendStatus == 1;
    }
}