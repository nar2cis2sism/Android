package engine.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import engine.android.util.file.FileManager;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
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
     * 将键值对拼装成XML格式的字符串
     */
    public static String getXMLString(String key, String value) {
        if (value != null)
        {
            if (value.length() == 0)
            {
                return String.format("<%s />", key);
            }
            else
            {
                return String.format("<%s>%s</%s>", key, value, key);
            }
        }

        return "";
    }

    /**
     * 下载图片
     * 
     * @param url 图片下载地址
     */
    public static Bitmap downloadImage(String url) {
        // android3.0版本开始就强制不能在主线程中访问网络，要把访问网络放在独立的线程中，
        // 否则会抛出android.os.NetworkOnMainThreadException
        try {
            return BitmapFactory.decodeStream(new URL(url).openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

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
     * 基准为2000年的时间转换为基准为1970年的时间
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
                    new Date(2000, 1, 1).getTime() - new Date(1970, 1, 1).getTime());
        }

        return time2000Milliseconds;
    }

    /**
     * Do not allow media scan
     */
    public static void disableMediaScan(File file) {
        FileManager.createFileIfNecessary(new File(file.getParentFile(), ".nomedia"));
    }

    /**
     * 打印对象信息
     */
    public static String toString(Object obj) {
        if (obj == null)
        {
            return "null";
        }

        try {
            StringBuilder sb = new StringBuilder("[").append(obj.getClass().getSimpleName()).append("]");
            for (Class<?> c = obj.getClass(); c != Object.class; c = c.getSuperclass())
            {
                for (Field field : c.getDeclaredFields())
                {
                    field.setAccessible(true);
                    sb.append("\n").append(field.getName()).append(":").append(field.get(obj));
                }
            }

            return sb.toString();
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

    /**
     * 我们认为长宽比大于4:3的就为宽屏
     */
    public static boolean isWideScreen(int width, int height) {
        int max = width;
        int min = height;
        if (max < min)
        {
            max = max ^ min;
            min = max ^ min;
            max = max ^ min;
        }
        
        return max * 3 > min * 4;
    }

    /**
     * 根据百分比值计算透明度
     */
    public static int computeAlpha(float percent) {
        return Math.round((Byte.MAX_VALUE - Byte.MIN_VALUE) * percent / 100);
    }
}