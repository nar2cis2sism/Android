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

import protocol.http.LoginData;

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
        this.password = AppUtil.encryptPassword(password);
        deviceID = new MyTelephonyDevice(MyContext.getContext()).getDeviceId();
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
        protected Object process(JSONObject obj) throws Exception {
            LoginData data = GsonUtil.parseJson(obj.toString(), LoginData.class);
            
            MySession.setToken(data.token);
            // 启动socket连接
            SocketManager sm = MyApp.global().getSocketManager();
            sm.setToken(data.token);
            sm.setup(ServerUrl.getSocketServerUrl());
            // 更新用户资料
            int avatar_ver = (int) (data.version >> 32);
            int version = (int) data.version;
            User user = processUser(avatar_ver);
            if (version > user.version)
            {
                MyApp.global().getHttpManager().sendHttpRequest(new GetUserInfo(user.version));
            }
            // 同步好友列表
            if (data.friend_list_timestamp != null && data.friend_list_timestamp > user.friend_list_timestamp)
            {
                MyApp.global().getHttpManager().sendHttpRequest(new QueryFriendList(user.friend_list_timestamp));
            }
            // 获取离线消息
            MyApp.global().getSocketManager().sendSocketRequest(new PullOfflineMessage(MessageDAO.getLatestMessageTimestamp()));
            
            return super.process(obj);
        }
        
        private User processUser(int avatar_ver) {
            User user = UserDAO.getUserByUsername(username);
            if (user == null)
            {
                // 首次登录
                user = new User();
                user.username = username;
                user.avatar_ver = avatar_ver;
                MyDAOManager.getDAO().save(user);
            }
            else if (avatar_ver > user.avatar_ver)
            {
                user.avatar_ver = avatar_ver;
                MyDAOManager.getDAO().update(user, User.AVATAR_VER);
            }
            
            MySession.setUser(user);
            return user;
        }
    }
}