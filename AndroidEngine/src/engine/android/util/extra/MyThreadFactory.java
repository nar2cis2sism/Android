package engine.android.util.extra;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class MyThreadFactory implements ThreadFactory {

    private final String namePrefix;
    private final boolean isDaemon;
    private final int threadPriority;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public MyThreadFactory(String name) {
        this(name, Thread.NORM_PRIORITY - 1);
    }

    public MyThreadFactory(String name, int threadPriority) {
        this(name, true, threadPriority);
    }

    private MyThreadFactory(String name, boolean isDaemon, int threadPriority) {
        namePrefix = name + "-";
        this.isDaemon = isDaemon;
        this.threadPriority = threadPriority;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());

        t.setDaemon(isDaemon);
        t.setPriority(threadPriority);

        return t;
    }
}