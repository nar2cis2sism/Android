package engine.android.framework.ui;

import android.app.ListFragment;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collection;

import engine.android.core.extra.JavaBeanAdapter;
import engine.android.core.extra.JavaBeanLoader;
import engine.android.framework.R;

/**
 * 封装一个列表视图，copy from{@link ListFragment}，仔细比对会有少许改动
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class BaseListFragment extends BaseFragment {
    
    private final Runnable mRequestFocus = new Runnable() {
        
        @Override
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };
    
    private ListView mList;
    private ListAdapter mAdapter;
    private View mEmptyView;

    private View mProgressContainer;
    private View mListContainer;
    
    private boolean mListShown;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content, container, false);
    }

    /**
     * Attach to list view once the view hierarchy has been created.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ensureList();
        super.onViewCreated(view, savedInstanceState);
        
        setupListView(mList);
    }

    protected void setupListView(ListView listView) {}

    /**
     * Detach from list view.
     */
    @Override
    public void onDestroyView() {
        if (mList != null)
        {
            mList.removeCallbacks(mRequestFocus);
            mList = null;
        }
        
        mListShown = false;
        mEmptyView = mProgressContainer = mListContainer = null;
        // System will cache the adapter and setup in the initial of ListView,
        // thus we can not set the header or footer view after that.
        mAdapter = null;
        super.onDestroyView();
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param l The ListView where the click happened
     * @param v The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id The row id of the item that was clicked
     */
    public void onListItemClick(ListView l, View v, int position, long id) {}

    /**
     * Provide the data for the list view.
     */
    public void setListAdapter(ListAdapter adapter) {
        boolean hadAdapter = mAdapter != null;
        mAdapter = adapter;
        if (mList != null)
        {
            mList.setAdapter(adapter);
            if (!mListShown && !hadAdapter)
            {
                // The list was hidden, and previously didn't have an
                // adapter.  It is now time to show it.
                setListShown(true, getView().getWindowToken() != null);
            }
        }
    }

    /**
     * Get the list view widget.
     */
    public ListView getListView() {
        ensureList();
        return mList;
    }

    /**
     * The default content for a ListFragment has a TextView that can
     * be shown when the list is empty.  If you would like to have it
     * shown, call this method to supply the text it should use.
     */
    public void setEmptyText(CharSequence text) {
        ensureList();
        if (mEmptyView instanceof TextView)
        {
            ((TextView) mEmptyView).setText(text);
        }
    }
    
    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     * 
     * <p>Applications do not normally need to use this themselves.  The default
     * behavior of ListFragment is to start with the list not being shown, only
     * showing it once an adapter is given with {@link #setListAdapter(ListAdapter)}.
     * If the list at that point had not been shown, when it does get shown
     * it will be do without the user ever seeing the hidden state.
     * 
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     */
    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }
    
    /**
     * Like {@link #setListShown(boolean)}, but no animation is used when
     * transitioning from the previous state.
     */
    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }
    
    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     * 
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     * @param animate If true, an animation will be used to transition to the
     * new state.
     */
    private void setListShown(boolean shown, boolean animate) {
        ensureList();
        if (mListShown == shown || mListContainer == null || mProgressContainer == null)
        {
            return;
        }

        mListShown = shown;
        if (shown)
        {
            if (animate)
            {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            }
            else
            {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }

            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        }
        else
        {
            if (animate)
            {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            }
            else
            {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }

            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
    }
    
    public void showProgress(boolean shown) {
        ensureList();
        if (mProgressContainer != null)
        {
            mProgressContainer.setVisibility(shown ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * Get the ListAdapter associated with this ListView.
     */
    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    private void ensureList() {
        if (mList != null)
        {
            return;
        }
        
        View root = getView();
        if (root == null)
        {
            throw new IllegalStateException("Content view not yet created");
        }
        
        if (root instanceof ListView)
        {
            mList = (ListView) root;
        }
        else
        {
            mEmptyView = root.findViewById(android.R.id.empty);
            mProgressContainer = root.findViewById(R.id.progressContainer);
            mListContainer = root.findViewById(R.id.listContainer);
            View rawListView = root.findViewById(android.R.id.list);
            if (!(rawListView instanceof ListView))
            {
                throw new RuntimeException(
                        "Content has view with id attribute 'android.R.id.list' "
                        + "that is not a ListView class");
            }
            
            mList = (ListView) rawListView;
            if (mList == null)
            {
                throw new RuntimeException(
                        "Your content must have a ListView whose id attribute is " +
                        "'android.R.id.list'");
            }
            
            if (mEmptyView != null)
            {
                mList.setEmptyView(mEmptyView);
            }
        }
        
        mListShown = true;
        mList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(mList, view, position, id);
            }
        });
        if (mAdapter != null)
        {
            ListAdapter adapter = mAdapter;
            mAdapter = null;
            setListAdapter(adapter);
        }
        else
        {
            // We are starting without an adapter, so assume we won't
            // have our data right away and start with the progress indicator.
            if (mProgressContainer != null)
            {
                setListShown(false, false);
            }
        }
        
        mList.post(mRequestFocus);
    }

    /******************************* 华丽丽的分割线 *******************************/

    /**
     * 内嵌布局替换ListView<br>
     * 一般在{@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}里面调用
     */
    protected static final void replaceListView(View root, int layoutId) {
        View listView = root.findViewById(android.R.id.list);
        ViewGroup listContainer = (ViewGroup) listView.getParent();
        listContainer.removeView(listView);

        LayoutInflater.from(root.getContext()).inflate(layoutId, listContainer);
    }

    /**
     * 内嵌布局注入到ListContainer中，与上面不同的是需要自定义EmptyView<br>
     * 一般在{@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}里面调用
     */
    protected static final void injectListContainer(View root, int layoutId) {
        View listView = root.findViewById(android.R.id.list);
        ViewGroup listContainer = (ViewGroup) listView.getParent();
        listContainer.removeAllViews();

        LayoutInflater.from(root.getContext()).inflate(layoutId, listContainer);
    }

    /**
     * This is called when the load of data is finished.
     */
    protected void notifyDataSetChanged() {}

    /**
     * This is called when the loader of data source is destroyed.
     */
    protected void notifyDataSetInvalidated() {}
    
    public <D> void setDataSource(JavaBeanAdapter<D> adapter, JavaBeanLoader<D> loader) {
        super.setDataSource(new DataSetSource<D>(adapter, loader));
    }
    
    public void setDataSource(CursorAdapter adapter, Loader<Cursor> loader) {
        super.setDataSource(new CursorDataSource(adapter, loader));
    }
    
    protected abstract class ListDataSource<D, A extends ListAdapter> extends DataSource<D> {
        
        protected final A adapter;
        
        public ListDataSource(A adapter, Loader<D> loader) {
            super(loader);
            this.adapter = adapter;
        }

        @Override
        public void onLoadFinished(Loader<D> loader, D data) {
            // Set the new data in the adapter.
            setNewData(data);
            // The list should now be shown.
            show();
            notifyDataSetChanged();
        }
        
        private void show() {
            if (mAdapter == null)
            {
                setListAdapter(adapter);
            }
            else
            {
                if (isResumed())
                {
                    setListShown(true);
                }
                else
                {
                    setListShownNoAnimation(true);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<D> loader) {
            // Clear the data in the adapter.
            clearData();
            notifyDataSetInvalidated();
        }
        
        protected abstract void setNewData(D data);
        
        protected abstract void clearData();
    }
    
    /**
     * A list data source of collection
     */
    public class DataSetSource<D> extends ListDataSource<Collection<D>, JavaBeanAdapter<D>> {
        
        public DataSetSource(JavaBeanAdapter<D> adapter, JavaBeanLoader<D> loader) {
            super(adapter, loader);
        }

        @Override
        protected void setNewData(Collection<D> data) {
            adapter.update(data);
        }

        @Override
        protected void clearData() {
            adapter.clear();
        }
    }
    
    /**
     * A list data source of cursor
     */
    public class CursorDataSource extends ListDataSource<Cursor, CursorAdapter> {
        
        public CursorDataSource(CursorAdapter adapter, Loader<Cursor> loader) {
            super(adapter, loader);
        }

        @Override
        protected void setNewData(Cursor data) {
            adapter.swapCursor(data);
        }

        @Override
        protected void clearData() {
            adapter.swapCursor(null);
        }
    }
}