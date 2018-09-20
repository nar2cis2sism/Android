package com.project.network.action.file;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.project.app.MyApp;
import com.project.app.MySession;
import com.project.network.action.Actions;
import com.project.storage.dao.UserDAO;

import engine.android.framework.network.http.HttpParser.Failure;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoInfo;
import engine.android.http.HttpResponse;
import engine.android.http.util.HttpParser;

/**
 * 上传头像
 * 
 * @author Daimon
 */
public class UploadAvatar extends FileUpload {
    
    public final String action = Actions.AVATAR;
    
    public UploadAvatar(PhotoInfo info) {
        super(info.getPhotoFile());
        setAction(action);
    }

    @Override
    public HttpParser buildParser() {
        return new Parser();
    }

    private class Parser implements HttpParser, Failure {

        @Override
        public Object parse(HttpResponse response) throws Exception {
            String crc = response.getHeader("crc");
            if (TextUtils.isEmpty(crc))
            {
                return this;
            }

            // 头像上传成功
            UserDAO.updateAvatarVersion(Long.parseLong(crc));
            MyApp.global().getImageManager().save(MySession.getUser().getAvatarUrl(), BitmapFactory.decodeFile(file.getAbsolutePath()));
            return null;
        }
    }
}