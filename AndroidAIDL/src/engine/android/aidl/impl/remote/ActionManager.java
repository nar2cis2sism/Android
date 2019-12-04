package engine.android.aidl.impl.remote;

import engine.android.aidl.Action;
import engine.android.aidl.Action.ActionParam;
import engine.android.aidl.Event;
import engine.android.aidl.ICallback;
import engine.android.aidl.impl.remote.AidlService.ActionCallable;
import engine.android.core.util.LogFactory.LOG;
import engine.android.util.extra.MyThreadFactory;

import android.os.RemoteException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 指令管理器
 * 
 * @author Daimon
 * @since 10/17/2014
 */
class ActionManager extends ActionDispatcher {
    
    private static final int MAX_CONNECTION
    = Math.max(3, Runtime.getRuntime().availableProcessors() - 1);
    
    private final ThreadPoolExecutor threadPool;

    private final ConcurrentHashMap<String, ActionTask> executingActionMap
    = new ConcurrentHashMap<String, ActionTask>();

    public ActionManager() {
        threadPool = new ThreadPoolExecutor(
                MAX_CONNECTION, 
                MAX_CONNECTION,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), 
                new MyThreadFactory("Action"));
        threadPool.allowCoreThreadTimeOut(true);
    }
    
    private class ActionTask implements Runnable {
        
        public final Action action;
        public final ActionCallable<? extends ActionParam> callable;
        
        public ActionTask(Action action, ActionCallable<? extends ActionParam> callable) {
            this.action = action;
            this.callable = callable;
        }

        @Override
        public void run() {
            executeAction(action, callable);
            executingActionMap.remove(action.action, this);
        }
    }

    /**
     * 发送指令
     */
    public void sendAction(Action action) {
        ActionCallable<? extends ActionParam> callable = dispatchAction(action);
        if (action.syncable)
        {
            executeAction(action, callable);
            return;
        }
        
        if (!executingActionMap.containsKey(action.action))
        {
            ActionTask task = new ActionTask(action, callable);
            executingActionMap.put(action.action, task);
            threadPool.execute(task);
        }
    }
    
    /**
     * 取消指令
     */
    public void cancelAction(String action) {
        ActionTask task = executingActionMap.remove(action);
        if (task != null)
        {
            task.callable.cancel();
        }
    }
    
    public void notifyCallback(Event event, ICallback iCallback) {
        ActionTask task = executingActionMap.remove(event.action);
        if (task != null && iCallback != null)
        {
            try {
                iCallback.handle(event);
            } catch (RemoteException e) {
                LOG.log(new ActionException(event.action, e));
            }
        }
    }
}