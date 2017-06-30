package engine.android.framework.network.http.util;

import engine.android.http.HttpResponse;

public interface HttpParser extends engine.android.http.util.HttpParser {

    ReturnData parse(HttpResponse response) throws Exception;
    
    public interface ReturnData {}
    
    /**
     * 表示请求失败时返回的数据
     */
    public interface Failure extends ReturnData {}
}