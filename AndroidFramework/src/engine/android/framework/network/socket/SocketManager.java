package engine.android.framework.network.socket;

import static engine.android.core.util.LogFactory.LOG.log;

import engine.android.core.ApplicationManager;
import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;
import engine.android.core.util.LogFactory;
import engine.android.framework.app.AppConfig;
import engine.android.framework.app.AppGlobal;
import engine.android.framework.network.socket.SocketResponse.Callback;
import engine.android.framework.network.socket.SocketResponse.SocketTimeout;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.socket.SocketConnectionListener;
import engine.android.socket.SocketConnector;
import engine.android.socket.SocketConnector.SocketData;
import engine.android.socket.SocketConnector.SocketReceiver;
import engine.android.util.Util;
import engine.android.util.extra.MyThreadFactory;
import engine.android.util.file.FileManager;
import engine.android.util.manager.SDCardManager;
import engine.android.util.secure.CRCUtil;
import engine.android.util.secure.HexUtil;
import engine.android.util.secure.Obfuscate;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import protocol.util.ProtocolWrapper;
import protocol.util.ProtocolWrapper.ProtocolEntity;
import protocol.util.ProtocolWrapper.ProtocolEntity.ProtocolData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Socket连接管理器
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class SocketManager implements SocketConnectionListener, Callback {

    private final Context context;

    private final AppConfig config;
    
    private final SocketHandler handler;
    
    private final SparseArray<SocketAction> pendingDatas = new SparseArray<SocketAction>();

    private final ExecutorService receiveExecutor;
    
    private SocketConnector socket;
    
    private String token;
    
    public SocketManager(Context context) {
        this.context = (config = AppGlobal.get(context).getConfig()).getContext();
        handler = new SocketHandler(this);
        receiveExecutor = new ThreadPoolExecutor(
                0, 
                1,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), 
                new MyThreadFactory("Socket回包"));
    }
    
    public void setToken(String token) {
        this.token = token;
    }

    public void setup(String address) {
        String host = HttpConnector.getHost(address);
        int port = 80;
        
        int index = host.indexOf(":");
        if (index > -1)
        {
            port = Util.getInt(host.substring(index + 1), port);
            host = host.substring(0, index);
        }
        
        setup(host, port);
    }
    
    public void setup(String host, int port) {
        if (TextUtils.isEmpty(token)) throw new RuntimeException("需要设置Token值");
        if (socket != null) socket.close();
        socket = new SocketConnector(host, port, config.getSocketTimeout(), !config.isOffline()) {
            
            @Override
            protected void handshake(InputStream in, OutputStream out) throws IOException {
                byte[] key = HexUtil.decode(token);
                out.write(key);
                // 读取握手信息
                byte[] bs = new byte[16];
                in.read(bs);
                
                byte[] crypt_key = Obfuscate.clarify(bs);
                int crc = CRCUtil.calculate(crypt_key, crypt_key.length);
                
                out.write(crc);
                int resp = in.read();
                if (resp == 0)
                {
                    // 握手成功
                    System.arraycopy(crypt_key, 0, key, 0, crypt_key.length);
                    System.arraycopy(crypt_key, 0, key, crypt_key.length, crypt_key.length);
                    
                    ProtocolWrapper.setEncryptSecret(key);
                }
                else if (resp == 1)
                {
                    // Token认证失败
                    throw new SocketException("Token认证失败");
                }
                else if (resp == 2)
                {
                    // CRC校验失败
                    throw new SocketException("CRC校验失败");
                }
                else
                {
                    throw new SocketException("握手失败");
                }
            }
        }
        .setReceiver(new SocketReceiver() {
            
            @Override
            public Object parseData(InputStream in) throws IOException {
                ProtocolEntity entity = ProtocolWrapper.parse(in);
                if (entity == null)
                {
                    throw new IOException("read bytes is -1.");
                }
                
                return entity;
            }
        })
        .setProxy(context)
        .setListener(this);
        if (config.isOffline()) socket.setServlet(config.getSocketServlet());
        handler.setup(context); // 启动扩展功能
        socket.connect();
    }
    
    /**
     * 断开连接
     */
    public void close() {
        if (socket != null)
        {
            socket.close();
            token = null;
        }
    }
    
    /**
     * 重连接
     */
    void reconnect() {
        if (token != null) socket.connect();
    }

    @Override
    public void onConnected(Socket socket) {
        if (socket == null)
        {
            log("Socket连接已建立");
        }
        else
        {
            log("Socket连接已建立:" + socket.getRemoteSocketAddress());
            try {
                socket.setSendBufferSize(64 * 1024);
                socket.setReceiveBufferSize(64 * 1024);
                socket.setTcpNoDelay(true);
                socket.setSoLinger(true, 0);
            } catch (SocketException e) {
                log(e);
            }

            handler.heartbeat().start(config.getSocketKeepAliveTime());
        }
    }
    
    private void onSend(ProtocolEntity entity) {
        log("发送socket信令包", entity);
        handler.heartbeat().poke();
    }

    @Override
    public void onReceive(Object data) {
        log("收到socket信令包", data);
        receiveExecutor.execute(new SocketParser((ProtocolEntity) data));
    }

    @Override
    public void onError(Exception e) {
        log(e);
        socket.close();
        // 自动重连
        handler.reconnect(Util.getRandom(2000, 5000));
    }

    @Override
    public void onClosed() {
        log("Socket连接已断开");
        handler.heartbeat().stop();
    }
    
    void onTimeout(int msgId, SocketTimeout timeout) {
        int index = pendingDatas.indexOfKey(msgId);
        if (index >= 0)
        {
            pendingDatas.removeAt(index);
            timeout.timeout(this);
        }
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

    private void receive(int cmd, int msgId, ProtocolData data) {
        if (config.isLogProtocol())
        {
            log("收到socket信令包", data.getClass().getSimpleName() + GsonUtil.toJson(data));
        }
        
        if (ApplicationManager.isDebuggable(context) && !config.isOffline())
        {
            exportProtocolToFile(data);
        }
        
        if (msgId == 0)
        {
            // 推送消息
            SocketPushReceiver receiver = config.getSocketPushReceiver();
            if (receiver != null) receiver.receive(cmd, data, this);
            return;
        }
        
        int index = pendingDatas.indexOfKey(msgId);
        if (index >= 0)
        {
            SocketResponse response = pendingDatas.valueAt(index).response;
            if (response != null) response.response(cmd, data, this);
            pendingDatas.removeAt(index);
        }
    }
    
    private void exportProtocolToFile(ProtocolData data) {
        if (!SDCardManager.isEnabled())
        {
            return;
        }
        
        File desDir = new File(SDCardManager.openSDCardAppDir(context), "protocols/socket");
        File file = new File(desDir, data.getClass().getSimpleName());
        FileManager.writeFile(file, GsonUtil.toJson(data).getBytes(), false);
    }

    @Override
    public void call(String action, int status, Object param) {
        ConnectionInterceptor interceptor = config.getSocketInterceptor();
        if (interceptor != null && interceptor.intercept(action, status, param))
        {
            return;
        }

        EventBus.getDefault().post(new Event(action, status, param));
    }

    private class SocketRequest implements SocketData {
        
        private final ProtocolEntity entity;

        private final AtomicBoolean isInitialized = new AtomicBoolean();
        
        private byte[] byteArray;
        
        public SocketRequest(ProtocolData data) {
            entity = ProtocolEntity.newInstance(data.hashCode(), data);
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
            onSend(entity);
            out.write(byteArray);
        }
    }
    
    private class SocketAction implements Runnable {
        
        public final SocketRequest request;
        public final SocketResponse response;
        
        private final AtomicBoolean isCancelled = new AtomicBoolean();
        
        public SocketAction(SocketRequest request, SocketResponse response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public void run() {
            request.init();
            if (!isCancelled.get())
            {
                socket.send(request);
                if (response == null)
                {
                    pendingDatas.remove(request.getMsgId());
                }
                else if (response instanceof SocketTimeout)
                {
                    handler.setTimeout(request.getMsgId(), (SocketTimeout) response);
                }
            }
        }

        public void cancel() {
            if (isCancelled.compareAndSet(false, true))
            {
                socket.cancel(request);
            }
        }
    }
    
    /**
     * 发送socket请求
     * 
     * @return 可用于取消请求
     */
    public int sendSocketRequest(ProtocolData data, SocketResponse response) {
        if (config.isLogProtocol())
        {
            log("发送socket信令包", data.getClass().getSimpleName() + GsonUtil.toJson(data));
        }

        SocketRequest request = new SocketRequest(data);
        int id = request.getMsgId();
        if (pendingDatas.indexOfKey(id) < 0)
        {
            SocketAction action = new SocketAction(request, response);
            pendingDatas.append(id, action);
            config.getSocketThreadPool().execute(action);
        }
        
        return id;
    }
    
    /**
     * 取消socket请求
     */
    public void cancelSocketRequest(int id) {
        int index = pendingDatas.indexOfKey(id);
        if (index >= 0)
        {
            pendingDatas.valueAt(index).cancel();
            pendingDatas.removeAt(index);
        }
    }
    
    /**
     * Implement this interface for individual logic of socket action.
     */
    public interface SocketBuilder {
        
        ProtocolData buildData();
        
        SocketResponse buildResponse();
    }
    
    /**
     * 发送socket请求
     * 
     * @return 可用于取消请求
     */
    public int sendSocketRequest(SocketBuilder socket) {
        return sendSocketRequest(socket.buildData(), socket.buildResponse());
    }
    
    static
    {
        LogFactory.addLogFile(SocketManager.class, HttpConnector.class);
    }
}