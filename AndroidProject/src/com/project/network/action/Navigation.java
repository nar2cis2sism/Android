package com.project.network.action;

import com.project.app.MyContext;
import com.project.app.MySession;
import com.project.app.bean.ServerUrl;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;

import org.json.JSONObject;

import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.network.http.HttpManager.JsonEntity;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import engine.android.util.AndroidUtil;
import protocol.java.json.AppUpgradeInfo;

/**
 * 获取导航配置
 * 
 * @author Daimon
 */
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
            String socket_server_url = data.getString("socket_server_url");
            String upload_server_url = data.getString("upload_server_url");
            String download_server_url = data.getString("download_server_url");
            
            ServerUrl url = new ServerUrl();
            url.socket_server_url = socket_server_url;
            url.upload_server_url = upload_server_url;
            url.download_server_url = download_server_url;
            MySession.setServerUrl(url);
            
            // APP升级信息
            JSONObject upgrade = data.optJSONObject("upgrade");
            if (upgrade != null)
            {
                // 需要升级
                return GsonUtil.parseJson(upgrade.toString(), AppUpgradeInfo.class);
            }
            
            return super.process(data);
        }
    }
}