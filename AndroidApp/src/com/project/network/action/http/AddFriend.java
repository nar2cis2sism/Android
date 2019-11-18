package com.project.network.action.http;

import static com.project.storage.dao.UserDAOManager.BaseDAO.dao;

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
import com.project.storage.dao.FriendDAO;
import com.project.storage.dao.UserDAO;
import com.project.storage.db.Friend;

import protocol.http.AddFriendData;

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
    
    private class Parser extends HttpJsonParser {
        
        @Override
        protected Object process(String json) throws Exception {
            AddFriendData data = GsonUtil.parseJson(json, AddFriendData.class);
            UserDAO.updateFriendListTimestamp(data.timestamp);
            
            Friend friend = FriendDAO.getFriendByAccount(account);
            if (op == 1)
            {
                // 删除好友
                if (friend != null) dao.remove(friend);
            }
            else
            {
                if (friend == null)
                {
                    dao.save(new Friend(account, data.info));
                }
                else if (data.info != null)
                {
                    dao.update(friend.fromProtocol(data.info));
                }
            }
            
            return null;
        }
    }
}