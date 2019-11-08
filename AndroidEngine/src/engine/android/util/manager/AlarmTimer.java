package engine.android.util.manager;

import static android.os.Build.VERSION.SDK_INT;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe class that supports one or more alarm timers
 * 
 * @author Daimon
 * @since 3/21/2014
 */
public class AlarmTimer {

    /** Daimon:ConcurrentHashMap **/
    private static final ConcurrentHashMap<String, AlarmTimer> map
    = new ConcurrentHashMap<String, AlarmTimer>();

    private static final String NAME = "name";

    private final Context context;
    private final String name;
    private final AlarmManager am;
    private final String ACTION;
    private final PendingIntent operation;

    private Runnable timeoutTask;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION.equals(intent.getAction()))
            {
                String name = intent.getStringExtra(NAME);
                AlarmTimer timer = name == null ? null : map.get(name);
                if (timer != null)
                {
                    timer.handleTimeout();
                }
            }
        }
    };

    private AlarmTimerDecorator decorator;

    private AlarmTimer(Context context, String name) {
        this.context = context.getApplicationContext();
        this.name = name;
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        ACTION = context.getPackageName() + getClass().getName() + name;
        Intent intent = new Intent(ACTION).putExtra(NAME, name);
        operation = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    void setDecorator(AlarmTimerDecorator decorator) {
        this.decorator = decorator;
    }
    
    AlarmTimerDecorator getDecorator() {
        return decorator;
    }

    /**
     * Return the named instance.
     */
    public static AlarmTimer getInstance(Context context, String name) {
        if (map.containsKey(name))
        {
            return map.get(name);
        }

        AlarmTimer timer = map.putIfAbsent(name, new AlarmTimer(context, name));
        if (timer == null)
        {
            timer = map.get(name);
            // Register broadcast receiver
            timer.registerBroadcast();
        }

        return timer;
    }

    public void clear() {
        cancel();
        context.unregisterReceiver(receiver);
        map.remove(name);
    }

    private void registerBroadcast() {
        context.registerReceiver(receiver, new IntentFilter(ACTION));
    }
    
    /**
     * @param alarmType One of ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC or
     *             RTC_WAKEUP.
     */
    public void triggerAtTime(int alarmType, long triggerAtMillis, Runnable timeoutTask) {
        this.timeoutTask = timeoutTask;
        if (SDK_INT >= 23)
        {
            am.setExactAndAllowWhileIdle(alarmType, triggerAtMillis, operation);
        }
        else if (SDK_INT >= 19)
        {
            am.setExact(alarmType, triggerAtMillis, operation);
        }
        else
        {
            am.set(alarmType, triggerAtMillis, operation);
        }
    }

    /**
     * cancel the timer
     */
    public void cancel() {
        am.cancel(operation);
    }

    private void handleTimeout() {
        if (decorator != null)
        {
            decorator.handleTimeout();
        }
        else
        {
            notifyTimeout();
        }
    }

    /**
     * notify the timer that a timeout event occurred
     */
    void notifyTimeout() {
        timeoutTask.run();
    }

    private interface AlarmTimerDecorator {

        void handleTimeout();
    }

    public static class IdleTimer implements AlarmTimerDecorator {

        private final AlarmTimer timer;

        private long timeout;
        private final AtomicLong lastEventTime = new AtomicLong();

        private final AtomicBoolean isRunning = new AtomicBoolean(false);

        private IdleTimer(AlarmTimer timer) {
            this.timer = timer;
        }

        /**
         * Return the named instance.
         */
        public static IdleTimer getInstance(Context context, String name) {
            AlarmTimer timer = AlarmTimer.getInstance(context, name);
            AlarmTimerDecorator decorator = timer.getDecorator();
            if (decorator instanceof IdleTimer)
            {
                return (IdleTimer) decorator;
            }
            
            IdleTimer idle = new IdleTimer(timer);
            timer.setDecorator(idle);
            return idle;
        }

        public void clear() {
            timer.clear();
        }

        public boolean isRunning() {
            return isRunning.get();
        }

        /**
         * Start the idle timer if not running
         * 
         * @param timeoutValue The idle timeout interval
         * @param timeoutUnits The idle timeout interval units
         * @param timeoutTask The runnable to run when the timer times out
         * @return true if the task was started and false if it is already running
         */
        public boolean start(long timeoutValue, TimeUnit timeoutUnits, Runnable timeoutTask) {
            if (isRunning.compareAndSet(false, true))
            {
                timeout = timeoutUnits.toMillis(timeoutValue);
                timer.timeoutTask = timeoutTask;
                lastEventTime.set(getCurrentTime());
                setAlarm();

                return true;
            }

            return false;
        }

        private void setAlarm() {
            timer.triggerAtTime(AlarmManager.ELAPSED_REALTIME_WAKEUP, getTimeoutTime(), timer.timeoutTask);
        }

        private static long getCurrentTime() {
            return SystemClock.elapsedRealtime();
        }

        private long getTimeoutTime() {
            return lastEventTime.get() + timeout;
        }

        /**
         * When poked, update the last event time
         */
        public void poke() {
            if (isRunning())
            {
                lastEventTime.set(getCurrentTime());
            }
        }

        /**
         * cancel the idle timer
         */
        public void cancel() {
            if (isRunning.compareAndSet(true, false))
            {
                timer.cancel();
            }
        }

        @Override
        public void handleTimeout() {
            if (isRunning())
            {
                long now = getCurrentTime();
                long timeout = getTimeoutTime();
                if (now < timeout)
                {
                    // If running but the timeout has been moved forward, reset
                    setAlarm();
                }
                else
                {
                    // we've timed out
                    isRunning.set(false);
                    timer.notifyTimeout();
                }
            }
        }
    }
}