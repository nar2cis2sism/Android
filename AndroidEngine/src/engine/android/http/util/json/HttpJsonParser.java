package engine.android.http.util.json;

import engine.android.http.HttpResponse;
import engine.android.http.util.HttpParser;
import engine.android.util.io.IOUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * Json解析器
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public abstract class HttpJsonParser implements HttpParser {

    @Override
    public void parse(HttpResponse response) throws Exception {
        parse(new JSONObject(read(response.getInputStream())));
    }

    protected String read(InputStream in) throws IOException {
        StringWriter sw = new StringWriter();
        IOUtil.writeStream(new InputStreamReader(in), sw);
        return sw.toString();
    }

    /**
     * 需要子类实现
     */

    protected abstract void parse(JSONObject json) throws Exception;
}