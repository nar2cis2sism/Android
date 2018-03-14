package com.project.network.action;

import com.google.gson.reflect.TypeToken;
import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;
import com.project.storage.dao.FriendDAO;
import com.project.storage.db.Friend;
import com.project.storage.db.User;
import com.project.storage.provider.ProviderContract.UserColumns;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOTransaction;
import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.network.http.HttpConnectorBuilder.JsonEntity;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;

import org.json.JSONObject;

import protocol.java.json.FriendSync;

import java.util.List;

/**
 * 查询好友列表
 * 
 * @author Daimon
 */
public class QueryFriendList implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.QUERY_FRIEND_LIST;
    
    public final String token;              // 用户登录凭证
    
    public final long timestamp;            // 上次更新的时间戳
    
    public QueryFriendList(long timestamp) {
        token = MySession.getToken();
        this.timestamp = timestamp;
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
    
    private class Parser extends HttpJsonParser implements DAOTransaction {
        
        long timestamp;
        int sync_type;
        int sync_status;
        List<FriendSync> list;
        
        @Override
        protected Object process(JSONObject data) throws Exception {
            timestamp = data.getLong("timestamp");
            sync_type = data.getInt("sync_type");
            sync_status = data.getInt("sync_status");
            list = GsonUtil.parseJson(data.getString("list"), 
                    new TypeToken<List<FriendSync>>() {}.getType());
            
            MyDAOManager.getDAO().execute(this);
            if (sync_status == 1)
            {
                // 继续同步
                MyApp.global().getHttpManager().sendHttpRequest(new QueryFriendList(timestamp));
            }
            
            return super.process(data);
        }

        @Override
        public boolean execute(DAOTemplate dao) throws Exception {
            if (sync_type == 0)
            {
                // 全量更新，清空历史数据
                dao.resetTable(Friend.class);
            }
            
            for (FriendSync item : list)
            {
                Friend friend = FriendDAO.getFriendByAccount(item.account);
                if (item.action == 1)
                {
                    // 删除好友
                    if (friend != null) dao.remove(friend);
                }
                else if (friend == null)
                {
                    dao.save(new Friend(item));
                }
            }
            
            // 同步完成，更新时间戳
            User user = MySession.getUser();
            user.friend_list_timestamp = timestamp;
            dao.update(user, UserColumns.FRIEND_LIST_TIMESTAMP);
            
            return true;
        }
    }
}