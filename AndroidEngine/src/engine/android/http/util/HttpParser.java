package engine.android.http.util;

import engine.android.http.HttpResponse;

/**
 * HTTP解析器
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public interface HttpParser {

    Object parse(HttpResponse response) throws Exception;
}