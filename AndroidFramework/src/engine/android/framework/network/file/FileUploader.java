package engine.android.framework.network.file;

import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;

import java.io.File;

/**
 * 文件上传
 * 
 * @author Daimon
 * @since 6/6/2014
 */
@SuppressWarnings("deprecation")
public class FileUploader extends HttpExecutor {
    
    private final MultipartEntity entity;

    public FileUploader(String url, MultipartEntity entity) {
        super(url, entity);
        this.entity = entity;
    }
    
    public FileUploader(String url, File file) {
        this(url, new MultipartEntity());
        entity.addPart("file", new FileBody(file));
    }
    
    public void addPart(FormBodyPart part) {
        entity.addPart(part);
    }
}