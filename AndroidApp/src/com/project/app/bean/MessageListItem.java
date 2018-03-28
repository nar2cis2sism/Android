package com.project.app.bean;

import engine.android.core.util.CalendarFormat;
import engine.android.framework.ui.util.DateRange;
import engine.android.framework.ui.util.DateRange.DateRangeLookUp;

import java.util.Calendar;

public class MessageListItem {
    
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
        map.register(start, end).setLabel("今天").setFormat("HH:mm");
        
        // Yesterday
        cal.setTimeInMillis(today);
        end = today;
        cal.add(Calendar.DATE, -1);
        start = cal.getTimeInMillis();
        map.register(start, end).setLabel("昨天").setFormat("昨天 HH:mm");
        
        // Older
        map.register(0, start).setLabel("更早").setFormat("MM月dd日");
    }

    public final DateRange category;            // 日期分类
    public final String time;                   // 时间
    public final String name;                   // 名称
    public final String message;                // 消息
    
    public MessageListItem(long time, String name, String message) {
        category = map.lookup(time);
        this.time = formatTime(category, time);
        this.name = name;
        this.message = message;
    }
    
    private static String formatTime(DateRange category, long time) {
        if (category == null)
        {
            return CalendarFormat.format(CalendarFormat.getCalendar(time));
        }
        
        return category.format(time);
    }
}