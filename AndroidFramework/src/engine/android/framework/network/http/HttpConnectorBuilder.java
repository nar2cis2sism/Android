package engine.android.framework.network.http;

import engine.android.framework.network.http.HttpConnectorBuilder.FormEntity.Form;
import engine.android.http.HttpConnector;
import engine.android.http.HttpRequest.HttpEntity;

import protocol.util.EntityUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络连接构造器
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class HttpConnectorBuilder {
    
    private final HttpManager manager;
    
    public HttpConnectorBuilder(HttpManager manager) {
        this.manager = manager;
    }
    
    private String url;
    private String action;
    private HttpEntity entity;
    
    public HttpConnectorBuilder setUrl(String url) {
        this.url = url;
        return this;
    }
    
    public HttpConnectorBuilder setAction(String action) {
        this.action = action;
        return this;
    }
    
    public HttpConnectorBuilder setEntity(HttpEntity entity) {
        this.entity = entity;
        return this;
    }
    
    public HttpConnector build() {
        HttpConnector conn = manager.buildHttpConnector(url, action, entity);
        reset();
        return conn;
    }
    
    private void reset() {
        url = action = null;
        entity = null;
    }
    
    public interface StringConverter<Entity> {
        
        String convert(Entity entity);
    }
    
    public <Entity> HttpConnectorBuilder setEntity(Entity entity) {
        return setEntity(entity, null);
    }
    
    public <Entity> HttpConnectorBuilder setEntity(Entity entity, StringConverter<Entity> converter) {
        return setEntity(new StringEntity<Entity>(entity, converter));
    }
    
    private static class StringEntity<Entity> implements HttpEntity {
        
        private final Entity entity;
        private final StringConverter<Entity> converter;
        
        private String string;
        private byte[] content;
        
        public StringEntity(Entity entity, StringConverter<Entity> converter) {
            this.entity = entity;
            this.converter = converter;
        }
        
        private byte[] getContent() {
            if (content == null) content = EntityUtil.toByteArray(toString());
            return content;
        }

        @Override
        public long getContentLength() {
            return getContent().length;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            out.write(getContent());
        }
        
        @Override
        public String toString() {
            if (string == null)
            {
                string = converter == null ? entity.toString() : converter.convert(entity);
            }
            
            return string;
        }
    }
    
    public interface JsonEntity {
        
        String toJson();
    }
    
    private static final StringConverter<JsonEntity> jsonConverter
    = new StringConverter<JsonEntity>() {

        @Override
        public String convert(JsonEntity entity) {
            return entity.toJson();
        }
    };
    
    public HttpConnectorBuilder setEntity(JsonEntity entity) {
        return setEntity(entity, jsonConverter);
    }
    
    public interface FormEntity {
        
        void buildForm(Form form);
        
        class Form {
            
            private final HashMap<String, Object> map = new HashMap<String, Object>();
            
            public void addParameter(String key, Object value) {
                map.put(key, value);
            }
        }
    }
    
    private static final StringConverter<FormEntity> formConverter
    = new StringConverter<FormEntity>() {

        @Override
        public String convert(FormEntity entity) {
            Form form = new Form();
            entity.buildForm(form);
            
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, Object> entry : form.map.entrySet())
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    sb.append("&");
                }
                
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
            
            return sb.toString();
        }
    };
    
    public HttpConnectorBuilder setEntity(FormEntity entity) {
        return setEntity(entity, formConverter);
    }
}