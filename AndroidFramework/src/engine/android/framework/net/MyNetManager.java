package engine.android.framework.net;

import engine.android.framework.MyContext;
import engine.android.framework.net.http.MyHttpManager;
import engine.android.util.Singleton;

/**
 * 网络通讯管理器
 * 
 * @author Daimon
 */
public class MyNetManager {
    
    private static final Singleton<MyHttpManager> http
    = new Singleton<MyHttpManager>() {
        
        @Override
        protected MyHttpManager create() {
            return new MyHttpManager(MyContext.getContext());
        }
    };
    
    public static MyHttpManager getHttpManager() {
        return http.get();
    }
}