package engine.android.plugin.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * 代理技术
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2014
 */
public abstract class PluginProxy<T> implements InvocationHandler {

    private static final HashMap<Class<?>, Proxy> map
    = new HashMap<Class<?>, Proxy>();
    
    public final T thisObject;
    
    public PluginProxy(T obj) {
        thisObject = obj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        return method.invoke(thisObject, args);
    }
    
    @SuppressWarnings("unchecked")
    public static <T, P extends PluginProxy<T>> T getProxy(
            Class<T> proxyInterface, P proxyInstance) {
        Proxy p = map.get(proxyInterface);
        if (p == null)
        {
            p = (Proxy) Proxy.newProxyInstance(
                    proxyInterface.getClassLoader(), 
                    new Class[] { proxyInterface }, 
                    proxyInstance);
            map.put(proxyInterface, p);
        }
        
        return (T) p;
    }
}