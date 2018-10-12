package com.project.network.action.http;

import com.project.app.MyApp;
import com.project.app.MyContext;
import com.project.app.MySession;
import com.project.app.bean.ServerUrl;
import com.project.network.NetworkConfig;
import com.project.network.action.Actions;
import com.project.network.action.socket.PullOfflineMessage;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;
import com.project.storage.dao.MessageDAO;
import com.project.storage.dao.UserDAO;
import com.project.storage.db.User;
import com.project.util.AppUtil;

import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.network.http.HttpConnectorBuilder.JsonEntity;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.network.socket.SocketManager;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import engine.android.util.manager.MyTelephonyDevice;

import org.json.JSONObject;

import java.util.List;

import protocol.http.AddFriendData;
import protocol.http.LoginData;
import protocol.http.SearchContactData;
import protocol.http.SearchContactData.ContactData;

/**
 * 添加删除好友
 * 
 * @author Daimon
 */
public class AddFriend implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.ADD_FRIEND;
    
    public final String token;              // 用户登录凭证
    
    public final String account;            // 联系人账号
    
    /**
     * 0：加为好友
     * 1：删除好友
     */
    public final int op;                    // 操作指令
    
    public AddFriend(String account, boolean delete) {
        token = MySession.getToken();
        this.account = account;
        op = delete ? 1 : 0;
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
        protected Object process(JSONObject obj) throws Exception {
            AddFriendData data = GsonUtil.parseJson(obj.toString(), AddFriendData.class);
            long timestamp = data.timestamp;
            
            
            
            
            
            return super.process(obj);
        }
    }
}