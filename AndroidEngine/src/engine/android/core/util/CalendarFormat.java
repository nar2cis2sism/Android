package engine.android.core.util;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 常见日期格式化转换符
 转换符说明示例 
 %te  一个月中的某一天（1～31）  2 
 %tb  指定语言环境的月份简称  Feb（英文）、二月（中文） 
 %tB  指定语言环境的月份全称  February（英文）、二月（中文） 
 %tA  指定语言环境的星期几全称  Monday（英文）、星期一（中文） 
 %ta  指定语言环境的星期几简称  Mon（英文）、周一（中文） 
 %tc  包括全部日期和时间信息  星期四 十一月 26 10:26:30 CST 2009 
 %tY  4位年份  2009 
 %tj  一年中的第几天（001～366）  085 
 %tm  月份  03 
 %td  一个月中的第几天（01～31）  08 
 %ty  2位年份  09 

 常见时间格式化转换符
 转换符说明示例 
 %tH  2位数字的24小时制的小时（00～23）  14 
 %tI  2位数字的12小时制的小时（01～12）  05 
 %tk  2位数字的24小时制的小时（1～23）  5 
 %tl  2位数字的12小时制的小时（1～12）  10 
 %tM  2位数字的分钟（00～59）  05 
 %tS  2位数字的秒数（00～60）  12 
 %tL  3位数字的毫秒数（000～999）  920 
 %tN  9位数字的微秒数（000000000～999999999）  062000000000 
 %tp  指定语言环境下上午或下午标记  下午（中文）、pm（英文） 
 %tz  相对于GMT RFC 82格式的数字时区偏移量  +0800 
 %tZ  时区缩写形式的字符串  CST 
 %ts  1970-01-01 00:00:00至现在经过的秒数  1206426646 
 %tQ  1970-01-01 00:00:00至现在经过的毫秒数  1206426737453 

 常见日期时间组合转换符
 转换符说明示例 
 %tF  “年-月-日”格式（4位年份）  2009-01-26 
 %tD  “月/日/年”格式（2位年份）  03/25/09 
 %tr  “时：分：秒 PM（AM）”格式（12小时制）  03:22:06 下午 
 %tT  “时：分：秒”格式（24小时制）  15:23:50 
 %tR  “时：分”格式（24小时制）  15:23 
 */

/**
 * 日期和时间的格式化工具（支持I18N）<p>
 * 功能：强大...<br>
 * 扩展：星座解析，数字翻译，精确时间，时区转换
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class CalendarFormat {

    private static final char PATTERN_INVALID = '\0';

    /**
     * 标准格式化样式<br>
     * 支持：GyMdkHmsSEDFwWahKzZLc<br>
     * E.G. yyyy-MM-dd HH:mm:ss.SSS EEEE
     */
    public static final String PATTREN_NORMAL = "yyyy-MM-dd HH:mm:ss";

    /**
     * 自定义格式化样式，E.G.<br>
     * %cyyyy(c)    二零零八(年)
     * %cM(c)       十一(月)
     * %cd(c)       二十六(日)
     * %cH(c)       三（时）
     * %cmm(c)      零六（分）
     * %css(c)      五十二（秒）
     * %ca          下午
     * %cE          星期六
     */
    public static final String PATTERN_CUSTOM   = "%c";

    /**
     * 二零零八年十一月二十六日
     */
    public static final String CHINESE_DATE     = "%cF";

    /**
     * 下午三时零六分五十二秒
     */
    public static final String CHINESE_TIME     = "%cr";

    /**
     * 中国时区
     */
    public static final TimeZone CHINA = TimeZone.getTimeZone("GMT+8:00");
    
    /**
     * 重庆时区
     */
    public static final TimeZone CHONGQING = TimeZone.getTimeZone("Asia/Chongqing");

    /**
     * 日期时间格式化对象<br>
     * Daimon:ThreadLocal
     */
    private static final ThreadLocal<SimpleDateFormat> dateTimeFormat
    = new ThreadLocal<SimpleDateFormat>() {
        
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(PATTREN_NORMAL);
        };
    };

    /**
     * 设置时区
     */
    public static void setTimeZone(TimeZone timezone) {
        dateTimeFormat.get().setTimeZone(timezone);
    }

    /******************************* 星座 *******************************/

    private static final String[] STARS = {
            "魔羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座",
            "巨蟹座", "狮子座", "处女座", "天秤座", "天羯座", "射手座"
    };

    private static final int[][] STARS_DATES = {
            { 1, 20 }, { 2, 19 }, { 3, 20 }, {  4, 20 }, {  5, 21 }, {  6, 21 },
            { 7, 22 }, { 8, 22 }, { 9, 23 }, { 10, 23 }, { 11, 22 }, { 12, 21 }
    };

    /**
     * 格式化日期
     */
    public static String format(Calendar date) {
        return dateTimeFormat.get().format(date.getTime());
    }

    /**
     * 格式化日期
     * 
     * @param pattern 日期样式
     */
    public static String format(Calendar date, String pattern) {
        int index = pattern.indexOf(PATTERN_CUSTOM);
        if (index >= 0)
        {
            pattern += PATTERN_INVALID;

            StringBuilder sb = new StringBuilder();

            int year = date.get(Calendar.YEAR);
            int month = date.get(Calendar.MONTH);
            int day_of_month = date.get(Calendar.DAY_OF_MONTH);
            int day_of_week = date.get(Calendar.DAY_OF_WEEK);
            int hour = date.get(Calendar.HOUR);
            int hour_of_day = date.get(Calendar.HOUR_OF_DAY);
            int am_pm = date.get(Calendar.AM_PM);
            int minite = date.get(Calendar.MINUTE);
            int second = date.get(Calendar.SECOND);

            int start = 0;
            int len = pattern.length();
            do
            {
                sb.append(pattern.substring(start, index));
                index += 2;
                char c = pattern.charAt(index);
                switch (c) {
                    case 'F':
                        sb.append(translate(year, true)).append("年")
                                .append(getNumber(month + 1, false)).append("月")
                                .append(getNumber(day_of_month, false)).append("日");
                        break;
                    case 'r':
                        sb.append(getAM_PM(am_pm))
                                .append(getNumber(hour, false)).append("时")
                                .append(getNumber(minite, true)).append("分")
                                .append(getNumber(second, true)).append("秒");
                        break;

                    default:
                        char last = c;
                        int count = 1;
                        for (int i = index + 1; i < len; i++)
                        {
                            c = pattern.charAt(i);
                            if (c == last)
                            {
                                count++;
                                continue;
                            }

                            index = i;
                            switch (last) {
                                case 'y':
                                    sb.append(translate(year, true).substring(4 - count));
                                    last = '年';
                                    break;
                                case 'M':
                                    sb.append(getNumber(month + 1, count > 1));
                                    last = '月';
                                    break;
                                case 'd':
                                    sb.append(getNumber(day_of_month, count > 1));
                                    last = '日';
                                    break;
                                case 'H':
                                    sb.append(count == 1 ? getNumber(hour, false)
                                            : getNumber(hour_of_day, false));
                                    last = '时';
                                    break;
                                case 'm':
                                    sb.append(getNumber(minite, count > 1));
                                    last = '分';
                                    break;
                                case 's':
                                    sb.append(getNumber(second, count > 1));
                                    last = '秒';
                                    break;
                                case 'a':
                                    sb.append(getAM_PM(am_pm));
                                    c = PATTERN_INVALID;
                                    break;
                                case 'E':
                                    sb.append(getWeekDay(day_of_week));
                                    c = PATTERN_INVALID;
                                    break;

                                default:
                                    c = PATTERN_INVALID;
                            }

                            if (c == 'c')
                            {
                                sb.append(last);
                            }
                            else
                            {
                                index--;
                            }

                            break;
                        }
                }

                start = ++index;
            } while ((index = pattern.indexOf(PATTERN_CUSTOM, index)) >= 0);

            if (start < len)
            {
                sb.append(pattern.substring(start, len));
            }

            pattern = sb.toString().trim();
        }

        return format(date.getTime(), pattern);
    }

    private static String format(Date date, String pattern) {
        if (pattern.contains("%t"))
        {
            int size = 0;
            Matcher m = Pattern.compile("%t[A-Za-z]").matcher(pattern);
            boolean flag = m.find();
            while (flag)
            {
                size++;
                flag = m.find(m.end());
            }

            if (size > 0)
            {
                Object[] array = new Object[size];
                Arrays.fill(array, date);
                pattern = String.format(pattern, array);
            }
        }

        SimpleDateFormat format = dateTimeFormat.get();
        format.applyPattern(pattern);
        try {
            return format.format(date);
        } finally {
            format.applyPattern(PATTREN_NORMAL);
        }
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Calendar getCalendar(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal;
    }

    /**
     * 将字符串转化为日期型数据
     * 
     * @param pattern 必须是标准格式化样式
     * @see CalendarFormat#PATTREN_NORMAL
     */
    public static Calendar parse(String src, String pattern) throws ParseException {
        SimpleDateFormat format = dateTimeFormat.get();
        format.applyPattern(pattern);
        try {
            return getCalendar(format.parse(src));
        } finally {
            format.applyPattern(PATTREN_NORMAL);
        }
    }

    /**
     * 获取日期所属的星座
     */
    public static String getStar(Calendar date) {
        return getStar(new int[] {
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH)
        });
    }

    private static String getStar(int[] a) {
        for (int i = 0; i < STARS_DATES.length; i++)
        {
            int[] b = STARS_DATES[i];
            if (compare(a, b, 0) <= 0)
            {
                return STARS[i];
            }
        }

        return STARS[0];
    }

    private static int compare(int[] a, int[] b, int index) {
        int i = a[index];
        int j = b[index];
        if (i > j)
        {
            return 1;
        }
        else if (i < j)
        {
            return -1;
        }
        else
        {
            if (++index >= a.length)
            {
                return 0;
            }
            else
            {
                return compare(a, b, index);
            }
        }
    }

    private static String getNumber(int number, boolean add0) {
        String s = String.valueOf(number);
        if (add0 && number < 10)
        {
            s = "0" + s;
        }

        return translate(s, false);
    }

    private static String getWeekDay(int day_of_week) {
        switch (day_of_week) {
            case Calendar.SUNDAY:
                return "星期日";
            case Calendar.MONDAY:
                return "星期一";
            case Calendar.TUESDAY:
                return "星期二";
            case Calendar.WEDNESDAY:
                return "星期三";
            case Calendar.THURSDAY:
                return "星期四";
            case Calendar.FRIDAY:
                return "星期五";
            case Calendar.SATURDAY:
                return "星期六";
        }

        return "";
    }

    private static String getAM_PM(int am_pm) {
        switch (am_pm) {
            case Calendar.AM:
                return "上午";
            case Calendar.PM:
                return "下午";
        }

        return "";
    }

    /**
     * 转换数字为中文显示
     * 
     * @param number 如2008
     * @param literal true译为二零零八,false译为二千零八
     */
    public static String translate(int number, boolean literal) {
        return translate(String.valueOf(number), literal);
    }

    private static String translate(String s, boolean literal) {
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; i++)
        {
            sb.append(translate(s.charAt(i)));
        }

        if (!literal)
        {
            boolean has0 = false;
            int index = 0;
            int i;

            while (len > index++)
            {
                i = len - index;
                if (s.charAt(i) > '0')
                {
                    sb.insert(i + 1, translate(index));
                    has0 = false;
                }
                else
                {
                    if ((index - 1) % 4 == 0)
                    {
                        sb.replace(i, i + 1, translate(index));
                        has0 = true;
                    }
                    else
                    {
                        if (has0)
                        {
                            sb.deleteCharAt(i);
                        }
                        else
                        {
                            has0 = true;
                        }
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * @param position 从右至左的位数
     * @return 个十百千万亿
     */
    private static String translate(int position) {
        switch (position % 4) {
            case 0:
                return "千";
            case 1:
                switch (position % 4) {
                    case 1:
                        return "万";
                    case 2:
                        return "亿";
                }

                break;
            case 2:
                return "十";
            case 3:
                return "百";
        }

        return "";
    }

    private static char translate(char c) {
        switch (c) {
            case '0':
                c = '零';
                break;
            case '1':
                c = '一';
                break;
            case '2':
                c = '二';
                break;
            case '3':
                c = '三';
                break;
            case '4':
                c = '四';
                break;
            case '5':
                c = '五';
                break;
            case '6':
                c = '六';
                break;
            case '7':
                c = '七';
                break;
            case '8':
                c = '八';
                break;
            case '9':
                c = '九';
                break;
        }

        return c;
    }

    /**
     * @return e.g. 中国标准时间
     */
    public static String getTimeZoneName(Calendar date) {
        TimeZone tz = TimeZone.getDefault();
        return tz.getDisplayName(tz.inDaylightTime(date.getTime()), TimeZone.LONG);
    }

    /**
     * @return e.g. GMT+08:00
     */
    public static String getTimeZoneText(Calendar date) {
        TimeZone tz = TimeZone.getDefault();
        int offset = tz.getRawOffset();
        if (tz.inDaylightTime(date.getTime()))
        {
            offset += tz.getDSTSavings();
        }

        offset /= 60000;
        char sign = '+';
        if (offset < 0)
        {
            sign = '-';
            offset = -offset;
        }

        StringBuilder sb = new StringBuilder(9);
        sb.append("GMT");
        sb.append(sign);
        appendNumber(sb, 2, offset / 60);
        sb.append(':');
        appendNumber(sb, 2, offset % 60);
        return sb.toString();
    }

    private static void appendNumber(StringBuilder sb, int count, int value) {
        String s = Integer.toString(value);
        if (count > s.length())
        {
            for (int i = 0, len = count - s.length(); i < len; i++)
            {
                sb.append("0");
            }
        }

        sb.append(s);
    }

    /**
     * 精确到时/分/秒
     * 
     * @param precision {@link Calendar#HOUR_OF_DAY}
     */
    public static void formatPrecision(Calendar date, int precision) {
        while (precision < Calendar.MILLISECOND) {
            date.set(++precision, 0);
        }
    }

    /**
     * 精确到天
     */
    public static void formatAllDay(Calendar date) {
        formatPrecision(date, Calendar.DAY_OF_WEEK_IN_MONTH);
    }

    /**************** Customize some utility formatters for consistency ****************/

    /** If set, then the weekday is shown. **/
    public static final int SHOW_WEEKDAY = DateUtils.FORMAT_SHOW_WEEKDAY;
    /** If set, then the year is always shown. **/
    public static final int SHOW_YEAR = DateUtils.FORMAT_SHOW_YEAR;
    /** If set, then the year is not shown. **/
    public static final int NO_YEAR = DateUtils.FORMAT_NO_YEAR;
    /**
     * If set, then if the date is shown, just the month name will be shown, not
     * the day of the month.
     **/
    public static final int NO_MONTH_DAY = DateUtils.FORMAT_NO_MONTH_DAY;
    /** If set, then the weekday (if shown) is abbreviated to a 3-letter string. **/
    public static final int ABBREV_WEEKDAY = DateUtils.FORMAT_ABBREV_WEEKDAY;
    /** If set, then the month (if shown) is abbreviated to a 3-letter string. **/
    public static final int ABBREV_MONTH = DateUtils.FORMAT_ABBREV_MONTH;
    /**
     * If set, then the weekday and the month (if shown) are abbreviated to
     * 3-letter strings.
     **/
    public static final int ABBREV_ALL = DateUtils.FORMAT_ABBREV_ALL;

    private static final Pattern dateTimeBySetting = Pattern.compile("\\d+\\D+\\d+\\D+\\d+");

    /**
     * Based on local language, it will adopt native words and order.(No time)
     * e.g.
     * English:  Wed, Jan 16, 2013
     * Japanese: 2013年1月16日（水）
     */
    public static String formatDateByLocale(long inTimeInMillis, int flags) {
        return DateUtils.formatDateTime(null, inTimeInMillis, flags |
                DateUtils.FORMAT_SHOW_DATE);
    }

    /**
     * Based on local language, it will adopt native words and order.
     * e.g.
     * English:  Wed, Jan 16, 2013
     * Japanese: 2013年1月16日（水）
     * @param showTime If true, then the time is shown with fixed format.
     */
    public static String formatDateTimeByLocale(Context context, long inTimeInMillis,
            int flags, boolean showTime) {
        String date = formatDateByLocale(inTimeInMillis, flags);

        if (showTime)
        {
            date += " " + formatTime(context, inTimeInMillis);
        }

        return date;
    }

    /**
     * Followed on device setting, it will adopt system symbol and order 
     * but it's limited to date and weekday excluded.(No time)
     * e.g.
     * English:  Wed, 2013/01/16
     * Japanese: 2013/01/16（水）
     */
    public static String formatDateBySetting(Context context, long inTimeInMillis,
            int flags) {
        return formatDateTimeBySetting(context, inTimeInMillis, flags, false);
    }

    /**
     * Followed on device setting, it will adopt system symbol and order 
     * but it's limited to date and weekday excluded.
     * e.g.
     * English:  Wed, 2013/01/16
     * Japanese: 2013/01/16（水）
     * 
     * @param showTime If true, then the time is shown with fixed format.
     */
    public static String formatDateTimeBySetting(Context context, long inTimeInMillis,
            int flags, boolean showTime) {
        java.text.DateFormat dateFormat =
                android.text.format.DateFormat.getDateFormat(context);
        String date = dateFormat.format(inTimeInMillis);
        if ((flags & SHOW_WEEKDAY) != 0)
        {
            // If show the weekday, then replace the date with system format and
            // keep the weekday unmodified.
            flags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE;
            String s = DateUtils.formatDateTime(null, inTimeInMillis, flags);
            Matcher m = dateTimeBySetting.matcher(s);
            if (m.find())
            {
                date = s.replace(m.group(), date);
            }
        }

        if (showTime)
        {
            date += " " + formatTime(context, inTimeInMillis);
        }

        return date;
    }

    /**
     * According to time setting of device and maybe effected by local language
     * format.
     */
    public static String formatTime(Context context, long inTimeInMillis) {
        return android.text.format.DateFormat.getTimeFormat(context).format(inTimeInMillis);
    }

    /**
     * Retrieve the time setting of device.
     */
    public static boolean is24HourFormat(Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }
}