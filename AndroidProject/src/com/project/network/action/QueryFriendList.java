package com.project.network.action;

import com.google.gson.reflect.TypeToken;
import com.project.app.MySession;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;
import com.project.storage.db.Friend;

import org.json.JSONObject;

import java.util.List;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOTransaction;
import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.network.http.HttpManager.StringEntity;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import protocol.java.json.FriendInfo;

/**
 * 查询好友列表
 * 
 * @author Daimon
 */
public class QueryFriendList implements HttpBuilder, StringEntity {
    
    public final String action = Actions.QUERY_FRIEND_LIST;
    
    public final String token;              // 用户登录凭证
    
    public final long timestamp;            // 上次更新的时间戳
    
    public QueryFriendList() {
        token = MySession.getToken();
        timestamp = 0;
    }

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
        
        private static final int TYPE_TOTAL         = 0;    // 全量更新
        private static final int TYPE_INCREMENT     = 1;    // 增量更新
        
        private long timestamp;
        private int sync_type;
        private int sync_status;
        private List<FriendInfo> friendList;
        
        @Override
        protected Object process(JSONObject data) {
            timestamp = data.optLong("timestamp");
            sync_type = data.optInt("sync_type");
            sync_status = data.optInt("sync_status");
            friendList = GsonUtil.parseJson(data.optString("list"), 
                    new TypeToken<List<FriendInfo>>() {}.getType());
            
            MyDAOManager.getDAO().execute(new DAOTransaction() {
                
                @Override
                public boolean execute(DAOTemplate dao) throws Exception {
                    return processDB(dao);
                }
            });
            
            return super.process(data);
        }
        
        private boolean processDB(DAOTemplate dao) {
            if (sync_type == TYPE_TOTAL)
            {
                // 清空数据
                dao.resetTable(Friend.class);
                dao.notifyChange(Friend.class);
            }
            
            if (friendList != null)
            {
                for (FriendInfo info : friendList)
                {
                    dao.save(new Friend(info));
                }
            }
            
            return true;
        }
    }
}