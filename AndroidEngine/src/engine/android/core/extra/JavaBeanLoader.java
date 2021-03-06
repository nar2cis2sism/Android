package engine.android.core.extra;

import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.res.Resources;

import java.util.Collection;

/**
 * 配合{@link JavaBeanAdapter}使用
 * 
 * @author Daimon
 * @since 6/6/2014
 * @see CursorLoader
 */
public abstract class JavaBeanLoader<D> extends AsyncTaskLoader<Collection<D>> {

    private ConfigChange config;

    private DataChangeObserver dataChangeObserver;
    private boolean registerObserver;

    private Collection<D> mData;

    public JavaBeanLoader(Context context) {
        super(context);
    }

    public void setConfigChange(ConfigChange config) {
        this.config = config;
    }

    public void setDataChangeObserver(DataChangeObserver dataChangeObserver) {
        this.dataChangeObserver = dataChangeObserver;
    }

    /**
     * Called when there is new data to deliver to the client.
     */
    @Override
    public void deliverResult(Collection<D> data) {
        if (isReset())
        {
            // An async query came in while the loader is stopped. We don't need
            // the result.
            if (data != null)
            {
                releaseResources(data);
            }

            return;
        }

        Collection<D> oldData = mData;
        mData = data;

        if (isStarted())
        {
            // If the Loader is currently started, we can immediately deliver
            // its results.
            super.deliverResult(data);
        }

        // At this point we can release the resources associated with
        // 'oldData' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldData != null && oldData != data)
        {
            releaseResources(oldData);
        }
    };

    /**
     * Handles a request to start the Loader.
     */
    protected void onStartLoading() {
        if (mData != null)
        {
            // If we currently have a result available, deliver it immediately.
            deliverResult(mData);
        }

        // Start watching for changes in the data.
        if (dataChangeObserver != null && !registerObserver)
        {
            dataChangeObserver.registerObserver(getContext());
            registerObserver = true;
        }

        if (takeContentChanged()
        ||  mData == null
        // Has something interesting in the configuration changed since
        // we last built the data?
        || (config != null && config.applyConfig(getContext().getResources())))
        {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    };

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(Collection<D> data) {
        if (data != null) releaseResources(data);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        // Ensure the loader is stopped
        onStopLoading();

        if (mData != null)
        {
            releaseResources(mData);
            mData = null;
        }

        // Stop monitoring for changes.
        if (registerObserver)
        {
            dataChangeObserver.unregisterObserver(getContext());
            registerObserver = false;
        }
    }

    /**
     * Helper function to take care of releasing resources associated with an
     * actively loaded data set.
     */
    protected void releaseResources(Collection<D> data) {
        // For something like a Cursor, we would close it here.
    }

    /**
     * Helper for determining if the configuration has changed in an interesting
     * way so we need to rebuild the data.
     */
    public interface ConfigChange {

        boolean applyConfig(Resources res);
    }

    /**
     * Helper class to look for interesting changes to the data so that the
     * loader can be updated.
     */
    public abstract class DataChangeObserver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }

        public abstract void registerObserver(Context context);

        public abstract void unregisterObserver(Context context);
        
        protected final void refresh() {
            // Tell the loader about the change.
            onContentChanged();
        }
    }
}