package engine.android.http.util.xml;

import java.util.Map;

/**
 * Http发包收包接口（懒人工具）
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public interface HttpPackage {

    /**
     * 发包请求
     * 
     * @param map key为发包的字段,value为发包的值
     */
    void request(Map<String, String> map);

    /**
     * 收包回应
     * 
     * @param map key为收包的字段,value为收包的值
     */
    void response(Map<String, String> map);
}