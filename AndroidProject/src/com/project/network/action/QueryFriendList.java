package com.project.network.action;

import com.google.gson.reflect.TypeToken;
import com.project.app.MySession;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;
import com.project.storage.db.Friend;
import com.project.storage.provider.ProviderContract.FriendColumns;

import org.json.JSONObject;

import java.util.List;

import engine.android.dao.DAOTemplate;
import engine.android.dao.DAOTemplate.DAOExpression;
import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.network.http.HttpManager.JsonEntity;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import protocol.java.json.FriendOp;

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
    public HttpConnector buildConnector(HttpManager http) {
        return http.buildHttpConnector(NetworkConfig.HTTP_URL, action, this);
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
        protected Object process(JSONObject data) throws Exception {
            long timestamp = data.getLong("timestamp");
            int sync_type = data.getInt("sync_type");
            int sync_status = data.getInt("sync_status");
            List<FriendOp> friendList = GsonUtil.parseJson(data.getString("list"), 
                    new TypeToken<List<FriendOp>>() {}.getType());
            
            DAOTemplate dao = MyDAOManager.getDAO();
            if (sync_type == 0)
            {
                // 全量更新，清空历史数据
                dao.resetTable(Friend.class);
            }
            
            if (friendList != null)
            {
                for (FriendOp op : friendList)
                {
                    if (op.op == 1)
                    {
                        // 删除好友
                        dao.edit(Friend.class).where(DAOExpression.create(FriendColumns.USER_ID).equal(op.uid)).delete();
                    }
                    else
                    {
                        dao.save(new Friend(op));
                    }
                }
            }
            
            return super.process(data);
        }
    }
}