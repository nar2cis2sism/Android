package com.project.network.action;

import com.project.app.MyApp;
import com.project.app.MyContext;
import com.project.app.MySession;
import com.project.network.Actions;
import com.project.network.NetworkConfig;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;
import com.project.storage.dao.UserDAO;
import com.project.storage.db.User;
import com.project.storage.provider.ProviderContract.UserColumns;

import org.json.JSONObject;

import engine.android.framework.network.http.HttpManager;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.framework.network.http.HttpManager.JsonEntity;
import engine.android.framework.network.socket.SocketManager;
import engine.android.framework.util.GsonUtil;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import engine.android.util.manager.MyTelephonyDevice;
import engine.android.util.secure.CryptoUtil;
import engine.android.util.secure.HexUtil;

/**
 * 用户登录
 * 
 * @author Daimon
 */
public class Login implements HttpBuilder, JsonEntity {
    
    public final String action = Actions.LOGIN;
    
    public final String username;          // 用户名
    
    public final String password;          // 密码
    
    public final String deviceID;          // 设备唯一标识
    
    public Login(String username, String password) {
        this.username = username;
        this.password = HexUtil.encode(CryptoUtil.SHA1((password + "000").getBytes()));
        deviceID = new MyTelephonyDevice(MyContext.getContext()).getDeviceId();
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
            String token = data.getString("token");
            long uid = data.getLong("uid");
            String user_info_ver = data.getString("user_info_ver");
            long friend_list_timestamp = data.optLong("friend_list_timestamp");
            String[] strs = user_info_ver.split(":");
            long version = Long.parseLong(strs[0]);
            long avatar_ver = Long.parseLong(strs[1]);
            
            MySession.setToken(token);
            // 启动socket连接
            SocketManager sm = MyApp.global().getSocketManager();
            sm.setToken(token);
            sm.setup(MySession.getServerUrl().socket_server_url);
            // 更新用户资料
            User user = processUser(uid, avatar_ver);
            if (version != user.version)
            {
                MyApp.global().getHttpManager().sendHttpRequest(new GetUserInfo(user.version));
            }
            // 同步好友列表
            if (friend_list_timestamp != user.friend_list_timestamp)
            {
                MyApp.global().getHttpManager().sendHttpRequest(new QueryFriendList(user.friend_list_timestamp));
            }
            
            return super.process(data);
        }
        
        private User processUser(long uid, long avatar_ver) {
            User user = UserDAO.getUserById(uid);
            if (user == null)
            {
                // 首次登录
                user = new User(uid);
                user.avatar_ver = avatar_ver;
                MyDAOManager.getDAO().save(user);
            }
            else if (avatar_ver != user.avatar_ver)
            {
                user.avatar_ver = avatar_ver;
                MyDAOManager.getDAO().update(user, UserColumns.AVATAR_VER);
            }
            
            MySession.setUser(user);
            return user;
        }
    }
}