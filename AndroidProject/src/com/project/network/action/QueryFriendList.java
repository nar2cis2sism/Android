package com.project.network.action;

import static engine.android.framework.app.App.getHttpManager;

import com.google.gson.reflect.TypeToken;
import com.project.app.MySession;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;
import com.project.storage.db.Friend;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOTransaction;
import engine.android.framework.network.event.EventObserver.EventCallback;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;

import org.json.JSONObject;

import protocol.java.json.FriendInfo;

import java.util.List;

/**
 * 查询好友列表
 * 
 * @author Daimon
 */
public class QueryFriendList implements HttpBuilder {
    
    public final String action = Actions.QUERY_FRIEND_LIST;
    
    public final String token;              // 用户登录凭证
    
    public final long timestamp;            // 上次更新的时间戳
    
    public QueryFriendList() {
        token = MySession.getToken();
        timestamp = 0;
    }

    @Override
    public HttpConnector buildHttpConnector() {
        return getHttpManager().buildHttpConnector(
                NetworkConfig.HTTP_URL, 
                action, 
                GsonUtil.toJson(this), 
                new Parser(action, getHttpManager()));
    }
    
    private class Parser extends HttpJsonParser {
        
        private static final int TYPE_TOTAL         = 0;    // 全量更新
        private static final int TYPE_INCREMENT     = 1;    // 增量更新
        
        private long timestamp;
        private int sync_type;
        private int sync_status;
        private List<FriendInfo> friendList;

        public Parser(String action, EventCallback callback) {
            super(action, callback);
        }
        
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