package com.project.network.action.http;
//package com.project.network.action;
//
//import com.project.network.Actions;
//import com.project.network.NetworkConfig;
//import com.project.network.http.HttpJsonParser;
//
//import engine.android.framework.network.http.HttpManager;
//import engine.android.framework.network.http.HttpManager.HttpBuilder;
//import engine.android.framework.network.http.HttpManager.JsonEntity;
//import engine.android.framework.util.GsonUtil;
//import engine.android.http.HttpConnector;
//import engine.android.http.util.HttpParser;
//import engine.android.util.secure.CryptoUtil;
//import engine.android.util.secure.HexUtil;
//
///**
// * 用户注册
// * 
// * @author Daimon
// */
//public class Register implements HttpBuilder, JsonEntity {
//    
//    public final String action = Actions.REGISTER;
//    
//    public final String username;          // 用户名
//    
//    public final String password;          // 密码
//    
//    public Register(String username, String password) {
//        this.username = username;
//        this.password = HexUtil.encode(CryptoUtil.SHA1((password + "000").getBytes()));
//    }
//
//    @Override
//    public HttpConnector buildConnector(HttpManager http) {
//        return http.buildHttpConnector(NetworkConfig.HTTP_URL, action, this);
//    }
//
//    @Override
//    public HttpParser buildParser() {
//        return new HttpJsonParser();
//    }
//
//    @Override
//    public String toJson() {
//        return GsonUtil.toJson(this);
//    }
//}