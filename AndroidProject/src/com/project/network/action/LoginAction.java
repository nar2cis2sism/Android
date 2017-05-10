package com.project.network.action;

import com.project.app.MyApp;
import com.project.app.MyContext;
import com.project.app.MySession;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;

import org.json.JSONObject;

import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.network.http.HttpManager.StringEntity;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import engine.android.util.manager.MyTelephonyDevice;
import engine.android.util.secure.CryptoUtil;
import engine.android.util.secure.HexUtil;

/**
 * 登录
 * 
 * @author Daimon
 */
public class LoginAction implements HttpBuilder, StringEntity {
    
    public final String action = Actions.LOGIN;
    
    public final String username;          // 用户名
    
    public final String password;          // 密码
    
    public final String deviceID;          // 设备唯一标识
    
    public LoginAction(String username, String password) {
        this.username = username;
        this.password = HexUtil.encode(CryptoUtil.SHA1((password + "000").getBytes()));
        deviceID = new MyTelephonyDevice(MyContext.getContext()).getDeviceId();
    }

    @Override
    public HttpConnector buildConnector(HttpManager http) {
        return http.buildHttpConnector(NetworkConfig.HTTP_URL, action, this);
    }
    
    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }

    @Override
    public HttpParser buildParser() {
        return new Parser();
    }
    
    private class Parser extends HttpJsonParser {
        
        @Override
        protected Object process(JSONObject data) {
            String token = data.optString("token");
            long uid = data.optLong("uid");
            String user_info_ver = data.optString("user_info_crc");
            
            MySession.setToken(token);
            // 启动socket连接
//            MyApp.getSocketManager().setup(MySession.getSocketAddress(), token);
            // 查询好友列表
            MyApp.getHttpManager().sendHttpRequest(new QueryFriendList());
            
            return super.process(data);
        }
    }
}