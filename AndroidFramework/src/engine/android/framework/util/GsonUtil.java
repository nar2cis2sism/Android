package engine.android.framework.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class GsonUtil {
    
    private static final Gson gson = new Gson();
    
    public static String toJson(Object src) {
        return gson.toJson(src);
    }
    
    public static <T> T parseJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
    
    public static <T> List<T> parseList(String json, Class<T> type) {
        return gson.fromJson(json, new TypeToken<List<T>>() {}.getType());
    }
}