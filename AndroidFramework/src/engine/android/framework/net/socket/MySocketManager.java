package engine.android.framework.net.socket;

import static engine.android.core.util.LogFactory.LOG.log;

import android.content.Context;
import android.util.SparseArray;

import engine.android.core.ApplicationManager;
import engine.android.core.util.LogFactory.LogUtil;
import engine.android.framework.MyConfiguration.MyConfiguration_NET;
import engine.android.framework.MyConfiguration.MyConfiguration_SOCKET;
import engine.android.framework.net.MyNetManager;
import engine.android.framework.net.event.Event;
import engine.android.framework.net.event.EventCallback;
import engine.android.framework.net.event.EventObserver;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.socket.SocketConnectionListener;
import engine.android.socket.SocketConnector;
import engine.android.socket.SocketConnector.SocketData;
import engine.android.socket.SocketConnector.SocketReceiver;
import engine.android.socket.util.CRCUtility;
import engine.android.util.MyThreadFactory;
import engine.android.util.Util;
import engine.android.util.file.FileManager;
import engine.android.util.manager.SDCardManager;
import engine.android.util.secure.HexUtil;
import engine.android.util.secure.Obfuscate;
import protocol.java.ProtocolWrapper;
import protocol.java.ProtocolWrapper.ProtocolEntity;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 统一由{@link MyNetManager}管理，无需自己构造实例
 * 
 * @author Daimon
 */
public class MySocketManager implements 
MyConfiguration_NET,
MyConfiguration_SOCKET, 
SocketConnectionListener, 
SocketReceiver, 
EventCallback {
    
    private static final int MAX_SOCKET_CONNECTION
    = Math.max(3, Runtime.getRuntime().availableProcessors() - 1);
    
    private final Context context;
    
    private final ThreadPoolExecutor socketThreadPool;
    
    private SocketConnector socket;
    
    private boolean isSocketConnected;
    
    private final SparseArray<SocketResponse> pendingDatas
    = new SparseArray<SocketResponse>();
    
    public MySocketManager(Context context) {
        this.context = context.getApplicationContext();
        
        socketThreadPool = new ThreadPoolExecutor(
                MAX_SOCKET_CONNECTION, 
                MAX_SOCKET_CONNECTION,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), 
                new MyThreadFactory("Socket请求"));
        socketThreadPool.allowCoreThreadTimeOut(true);
    }
    
    public ThreadPoolExecutor getThreadPool() {
        return socketThreadPool;
    }

    public void setup(String address, String token) {
        String host = HttpConnector.getHost(address);
        int port = 8080;
        
        int index = host.indexOf(":");
        if (index > -1)
        {
            port = Util.getInt(host.substring(index + 1), port);
            host = host.substring(0, index);
        }
        
        setup(host, port, token);
    }
    
    public void setup(String host, int port, final String token) {
        if (socket != null)
        {
            socket.close();
        }
        
        socket = new SocketConnector(host, port, SOCKET_TIMEOUT, true) {
            
            @Override
            protected void handshake(InputStream in, OutputStream out) throws IOException {
                byte[] key = HexUtil.decode(token);
                out.write(key);
                // 读取握手信息
                byte[] bs = new byte[16];
                in.read(bs);
                
                byte[] crypt_key = Obfuscate.clarify(bs);
                int crc = CRCUtility.calculate(crypt_key, crypt_key.length);
                
                out.write(crc);
                int resp = in.read();
                if (resp == 0)
                {
                    // 握手成功
                    System.arraycopy(crypt_key, 0, key, 0, crypt_key.length);
                    System.arraycopy(crypt_key, 0, key, crypt_key.length, crypt_key.length);
                    
                    ProtocolWrapper.setEncryptSecret(key);
                }
                else if (resp == -1)
                {
                    // Token认证失败
                    throw new SocketException("Token认证失败");
                }
                else if (resp == -2)
                {
                    // CRC校验失败
                    throw new SocketException("CRC校验失败");
                }
                else
                {
                    throw new SocketException("握手失败");
                }
            }
        };
        socket.setProxy(context);
        socket.setListener(this);
        socket.setReceiver(this);
//        if (NET_OFF)
//        {
//            socket.setServlet(new MySocketServletImpl());
//        }
        
        socket.connect();
    }
    
    public boolean isSocketConnected() {
        return isSocketConnected;
    }

    @Override
    public void onConnected(Socket socket) {
        isSocketConnected = true;
        log("Socket连接已建立:" + socket.getInetAddress());

        try {
            socket.setSendBufferSize(64 * 1024);
            socket.setReceiveBufferSize(64 * 1024);
            socket.setTcpNoDelay(true);
            socket.setSoLinger(true, 0);
        } catch (SocketException e) {
            log(e);
        }
    }

    @Override
    public void onReceive(Object data) {
        ProtocolEntity entity = (ProtocolEntity) data;
        log("收到socket信令包", entity);
        
        socketThreadPool.execute(new SocketParser(entity));
    }

    @Override
    public void onError(Exception e) {
        log(e);
        socket.close();
    }

    @Override
    public void onClosed() {
        isSocketConnected = false;
        log("Socket连接已断开");
    }

    @Override
    public Object parseData(InputStream in) throws IOException {
        ProtocolEntity entity = ProtocolWrapper.parse(in);
        if (entity == null)
        {
            throw new IOException("read bytes is -1.");
        }
        
        return entity;
    }
    
    private class SocketParser implements Runnable {
        
        private final ProtocolEntity entity;
        
        public SocketParser(ProtocolEntity entity) {
            this.entity = entity;
        }

        @Override
        public void run() {
            try {
                entity.parseBody();
                receive(entity.getCmd(), entity.getMsgId(), entity.getData());
            } catch (Exception e) {
                // Should not be arrived.
                throw new RuntimeException(e);
            }
        }
    }

    public void receive(int cmd, int msgId, ProtocolData data) {
        if (NET_LOG_PROTOCOL)
        {
            log(data.getClass().getSimpleName() + GsonUtil.toJson(data));
        }
        
        if (ApplicationManager.isDebuggable() && !NET_OFF)
        {
            exportProtocolToFile(data);
        }
        
        if (msgId == 0)
        {
            // 推送消息
//            SocketPushReceiver.push(cmd, data, this);
            return;
        }
        
        int index = pendingDatas.indexOfKey(msgId);
        if (index >= 0)
        {
            SocketResponse response = pendingDatas.valueAt(index);
            if (response != null && response.response(data, this))
            {
                pendingDatas.removeAt(index);
            }
        }
    }
    
    private void exportProtocolToFile(ProtocolData data) {
        if (!SDCardManager.isEnabled())
        {
            return;
        }
        
        File desDir = new File(SDCardManager.openSDCardAppDir(context), 
                "protocols/socket");
        
        File file = new File(desDir, data.getClass().getSimpleName());
        FileManager.writeFile(file, GsonUtil.toJson(data).getBytes(), false);
    }
    
    public static class SocketRequest implements SocketData {

        private static final AtomicInteger generator = new AtomicInteger(1);
        
        private final ProtocolEntity entity;
        
        private byte[] byteArray;

        private final AtomicBoolean isInitialized = new AtomicBoolean();
        
        SocketRequest(ProtocolData data) {
            entity = ProtocolEntity.newInstance(generator.getAndIncrement(), 0, data);
        }
        
        public void init() {
            if (isInitialized.compareAndSet(false, true))
            {
                try {
                    entity.generateBody();
                    byteArray = ProtocolWrapper.wrap(entity);
                } catch (Exception e) {
                    // Should not be arrived.
                    throw new RuntimeException(e);
                }
            }
        }
        
        public int getMsgId() {
            return entity.getMsgId();
        }
        
        @Override
        public void wrapData(OutputStream out) throws IOException {
            init();
            
            log("发送socket信令包", entity);
            out.write(byteArray);
        }
    }
    
    public static interface SocketResponse {
        
        /**
         * @return true不再继续接收后续事件
         */
        boolean response(ProtocolData data, EventCallback callback);
    }
    
    public void sendSocketRequest(SocketRequest request, 
            SocketResponse response) {
        int msgId = request.getMsgId();
        if (pendingDatas.indexOfKey(msgId) < 0)
        {
            pendingDatas.append(msgId, response);
            socket.send(request);
        }
    }
    
    public void cancelSocketRequest(SocketRequest request) {
        int msgId = request.getMsgId();
        int index = pendingDatas.indexOfKey(msgId);
        if (index >= 0)
        {
            pendingDatas.removeAt(index);
            socket.cancel(request);
        }
    }
    
    @Override
    public void call(String action, int status, Object param) {
        log(action + "|" + status + "|" + param);
        EventObserver.getDefault().post(new Event(action, status, param));
        
//        if (!interceptor.intercept(action, status, param))
//        {
//            EventBus.getDefault().post(new Event(action, status, param));
//        }
    }

    public SocketRequest buildSocketRequest(ProtocolData data) {
        if (NET_LOG_PROTOCOL)
        {
            log(LogUtil.getCallerStackFrame(), GsonUtil.toJson(data));
        }
        
        return new SocketRequest(data);
    }
    
    public void sendSocketRequestAsync(final SocketRequest request, 
            final SocketResponse response) {
        socketThreadPool.execute(new Runnable() {
            
            @Override
            public void run() {
                request.init();
                sendSocketRequest(request, response);
            }
        });
    }
}