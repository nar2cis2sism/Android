package com.project.network.action.http;

import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.network.http.HttpConnectorBuilder.JsonEntity;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;

import com.project.app.MySession;
import com.project.network.NetworkConfig;
import com.project.network.action.Actions;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;

import protocol.http.UserData;

public class GetUserInfo implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.GET_USER_INFO;
    
    public final String token;              // 用户登录凭证
    
    public final int version;               // 用户信息版本
    
    public GetUserInfo(int version) {
        token = MySession.getToken();
        this.version = version;
    }

    @Override
    public HttpConnector buildConnector(HttpConnectorBuilder builder) {
        return builder
              .setAction(action)
              .setUrl(NetworkConfig.HTTP_URL)
              .setEntity(this)
              .build();
    }

    @Override
    public HttpParser buildParser() {
        return new Parser();
    }

    @Override
    public String toJson() {
        return GsonUtil.toJson(this);
    }
    
    private static class Parser extends HttpJsonParser {
        
        @Override
        protected Object process(String json) throws Exception {
            UserData data = GsonUtil.parseJson(json, UserData.class);
            
            MyDAOManager.getDAO().update(MySession.getUser().fromProtocol(data));
            
            return null;
        }
    }
}