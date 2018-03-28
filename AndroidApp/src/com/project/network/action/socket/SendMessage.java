package com.project.network.action.socket;

import engine.android.framework.network.socket.SocketManager.SocketBuilder;
import engine.android.framework.network.socket.SocketResponse;
import protocol.java.ProtocolWrapper.ProtocolEntity.ProtocolData;
import protocol.java.stream.req.Message;

/**
 * 发送消息
 * 
 * @author Daimon
 */
public class SendMessage implements SocketBuilder {
    
    private final Message message;
    
    public SendMessage(Message message) {
        this.message = message;
    }

    @Override
    public ProtocolData buildData() {
        return message;
    }

    @Override
    public SocketResponse buildResponse() {
        // TODO Auto-generated method stub
        return null;
    }
    
//    public final String action = Actions.LOGIN;
//    
//    public final String username;          // 用户名
//    
//    public final String password;          // 密码
//    
//    public final String deviceID;          // 设备唯一标识
//    
//    public SendMessage(String username, String password) {
//        this.username = username;
//        this.password = HexUtil.encode(CryptoUtil.SHA1((password + "000").getBytes()));
//        deviceID = new MyTelephonyDevice(MyContext.getContext()).getDeviceId();
//    }
//
//    @Override
//    public HttpConnector buildConnector(HttpConnectorBuilder builder) {
//        return builder
//              .setAction(action)
//              .setUrl(NetworkConfig.HTTP_URL)
//              .setEntity(this)
//              .build();
//    }
//
//    @Override
//    public HttpParser buildParser() {
//        return new Parser();
//    }
//
//    @Override
//    public String toJson() {
//        return GsonUtil.toJson(this);
//    }
//    
//    private class Parser extends HttpJsonParser {
//        
//        @Override
//        protected Object process(JSONObject data) throws Exception {
//            String token = data.getString("token");
//            String user_info_ver = data.getString("user_info_ver");
//            long friend_list_timestamp = data.optLong("friend_list_timestamp");
//            String[] strs = user_info_ver.split(":");
//            long version = Long.parseLong(strs[0]);
//            long avatar_ver = Long.parseLong(strs[1]);
//            
//            MySession.setToken(token);
//            // 启动socket连接
//            SocketManager sm = MyApp.global().getSocketManager();
//            sm.setToken(token);
//            sm.setup(ServerUrl.getSocketServerUrl());
//            // 更新用户资料
//            User user = processUser(avatar_ver);
//            if (version != user.version)
//            {
//                MyApp.global().getHttpManager().sendHttpRequest(new GetUserInfo(user.version));
//            }
//            // 同步好友列表
//            if (friend_list_timestamp != user.friend_list_timestamp)
//            {
//                MyApp.global().getHttpManager().sendHttpRequest(new QueryFriendList(user.friend_list_timestamp));
//            }
//            
//            return super.process(data);
//        }
//        
//        private User processUser(long avatar_ver) {
//            User user = UserDAO.getUserByUsername(username);
//            if (user == null)
//            {
//                // 首次登录
//                user = new User();
//                user.username = username;
//                user.avatar_ver = avatar_ver;
//                MyDAOManager.getDAO().save(user);
//            }
//            else if (avatar_ver != user.avatar_ver)
//            {
//                user.avatar_ver = avatar_ver;
//                MyDAOManager.getDAO().update(user, UserColumns.AVATAR_VER);
//            }
//            
//            MySession.setUser(user);
//            return user;
//        }
//    }
}