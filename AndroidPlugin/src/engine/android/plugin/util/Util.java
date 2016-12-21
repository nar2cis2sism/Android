package engine.android.plugin.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 工具类
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */

public final class Util {

    public static String toString(Object obj) {
        if (obj == null)
        {
            return "null";
        }

        try {
            StringBuilder sb = new StringBuilder("[").append(obj.getClass().getSimpleName()).append("]");
            for (Class<?> c = obj.getClass(); c != Object.class; c = c.getSuperclass())
            {
                for (Field field : c.getDeclaredFields())
                {
                    if (Modifier.isStatic(field.getModifiers()))
                    {
                        continue;
                    }
                    
                    field.setAccessible(true);
                    sb.append("\n").append(field.getName()).append(":").append(field.get(obj));
                }
            }

            return sb.toString();
        } catch (Exception e) {
            return obj.toString();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T obj) {
        if (obj == null)
        {
            return null;
        }

        try {
            Class<?> c = obj.getClass();
            Object clone = c.newInstance();
            for (; c != Object.class; c = c.getSuperclass())
            {
                for (Field field : c.getDeclaredFields())
                {
                    field.setAccessible(true);
                    field.set(clone, field.get(obj));
                }
            }

            return (T) clone;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}