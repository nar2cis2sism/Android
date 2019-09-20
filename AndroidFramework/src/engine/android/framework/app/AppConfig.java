package engine.android.framework.app;

import engine.android.framework.app.image.ImageManager.Transformer;
import engine.android.framework.network.ConnectionStatus.ConnectionInterceptor;
import engine.android.framework.network.socket.SocketPushReceiver;
import engine.android.http.HttpProxy.HttpServlet;
import engine.android.socket.SocketProxy.SocketServlet;
import engine.android.util.extra.MyThreadFactory;

import android.content.Context;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 配置应用程序公用的功能组件
 * 
 * @author Daimon
 * @since 6/6/2016
 */
public class AppConfig {
    
    private Context context;
    
    public AppConfig(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public Context getContext() {
        return context;
    }
    
    public boolean isOffline() {
        return configNetwork().offline;
    }
    
    public boolean isLogProtocol() {
        return configNetwork().logProtocol;
    }
    
    public ThreadPoolExecutor getHttpThreadPool() {
        ThreadPoolExecutor threadPool = configHttp().threadPool;
        if (threadPool == null)
        {
            configHttp().threadPool = threadPool = ConnectionConfig.getDefaultThreadPool("Http网络连接");
        }
        
        return threadPool;
    }
    
    public ThreadPoolExecutor getSocketThreadPool() {
        ThreadPoolExecutor threadPool = configSocket().threadPool;
        if (threadPool == null)
        {
            configSocket().threadPool = threadPool = ConnectionConfig.getDefaultThreadPool("Socket请求");
        }
        
        return threadPool;
    }
    
    public ConnectionInterceptor getHttpInterceptor() {
        return configHttp().interceptor;
    }
    
    public ConnectionInterceptor getSocketInterceptor() {
        return configSocket().interceptor;
    }
    
    public int getHttpTimeout() {
        return configHttp().timeout;
    }
    
    public int getSocketTimeout() {
        return configSocket().timeout;
    }
    
    public HttpServlet getHttpServlet() {
        return configHttp().servlet;
    }
    
    public SocketServlet getSocketServlet() {
        return configSocket().servlet;
    }
    
    public SocketPushReceiver getSocketPushReceiver() {
        return configSocket().receiver;
    }
    
    public int getSocketKeepAliveTime() {
        return configSocket().keepAliveTime;
    }
    
    public File getImageDir() {
        File imageDir = configImage().imageDir;
        if (imageDir == null)
        {
            configImage().imageDir = imageDir = context.getDir("image", 0);
        }
        
        return imageDir;
    }
    
    public Transformer getImageTransformer() {
        return configImage().transformer;
    }
    
    public static class NetworkConfig {
        
        boolean offline;
        
        boolean logProtocol;
        
        /**
         * 单机模式
         */
        public NetworkConfig setOffline(boolean offline) {
            this.offline = offline;
            return this;
        }
        
        /**
         * 日志记录网络协议
         */
        public NetworkConfig setProtocolLog(boolean enable) {
            logProtocol = enable;
            return this;
        }
    }
    
    private static class ConnectionConfig {
        
        private static final int MAX_CONNECTION
        = Math.max(3, Runtime.getRuntime().availableProcessors() - 1);
        
        ThreadPoolExecutor threadPool;
        
        ConnectionInterceptor interceptor;
        
        int timeout;
        
        public static ThreadPoolExecutor getDefaultThreadPool(String threadPoolName) {
            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                    MAX_CONNECTION, 
                    MAX_CONNECTION,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(), 
                    new MyThreadFactory(threadPoolName));
            threadPool.allowCoreThreadTimeOut(true);
            
            return threadPool;
        }
        
        /**
         * 设置网络连接线程池
         */
        public void setThreadPool(ThreadPoolExecutor threadPool) {
            this.threadPool = threadPool;
        }
        
        /**
         * 设置网络拦截器
         */
        public void setInterceptor(ConnectionInterceptor interceptor) {
            this.interceptor = interceptor;
        }
        
        /**
         * 设置网络连接超时时间，单位:ms
         */
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
    
    public static class HttpConfig extends ConnectionConfig {
        
        HttpServlet servlet;
        
        /**
         * 设置单机调试服务器
         */
        public void setServlet(HttpServlet servlet) {
            this.servlet = servlet;
        }
    }
    
    public static class SocketConfig extends ConnectionConfig {
        
        SocketServlet servlet;
        
        SocketPushReceiver receiver;
        
        int keepAliveTime = 60;
        
        /**
         * 设置单机调试服务器
         */
        public void setServlet(SocketServlet servlet) {
            this.servlet = servlet;
        }
        
        /**
         * 设置推送消息接收器
         */
        public void setPushReceiver(SocketPushReceiver receiver) {
            this.receiver = receiver;
        }
        
        /**
         * 设置心跳保活时长，默认为1分钟
         * 
         * @param keepAliveTime 单位：秒
         */
        public void setKeepAliveTime(int keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }
    }
    
    public static class ImageConfig {
        
        File imageDir;
        
        Transformer transformer;
        
        /**
         * 设置图片存储目录
         */
        public ImageConfig setImageDir(File imageDir) {
            this.imageDir = imageDir;
            return this;
        }
        
        /**
         * 设置图片转换器
         */
        public ImageConfig setTransformer(Transformer transformer) {
            this.transformer = transformer;
            return this;
        }
    }
    
    /**
     * 配置网络连接
     */
    public NetworkConfig configNetwork() {
        if (network == null) network = new NetworkConfig();
        return network;
    }
    
    /**
     * 配置Http连接
     */
    public HttpConfig configHttp() {
        if (http == null) http = new HttpConfig();
        return http;
    }
    
    /**
     * 配置Socket连接
     */
    public SocketConfig configSocket() {
        if (socket == null) socket = new SocketConfig();
        return socket;
    }
    
    /**
     * 配置图片管理
     */
    public ImageConfig configImage() {
        if (image == null) image = new ImageConfig();
        return image;
    }
    
    private NetworkConfig network;
    private HttpConfig http;
    private SocketConfig socket;
    private ImageConfig image;
}