//package com.project.network.action;
//
//import com.project.app.MySession;
//import com.project.network.Actions;
//import com.project.network.NetworkConfig;
//import com.project.network.http.HttpJsonParser;
//
//import org.json.JSONObject;
//
//import engine.android.framework.network.http.HttpManager;
//import engine.android.framework.network.http.HttpManager.HttpBuilder;
//import engine.android.framework.network.http.HttpManager.JsonEntity;
//import engine.android.framework.util.GsonUtil;
//import engine.android.http.HttpConnector;
//import engine.android.http.util.HttpParser;
//import protocol.java.json.UserInfo;
//
///**
// * 修改个人信息
// * 
// * @author Daimon
// */
//public class EditUserInfo implements HttpBuilder, JsonEntity {
//    
//    public final String action = Actions.EDIT_USER_INFO;
//    
//    public final String token;              // 用户登录凭证
//    
//    public final long version;              // 用户信息版本
//    
//    public EditUserInfo() {
//        token = MySession.getToken();
//        version = 0;
//    }
//
//    @Override
//    public HttpConnector buildConnector(HttpManager http) {
//        return http.buildHttpConnector(NetworkConfig.HTTP_URL, action, this);
//    }
//
//    @Override
//    public HttpParser buildParser() {
//        return new Parser();
//    }
//
//    @Override
//    public String toJson() {
//        return GsonUtil.toJson(this);
//    }
//    
//    private class Parser extends HttpJsonParser {
//        
//        @Override
//        protected Object process(JSONObject data) throws Exception {
//            UserInfo info = GsonUtil.parseJson(data.toString(), UserInfo.class);
//            
//            return super.process(data);
//        }
//    }
//}