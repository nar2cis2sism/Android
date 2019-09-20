package com.project.app.bean;

import engine.android.core.util.CalendarFormat;
import engine.android.core.util.CalendarFormat.DateRange;
import engine.android.util.image.AsyncImageLoader.ImageUrl;

import com.project.storage.db.Friend;
import com.project.storage.db.Message;

import java.util.Calendar;

public class MessageListItem {
    
    public final long time;                     // 时间
    public final String name;                   // 名称
    public final String message;                // 消息
    
    public final DateRange category;            // 日期分类
    public final String timeText;               // 时间文本
    
    public Friend friend;
    public long unreadCount;                    // 未读消息数量
    
    public MessageListItem(long time, String name, String message) {
        this.time = time;
        this.name = name;
        this.message = message;
        category = DateRangeLookUp.getDefault().lookup(time);
        timeText = formatTime(category, time);
    }
    
    public MessageListItem(Message msg, Friend friend) {
        this(msg.creationTime, friend.displayName, msg.content);
        this.friend = friend;
    }
    
    private static String formatTime(DateRange category, long time) {
        return category != null ? category.format(time)
             : CalendarFormat.format(CalendarFormat.getCalendar(time));
    }
    
    public ImageUrl getFriendAvatarUrl() {
        return friend != null ? friend.getAvatarUrl() : null;
    }
}

class DateRangeLookUp extends DateRange.DateRangeLookUp {

    private static DateRangeLookUp lookup;

    public static DateRangeLookUp getDefault() {
        if (lookup == null || lookup.lookup(System.currentTimeMillis()) == null)
        {
            // 过了今天需要重新创建
            lookup = createDefault();
        }

        return lookup;
    }

    private static DateRangeLookUp createDefault() {
        DateRangeLookUp map = new DateRangeLookUp();
        // Today
        Calendar cal = Calendar.getInstance();
        CalendarFormat.formatAllDay(cal);
        long today = cal.getTimeInMillis();

        long start = today;
        cal.add(Calendar.DATE, 1);
        long end = cal.getTimeInMillis();
        map.register(start, end).setLabel("今天").setFormat("HH:mm");
        // Yesterday
        end = start;
        cal.setTimeInMillis(today);
        cal.add(Calendar.DATE, -1);
        start = cal.getTimeInMillis();
        map.register(start, end).setLabel("昨天").setFormat("昨天 HH:mm");
        // Older
        map.register(0, start).setLabel("更早").setFormat("M月d日");

        return map;
    }
}