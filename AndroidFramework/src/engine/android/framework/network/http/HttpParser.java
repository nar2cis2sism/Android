package engine.android.framework.network.http;

import engine.android.http.HttpResponse;

/**
 * HTTP解析器扩展
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public interface HttpParser extends engine.android.http.util.HttpParser {

    ReturnData parse(HttpResponse response) throws Exception;
    
    interface ReturnData {}
    
    /**
     * 表示请求失败时返回的数据
     */
    interface Failure extends ReturnData {}
}