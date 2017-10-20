package engine.android.socket;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;

import engine.android.socket.SocketProxy.SocketServlet;
import engine.android.socket.util.SocketUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Socket连接器（使用队列机制）<p>
 * 需要声明权限<uses-permission android:name="android.permission.INTERNET" />
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class SocketConnector {

    private final String host;                          // 主机地址

    private final int port;                             // 端口号

    private final int timeout;                          // 连接超时

    private final boolean handShake;                    // 握手成功才能连接

    private final LinkedList<SocketData> conns;         // 请求队列

    private SocketConnectionListener listener;          // Socket连接监听器

    private SocketReceiver receiver;                    // Socket接收器（解析数据）

    private java.net.Proxy proxy;                       // 连接代理

    private SocketProxy socketProxy;                    // Socket代理（单机测试用）

    private SocketServlet servlet;

    private final ThreadFactory socketThreadFactory = new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Socket网络连接");

            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);

            return t;
        }
    };

    private final ExecutorService socketThreadPool
    = Executors.newSingleThreadExecutor(socketThreadFactory);

    /**
     * Socket数据包
     */
    public interface SocketData {

        void wrapData(OutputStream out) throws IOException;
    }

    /**
     * Socket接收数据解析
     */
    public interface SocketReceiver {

        Object parseData(InputStream in) throws IOException;
    }

    public SocketConnector(String host, int port) {
        this(host, port, 0);
    }

    public SocketConnector(String host, int port, int timeout) {
        this(host, port, timeout, false);
    }

    /**
     * @param host 主机地址
     * @param port 端口号
     * @param timeout 超时时间
     * @param handShake 通讯是否需要握手
     */
    public SocketConnector(String host, int port, int timeout, boolean handShake) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.handShake = handShake;
        conns = new LinkedList<SocketData>();
    }

    /**
     * 根据手机设置自动选择代理<br>
     * 需要声明权限<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     */
    public SocketConnector setProxy(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService
                (Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();// 获取当前网络连接信息
        if (info != null && info.getType() != ConnectivityManager.TYPE_WIFI)
        {
            // 不是WIFI网络
            if (Proxy.getHost(context) != null)
            {
                // 有代理网关
                proxy = new java.net.Proxy(Type.SOCKS,
                        new InetSocketAddress(Proxy.getHost(context), Proxy.getPort(context)));
            }
        }

        return this;
    }

    /**
     * 设置代理主机
     */

    public SocketConnector setProxy(java.net.Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * 设置代理地址（不含scheme）
     */

    public SocketConnector setProxyAddress(String address) {
        int port = 80;
        int index = address.indexOf(":");
        if (index > 0)
        {
            port = Integer.parseInt(address.substring(index + 1));
            address = address.substring(0, index);
        }

        proxy = new java.net.Proxy(Type.SOCKS, new InetSocketAddress(address, port));
        return this;
    }

    public SocketConnector setListener(SocketConnectionListener listener) {
        this.listener = listener;
        return this;
    }

    public SocketConnector setReceiver(SocketReceiver receiver) {
        this.receiver = receiver;
        return this;
    }

    private SocketReceiver getReceiver() {
        if (receiver == null)
        {
            receiver = new SocketReceiver() {

                @Override
                public Object parseData(InputStream in) throws IOException {
                    int length = in.read();
                    byte[] data = new byte[length];
                    in.read(data);
                    return data;
                }
            };
        }

        return receiver;
    }

    public SocketConnector setServlet(SocketServlet servlet) {
        this.servlet = servlet;
        return this;
    }

    private boolean useSocketProxy() {
        return servlet != null;
    }

    private AtomicBoolean isClosed;

    /**
     * 连接网络
     */
    public synchronized void connect() {
        if (isClosed == null)
        {
            isClosed = new AtomicBoolean();
            socketThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    connect(isClosed);
                }
            });
        }
        else
        {
            throw new IllegalStateException("Please close the socket first.");
        }
    }

    /**
     * 关闭网络连接
     */
    public synchronized void close() {
        if (isClosed != null && isClosed.compareAndSet(false, true))
        {
            isClosed = null;

            socketLock.lock();
            try {
                closeSocket();

                if (listener != null)
                {
                    listener.onClosed();
                }
            } finally {
                socketLock.unlock();
            }
        }
    }

    public boolean isClosed() {
        return isClosed == null || isClosed.get();
    }

    /** Lock held by take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** Wait queue for waiting takes */
    private final Condition notEmpty = takeLock.newCondition();

    /**
     * Signals a waiting take. Called only from put/offer (which do not
     * otherwise ordinarily lock takeLock.)
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    private SocketData take() throws InterruptedException {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            if (conns.size() == 0)
            {
                notEmpty.await();
            }

            return conns.peek();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * 发送数据
     */
    public void send(SocketData data) {
        int size = conns.size();
        conns.add(data);
        if (size == 0)
        {
            signalNotEmpty();
        }
    }

    /**
     * 如果数据还未发出去，可以取消发送，数据必须实现{@link #equals(Object)}接口
     */
    public void cancel(SocketData data) {
        conns.remove(data);
    }

    public void clear() {
        conns.clear();
    }
    
    /******************** 数据收发 ********************/

    private Socket socket;                              // Socket连接

    private InputStream in;                             // 数据输入流

    private OutputStream out;                           // 数据输出流

    private AtomicBoolean isRunning;

    private void recv(AtomicBoolean isRunning) {
        while (isRunning.get())
        {
            try {
                while (isRunning.get())
                {
                    Object data = getReceiver().parseData(in);

                    if (data != null && listener != null)
                    {
                        listener.onReceive(data);
                    }
                }
            } catch (Exception e) {
                if (!isRunning.get())
                {
                    return;
                }

                onError(e);
            }
        }
    }

    private void send(AtomicBoolean isRunning) {
        while (isRunning.get())
        {
            try {
                while (isRunning.get())
                {
                    SocketData data = take();

                    if (data != null)
                    {
                        data.wrapData(out);
                        out.flush();
                        conns.poll();
                    }
                }
            } catch (Exception e) {
                if (!isRunning.get())
                {
                    return;
                }

                onError(e);
            }
        }
    }

    /******************** 网络连接 ********************/

    private final ReentrantLock socketLock = new ReentrantLock();

    private void connect(final AtomicBoolean isClosed) {
        socketLock.lock();
        try {
            if (isClosed.get())
            {
                return;
            }

            if (useSocketProxy())
            {
                socketProxy = new SocketProxy(servlet);
            }
            else
            {
                if (proxy == null)
                {
                    socket = new Socket();
                }
                else
                {
                    socket = new Socket(proxy);
                }
            }
        } finally {
            socketLock.unlock();
        }

        if (!useSocketProxy())
        {
            try {
                // 建立连接
                socket.connect(new InetSocketAddress(host, port), timeout);
            } catch (Exception e) {
                socket = null;

                if (!isClosed.get())
                {
                    onError(e);
                }

                return;
            }
        }

        socketLock.lock();
        try {
            if (isClosed.get())
            {
                closeSocket();
                return;
            }

            if (useSocketProxy())
            {
                in = socketProxy.getInputStream();
                out = socketProxy.getOutputStream();
            }
            else
            {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            }
            
            if (handShake) handshake(in, out);
                onConnected();
        } catch (Exception e) {
            closeSocket();
            onError(e);
        } finally {
            socketLock.unlock();
        }
    }
    
    /**
     * 可自定义握手协议
     */
    protected void handshake(InputStream in, OutputStream out) throws IOException {
        // 读取握手信息
        byte[] bs = new byte[16];
        in.read(bs);
        int crc = SocketUtil.handshake(bs);
        out.write(crc);
        if (in.read() == 0)
        {
            // 握手成功
        }
        else
        {
            throw new SocketException("握手失败");
        }
    }
    
    private void onConnected() {
        if (listener != null)
        {
            listener.onConnected(socket);
        }

        // 建立网络收发线程
        isRunning = new AtomicBoolean(true);
        startThread(new Runnable() {

            @Override
            public void run() {
                recv(isRunning);
            }
        }, "Socket数据接收");
        startThread(new Runnable() {

            @Override
            public void run() {
                send(isRunning);
            }
        }, "Socket数据发送");
    }

    private void closeSocket() {
        if (isRunning != null && isRunning.compareAndSet(true, false))
        {
            isRunning = null;
        }

        if (socketProxy != null)
        {
            socketProxy.close();
            socketProxy = null;
        }

        if (socket != null)
        {
            if (socket.isConnected())
            {
                try {
                    socket.shutdownInput();
                } catch (IOException e) {}

                try {
                    socket.shutdownOutput();
                } catch (IOException e) {}
            }

            try {
                socket.close();
            } catch (IOException e) {}

            socket = null;
        }

        signalNotEmpty();

        in = null;
        out = null;
    }

    private void onError(Exception e) {
        if (listener != null)
        {
            listener.onError(e);
        }
    }

    private void startThread(Runnable r, String name) {
        Thread t = socketThreadFactory.newThread(r);
        t.setName(name);
        t.start();
    }
}