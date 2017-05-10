package engine.android.framework.network.socket;

import static engine.android.core.util.LogFactory.LOG.log;

import android.content.Context;
import android.util.SparseArray;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import engine.android.core.ApplicationManager;
import engine.android.core.extra.EventBus;
import engine.android.core.extra.EventBus.Event;
import engine.android.framework.app.AppConfig;
import engine.android.framework.app.AppGlobal;
import engine.android.framework.network.ConnectionStatus;
import engine.android.framework.network.socket.SocketManager.SocketResponse.Callback;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.socket.SocketConnectionListener;
import engine.android.socket.SocketConnector;
import engine.android.socket.SocketConnector.SocketData;
import engine.android.socket.SocketConnector.SocketReceiver;
import engine.android.socket.util.CRCUtility;
import engine.android.util.Util;
import engine.android.util.file.FileManager;
import engine.android.util.manager.SDCardManager;
import engine.android.util.secure.HexUtil;
import engine.android.util.secure.Obfuscate;
import protocol.java.ProtocolWrapper;
import protocol.java.ProtocolWrapper.ProtocolEntity;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;

/**
 * Socket连接管理器<p>
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class SocketManager implements SocketConnectionListener {

    private final Context context;

    private final AppConfig config;
    
    private SocketConnector socket;
    
    private boolean isSocketConnected;
    
    private final SparseArray<SocketAction> pendingDatas
    = new SparseArray<SocketAction>();
    
    public SocketManager(Context context) {
        this.context = (config = AppGlobal.get(context).getConfig()).getContext();
    }

    public void setup(String address, String token) {
        String host = HttpConnector.getHost(address);
        int port = 80;
        
        int index = host.indexOf(":");
        if (index > -1)
        {
            port = Util.getInt(host.substring(index + 1), port);
            host = host.substring(0, index);
        }
        
        setup(host, port, token);
    }
    
    public void setup(String host, int port, final String token) {
        if (socket != null) socket.close();
        socket = new SocketConnector(host, port, config.getSocketTimeout(), true) {
            
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
        socket.setReceiver(new SocketReceiver() {
            
            @Override
            public Object parseData(InputStream in) throws IOException {
                ProtocolEntity entity = ProtocolWrapper.parse(in);
                if (entity == null)
                {
                    throw new IOException("read bytes is -1.");
                }
                
                return entity;
            }
        });
        socket.setProxy(context);
        socket.setListener(this);
        if (config.isOffline()) socket.setServlet(config.getSocketServlet());
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
        
        config.getSocketThreadPool().execute(new SocketParser(entity));
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
            log(data.getClass().getSimpleName(), "服务器返回--" + GsonUtil.toJson(data));
        }
        
        if (ApplicationManager.isDebuggable(context) && !config.isOffline())
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
            SocketAction action = pendingDatas.valueAt(index);
            if (action.response.response(data, action))
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
    
    private static class SocketRequest implements SocketData {

        private static final AtomicInteger generator = new AtomicInteger(1);
        
        private final ProtocolEntity entity;
        
        private byte[] byteArray;

        private final AtomicBoolean isInitialized = new AtomicBoolean();
        
        public SocketRequest(ProtocolData data) {
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
    
    public interface SocketResponse {
        
        /**
         * @return True表示不再继续接收后续事件
         */
        boolean response(ProtocolData data, Callback callback);
        
        public interface Callback extends ConnectionStatus {

            void call(String action, int status, Object param);
        }
    }
    
    private class SocketAction implements Runnable, Callback {
        
        public final SocketRequest request;
        public final SocketResponse response;
        
        public SocketAction(SocketRequest request, SocketResponse response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public void run() {
            request.init();
            socket.send(request);
        }

        @Override
        public void call(String action, int status, Object param) {
            int index = pendingDatas.indexOfKey(request.getMsgId());
            if (index < 0) return;
            
            log(action + "|" + status + "|" + param);
            
            ConnectionInterceptor interceptor = config.getSocketInterceptor();
            if (interceptor != null && interceptor.intercept(action, status, param))
            {
                return;
            }

            EventBus.getDefault().post(new Event(action, status, param));
        }
    }
    
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
        ProtocolData data = socket.buildData();
        if (config.isLogProtocol())
        {
            log(data.getClass().getSimpleName(), "发送请求--" + GsonUtil.toJson(data));
        }

        SocketRequest request = new SocketRequest(data);
        SocketResponse response = socket.buildResponse();
        SocketAction action = new SocketAction(request, response);
        
        int msgId = request.getMsgId();
        if (response != null) pendingDatas.append(msgId, action);
        
        config.getSocketThreadPool().execute(action);
        return msgId;
    }
    
    /**
     * 取消socket请求
     */
    public void cancelSocketRequest(int id) {
        int index = pendingDatas.indexOfKey(id);
        if (index >= 0)
        {
            socket.cancel(pendingDatas.valueAt(index).request);
            pendingDatas.removeAt(index);
        }
    }
}