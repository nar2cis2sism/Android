package engine.android.framework;

import static engine.android.core.ApplicationManager.isDebuggable;
import engine.android.framework.net.MyNetConfig.URLConfig;

/**
 * 全局配置
 * 
 * @author Daimon
 */
public class MyConfiguration {

    public static interface MyConfiguration_APP {
        
        /** 测试版本 **/
        public static final boolean APP_TESTING = !isDebuggable() ? false : 
                                                  true; // Modify it for test
    }

    public static interface MyConfiguration_SHARED_PREFERENCES {

        public static final String SHARED_PREFERENCES_NAME = "project";
    }

    public static interface MyConfiguration_DB {
        
        public static final String DB_NAME = "project.db";
        public static final int DB_VERSION = 1;
    }
    
    public static interface MyConfiguration_NET {
        
        /** 测试服务器 **/
        public static final boolean NET_TEST = !isDebuggable() ? false : 
                                               true; // Modify it for test
        
        /** 单机不联网 **/
        public static final boolean NET_OFF = !isDebuggable() ? false : 
                                              true; // Modify it for test

        /** 打印协议 **/
        public static final boolean NET_LOG_PROTOCOL = !isDebuggable() ? false : 
                                                       true; // Modify it for test
    }

    public static interface MyConfiguration_HTTP {
        
        public static final String HTTP_URL = MyConfiguration_NET.NET_TEST ? 
                URLConfig.URL_TEST : URLConfig.URL_PRODUCT;
        
        public static final int HTTP_TIMEOUT   = 10000; // (10s)
    }
    
    public static interface MyConfiguration_SOCKET {
        
        public static final int SOCKET_TIMEOUT = 10000; // (10s)
    }
}