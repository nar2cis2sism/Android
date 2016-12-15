package com.project.http.builder;

import static engine.android.framework.net.MyNetManager.getHttpManager;

import com.project.MySession;
import com.project.action.Actions;
import com.project.http.json.MyHttpJsonParser;

import engine.android.framework.MyConfiguration.MyConfiguration_HTTP;
import engine.android.framework.net.MyNetManager;
import engine.android.framework.net.event.EventCallback;
import engine.android.framework.net.http.MyHttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
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
    
    public final String deviceID           // 设备唯一标识
    = "";
    
    public LoginAction(String username, String password) {
        this.username = username;
        this.password = HexUtil.encode(CryptoUtil.SHA1((password + "000").getBytes()));
    }

    @Override
    public HttpConnector buildHttpConnector() {
        return getHttpManager().buildHttpConnector(
                MyConfiguration_HTTP.HTTP_URL, 
                action, 
                GsonUtil.toJson(this), 
                new Parser(action, getHttpManager()))
                .setRemark("用户登录");
    }
    
    private class Parser extends MyHttpJsonParser {

        public Parser(String action, EventCallback callback) {
            super(action, callback);
        }
        
        @Override
        protected Object process(JSONObject data) {
            String token = data.optString("token");
            long uid = data.optLong("uid");
            String user_info_ver = data.optString("user_info_crc");
            
            setupSocket(token);
            
            return super.process(data);
        }
        
        private void setupSocket(String token) {
            String address = MySession.getSocketAddress();
            MyNetManager.getSocketManager().setup(address, token);
        }
    }
}