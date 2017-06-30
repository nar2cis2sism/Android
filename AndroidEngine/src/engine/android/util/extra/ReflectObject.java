package engine.android.util.extra;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 反射对象
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2012
 */
public final class ReflectObject {

    private final Object obj;

    public ReflectObject(Object obj) {
        if ((this.obj = obj) == null)
        {
            throw new NullPointerException();
        }
    }

    public Object getObject() {
        return obj;
    }

    /**
     * 利用反射获取类的方法
     * 
     * @param methodName 方法名称
     * @param parameterTypes 参数类型
     */
    public static Method getMethod(Class<?> c, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        for (; c != Object.class; c = c.getSuperclass())
        {
            try {
                method = c.getDeclaredMethod(methodName, parameterTypes);
                break;
            } catch (Exception e) {
                // 这里甚么都不要做！
                // 如果这里的异常打印或者往外抛，就不会往下执行，最后就不会进入到父类中了
            }
        }

        return method;
    }

    /**
     * 利用反射获取对象的方法
     * 
     * @param methodName 方法名称
     * @param parameterTypes 参数类型
     */
    public Method getMethod(String methodName, Class<?>... parameterTypes) {
        return getMethod(obj.getClass(), methodName, parameterTypes);
    }

    /**
     * 利用反射执行对象的方法
     * 
     * @param method 执行方法
     * @param params 方法参数
     */
    public Object invoke(Method method, Object... params) throws Exception {
        if (method == null)
        {
            throw new NoSuchMethodException();
        }

        method.setAccessible(true);
        return method.invoke(obj, params);
    }

    /**
     * 利用反射执行类的静态方法
     * 
     * @param method 执行方法
     * @param params 方法参数
     */
    public static Object invokeStatic(Method method, Object... params) throws Exception {
        if (method == null || !Modifier.isStatic(method.getModifiers()))
        {
            throw new NoSuchMethodException();
        }

        method.setAccessible(true);
        return method.invoke(null, params);
    }

    /**
     * 利用反射执行对象的方法（不抛出异常）
     * 
     * @param method 执行方法
     * @param params 方法参数
     */
    public Object invokeWithoutThrow(Method method, Object... params) {
        try {
            return invoke(method, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 利用反射执行类的静态方法（不抛出异常）
     * 
     * @param method 执行方法
     * @param params 方法参数
     */
    public static Object invokeStaticWithoutThrow(Method method, Object... params) {
        try {
            return invokeStatic(method, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 利用反射执行对象的无参数方法
     * 
     * @param methodName 方法名称
     */
    public Object invoke(String methodName) throws Exception {
        return invoke(getMethod(methodName, (Class[]) null), (Object[]) null);
    }

    /**
     * 利用反射执行类的无参数静态方法
     * 
     * @param methodName 方法名称
     */
    public static Object invokeStatic(Class<?> c, String methodName) throws Exception {
        return invokeStatic(getMethod(c, methodName, (Class[]) null), (Object[]) null);
    }

    /**
     * 利用反射执行对象的无参数方法（不抛出异常）
     * 
     * @param methodName 方法名称
     */
    public Object invokeWithoutThrow(String methodName) {
        try {
            return invoke(methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 利用反射执行类的无参数静态方法（不抛出异常）
     * 
     * @param methodName 方法名称
     */
    public static Object invokeStaticWithoutThrow(Class<?> c, String methodName) {
        try {
            return invokeStatic(c, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 利用反射获取类的变量
     * 
     * @param fieldName 变量名称
     */
    public static Field getField(Class<?> c, String fieldName) {
        Field field = null;
        for (; c != Object.class; c = c.getSuperclass())
        {
            try {
                field = c.getDeclaredField(fieldName);
                break;
            } catch (Exception e) {
                // 这里甚么都不要做！
                // 如果这里的异常打印或者往外抛，就不会往下执行，最后就不会进入到父类中了
            }
        }

        return field;
    }

    /**
     * 利用反射获取对象的变量
     * 
     * @param fieldName 变量名称
     */
    public Field getField(String fieldName) {
        return getField(obj.getClass(), fieldName);
    }

    /**
     * 利用反射获取变量的值
     * 
     * @param field 变量域
     */
    public Object get(Field field) throws Exception {
        if (field == null)
        {
            throw new NoSuchFieldException();
        }

        field.setAccessible(true);
        return field.get(obj);
    }

    /**
     * 利用反射获取变量的值
     * 
     * @param fieldName 变量名称
     */
    public Object get(String fieldName) throws Exception {
        return get(getField(fieldName));
    }

    /**
     * 利用反射获取类变量的值
     * 
     * @param field 变量域
     */
    public static Object getStatic(Field field) throws Exception {
        if (field == null || !Modifier.isStatic(field.getModifiers()))
        {
            throw new NoSuchFieldException();
        }

        field.setAccessible(true);
        return field.get(null);
    }

    /**
     * 利用反射设置变量的值
     * 
     * @param field 变量域
     * @param value 欲设置的值
     */
    public void set(Field field, Object value) throws Exception {
        if (field == null)
        {
            throw new NoSuchFieldException();
        }

        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 利用反射设置变量的值
     * 
     * @param fieldName 变量名称
     * @param value 欲设置的值
     */
    public void set(String fieldName, Object value) throws Exception {
        set(getField(fieldName), value);
    }

    /**
     * 利用反射设置类变量的值
     * 
     * @param field 变量域
     * @param value 欲设置的值
     */
    public static void setStatic(Field field, Object value) throws Exception {
        if (field == null || !Modifier.isStatic(field.getModifiers()))
        {
            throw new NoSuchFieldException();
        }

        field.setAccessible(true);
        field.set(null, value);
    }
}