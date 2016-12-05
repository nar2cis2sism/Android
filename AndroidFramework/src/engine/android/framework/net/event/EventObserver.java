package engine.android.framework.net.event;

import engine.android.util.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 网络事件监听
 * 
 * @author Daimon
 */
public class EventObserver {
    
    private static final Singleton<EventObserver> instance = new Singleton<EventObserver>() {
        
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
    private final HashMap<EventHandler, ArrayList<String>> actionsByObserver;
    
    /**
     * Creates a new EventObserver instance; each instance is a separate scope in which events are delivered. 
     * To use a central bus, consider {@link #getDefault()}.
     */
    public EventObserver() {
        observersByAction = new HashMap<String, CopyOnWriteArrayList<EventHandler>>();
        actionsByObserver = new HashMap<EventHandler, ArrayList<String>>();
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
        
        ArrayList<String> actions = actionsByObserver.get(subscriber);
        if (actions == null)
        {
            actions = new ArrayList<String>();
            actionsByObserver.put(subscriber, actions);
        }
        
        actions.add(action);
    }

    /**
     * Unregisters the given subscriber from all events.
     */
    public void unregister(EventHandler subscriber) {
        ArrayList<String> actions = actionsByObserver.remove(subscriber);
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
        CopyOnWriteArrayList<EventHandler> observers = observersByAction.get(event.action);
        if (observers != null)
        {
            for (EventHandler handler : observers)
            {
                handler.handleEvent(event);
            }
        }
    }
}