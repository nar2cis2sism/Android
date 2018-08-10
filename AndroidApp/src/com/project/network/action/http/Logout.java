package com.project.network.action.http;

import com.project.app.MySession;
import com.project.network.NetworkConfig;
import com.project.network.action.Actions;
import com.project.network.http.HttpJsonParser;

import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.network.http.HttpConnectorBuilder.JsonEntity;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;

/**
 * 用户注销
 * 
 * @author Daimon
 */
public class Logout implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.LOGOUT;
    
    public final String token;              // 用户登录凭证
    
    public Logout() {
        token = MySession.getToken();
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
        return new HttpJsonParser();
    }

    @Override
    public String toJson() {
        return GsonUtil.toJson(this);
    }
}