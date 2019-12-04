package engine.android.aidl.impl.remote;

import engine.android.aidl.Action;
import engine.android.aidl.Action.ActionParam;
import engine.android.aidl.impl.remote.AidlService.ActionCallable;
import engine.android.core.util.LogFactory.LOG;
import engine.android.util.api.TypeUtil;
import engine.android.util.extra.ReflectObject;

import android.os.Bundle;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * 指令分发装置
 * 
 * @author Daimon
 * @since 10/17/2014
 */
class ActionDispatcher {

    private final HashMap<String, ActionCallable<? extends ActionParam>> actionCallableMap
    = new HashMap<String, ActionCallable<? extends ActionParam>>();

    public void registerAction(String action, ActionCallable<? extends ActionParam> callable) {
        actionCallableMap.put(action, callable);
    }

    public ActionCallable<? extends ActionParam> dispatchAction(Action action) {
        ActionCallable<? extends ActionParam> callable = actionCallableMap.get(action.action);
        if (callable != null) return callable;
        throw new ActionException("未注册指令：" + action);
    }
    
    public static void executeAction(Action action, ActionCallable<? extends ActionParam> callable) {
        Bundle bundle = action.data;
        if (bundle != null)
        {
            try {
                ReflectObject ro = new ReflectObject(callable);
                for (Class<?> c = callable.getClass(); c != null; c = c.getSuperclass())
                {
                    Type[] types = c.getGenericInterfaces();
                    if (types != null)
                    {
                        for (Type type : types)
                        {
                            if (type instanceof ActionCallable)
                            {
                                c = TypeUtil.getClass(type, 0);
                                ActionParam param = (ActionParam) c.newInstance();
                                param.readFromBundle(bundle);

                                ro.invoke(ro.getMethod("call", ActionParam.class), param);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.log(new ActionException(action.action, e));
            }
        }
        else
        {
            callable.call(null);
        }
    }
}

class ActionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ActionException(String detailMessage) {
        super(detailMessage);
    }

    public ActionException(String action, Throwable throwable) {
        super(action, throwable);
    }
}