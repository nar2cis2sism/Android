package engine.android.framework.ui;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CursorAdapter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */

public abstract class BaseCursorFragment extends BaseListFragment
implements LoaderCallbacks<Cursor> {

    public static interface Implement {

        public CursorAdapter createAdapter();

        public Loader<Cursor> createLoader();

    }

    private final int LOADER_ID = getClass().getName().hashCode();

    // 后台刷新功能
    private final AtomicBoolean backgroundRefresh = new AtomicBoolean(true);
    private final AtomicBoolean isDataChanged = new AtomicBoolean();

    // This is the Adapter being used to display the list's data.
    private CursorAdapter adapter;

    // 子类实现接口
    private Implement implement;
    protected abstract Implement implement(Activity activity);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BaseCursorFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        implement = implement(activity);
    }

    @Override
    public CursorAdapter getListAdapter() {
        return adapter;
    }

    public Loader<Cursor> getLoader() {
        return getLoaderManager().getLoader(LOADER_ID);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Create an empty adapter we will use to display the loaded data.
        setListAdapter(adapter = implement.createAdapter());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Start out with a progress indicator.
        setListShown(false);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!backgroundRefresh.get() && isDataChanged.compareAndSet(true, false))
        {
            setListShown(false);
            refreshImmediately();
        }
    }

    @Override
    public void onStop() {
        if (getActivity().isFinishing() && !getActivity().isChangingConfigurations())
        {
            getLoaderManager().destroyLoader(LOADER_ID);
        }

        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        implement = null;
    }

    /**
     * 通知List刷新（支持异步调用）
     */

    public final void refresh() {
        if (backgroundRefresh.get() || isResumed())
        {
            refreshImmediately();
        }
        else
        {
            isDataChanged.set(true);
        }
    }

    private void refreshImmediately() {
        if (isAdded() && getLoader() != null)
        {
            // Tell the loader about the change.
            getLoader().onContentChanged();
        }
    }

    /**
     * 启动/关闭后台刷新功能
     */

    public final void setBackgroundRefresh(boolean enable) {
        if (backgroundRefresh.compareAndSet(!enable, enable))
        {
            if (backgroundRefresh.get() && isDataChanged.compareAndSet(true, false))
            {
                refreshImmediately();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return implement.createLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Set the new data in the adapter.
        adapter.swapCursor(data);
        notifyDataSetChanged();

        // The list should now be shown.
        if (isResumed())
        {
            setListShown(true);
        }
        else
        {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear the data in the adapter.
        adapter.swapCursor(null);
        notifyDataSetInvalidated();
    }

    protected void notifyDataSetChanged() {}

    protected void notifyDataSetInvalidated() {}
}