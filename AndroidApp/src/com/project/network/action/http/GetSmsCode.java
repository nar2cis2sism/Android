package com.project.network.action.http;

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
 * 获取手机验证码
 * 
 * @author Daimon
 */
public class GetSmsCode implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.GET_SMS_CODE;
    
    public final String mobile_phone;       // 手机号
    
    /**
     * 0：允许重复
     * 1：禁止重复
     */
    public int type;                        // 号码验重类型
    
    public GetSmsCode(String mobile_phone) {
        this.mobile_phone = mobile_phone;
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