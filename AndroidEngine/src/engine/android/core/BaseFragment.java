package engine.android.core;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import engine.android.core.util.PresentManager;
import engine.android.core.util.PresentManager.BasePresenter;
import engine.android.util.extra.ReflectObject;

/**
 * Fragment基类<p>
 * 功能：封装一些基础函数
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class BaseFragment extends Fragment {

    // 自定义回调接口（与Activity进行交互）
    private Callbacks mCallbacks;
    public interface Callbacks {}

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

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            setHasOptionsMenu(true);
            setupActionBar(actionBar);
        }
        
        if (dataSource != null) dataSource.restart();
        getPresenters().onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getPresenters().onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        getPresenters().onStart();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        getPresenters().onSaveInstanceState(outState);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        getPresenters().onStop();
    }

    @Override
    public void onDestroy() {
        getPresenters().onDestroy();
        listener = null;
        if (dataSource != null)
        {
            dataSource.destroy();
            dataSource = null;
        }
    
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    
        // Reset the active callback interface.
        mCallbacks = null;
    }

    protected void setupActionBar(ActionBar actionBar) {}

    public final ActionBar getActionBar() {
        if (getActivity() != null)
        {
            return getActivity().getActionBar();
        }

        return null;
    }

    public Context getContext() {
        if (getActivity() != null) return getActivity();
        return ApplicationManager.getMainApplication();
    }

    public final Callbacks getCallbacks() {
        return mCallbacks;
    }

    /**
     * @see Activity#finish()
     */
    public final void finish() {
        try {
            if (getFragmentManager().popBackStackImmediate())
            {
                return;
            }
        } catch (Exception e) {
            // 启动过程中关闭界面会抛出异常java.lang.IllegalStateException: Recursive entry to executePendingTransactions
            // 只能通过非常手段进行下一步处理
            if (getBackStackEntryCount() > 0)
            {
                getFragmentManager().popBackStack();
                return;
            }
        }
        
        if (getActivity() != null)
        {
            getActivity().finish();
        }
    }
    
    /**
     * {@link FragmentManager#getBackStackEntryCount()}方法返回结果不及时
     */
    private int getBackStackEntryCount() {
        ReflectObject ref = new ReflectObject(getFragmentManager());
        try {
            List mBackStackIndices = (List) ref.get("mBackStackIndices");
            if (mBackStackIndices != null)
            {
                return mBackStackIndices.size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
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

    public interface Listener<D> {

        /**
         * 数据更新
         * 
         * @param data 更新后的数据
         */
        void update(D data);
    }

    private Object data;
    private Listener listener;

    /**
     * 设置数据监听器
     * 
     * @param data 初始数据
     * @param listener 数据监听器
     */
    protected <D> void setListener(D data, Listener<D> listener) {
        this.data = data;
        this.listener = listener;
    }

    /**
     * 这里返回的永远是初始数据
     */
    public final <D> D getData() {
        return (D) data;
    }

    /**
     * 通知数据更新
     */
    public void notifyDataChanged(Object data) {
        if (listener != null)
        {
            listener.update(data);
        }
    }

    /******************* 数据源（封装数据加载机制） *******************/
    
    public abstract class DataSource<D> implements LoaderCallbacks<D> {
    
        private final int LOADER_ID = hashCode();
        
        private final Loader<D> loader;
        
        public DataSource(Loader<D> loader) {
            this.loader = loader;
        }
    
        @Override
        public Loader<D> onCreateLoader(int id, Bundle args) {
            return loader;
        }
        
        void restart() {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
        
        void destroy() {
            getLoaderManager().destroyLoader(LOADER_ID);
        }
    
        void loadData() {
            // Tell the loader about the change.
            loader.onContentChanged();
        }
    }

    private DataSource<?> dataSource;
    
    /**
     * 设置数据源<br>
     * Call it before {@link #onActivityCreated(Bundle)}
     */
    public void setDataSource(DataSource<?> dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * 刷新数据（通知数据源更新数据）
     */
    public void refresh() {
        if (dataSource != null && isAdded())
        {
            dataSource.loadData();
        }
    }

    /******************************* Presenter *******************************/
    
    private PresentManager presenterManager;
    
    private final Presenters presenters = new Presenters();
    
    private boolean addIsCalled;

    /**
     * Must keep empty constructor of presenterCls for the instantiation.
     */
    public <P extends Presenter<C>, C extends BaseFragment> P addPresenter(Class<P> presenterCls) {
        if (presenterManager == null) presenterManager = new PresentManager();
        P p = presenterManager.addPresenter(presenterCls);
        p.setCallbacks(this).onCreate(getContext());
        addIsCalled = true;
        return p;
    }
    
    public <P extends Presenter<C>, C extends BaseFragment> void addPresenter(P p) {
        if (presenterManager == null) presenterManager = new PresentManager();
        presenterManager.addPresenter(p);
        p.setCallbacks(this).onCreate(getContext());
        addIsCalled = true;
    }
    
    public <P extends Presenter<C>, C extends BaseFragment> P getPresenter(Class<P> presenterCls) {
        if (presenterManager == null) return null;
        return presenterManager.getPresenter(presenterCls);
    }
    
    private Presenters getPresenters() {
        if (addIsCalled)
        {
            addIsCalled = false;
            presenters.set(presenterManager.getPresenters());
        }
        
        return presenters;
    }
    
    public static abstract class Presenter<C extends BaseFragment> extends BasePresenter<C> {
        
        protected void onCreate(Context context) {}
        
        protected void onActivityCreated(Bundle savedInstanceState) {}
        
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {}
        
        protected void onStart() {}
        
        protected void onSaveInstanceState(Bundle outState) {}
        
        protected void onStop() {}
        
        protected void onDestroy() {}
        
        @Override
        protected final Presenter<C> setCallbacks(BaseFragment callbacks) {
            super.setCallbacks((C) callbacks);
            return this;
        }
    }

    private static class Presenters {
        
        private LinkedList<Presenter> presenters;
        
        public void set(Collection<BasePresenter<?>> collection) {
            if (presenters == null)
            {
                presenters = new LinkedList<Presenter>();
            }
            else
            {
                presenters.clear();
            }
            
            for (BasePresenter<?> p : collection)
            {
                if (p instanceof Presenter)
                {
                    presenters.add((Presenter) p);
                }
            }
        }
        
        public void onActivityCreated(Bundle savedInstanceState) {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onActivityCreated(savedInstanceState);
        }
        
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onActivityResult(requestCode, resultCode, data);
        }
        
        public void onStart() {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onStart();
        }
        
        public void onSaveInstanceState(Bundle outState) {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onSaveInstanceState(outState);
        }
        
        public void onStop() {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onStop();
        }
        
        public void onDestroy() {
            if (presenters == null) return;
            for (Presenter p : presenters) p.onDestroy();
        }
    }
}