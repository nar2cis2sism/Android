package com.project.network.action;

import static engine.android.framework.app.App.getHttpManager;

import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;

import engine.android.framework.app.AppContext;
import engine.android.framework.network.event.EventObserver.EventCallback;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.util.manager.MyTelephonyDevice;
import engine.android.util.secure.CryptoUtil;
import engine.android.util.secure.HexUtil;

import org.json.JSONObject;

/**
 * 登录
 * 
 * @author Daimon
 */
public class LoginAction implements HttpBuilder {
    
    public final String action = Actions.LOGIN;
    
    public final String username;          // 用户名
    
    public final String password;          // 密码
    
    public final String deviceID;          // 设备唯一标识
    
    public LoginAction(String username, String password) {
        this.username = username;
        this.password = HexUtil.encode(CryptoUtil.SHA1((password + "000").getBytes()));
        deviceID = new MyTelephonyDevice(AppContext.getContext()).getDeviceId();
    }

    @Override
    public HttpConnector buildHttpConnector() {
        return getHttpManager().buildHttpConnector(
                NetworkConfig.HTTP_URL, 
                action, 
                GsonUtil.toJson(this), 
                new Parser(action, getHttpManager()));
    }
    
    private class Parser extends HttpJsonParser {

        public Parser(String action, EventCallback callback) {
            super(action, callback);
        }
        
        @Override
        protected Object process(JSONObject data) {
            String token = data.optString("token");
            long uid = data.optLong("uid");
            String user_info_ver = data.optString("user_info_crc");
            
            MySession.setToken(token);
            // 启动socket连接
            MyApp.getSocketManager().setup(MySession.getSocketAddress(), token);
            // 查询好友列表
            MyApp.getHttpManager().sendHttpRequestAsync(new QueryFriendList());
            
            return super.process(data);
        }
    }
}