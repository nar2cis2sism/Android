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

import protocol.http.SearchContactData;
import protocol.http.SearchContactData.ContactData;

import java.util.List;

public class SearchContact implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.SEARCH_CONTACT;
    
    public final String token;              // 用户登录凭证
    
    public final String key;                // 搜索关键字
    
    /**
     * 高位表示“起始索引(Int32)”
     * 低位表示“搜索数量(Int32)”
     */
    public final Long range;                // 搜索范围
    
    public SearchContact(String key) {
        token = MySession.getToken();
        this.key = key;
        range = null;
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
            SearchContactData data = GsonUtil.parseJson(json, SearchContactData.class);
            
            int count = data.count;
            List<ContactData> list = data.list;
            
            
            
            
            return list;
        }
    }
}