package com.project.network.action;

import android.text.TextUtils;

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
            String user_info_ver = data.getString("user_info_crc");
            long friend_list_timestamp = data.optLong("friend_list_timestamp");
            
            MySession.setToken(token);
            // 启动socket连接
            SocketManager sm = MyApp.global().getSocketManager();
            sm.setToken(token);
            sm.setup(MySession.getServerUrl().socket_server_url);
            // 更新用户资料
            updateUser(uid, user_info_ver);
            
            
            
            // 查询好友列表
//            MyApp.getHttpManager().sendHttpRequest(new QueryFriendList());
            
            return super.process(data);
        }
        
        private void updateUser(long uid, String user_info_ver) {
            boolean updateUser = true;

            String[] strs = user_info_ver.split(":");
            String version = strs[0];
            String avatar_ver = strs[1];
            
            User user = UserDAO.getUserById(uid);
            if (user == null)
            {
                // 首次登录
                user = new User(uid);
                user.avatar_ver = avatar_ver;
                MyDAOManager.getDAO().save(user);
            }
            else
            {
                updateUser = !TextUtils.equals(version, user.version);
                if (!TextUtils.equals(avatar_ver, user.avatar_ver))
                {
                    user.avatar_ver = avatar_ver;
                    MyDAOManager.getDAO().update(user, UserColumns.AVATAR_VER);
                }
            }
        }
    }
}