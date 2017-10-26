package com.project.app.bean;

import engine.android.core.util.CalendarFormat;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class MessageListItem {
    
    public final DateRange category;            // 分类
    public final String date;                   // 日期
    public final String title;                  // 标题
    public final String content;                // 内容
    
    public MessageListItem(long date, String title, String content) {
        category = DateRange.lookupDateRange(date);
        this.date = formatDate(category, date);
        this.title = title;
        this.content = content;
    }
    
    private static String formatDate(DateRange category, long date) {
        Calendar cal = CalendarFormat.getCalendar(date);
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
            return CalendarFormat.format(cal, "昨天HH:mm");
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
        
        private static final List<DateRange> dateList;
        
        static
        {
            dateList = new LinkedList<DateRange>();
            
            // Today
            Calendar cal = Calendar.getInstance();
            CalendarFormat.formatAllDay(cal);
            long today = cal.getTimeInMillis();
            
            long start = today;
            cal.add(Calendar.DAY_OF_MONTH, 1);
            long end = cal.getTimeInMillis();
            DateRange dateRange = new DateRange(start, end, LABLE_TODAY);
            dateList.add(dateRange);
            
            // Yesterday
            cal.setTimeInMillis(today);
            end = today;
            cal.add(Calendar.DAY_OF_MONTH, -1);
            start = cal.getTimeInMillis();
            dateRange = new DateRange(start, end, LABLE_YESTODAY);
            dateList.add(dateRange);
            
            // older
            dateRange = new DateRange(0, start, LABLE_OLDER);
            dateList.add(dateRange);
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