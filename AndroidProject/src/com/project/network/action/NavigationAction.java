package com.project.network.action;

import com.project.app.MyContext;
import com.project.app.MySession;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;

import org.json.JSONObject;

import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.network.http.HttpManager.StringEntity;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import engine.android.util.AndroidUtil;
import protocol.java.json.AppUpgradeInfo;

/**
 * 获取导航
 * 
 * @author Daimon
 */
public class NavigationAction implements HttpBuilder, StringEntity {
    
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
    public String toString() {
        return GsonUtil.toJson(this);
    }

    @Override
    public HttpParser buildParser() {
        return new Parser();
    }
    
    private class Parser extends HttpJsonParser {
        
        @Override
        protected Object process(JSONObject data) {
            // Socket服务器地址
            MySession.setSocketAddress(data.optString("socket_server_url"));
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