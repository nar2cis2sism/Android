package engine.android.core.extra;

import android.os.Handler;
import android.os.Looper;

import engine.android.core.ApplicationManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件总线<p>
 * 功能：事件监听及发送机制
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public final class EventBus {
    
    private static final EventBus instance = new EventBus();
    
    public static final EventBus getDefault() {
        return instance;
    }

    /******************************* 华丽丽的分割线 *******************************/
    
    private final HashMap<String, CopyOnWriteArrayList<EventHandler>> observersByAction;
    private final HashMap<EventHandler, LinkedList<String>> actionsByObserver;
    
    private final Handler mainHandler;
    
    /**
     * Creates a new EventBus instance; each instance is a separate scope in which events are delivered. 
     * To use a central observer, consider {@link #getDefault()}.
     */
    public EventBus() {
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
     * Posts the given event to the event bus.
     */
    public void post(final Event event) {
        CopyOnWriteArrayList<EventHandler> observers = observersByAction.get(event.action);
        if (observers != null)
        {
            for (final EventHandler handler : observers)
            {
                if (handler instanceof EventProcessor)
                {
                    handler.handleEvent(event);
                }
                else
                {
                    if (ApplicationManager.isMainThread()) handler.handleEvent(event);
                    else mainHandler.post(new Runnable() {
                        
                        @Override
                        public void run() {
                            handler.handleEvent(event);
                        }
                    });
                }
            }
        }
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
    
    /**
     * 传输事件
     */
    public static class Event {
        
        public final String action;
        public final int status;
        public final Object param;
        
        public Event(String action, int status, Object param) {
            this.action = action;
            this.status = status;
            this.param = param;
        }
    }
}