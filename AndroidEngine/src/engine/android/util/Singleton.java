package engine.android.util;

/**
 * Singleton helper class for lazily initialization.
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2012
 */
public abstract class Singleton<T> {

    private volatile T mInstance;

    protected abstract T create();

    public final T get() {
        if (mInstance == null)
        {
            synchronized (this) {
                if (mInstance == null)
                {
                    mInstance = create();
                }
            }
        }

        return mInstance;
    }
}