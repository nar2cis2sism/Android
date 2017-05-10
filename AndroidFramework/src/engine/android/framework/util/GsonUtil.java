package engine.android.framework.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public final class GsonUtil {
    
    private static final Gson gson = new Gson();
    
    public static String toJson(Object src) {
        return gson.toJson(src);
    }
    
    public static <T> T parseJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
    
    public static <T> T parseJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
}