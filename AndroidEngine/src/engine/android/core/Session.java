package engine.android.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 程序会话<p>
 * 功能：储存全局属性，支持多进程数据共享
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public final class Session {

    private static final Object NULL_VALUE = new Object();

    final long launchTime;                                      // 程序启动时间

    private final ConcurrentHashMap<String, Object> attributes; // 会话属性表

    /******************************* 华丽丽的分割线 *******************************/

    private boolean isMultiProcess;                             // 多进程模式

    private ConcurrentHashMap<String, SessionData> changeMap;   // 标识数据改变状态

    private SessionReceiver recevier;                           // 通过广播通讯方式实现数据共享

    private SessionDataSerializable serialize;                  // 数据传输序列化机制

    Session() {
        launchTime = System.currentTimeMillis();
        attributes = new ConcurrentHashMap<String, Object>();
    }

    /**
     * 开启多进程模式（保证数据同步）
     */
    public void startMultiProcessMode(Context context, SessionDataSerializable serialize) {
        isMultiProcess = true;
        changeMap = new ConcurrentHashMap<String, SessionData>();
        recevier = new SessionReceiver(context, changeMap);
        this.serialize = serialize;
        context.registerReceiver(recevier, new IntentFilter(SessionReceiver.ACTION));
    }

    /**
     * 放置属性
     * 
     * @param name 属性名称
     */
    public Session putAttribute(String name) {
        return setAttribute(name, null);
    }

    /**
     * 判断是否存有某个属性
     * 
     * @param name 属性名称
     */
    public boolean hasAttribute(String name) {
        checkChange(name);
        return attributes.containsKey(name);
    }
    
    /**
     * 设置属性（保存或者修改应用程序用到的全局属性）
     * 
     * @param name 属性名
     * @param value 属性值
     */
    public Session setAttribute(String name, Object value) {
        attributes.put(name, value == null ? NULL_VALUE : value);
        if (isMultiProcess && serialize.share(name))
        {
            changeMap.put(name, new SessionData(SessionData.STATUS_SYNC));
            recevier.sendBroadcast(name, serialize.serialize(name, value), SessionData.STATUS_UPDATE);
        }

        return this;
    }

    /**
     * 移除属性
     * 
     * @param name 属性名称
     * @return 移除的属性值
     */
    public Object removeAttribute(String name) {
        Object value = attributes.remove(name);
        if (isMultiProcess && serialize.share(name))
        {
            changeMap.put(name, new SessionData(SessionData.STATUS_SYNC));
            recevier.sendBroadcast(name, null, SessionData.STATUS_REMOVE);
        }

        return value == NULL_VALUE ? null : value;
    }

    /**
     * 获取属性值
     * 
     * @param name 属性名称
     */
    public Object getAttribute(String name) {
        checkChange(name);

        Object value = attributes.get(name);
        return value == NULL_VALUE ? null : value;
    }

    /**
     * 获取属性值
     * 
     * @param name 属性名称
     * @param defaultValue 默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, T defaultValue) {
        checkChange(name);

        Object value = attributes.get(name);
        return value == null || value == NULL_VALUE ? defaultValue : (T) value;
    }

    /**
     * 同步更新
     */
    private void checkChange(String name) {
        if (changeMap == null) return;
        
        SessionData data = changeMap.get(name);
        if (data == null) return;
        
        if (data.status == SessionData.STATUS_REMOVE)
        {
            attributes.remove(name);
        }
        else if (data.status == SessionData.STATUS_UPDATE)
        {
            Object value = serialize.deserialize(name, data.data);
            attributes.put(name, value == null ? NULL_VALUE : value);
        }

        changeMap.remove(name);
    }

    @Override
    public String toString() {
        return attributes.toString();
    }

    /**
     * 多进程传输数据系列化接口
     */
    public interface SessionDataSerializable {
        
        /**
         * 可以指定数据共享
         */
        boolean share(String name);

        byte[] serialize(String name, Object value);

        Object deserialize(String name, byte[] data);
    }
    
    private static class SessionData {

        public static final byte STATUS_NONE   = 0;
        public static final byte STATUS_UPDATE = 1;
        public static final byte STATUS_REMOVE = 2;
        public static final byte STATUS_SYNC   = 3;
        
        public byte status;
        public byte[] data;
        
        public SessionData() {}
        
        public SessionData(byte status) {
            this.status = status;
        }
    }

    public static final class SessionReceiver extends BroadcastReceiver {

        private static final String ACTION = SessionReceiver.class.getName();

        private static final String EXTRA_KEY    = "key";
        private static final String EXTRA_VALUE  = "value";
        private static final String EXTRA_STATUS = "status";

        private final Context context;

        private final ConcurrentHashMap<String, SessionData> changeMap;

        SessionReceiver(Context context, ConcurrentHashMap<String, SessionData> changeMap) {
            this.context = context.getApplicationContext();
            this.changeMap = changeMap;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION.equals(intent.getAction()))
            {
                String key = intent.getStringExtra(EXTRA_KEY);
                byte[] value = intent.getByteArrayExtra(EXTRA_VALUE);
                byte status = intent.getByteExtra(EXTRA_STATUS, SessionData.STATUS_NONE);
                
                SessionData data = changeMap.get(key);
                if (data == null)
                {
                    changeMap.put(key, data = new SessionData());
                }
                
                if (data.status == SessionData.STATUS_SYNC)
                {
                    data.status = SessionData.STATUS_NONE;
                    data.data = null;
                }
                else
                {
                    data.status = status;
                    data.data = value;
                }
            }
        }

        public void sendBroadcast(String key, byte[] value, byte status) {
            Intent intent = new Intent(ACTION);
            intent.putExtra(EXTRA_KEY, key);
            intent.putExtra(EXTRA_VALUE, value);
            intent.putExtra(EXTRA_STATUS, status);
            context.sendBroadcast(intent);
        }
    }
}