package com.project.http.builder;

import static engine.android.framework.net.MyNetManager.getHttpManager;

import com.project.MySession;
import com.project.action.Actions;
import com.project.http.json.MyHttpJsonParser;

import engine.android.framework.MyConfiguration.MyConfiguration_HTTP;
import engine.android.framework.MyContext;
import engine.android.framework.net.event.EventCallback;
import engine.android.framework.net.http.MyHttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.util.AndroidUtil;

import org.json.JSONObject;

import protocol.java.json.AppUpgradeInfo;

/**
 * 获取导航
 * 
 * @author Daimon
 */
public class NavigationAction implements HttpBuilder {
    
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
    public HttpConnector buildHttpConnector() {
        return getHttpManager().buildHttpConnector(
                MyConfiguration_HTTP.HTTP_URL, 
                action, 
                GsonUtil.toJson(this), 
                new Parser(action, getHttpManager()))
                .setRemark("获取导航");
    }
    
    private class Parser extends MyHttpJsonParser {

        public Parser(String action, EventCallback callback) {
            super(action, callback);
        }
        
        @Override
        protected Object process(JSONObject data) {
            // Socket服务器地址
            MySession.setSocketAddress(data.optString("socket_server_url"));
            // APP升级信息
            JSONObject upgrade = data.optJSONObject("upgrade");
            if (upgrade != null)
            {
                // 需要升级
                return GsonUtil.fromJson(upgrade.toString(), AppUpgradeInfo.class);
            }
            
            return super.process(data);
        }
    }
}