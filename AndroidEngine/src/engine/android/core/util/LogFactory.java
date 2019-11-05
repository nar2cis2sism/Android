package engine.android.core.util;

import static engine.android.core.ApplicationManager.getMainApplication;

import engine.android.core.util.LogFactory.LogUtil;
import engine.android.util.AndroidUtil;
import engine.android.util.api.StringUtil;
import engine.android.util.extra.MyThreadFactory;
import engine.android.util.file.FileManager;
import engine.android.util.io.IOUtil;
import engine.android.util.os.DeviceUtil;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 日志管理工厂<p>
 * 功能：日志文件映射关系，开关与导出
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class LogFactory implements Runnable {

    private static File logDir;                                 // 日志输出目录

    private static final AtomicBoolean logEnabled
    = new AtomicBoolean();                                      // 日志开启状态
    
    private static final AtomicBoolean logOpened
    = new AtomicBoolean();                                      // 日志是否开启过

    private static final ConcurrentHashMap<String, String> map
    = new ConcurrentHashMap<String, String>();                  // [类名-日志文件]映射表
    private static final String MAPPING_PREFIX = "mapping:";

    private static final ConcurrentHashMap<String, LogFile> logs
    = new ConcurrentHashMap<String, LogFile>();                 // 日志文件查询表

    private static final String DEFAULT_LOG_FILE = "log.txt";   // 默认日志输出文件
    
    private static final LinkedBlockingQueue<LogRecord> queue   // 日志输出队列
    = new LinkedBlockingQueue<LogRecord>();
    
    private static final ExecutorService executor               // 日志输出单线程池
    = Executors.newSingleThreadExecutor(new MyThreadFactory("log"));
    
    private LogFactory() {}

    /**
     * 开启/关闭日志记录<br>
     * 一般在{@link Application#onCreate()}中开启
     */
    public static void enableLOG(boolean enable) {
        if (logEnabled.compareAndSet(!enable, enable) && logOpened.compareAndSet(false, true))
        {
            if (getMainApplication().isMainProcess())
            {
                // 清理上一次的日志记录
                FileManager.clearDir(getLogDir());
                
                LOG.log(null, null, "程序启动", getMainApplication().getLaunchTime());
                LOG.log(DeviceUtil.getDeviceInfo(), AndroidUtil.getVersionName(getMainApplication()));
            }
            // 启动日志线程
            executor.execute(new LogFactory());
        }
    }

    /**
     * 日志功能是否开启
     */
    public static boolean isLogEnabled() {
        return logEnabled.get();
    }

    /**
     * 获取日志输出目录
     */
    public static File getLogDir() {
        if (logDir == null)
        {
            logDir = getMainApplication().getDir("log", 0);
        }

        return logDir;
    }

    /**
     * 导出日志文件
     */
    public static boolean export(File dir) {
        return FileManager.copyTo(dir, getLogDir().listFiles());
    }

    /**
     * 添加日志文件
     */
    public static void addLogFile(Class<?> logClass, String logFile) {
        map.putIfAbsent(logClass.getName(), logFile);
    }

    /**
     * 添加日志映射
     */
    public static void addLogFile(Class<?> logClass, Class<?> mapClass) {
        String logFile = map.get(mapClass.getName());
        if (logFile == null)
        {
            logFile = MAPPING_PREFIX + mapClass.getName();
        }
        
        addLogFile(logClass, logFile);
    }

    /**
     * 操作日志文件
     */
    private static LogFile getLogFile(String className) {
        String logFile = null;
        if (!TextUtils.isEmpty(className))
        {
            logFile = map.get(className);
            if (logFile != null && logFile.startsWith(MAPPING_PREFIX))
            {
                map.put(className, logFile = map.get(logFile.substring(MAPPING_PREFIX.length())));
            }
        }

        return getOrCreateLogFile(logFile);
    }

    /**
     * @param logFile 日志文件名称
     */
    private static LogFile getOrCreateLogFile(String logFile) {
        if (logFile == null)
        {
            logFile = DEFAULT_LOG_FILE;
        }
        
        LogFile log = logs.get(logFile);
        if (log == null)
        {
            logs.putIfAbsent(logFile, new LogFile(new File(getLogDir(), logFile)));
            log = logs.get(logFile);
        }
        
        return log;
    }

    @Override
    public void run() {
        while (true) {
            try {
                LogRecord log = queue.take();
                if (log != null)
                {
                    getLogFile(log.className).LOG(log);
                }
            } catch (Exception e) {
                // Continue.
            }
        }
    }

    /**
     * 日志输出
     */
    public static final class LOG {
        
        /**
         * 输出日志（取调用函数的类名+方法名作为标签）
         * 
         * @param message 日志内容
         */
        public static void log(Object message) {
            log(LogUtil.getCallerStackFrame(), message);
        }

        /**
         * 输出日志
         * 
         * @param tag 日志标签
         * @param message 日志内容
         */
        public static void log(String tag, Object message) {
            String className = null;
            StackTraceElement stack = LogUtil.getCallerStackFrame();
            if (stack != null)
            {
                className = stack.getClassName();
            }

            log(className, tag, message, System.currentTimeMillis());
        }

        /**
         * 输出日志
         * 
         * @param stack 取函数的类名+方法名作为标签
         * @param message 日志内容
         */
        public static void log(StackTraceElement stack, Object message) {
            String className = null;
            String tag = null;
            if (stack != null)
            {
                className = stack.getClassName();
                tag = LogUtil.getClassAndMethod(stack);
            }

            log(className, tag, message, System.currentTimeMillis());
        }

        private static void log(String className, String tag, Object message, long timeInMillis) {
            String msg = getMessage(message);
            if (isLogEnabled())
            {
                queue.offer(new LogRecord(className, tag, msg, timeInMillis));
            }

            if (getMainApplication().isDebuggable())
            {
                Log.d(tag, msg);
            }
        }
        
        private static String getMessage(Object message) {
            if (message instanceof Throwable)
            {
                return LogUtil.getExceptionInfo((Throwable) message);
            }
            else
            {
                return message == null ? "" : message.toString();
            }
        }
    }
    
    public static final class LogUtil {

        private static final int CURRENT_STACK_FRAME = 3;

        /**
         * 获取当前函数堆栈信息
         */
        public static StackTraceElement getCurrentStackFrame() {
            return getStackFrame(CURRENT_STACK_FRAME);
        }

        /**
         * 获取调用函数堆栈信息
         */
        public static StackTraceElement getCallerStackFrame() {
            return getStackFrame(CURRENT_STACK_FRAME + 1);
        }

        /**
         * 顾名思义
         */
        public static StackTraceElement getSuperCallerStackFrame() {
            return getStackFrame(CURRENT_STACK_FRAME + 2);
        }

        private static StackTraceElement getStackFrame(int index) {
            StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            return stacks.length > ++index ? stacks[index] : null;
        }

        /**
         * 获取异常信息
         */
        public static String getExceptionInfo(Throwable tr) {
            return Log.getStackTraceString(tr);
        }
        
        /**
         * 提取类名+方法名
         */
        public static String getClassAndMethod(StackTraceElement stack) {
            String className = stack.getClassName();
            return String.format("%s.%s", 
                    className.substring(className.lastIndexOf(".") + 1), 
                    stack.getMethodName());
        }

        /**
         * Daimon:获取固定字节长度文本，超过用省略号代替，不足用空格填充
         */
        public static String getFixedText(String text, int length) {
            StringBuilder sb = new StringBuilder();
            int len;

            if (TextUtils.isEmpty(text))
            {
                len = 0;
            }
            else
            {
                sb.append(text);
                len = length(text);
            }

            if (len < length)
            {
                for (int i = len; i < length; i++)
                {
                    sb.append(" ");
                }
            }
            else if (len > length)
            {
                sb.delete(0, sb.length());
                text = substring(text, length - 3);

                sb.append(text).append("...");
                len = length(text) + 3;

                if (len < length)
                {
                    for (int i = len; i < length; i++)
                    {
                        sb.append(" ");
                    }
                }
            }

            return sb.toString();
        }

        private static String substring(String s, int length) {
            int len = length;
            String sub;
            while (length(sub = s.substring(0, len)) > length)
            {
                len--;
            }

            return sub;
        }

        private static int length(String s) {
            return StringUtil.getByteLength(s);
        }
    }
}

/**
 * 日志文件
 */
class LogFile {

    private final File logFile;
    
    private FileWriter fw;

    public LogFile(File logFile) {
        this.logFile = logFile;
    }

    public void LOG(LogRecord record) {
        try {
            if (fw == null)
            {
                fw = new FileWriter(logFile, true); // The file will be created
                                                    // if it does not exist.
            }

            fw.append(record.toString()).append('\n');
            fw.flush();
        } catch (Exception e) {
            if (fw != null)
            {
                IOUtil.closeSilently(fw);
                fw = null;
            }
        }
    }
}

/**
 * 日志记录
 */
class LogRecord {

    private static final Calendar CAL = Calendar.getInstance();
    
    public final String className;

    public final String tag;

    public final String message;

    public final long timeInMillis;

    public LogRecord(String className, String tag, String message, long timeInMillis) {
        this.className = className;
        this.tag = tag;
        this.message = message;
        this.timeInMillis = timeInMillis;
    }

    @Override
    public String toString() {
        if (TextUtils.isEmpty(tag))
        {
            if (TextUtils.isEmpty(message))
            {
                return "";
            }

            return String.format("%s|%s", getTime(), message);
        }

        return String.format("%s|%s|%s", getTime(), getTag(), message);
    }

    public String getTime() {
        CAL.setTimeInMillis(timeInMillis);
        return CalendarFormat.format(CAL, "MM-dd HH:mm:ss.SSS");
    }

    public String getTag() {
        return LogUtil.getFixedText(tag, 40);
    }
}