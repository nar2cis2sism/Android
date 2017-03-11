package com.project.app;

/**
 * 全局配置
 * 
 * @author Daimon
 */
public class MyConfiguration {
    
    public static interface MyConfiguration_NETWORK {
        
        /** 单机不联网 **/
        public static final boolean NET_OFF = true;

        /** 打印协议 **/
        public static final boolean NET_LOG_PROTOCOL = true;
        
        /** 测试服务器 **/
        public static final boolean NET_TEST = true;
    }

    public static interface MyConfiguration_HTTP {
        
        public static final int HTTP_TIMEOUT   = 5000; // (5s)
    }
    
    public static interface MyConfiguration_SOCKET {
        
        public static final int SOCKET_TIMEOUT = 5000; // (5s)
    }
}