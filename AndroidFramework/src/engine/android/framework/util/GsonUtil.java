package engine.android.framework.util;

import com.google.gson.Gson;

public class GsonUtil {
    
    private static final Gson gson = new Gson();
    
    public static String toJson(Object src) {
        return gson.toJson(src);
    }
    
    public static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}