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
 * @version N
 * @since 6/6/2014
 */
public final class Session {

    private static final Object NULL_VALUE = new Object();

    private final long launchTime;                              // 程序启动时间

    private final ConcurrentHashMap<String, Object> attributes; // 会话属性表

    /******************************* 华丽丽的分割线 *******************************/

    private boolean isMultiProcess;                             // 多进程模式

    private static final byte STATUS_NONE   = 0;
    private static final byte STATUS_UPDATE = 1;
    private static final byte STATUS_REMOVE = 2;
    private static final byte STATUS_SYNC   = 3;

    private ConcurrentHashMap<String, Byte> changeMap;          // 标识数据改变状态

    private SessionReceiver recevier;                           // 通过广播通讯方式

    private SessionDataSource source;                           // 数据源

    Session() {
        launchTime = System.currentTimeMillis();
        attributes = new ConcurrentHashMap<String, Object>();
    }

    /**
     * 开启多进程模式（保证数据同步）
     * 
     * @param source 数据来源（同取一个地方的数据使数据一致）
     */

    public void startMultiProcessMode(Context context, SessionDataSource source) {
        isMultiProcess = true;
        changeMap = new ConcurrentHashMap<String, Byte>();
        recevier = new SessionReceiver(context, changeMap);
        this.source = source;
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
        if (value == null)
        {
            value = NULL_VALUE;
        }
        
        attributes.put(name, value);

        if (isMultiProcess)
        {
            source.setAttribute(name, value);
            changeMap.put(name, STATUS_SYNC);
            recevier.sendBroadcast(name, STATUS_UPDATE);
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

        if (isMultiProcess)
        {
            source.setAttribute(name, null);
            changeMap.put(name, STATUS_SYNC);
            recevier.sendBroadcast(name, STATUS_REMOVE);
        }

        return value;
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
        return value == NULL_VALUE ? defaultValue
                : (value == null ? defaultValue : (T) value);
    }

    /**
     * 同步更新
     */

    private void checkChange(String name) {
        if (changeMap == null)
        {
            return;
        }

        Byte changeStatus = changeMap.get(name);
        if (changeStatus == null)
        {
            return;
        }

        if (changeStatus == STATUS_REMOVE)
        {
            attributes.remove(name);
        }
        else if (changeStatus == STATUS_UPDATE)
        {
            Object value = source.getAttribute(name);
            if (value == null)
            {
                attributes.remove(name);
            }
            else
            {
                attributes.put(name, value);
            }
        }

        changeMap.remove(name);
    }

    /**
     * 获取会话创建时间
     */

    public long getCreationTime() {
        return launchTime;
    }

    @Override
    public String toString() {
        return attributes.toString();
    }

    public static interface SessionDataSource {

        public void setAttribute(String name, Object value);

        public Object getAttribute(String name);
    }

    public static final class SessionReceiver extends BroadcastReceiver {

        public static final String ACTION = SessionReceiver.class.getName();

        public static final String EXTRA_KEY    = "key";
        public static final String EXTRA_STATUS = "status";

        private final Context context;

        private final ConcurrentHashMap<String, Byte> changeMap;

        SessionReceiver(Context context, ConcurrentHashMap<String, Byte> changeMap) {
            this.context = context.getApplicationContext();
            this.changeMap = changeMap;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION.equals(intent.getAction()))
            {
                String key = intent.getStringExtra(EXTRA_KEY);
                byte status = intent.getByteExtra(EXTRA_STATUS, STATUS_NONE);

                Byte changeStatus = changeMap.get(key);
                if (changeStatus == null || changeStatus != STATUS_SYNC)
                {
                    changeStatus = status;
                }
                else
                {
                    changeStatus = STATUS_NONE;
                }

                changeMap.put(key, changeStatus);
            }
        }

        public void sendBroadcast(String key, byte status) {
            Intent intent = new Intent(ACTION);
            intent.putExtra(EXTRA_KEY, key);
            intent.putExtra(EXTRA_STATUS, status);
            context.sendBroadcast(intent);
        }
    }
}