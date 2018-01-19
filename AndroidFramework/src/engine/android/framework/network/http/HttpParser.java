package engine.android.framework.network.http;

import engine.android.http.HttpResponse;

public interface HttpParser extends engine.android.http.util.HttpParser {

    ReturnData parse(HttpResponse response) throws Exception;
    
    interface ReturnData {}
    
    /**
     * 表示请求失败时返回的数据
     */
    interface Failure extends ReturnData {}
}