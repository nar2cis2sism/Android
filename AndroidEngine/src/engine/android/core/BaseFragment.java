package engine.android.core;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment基类<p>
 * 功能：封装一些基础函数
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */

public abstract class BaseFragment extends Fragment {

    // 自定义回调接口（与Activity进行交互）
    private Callbacks mCallbacks;
    public static interface Callbacks {}

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BaseFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof Callbacks)
        {
            mCallbacks = (Callbacks) activity;
        }
    }

    @Override
    public abstract View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState);

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Injector.inject(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            setupActionBar(actionBar);
        }
    }

    protected void setupActionBar(ActionBar actionBar) {}

    public final ActionBar getActionBar() {
        if (getActivity() != null)
        {
            return getActivity().getActionBar();
        }

        return null;
    }

    public final Context getContext() {
        if (getActivity() != null)
        {
            return getActivity();
        }

        return ApplicationManager.getApplicationManager();
    }

    public final Callbacks getCallbacks() {
        return mCallbacks;
    }
    
    @Override
    public void onStop() {
        if (loader != null && getActivity().isFinishing() && !getActivity().isChangingConfigurations())
        {
            loader.destroy();
        }
        
        super.onStop();
    }

    @Override
    public void onDestroy() {
        listener = null;
        loader = null;

        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface.
        mCallbacks = null;
    }

    /**
     * @see Activity#finish()
     */
    public final void finish() {
        if (!getFragmentManager().popBackStackImmediate())
        {
            if (getActivity() != null)
            {
                getActivity().finish();
            }
        }
    }

    /**
     * @see #findViewById(int, boolean)
     */
    public View findViewById(int id) {
        return findViewById(id, false);
    }

    /**
     * 查找视图
     * 
     * @param id 视图ID
     * @param findFromActivity 允许从Activity中查找
     */
    public final View findViewById(int id, boolean findFromActivity) {
        View view = getView();
        if (view != null)
        {
            view = view.findViewById(id);
        }

        if (view == null && findFromActivity)
        {
            Activity a = getActivity();
            if (a != null)
            {
                view = a.findViewById(id);
            }
        }

        return view;
    }

    /******************* 自定义数据监听器（与Fragment进行交互） *******************/

    public static interface Listener {

        /**
         * 数据更新
         */
        void update(Object data);
    }

    private Object data;
    private Listener listener;

    /**
     * 设置数据监听器
     * @param data 初始数据
     * @param listener 数据监听器
     */
    public void setListener(Object data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    /**
     * 这里返回的永远是初始数据
     */
    protected final Object getData() {
        return data;
    }

    /**
     * 通知数据更新
     */
    protected void notifyDataChanged(Object data) {
        if (listener != null)
        {
            listener.update(data);
        }
    }
    
    private DataLoader<?> loader;
    
    /**
     * 设置数据加载器（同时启动数据加载）
     */
    public void setDataLoader(DataLoader<?> loader) {
        (this.loader = loader).init();
    }
    
    /**
     * 刷新数据（通知数据加载器更新数据）
     */
    public void refresh() {
        if (loader != null)
            loader.loadData();
    }
    
    /**
     * 数据加载器
     */
    public abstract class DataLoader<D> implements LoaderCallbacks<D> {

        private final int LOADER_ID = hashCode();
        
        private final Loader<D> loader;
        
        public DataLoader(Loader<D> loader) {
            this.loader = loader;
        }

        @Override
        public Loader<D> onCreateLoader(int id, Bundle args) {
            return loader;
        }
        
        void init() {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
        
        void destroy() {
            getLoaderManager().destroyLoader(LOADER_ID);
        }

        void loadData() {
            if (isAdded())
            {
                // Tell the loader about the change.
                loader.onContentChanged();
            }
        }
    }
}