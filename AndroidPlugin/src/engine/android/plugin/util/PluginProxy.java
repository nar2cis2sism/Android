package engine.android.plugin.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PluginProxy<T> implements InvocationHandler {

    private static final ConcurrentHashMap<Class<?>, Proxy> map
    = new ConcurrentHashMap<Class<?>, Proxy>();
    
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
            Class<?> proxyInterface, P proxyInstance) {
        if (map.containsKey(proxyInterface))
        {
            return (T) map.get(proxyInterface);
        }
        
        Proxy p = map.putIfAbsent(proxyInterface, 
                (Proxy) Proxy.newProxyInstance(
                        proxyInterface.getClassLoader(), 
                        new Class[] { proxyInterface }, 
                        proxyInstance));
        if (p == null)
        {
            p = map.get(proxyInterface);
        }
        
        return (T) p;
    }
}