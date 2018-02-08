package com.project.app.bean;

import engine.android.core.util.CalendarFormat;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class MessageListItem {

    public final DateRange category;            // 日期分类
    public final String time;                   // 时间
    public final String name;                   // 名称
    public final String message;                // 消息
    
    public MessageListItem(long time, String name, String message) {
        category = DateRange.lookupDateRange(time);
        this.time = formatTime(category, time);
        this.name = name;
        this.message = message;
    }
    
    private static String formatTime(DateRange category, long time) {
        Calendar cal = CalendarFormat.getCalendar(time);
        if (category == null)
        {
            return CalendarFormat.format(cal);
        }
        
        String label = category.getLabel();
        if (DateRange.LABLE_TODAY.equals(label))
        {
            return CalendarFormat.format(cal, "HH:mm");
        }
        else if (DateRange.LABLE_YESTODAY.equals(label))
        {
            return CalendarFormat.format(cal, "昨天");
        }
        else if (DateRange.LABLE_OLDER.equals(label))
        {
            return CalendarFormat.format(cal, "MM月dd日");
        }
        
        return null;
    }
    
    public static class DateRange {
        
        public static final String LABLE_TODAY      = "今天";
        public static final String LABLE_YESTODAY   = "昨天";
        public static final String LABLE_OLDER      = "更早";
        
        private final long start;
        private final long end;
        private final String label;
        
        DateRange(long start, long end, String label) {
            this.start = start;
            this.end = end;
            this.label = label;
        }
        
        boolean includes(long time) {
            return start <= time && time < end;
        }
        
        public String getLabel() {
            return label;
        }
        
        public long getTime() {
            return start;
        }
        
        private static final List<DateRange> dateList;
        
        static
        {
            dateList = new LinkedList<DateRange>();
            
            // Today
            Calendar cal = Calendar.getInstance();
            CalendarFormat.formatAllDay(cal);
            long today = cal.getTimeInMillis();
            
            long start = today;
            cal.add(Calendar.DATE, 1);
            long end = cal.getTimeInMillis();
            dateList.add(new DateRange(start, end, LABLE_TODAY));
            
            // Yesterday
            cal.setTimeInMillis(today);
            end = today;
            cal.add(Calendar.DATE, -1);
            start = cal.getTimeInMillis();
            dateList.add(new DateRange(start, end, LABLE_YESTODAY));
            
            // older
            dateList.add(new DateRange(0, start, LABLE_OLDER));
        }
        
        public static DateRange lookupDateRange(long date) {
            for (DateRange dateRange : dateList)
            {
                if (dateRange.includes(date))
                {
                    return dateRange;
                }
            }
            
            return null;
        }
    }
}