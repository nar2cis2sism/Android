package engine.android.core.util;

import static engine.android.core.ApplicationManager.getMainApplication;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import android.util.StringBuilderPrinter;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import engine.android.core.ApplicationManager;

/**
 * 日志管理工厂<p>
 * 功能：日志文件映射关系，开关与导出
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class LogFactory {

    private static File logDir;                                 // 日志输出目录

    private static final AtomicBoolean logEnabled
    = new AtomicBoolean();                                      // 日志开启状态
    
    private static final AtomicBoolean logOpened
    = new AtomicBoolean();                                      // 日志是否开启过

    private static final ConcurrentHashMap<String, String> map
    = new ConcurrentHashMap<String, String>();                  // [类名-日志文件]映射表

    private static final ConcurrentHashMap<String, LogFile> logs
    = new ConcurrentHashMap<String, LogFile>();                 // 日志文件查询表

    private static final String DEFAULT_LOG_FILE = "log.txt";   // 默认日志输出文件

    /**
     * 开启/关闭日志记录<br>
     * 一般在{@link Application#onCreate()}中开启
     */
    public static void enableLOG(boolean enable) {
        if (logEnabled.compareAndSet(!enable, enable) && logOpened.compareAndSet(false, true))
        {
            LOG.log(null, null, "程序启动", getMainApplication().getLaunchTime());
            // 输出空行
            LOG.log("", null);
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
    private static synchronized File getLogDir() {
        if (logDir == null)
        {
            String logDirName = "log";
            // 其它进程加后缀，与主进程Log区分开
            String processName = ApplicationManager.getProcessName();
            if (!TextUtils.equals(processName, getMainApplication().getApplicationInfo().processName))
            {
                logDirName += "-" + processName;
            }
            
            logDir = getMainApplication().getDir(logDirName, 0);
            purge();
        }

        return logDir;
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
        if (logFile != null)
        {
            map.putIfAbsent(logClass.getName(), logFile);
        }
    }

    /**
     * 操作日志文件
     */
    private static LogFile getLogFile(String className) {
        String logFile;
        if (TextUtils.isEmpty(className))
        {
            logFile = DEFAULT_LOG_FILE;
        }
        else
        {
            logFile = map.get(className);
            if (logFile == null)
            {
                logFile = DEFAULT_LOG_FILE;
            }
        }

        return getOrCreateLogFile(logFile);
    }

    /**
     * @param logFile 日志文件名称
     */
    private static LogFile getOrCreateLogFile(String logFile) {
        LogFile log = logs.get(logFile);
        if (log == null)
        {
            logs.putIfAbsent(logFile, new LogFile(new File(getLogDir(), logFile)));
            log = logs.get(logFile);
        }
        
        return log;
    }

    /**
     * 清理上一次的日志记录
     */
    private static void purge() {
        if (logDir.exists())
        {
            for (File f : logDir.listFiles())
            {
                delete(f);
            }
        }
    }

    /**
     * 删除文件或目录（即使包含有文件）
     */
    private static void delete(File file) {
        if (!file.exists())
        {
            return;
        }

        if (file.isDirectory())
        {
            for (File f : file.listFiles())
            {
                delete(f);
            }

            file.delete();
        }
        else
        {
            file.delete();
        }
    }

    /**
     * 需要导出日志时调用此方法获取日志目录（会阻塞当前线程）
     */
    public static File flush() {
        for (LogFile log : logs.values())
        {
            try {
                log.flush();
            } catch (Exception e) {
                Log.w(LogFactory.class.getName(),
                        String.format("Failed to flush Log:\n%s", log), e);
            }
        }
        
        return getLogDir();
    }

    /**
     * 日志文件
     */
    private static class LogFile implements Runnable {

        private static final int CAPACITY = 10;

        private static final ConcurrentLinkedQueue<LogRecord> logs
        = new ConcurrentLinkedQueue<LogRecord>();

        private final File logFile;

        private final AtomicBoolean isFlushing = new AtomicBoolean();

        public LogFile(File logFile) {
            this.logFile = logFile;
        }

        public void LOG(String tag, String message, long timeInMillis) {
            logs.offer(new LogRecord(tag, message, timeInMillis));
            if (logs.size() >= CAPACITY && isFlushing.compareAndSet(false, true))
            {
                new Thread(this, logFile.getName()).start();
            }
        }

        public synchronized void flush() throws Exception {
            FileWriter fw = null;
            try {
                fw = new FileWriter(logFile, true); // The file will be created
                                                    // if it does not exist.
                LogRecord record;
                while ((record = logs.peek()) != null)
                {
                    fw.append(record.toString()).append('\n');
                    logs.poll();
                }
            } finally {
                if (fw != null)
                {
                    fw.close();
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            StringBuilderPrinter printer = new StringBuilderPrinter(sb);
            LogRecord record;
            while ((record = logs.poll()) != null)
            {
                printer.println(record.toString());
            }

            return sb.toString();
        }

        @Override
        public void run() {
            try {
                flush();
            } catch (Exception e) {
                Log.w(getClass().getName(),
                        String.format("Failed to write into Log (%s)",
                                logFile.getName()), e);
            }

            isFlushing.set(false);
        }

        /**
         * 日志记录
         */
        private static class LogRecord {

            private static final Calendar CAL = Calendar.getInstance();

            private final String tag;

            private final String message;

            private final long timeInMillis;

            public LogRecord(String tag, String message, long timeInMillis) {
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
                LogFile log = getLogFile(className);
                if (log != null)
                {
                    log.LOG(tag, msg, timeInMillis);
                }
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
            return s.replaceAll("[^\\x00-\\xff]", "**").length();
        }
    }
}