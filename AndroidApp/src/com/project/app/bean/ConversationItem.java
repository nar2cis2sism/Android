package com.project.app.bean;

import com.project.storage.db.Message;

import engine.android.core.util.CalendarFormat;
import engine.android.framework.ui.util.DateRange;
import engine.android.framework.ui.util.DateRange.DateRangeLookUp;

import java.util.Calendar;

public class ConversationItem {
    
    private static final DateRangeLookUp map = new DateRangeLookUp();
    
    static
    {
        // Today
        Calendar cal = Calendar.getInstance();
        CalendarFormat.formatAllDay(cal);
        long today = cal.getTimeInMillis();
        
        long start = today;
        cal.add(Calendar.DATE, 1);
        long end = cal.getTimeInMillis();
        map.register(start, end).setFormat("HH:mm");
        
        // Yesterday
        end = start;
        cal.setTimeInMillis(today);
        cal.add(Calendar.DATE, -1);
        start = cal.getTimeInMillis();
        map.register(start, end).setFormat("昨天 HH:mm");
        
        // This Week
        end = start;
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        start = cal.getTimeInMillis();
        map.register(start, end).setFormat("E HH:mm");
        
        // This Year
        end = start;
        cal.setTimeInMillis(today);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        start = cal.getTimeInMillis();
        map.register(start, end).setFormat("M月d日 HH:mm");
        
        // Older
        map.register(0, start).setFormat("yyyy年M月d日 HH:mm");
    }
    
    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    
    public final Message message;
    
    public final String time;
    
    public ConversationItem(Message message) {
        this.message = message;
        time = formatTime(map.lookup(message.creationTime), message.creationTime);
    }
    
    private static String formatTime(DateRange range, long time) {
        return range != null ? range.format(time)
             : CalendarFormat.format(CalendarFormat.getCalendar(time));
    }
    
    public boolean inFiveMinutes(ConversationItem item) {
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