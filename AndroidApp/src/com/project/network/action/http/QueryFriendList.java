package com.project.network.action.http;

import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.network.NetworkConfig;
import com.project.network.action.Actions;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;
import com.project.storage.dao.FriendDAO;
import com.project.storage.dao.UserDAO;
import com.project.storage.db.Friend;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOTransaction;
import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.network.http.HttpConnectorBuilder.JsonEntity;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;

import org.json.JSONObject;

import protocol.http.FriendListData;
import protocol.http.FriendListData.FriendListItem;

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
    
    private static class Parser extends HttpJsonParser implements DAOTransaction {
        
        FriendListData data;
        
        @Override
        protected Object process(JSONObject obj) throws Exception {
            data = GsonUtil.parseJson(obj.toString(), FriendListData.class);
            
            MyDAOManager.getDAO().execute(this);
            if (data.sync_status == 1)
            {
                // 继续同步
                MyApp.global().getHttpManager().sendHttpRequest(new QueryFriendList(data.timestamp));
            }
            
            return super.process(obj);
        }

        @Override
        public boolean execute(DAOTemplate dao) throws Exception {
            if (data.sync_type == 0)
            {
                // 全量更新，清空历史数据
                dao.resetTable(Friend.class);
            }
            
            for (FriendListItem item : data.list)
            {
                Friend friend = FriendDAO.getFriendByAccount(item.account);
                if (item.op == 1)
                {
                    // 删除好友
                    if (friend != null) dao.remove(friend);
                }
                else
                {
                    // 加为好友
                    if (friend == null)
                    {
                        dao.save(new Friend(item.account, item.info));
                    }
                    else if (item.info != null)
                    {
                        dao.update(friend.fromProtocol(item.info));
                    }
                }
            }
            
            // 同步完成，更新时间戳
            UserDAO.updateFriendListTimestamp(data.timestamp);
            
            return true;
        }
    }
}