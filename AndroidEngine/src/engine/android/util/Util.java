package engine.android.util;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;

/**
 * 工具类
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public final class Util {

    private static final HashMap<String, Object> buffer
    = new HashMap<String, Object>();   // 变量缓存表

    /**
     * 从字符串中取boolean型值
     */
    public static boolean getBoolean(String s) {
        if (!TextUtils.isEmpty(s))
        {
            s = s.trim().toLowerCase();
            if (s.equals("0") || s.equals("n") || s.equals("no")
            ||  s.equals("f") || s.equals("false"))
            {
                return false;
            }
            else
            {
                return true;
            }
        }

        return false;
    }

    /**
     * 从字符串中取整数值
     * 
     * @param defaultValue 如果字符串格式有错，则返回默认值
     */
    public static int getInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 获取字符串（消除空字符串的隐患）
     * 
     * @param defaultValue 默认值
     */
    public static String getString(String s, String defaultValue) {
        return !TextUtils.isEmpty(s) ? s : defaultValue;
    }

    /**
     * 获取字符串（消除空指针异常的隐患）
     * 
     * @param defaultValue 默认值
     */
    public static String getString(Object obj, String defaultValue) {
        return obj == null ? defaultValue : obj.toString();
    }

    /**
     * 产生随机数
     * 
     * @param min 最小值（包含）
     * @param max 最大值（不包含）
     */
    public static int getRandom(int min, int max) {
        final String RANDOM = "random";
        Random random;
        if (buffer.containsKey(RANDOM))
        {
            random = (Random) buffer.get(RANDOM);
        }
        else
        {
            buffer.put(RANDOM, random = new Random());
        }

        return getRandom(random, min, max);
    }

    /**
     * 产生随机数
     * 
     * @param min 最小值（包含）
     * @param max 最大值（不包含）
     */
    public static int getRandom(Random random, int min, int max) {
        if (random == null)
        {
            throw new NullPointerException();
        }

        return Math.abs(random.nextInt()) % (max - min) + min;
    }

    /**
     * 将金钱格式化为国际货币的显示形式（如1,000,000）
     */
    public static String formatMoney(double money) {
        return formatNumber(money, ",##0");
    }

    /**
     * 格式化数字
     */
    public static String formatNumber(double number, String pattern) {
        return getDecimalFormat(pattern).format(number);
    }

    /**
     * 解析数字
     */
    public static Number parseNumber(String number, String pattern) {
        try {
            return getDecimalFormat(pattern).parse(number);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static DecimalFormat getDecimalFormat(String pattern) {
        final String DECIMAL_FORMAT = "decimalFormat";
        DecimalFormat decimalFormat;
        if (buffer.containsKey(DECIMAL_FORMAT))
        {
            decimalFormat = (DecimalFormat) buffer.get(DECIMAL_FORMAT);
        }
        else
        {
            buffer.put(DECIMAL_FORMAT, decimalFormat = new DecimalFormat());
        }

        decimalFormat.applyPattern(pattern);
        return decimalFormat;
    }

    /**
     * 基准为2000年的时间转换为基准为1970年的时间
     * 
     * @param time 单位：秒
     */
    public static Date getTimeTo2000(int time) {
        return new Date(getTime2000Milliseconds() + time * 1000);
    }

    /**
     * 基准为1970年的时间转换为基准为2000年的时间
     * 
     * @return 单位：秒
     */
    public static int getTimeTo2000(Date time) {
        return (int) ((time.getTime() - getTime2000Milliseconds()) / 1000);
    }

    private static long getTime2000Milliseconds() {
        final String TIME2000MILLISECONDS = "time2000Milliseconds";
        long time2000Milliseconds;
        if (buffer.containsKey(TIME2000MILLISECONDS))
        {
            time2000Milliseconds = (Long) buffer.get(TIME2000MILLISECONDS);
        }
        else
        {
            buffer.put(TIME2000MILLISECONDS, time2000Milliseconds =
                    new GregorianCalendar(2000, 0, 1).getTimeInMillis()
                  - new GregorianCalendar(1970, 0, 1).getTimeInMillis());
        }

        return time2000Milliseconds;
    }

    /**
     * 打印对象信息
     */
    public static String toString(Object obj) {
        if (obj == null)
        {
            return "Null";
        }

        try {
            StringBuilder sb = new StringBuilder().append(obj.getClass().getSimpleName()).append("[");
            for (Class<?> c = obj.getClass(); c != Object.class; c = c.getSuperclass())
            {
                for (Field field : c.getDeclaredFields())
                {
                    if (Modifier.isStatic(field.getModifiers()))
                    {
                        continue;
                    }
                    
                    field.setAccessible(true);
                    sb.append(field.getName()).append("=").append(field.get(obj)).append(",");
                }
            }

            return sb.deleteCharAt(sb.length() - 1).append("]").toString();
        } catch (Exception e) {
            return obj.toString();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T obj) {
        if (obj == null)
        {
            return null;
        }

        try {
            Class<?> c = obj.getClass();
            Object clone = c.newInstance();
            for (; c != Object.class; c = c.getSuperclass())
            {
                for (Field field : c.getDeclaredFields())
                {
                    field.setAccessible(true);
                    field.set(clone, field.get(obj));
                }
            }

            return (T) clone;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}