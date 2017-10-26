package com.project.network.action;

import com.project.app.MySession;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;
import com.project.storage.db.User;

import org.json.JSONObject;

import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.network.http.HttpManager.JsonEntity;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import protocol.java.json.UserInfo;

/**
 * 获取个人信息
 * 
 * @author Daimon
 */
public class GetUserInfo implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.GET_USER_INFO;
    
    public final String token;              // 用户登录凭证
    
    public final long version;              // 用户信息版本
    
    public GetUserInfo(long version) {
        token = MySession.getToken();
        this.version = version;
    }

    @Override
    public HttpConnector buildConnector(HttpManager http) {
        return http.buildHttpConnector(NetworkConfig.HTTP_URL, action, this);
    }

    @Override
    public HttpParser buildParser() {
        return new Parser();
    }

    @Override
    public String toJson() {
        return GsonUtil.toJson(this);
    }
    
    private class Parser extends HttpJsonParser {
        
        @Override
        protected Object process(JSONObject data) throws Exception {
            long version = data.getLong("version");
            UserInfo info = GsonUtil.parseJson(data.toString(), UserInfo.class);
            
            User user = MySession.getUser();
            user.setUserInfo(info);
            user.version = version;
            MyDAOManager.getDAO().update(user);
            
            return super.process(data);
        }
    }
}