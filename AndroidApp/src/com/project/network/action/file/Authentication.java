package com.project.network.action.file;

import com.project.app.MyContext;
import com.project.network.action.Actions;

import engine.android.core.util.LogFactory.LOG;
import engine.android.framework.network.http.HttpConnectorBuilder;
import engine.android.framework.ui.presenter.PhotoPresenter.PhotoInfo;
import engine.android.http.HttpConnector;
import engine.android.http.util.HttpParser;
import engine.android.util.file.FileManager;
import engine.android.util.secure.ZipUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 实名认证
 * 
 * @author Daimon
 */
public class Authentication extends FileUpload {
    
    public final String action = Actions.AUTHENTICATION;
    
    private final Collection<PhotoInfo> info;
    
    public Authentication(Collection<PhotoInfo> info) {
        super(new File(FileManager.getCacheDir(MyContext.getContext(), true), "authentication.zip"));
        this.info = info;
        setAction(action);
    }
    
    @Override
    public HttpConnector buildConnector(HttpConnectorBuilder builder) {
        try {
            List<File> list = new ArrayList<File>(info.size());
            for (PhotoInfo item : info)
            {
                list.add(item.getPhotoFile());
            }
            
            ZipUtil.zip(file, list.toArray(new File[list.size()]));
        } catch (Exception e) {
            LOG.log(e);
        }
        
        return super.buildConnector(builder);
    }

    @Override
    public HttpParser buildParser() {
        return null;
    }
}