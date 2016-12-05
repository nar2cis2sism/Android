package engine.android.http.util;

import engine.android.http.HttpResponse;

/**
 * Http解析器
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public interface HttpParser {

    public void parse(HttpResponse response) throws Exception;
}