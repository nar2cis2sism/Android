package com.project.network.action.http;

import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.network.http.HttpConnectorBuilder.JsonEntity;
import engine.android.framework.network.http.HttpManager.HttpBuilder;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import engine.android.util.api.StringUtil;
import engine.android.util.extra.ChangeStatus;

import com.project.app.MySession;
import com.project.network.NetworkConfig;
import com.project.network.action.Actions;
import com.project.network.http.HttpJsonParser;
import com.project.storage.MyDAOManager;
import com.project.storage.db.User;
import com.project.storage.provider.ProviderContract.UserColumns;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 修改个人信息
 * 
 * @author Daimon
 */
public class EditUserInfo implements HttpBuilder, JsonEntity, UserColumns {
    
    public final String action = Actions.EDIT_USER_INFO;
    
    public final User user;
    public final ChangeStatus status;
    
    public EditUserInfo(User user) {
        this.user = user;
        status = new ChangeStatus();
        init();
    }
    
    private void init() {
        User origin = MySession.getUser();
        status.setChanged(NICKNAME, !StringUtil.equals(user.nickname, origin.nickname));
        status.setChanged(GENDER, user.gender != origin.gender);
        status.setChanged(BIRTHDAY, user.birthday != origin.birthday);
        status.setChanged(REGION, !StringUtil.equals(user.region_code, origin.region_code));
        status.setChanged(SIGNATURE, !StringUtil.equals(user.signature, origin.signature));
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
        try {
            JSONObject json = new JSONObject()
            .put("action", action)
            .put("token", MySession.getToken())
            .put("version", user.version);
            
            if (status.isChanged(NICKNAME)) json.put("nickname", user.nickname);
            if (status.isChanged(GENDER)) json.put("gender", user.gender);
            if (status.isChanged(BIRTHDAY)) json.put("birthday", user.birthday);
            if (status.isChanged(REGION)) json.put("region", user.toRegion());
            if (status.isChanged(SIGNATURE)) json.put("signature", user.signature);
   
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    private class Parser extends HttpJsonParser {
        
        @Override
        protected Object process(String json) throws Exception {
            JSONObject data = new JSONObject(json);
            user.version = data.getInt("version");
            status.setChanged(VERSION, true);
            
            MyDAOManager.getDAO().update(user, status.getChangedProperties());
            MySession.setUser(user);
            
            return null;
        }
    }
}