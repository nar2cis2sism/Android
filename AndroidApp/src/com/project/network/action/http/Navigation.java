package com.project.network.action.http;

import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.network.http.HttpConnectorBuilder.JsonEntity;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import engine.android.util.AndroidUtil;

import com.project.app.MyContext;
import com.project.app.MySession;
import com.project.app.bean.ServerUrl;
import com.project.network.NetworkConfig;
import com.project.network.action.Actions;
import com.project.network.http.HttpJsonParser;

import protocol.http.NavigationData;

public class Navigation implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.NAVIGATION;
    
    /**
     * 1: IOS
     * 2: Android
     * 3: 其他
     * 4: Pad
     */
    public final int device = 2;           // 客户端类型
    
    public final String version            // 客户端版本号
    = AndroidUtil.getVersionName(MyContext.getContext());

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
            NavigationData data = GsonUtil.parseJson(json, NavigationData.class);
            
            ServerUrl url = new ServerUrl();
            url.socket_server_url = data.socket_server_url;
            url.upload_server_url = data.upload_server_url;
            url.download_server_url = data.download_server_url;
            MySession.setServerUrl(url);
            // APP升级信息
            MySession.setUpgradeInfo(data.upgrade);
            
            return null;
        }
    }
}