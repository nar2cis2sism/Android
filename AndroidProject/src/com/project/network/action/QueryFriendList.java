package com.project.network.action;

import static engine.android.framework.app.App.getHttpManager;

import com.project.app.MySession;
import com.project.app.storage.db.Friend;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;

import org.json.JSONObject;

import java.util.List;

import engine.android.framework.network.event.EventCallback;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import protocol.java.json.FriendInfo;

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

        public Parser(String action, EventCallback callback) {
            super(action, callback);
        }
        
        @Override
        protected Object process(JSONObject data) {
            long timestamp = data.optLong("timestamp");
            int sync_type = data.optInt("sync_type");
            int sync_status = data.optInt("sync_status");
            List<FriendInfo> friendList = GsonUtil.parseList(data.optString("list"), FriendInfo.class);
            
            if (sync_type == TYPE_TOTAL)
            {
                // 清空数据
//                MyApp.getDAOTemplate().resetTable(c);
            }
            
            if (friendList != null)
            {
                for (FriendInfo info : friendList)
                {
                    Friend friend = new Friend(info);
                }
            }
            
            
//            String token = data.optString("token");
//            long uid = data.optLong("uid");
//            String user_info_ver = data.optString("user_info_crc");
//            
//            // 启动socket连接
//            MyApp.getSocketManager().setup(MySession.getSocketAddress(), token);
            
            return super.process(data);
        }
    }
}