package com.project.network.action.http;

import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.network.http.HttpConnectorBuilder.JsonEntity;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;

import com.project.network.NetworkConfig;
import com.project.network.action.Actions;
import com.project.network.http.HttpJsonParser;
import com.project.util.AppUtil;

/**
 * 用户注册
 * 
 * @author Daimon
 */
public class Register implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.REGISTER;
    
    public final String username;           // 用户名
    
    public final String password;           // 密码
    
    /**
     * 0：手机号码注册
     */
    public int type;                        // 注册方式
    
    /**
     * 手机号码注册时为短信验证码
     */
    public String passport;
    
    public Register(String username, String password) {
        this.username = username;
        this.password = AppUtil.encryptPassword(password);
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