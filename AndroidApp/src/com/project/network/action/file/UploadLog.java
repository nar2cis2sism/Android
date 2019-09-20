package com.project.network.action.file;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.File;

import engine.android.core.util.LogFactory.LOG;
import engine.android.util.file.FileManager;
import engine.android.util.secure.ZipUtil;

import com.project.network.NetworkConfig;

/**
 * 上传日志
 * 
 * @author Daimon
 */
public class UploadLog extends engine.android.framework.network.file.FileUploader {

    private final File logZip;

    public UploadLog(File logDir) {
        super(NetworkConfig.LOG_UPLOAD_URL, new MultipartEntity());
        init(logDir, logZip = new File(logDir.getAbsolutePath() + ".zip"));
    }
    
    private void init(File logDir, File logZip) {
        try {
            ZipUtil.zip(logZip, logDir);

            addPart(new FormBodyPart("file", new FileBody(logZip)));
            addPart(new FormBodyPart("name", new StringBody(logZip.getName())));
        } catch (Exception e) {
            LOG.log(e);
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public synchronized HttpEntity execute() throws Exception {
        try {
            return super.execute();
        } finally {
            FileManager.delete(logZip);
        }
    }
}