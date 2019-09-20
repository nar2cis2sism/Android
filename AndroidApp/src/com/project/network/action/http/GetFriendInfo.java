package com.project.network.action.http;

import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.network.http.HttpConnectorBuilder.JsonEntity;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;

import com.google.gson.GsonBuilder;
import com.project.app.MySession;
import com.project.network.NetworkConfig;
import com.project.network.action.Actions;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;
import com.project.storage.db.Friend;

import protocol.http.FriendData;

import java.lang.reflect.Modifier;

public class GetFriendInfo implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.GET_FRIEND_INFO;
    
    public final String token;              // 用户登录凭证
    
    public final String account;            // 好友账号
    
    public final long version;              // 好友资料版本
    
    private final Friend friend;
    
    public GetFriendInfo(Friend friend) {
        token = MySession.getToken();
        account = friend.account;
        version = friend.version;
        this.friend = friend;
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
        return new GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.PRIVATE)
        .create()
        .toJson(this);
    }
    
    private class Parser extends HttpJsonParser {
        
        @Override
        protected Object process(String json) throws Exception {
            FriendData data = GsonUtil.parseJson(json, FriendData.class);
            
            friend.version = data.version;
            if (data.info != null)
            {
                // 好友信息更新
                MyDAOManager.getDAO().update(friend.fromProtocol(data.info));
            }
            else
            {
                MyDAOManager.getDAO().update(friend, Friend.VERSION);
            }

            return friend;
        }
    }
}