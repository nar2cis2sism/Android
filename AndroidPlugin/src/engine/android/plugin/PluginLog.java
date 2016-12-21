package engine.android.plugin;

import android.util.Log;

public class PluginLog {
    
    public static boolean DEBUGGABLE = true;

    public static void debug(Object message) {
        if (!DEBUGGABLE) return;
        
        debug(getCallerStackFrame(), message);
    }

    public static void debug(StackTraceElement stack, Object message) {
        if (!DEBUGGABLE) return;
        
        String tag = null;
        if (stack != null)
        {
            String className = stack.getClassName();
            tag = String.format("%s.%s",
                    className.substring(className.lastIndexOf(".") + 1),
                    stack.getMethodName());
        }

        debug(tag, message);
    }
    
    public static void debug(String tag, Object message) {
        if (!DEBUGGABLE) return;
        
        String msg;
        if (message instanceof Throwable)
        {
            msg = Log.getStackTraceString((Throwable) message);
        }
        else
        {
            msg = message == null ? "" : message.toString();
        }
        
        Log.d(tag, msg);
    }

    public static void log(Object message) {
        log(getCallerStackFrame(), message);
    }

    public static void log(StackTraceElement stack, Object message) {
        String tag = null;
        if (stack != null)
        {
            String className = stack.getClassName();
            tag = String.format("%s.%s",
                    className.substring(className.lastIndexOf(".") + 1),
                    stack.getMethodName());
        }

        log(tag, message);
    }
    
    public static void log(String tag, Object message) {
        String msg;
        if (message instanceof Throwable)
        {
            msg = Log.getStackTraceString((Throwable) message);
        }
        else
        {
            msg = message == null ? "" : message.toString();
        }
        
        Log.i(tag, msg);
    }

    private static final int CURRENT_STACK_FRAME = 3;

    public static final StackTraceElement getCallerStackFrame() {
        return getStackFrame(CURRENT_STACK_FRAME + 1);
    }

    private static final StackTraceElement getStackFrame(int index) {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        return stacks.length > ++index ? stacks[index] : null;
    }
}