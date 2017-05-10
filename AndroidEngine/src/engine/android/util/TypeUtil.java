package engine.android.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * 泛型的一些操作方法
 * 
 * @author Daimon
 * @version N
 * @since 2/2/2015
 */
public final class TypeUtil {

    /**
     * 获取泛型参数代表的类
     * 
     * @param type 泛型类
     * @param i 表示第几个参数
     */
    @SuppressWarnings("rawtypes")
    public static Class getClass(Type type, int i) {
        if (type instanceof ParameterizedType)
        {
            // 处理泛型类型
            return getGenericClass((ParameterizedType) type, i);
        }
        else if (type instanceof TypeVariable)
        {
            // 处理泛型擦拭对象
            return getClass(((TypeVariable) type).getBounds()[0], 0);
        }
        else
        {
            // class本身也是type，强制转型
            return (Class) type;
        }
    }

    @SuppressWarnings("rawtypes")
    private static Class getGenericClass(ParameterizedType parameterizedType, int i) {
        Type genericClass = parameterizedType.getActualTypeArguments()[i];
        if (genericClass instanceof ParameterizedType)
        {
            // 处理多级泛型
            return (Class) ((ParameterizedType) genericClass).getRawType();
        }
        else if (genericClass instanceof GenericArrayType)
        {
            // 处理数组泛型
            return (Class) ((GenericArrayType) genericClass).getGenericComponentType();
        }
        else if (genericClass instanceof TypeVariable)
        {
            // 处理泛型擦拭对象
            return getClass(((TypeVariable) genericClass).getBounds()[0], 0);
        }
        else
        {
            return (Class) genericClass;
        }
    }
}