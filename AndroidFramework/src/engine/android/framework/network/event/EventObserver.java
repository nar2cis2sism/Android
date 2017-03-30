package engine.android.framework.network.event;

import android.os.Handler;
import android.os.Looper;

import engine.android.framework.network.ConnectionStatus;
import engine.android.util.Singleton;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 网络事件监听
 * 
 * @author Daimon
 */
public class EventObserver {
    
    private static final Singleton<EventObserver> instance
    = new Singleton<EventObserver>() {
        
        @Override
        protected EventObserver create() {
            return new EventObserver();
        }
    };
    
    public static final EventObserver getDefault() {
        return instance.get();
    }

    /******************************* 华丽丽的分割线 *******************************/
    
    private final HashMap<String, CopyOnWriteArrayList<EventHandler>> observersByAction;
    private final HashMap<EventHandler, LinkedList<String>> actionsByObserver;
    
    private final Handler mainHandler;
    
    /**
     * Creates a new EventObserver instance; each instance is a separate scope in which events are delivered. 
     * To use a central observer, consider {@link #getDefault()}.
     */
    public EventObserver() {
        observersByAction = new HashMap<String, CopyOnWriteArrayList<EventHandler>>();
        actionsByObserver = new HashMap<EventHandler, LinkedList<String>>();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Registers the given subscriber for special action to receive events.
     * Subscribers must call {@link #unregister(EventHandler)} once they are
     * no longer interested in receiving events.
     */
    public void register(String action, EventHandler subscriber) {
        CopyOnWriteArrayList<EventHandler> observers = observersByAction.get(action);
        if (observers == null)
        {
            observers = new CopyOnWriteArrayList<EventHandler>();
            observersByAction.put(action, observers);
        }
        
        observers.add(subscriber);
        
        LinkedList<String> actions = actionsByObserver.get(subscriber);
        if (actions == null)
        {
            actions = new LinkedList<String>();
            actionsByObserver.put(subscriber, actions);
        }
        
        actions.add(action);
    }
    
    /**
     * Unregisters the given subscriber from all events.
     */
    public void unregister(EventHandler subscriber) {
        LinkedList<String> actions = actionsByObserver.remove(subscriber);
        if (actions == null)
        {
            return;
        }
        
        for (String action : actions)
        {
            CopyOnWriteArrayList<EventHandler> observers = observersByAction.get(action);
            for (EventHandler handler : observers)
            {
                if (handler == subscriber)
                {
                    observers.remove(handler);
                    break;
                }
            }
        }
    }
    
    /**
     * Posts the given event to the event observer.
     */
    public void post(Event event) {
        handleEvent(event, false);
    }
    
    private void handleEvent(final Event event, boolean mainThread) {
        String action = event.action;
        CopyOnWriteArrayList<EventHandler> observers = observersByAction.get(action);
        if (observers != null)
        {
            for (EventHandler handler : observers)
            {
                if (handler instanceof EventProcessor)
                {
                    if (!mainThread) handler.handleEvent(event);
                }
                else
                {
                    if (mainThread) handler.handleEvent(event);
                    else mainHandler.post(new Runnable() {
                        
                        @Override
                        public void run() {
                            handleEvent(event, true);
                        }
                    });
                }
            }
        }
    }
    
    /**
     * 网络事件回调
     */
    public interface EventCallback extends ConnectionStatus {

        void call(String action, int status, Object param);
    }

    /**
     * 事件处理器（在主线程处理）
     */
    public interface EventHandler {

        void handleEvent(Event event);
    }
    
    /**
     * 事件处理器（在当前线程处理）
     */
    public interface EventProcessor extends EventHandler {}
}