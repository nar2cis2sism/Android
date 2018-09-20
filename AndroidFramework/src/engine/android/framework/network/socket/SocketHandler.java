package engine.android.framework.network.socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;

import protocol.socket.SimpleData;

import engine.android.framework.network.socket.SocketResponse.SocketTimeout;
import engine.android.http.HttpConnector;

import java.util.concurrent.TimeUnit;

/**
 * 辅助类，提供一个后台循环线程<br>
 * 功能：断线自动重连+心跳包发送+超时处理
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2016
 */
class SocketHandler {
    
    private final SocketManager manager;
    
    private Handler handler;
    
    private ConnectManager conn;
    private HeartbeatManager heartbeat;
    private TimeoutManager timeout;
    
    public SocketHandler(SocketManager manager) {
        this.manager = manager;
    }
    
    public void setup(Context context) {
        if (handler != null)
        {
            return;
        }
        
        HandlerThread thread = new HandlerThread("socket保活线程");
        thread.start();
        handler = new Handler(thread.getLooper(), timeout = new TimeoutManager());
        
        conn = new ConnectManager(context);
        heartbeat = new HeartbeatManager();
    }
    
    public void reconnect(long delay) {
        handler.postDelayed(conn, delay);
    }
    
    public HeartbeatManager heartbeat() {
        return heartbeat;
    }
    
    public void setTimeout(int msgId, SocketTimeout timeout) {
        this.timeout.set(msgId, timeout);
    }

    /**
     * 自动重连机制
     */
    public class ConnectManager extends BroadcastReceiver implements Runnable {
        
        private final IntentFilter filter;
        private boolean isAccessible;
        
        public ConnectManager(Context context) {
            filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            isAccessible = HttpConnector.isAccessible(context);
            context.registerReceiver(this, filter, null, handler);
        }
    
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean noNetwork = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (isAccessible ^ noNetwork)
            {
                return;
            }
            
            isAccessible = !noNetwork;
            handler.removeCallbacks(this);
            run();
        }
    
        @Override
        public void run() {
            if (isAccessible) manager.reconnect();
        }
    }
    
    /**
     * 心跳机制
     */
    public class HeartbeatManager implements Runnable {
        
        private static final int TRIGGER_AHEAD = 10;         // 心跳包提前发送时间，单位：秒
        
        private long interval;                               // 心跳包循环发送间隔，单位：毫秒
        private long lastTriggerTime = -1;
        
        public void start(int interval) {
            this.interval = TimeUnit.SECONDS.toMillis(interval - TRIGGER_AHEAD);
            poke();
        }
        
        public void stop() {
            lastTriggerTime = -1;
            handler.removeCallbacks(this);
        }
        
        public void poke() {
            if (interval <= 0)
            {
                return;
            }
            
            if (lastTriggerTime == -1)
            {
                lastTriggerTime = SystemClock.uptimeMillis();
                post();
            }
            else
            {
                lastTriggerTime = SystemClock.uptimeMillis();
            }
        }
        
        private void post() {
            handler.postAtTime(this, lastTriggerTime + interval);
        }

        @Override
        public void run() {
            if (SystemClock.uptimeMillis() - lastTriggerTime < interval)
            {
                post();
            }
            else
            {
                lastTriggerTime = -1;
                manager.sendSocketRequest(new SimpleData(), null);
            }
        }
    }
    
    /**
     * 超时机制
     */
    public class TimeoutManager implements Callback {
        
        public void set(int msgId, SocketTimeout timeout) {
            int delay = timeout.getTimeout();
            if (delay <= 0)
            {
                return;
            }
            
            Message msg = Message.obtain();
            msg.what = msgId;
            msg.obj = timeout;
            handler.sendMessageDelayed(msg, delay);
        }

        @Override
        public boolean handleMessage(Message msg) {
            manager.onTimeout(msg.what, (SocketTimeout) msg.obj);
            return true;
        }
    }
}