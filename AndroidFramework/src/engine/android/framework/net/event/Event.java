package engine.android.framework.net.event;

/**
 * 网络事件传输
 * 
 * @author Daimon
 */
public class Event {
    
    public final String action;
    public final int status;
    public final Object param;
    
    public Event(String action, int status, Object param) {
        this.action = action;
        this.status = status;
        this.param = param;
    }
}