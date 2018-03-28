package engine.android.framework.ui.util;

import engine.android.core.util.CalendarFormat;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * 描述一个日期范围区间
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class DateRange {
    
    private static final Calendar cal = Calendar.getInstance();
    
    private final long start,end;               // 日期起止时间
    
    private int type;                           // 日期类型
    private String label;                       // 显示标签
    private String format;                      // 格式化样式
    
    DateRange(long start, long end) {
        this.start = start;
        this.end = end;
    }
    
    public int getType() {
        return type;
    }
    
    public DateRange setType(int type) {
        this.type = type;
        return this;
    }
    
    public String getLabel() {
        return label;
    }
    
    public DateRange setLabel(String label) {
        this.label = label;
        return this;
    }
    
    public String getFormat() {
        return format;
    }
    
    public DateRange setFormat(String format) {
        this.format = format;
        return this;
    }
    
    /**
     * 时间格式化
     */
    public String format(long time) {
        cal.setTimeInMillis(time);
        return CalendarFormat.format(cal, format);
    }
    
    /**
     * 获取起始时间，一般为当天零点
     */
    public long getTime() {
        return start;
    }
    
    boolean includes(long time) {
        return start <= time && time < end;
    }
    
    public static class DateRangeLookUp {
        
        private final List<DateRange> dateList = new LinkedList<DateRange>();
        
        public DateRange register(long start, long end) {
            DateRange range = new DateRange(start, end);
            dateList.add(range);
            return range;
        }
        
        /**
         * 根据时间查询日期范围
         */
        public DateRange lookup(long time) {
            for (DateRange range : dateList)
            {
                if (range.includes(time))
                {
                    return range;
                }
            }
            
            return null;
        }
    }
}