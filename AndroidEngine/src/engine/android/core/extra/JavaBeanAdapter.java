package engine.android.core.extra;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Build;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Daimon
 * @version N
 * @since 6/6/2014
 * @see ArrayAdapter
 */
public abstract class JavaBeanAdapter<T> extends BaseAdapter implements Filterable {

    private List<T> mObjects;

    private final Object mLock = new Object();

    private int mResource;

    private boolean mNotifyOnChange = true;

    private Context mContext;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private ArrayList<T> mOriginalValues;
    private JavaBeanFilter mFilter;
    
    private FilterMatcher<T> mMatcher;

    private LayoutInflater mInflater;

    public JavaBeanAdapter(Context context, int resource) {
        init(context, resource, new ArrayList<T>());
    }

    public JavaBeanAdapter(Context context, int resource, T[] objects) {
        init(context, resource, Arrays.asList(objects));
    }

    public JavaBeanAdapter(Context context, int resource, List<T> objects) {
        init(context, resource, objects);
    }

    public void add(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                mOriginalValues.add(object);
                return;
            }
            
            mObjects.add(object);
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void addAll(Collection<? extends T> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                mOriginalValues.addAll(collection);
                return;
            }

            mObjects.addAll(collection);
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void addAll(T... items) {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                Collections.addAll(mOriginalValues, items);
                return;
            }

            Collections.addAll(mObjects, items);
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void insert(T object, int index) {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                mOriginalValues.add(index, object);
                return;
            }
            
            mObjects.add(index, object);
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void set(T object, int index) {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                mOriginalValues.set(index, object);
                return;
            }
            
            mObjects.set(index, object);
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void set(T oldObject, T newObject) {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                mOriginalValues.set(mOriginalValues.indexOf(oldObject), newObject);
                return;
            }
            
            mObjects.set(mObjects.indexOf(oldObject), newObject);
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void remove(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                mOriginalValues.remove(object);
                return;
            }
            
            mObjects.remove(object);
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void remove(int index) {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                mOriginalValues.remove(index);
                return;
            }
            
            mObjects.remove(index);
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public boolean contains(T object) {
        if (mOriginalValues != null)
        {
            return mOriginalValues.contains(object);
        }
        
        return mObjects.contains(object);
    }

    public void update(Collection<? extends T> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                mOriginalValues.clear();
                if (collection != null) mOriginalValues.addAll(collection);
                return;
            }
            
            mObjects.clear();
            if (collection != null) mObjects.addAll(collection);
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                mOriginalValues.clear();
                return;
            }
            
            mObjects.clear();
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }
    
    public List<T> getItems() {
        if (mOriginalValues != null)
        {
            return mOriginalValues;
        }
        
        return mObjects;
    }

    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            if (mOriginalValues != null)
            {
                Collections.sort(mOriginalValues, comparator);
                return;
            }
            
            Collections.sort(mObjects, comparator);
        }

        if (mNotifyOnChange) notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}.  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     *
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     * 
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    private void init(Context context, int resource, List<T> objects) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mResource = resource;
        mObjects = objects;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public T getItem(int position) {
        return mObjects.get(position);
    }

    public int getPosition(T item) {
        return mObjects.indexOf(item);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null)
        {
            (convertView = newView(position, parent))
            .setTag(holder = new ViewHolder(convertView));
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder != null)
        {
            bindView(position, holder, getItem(position));
        }

        return convertView;
    }
    
    protected View newView(int position, ViewGroup parent) {
        return mInflater.inflate(mResource, parent, false);
    }

    protected abstract void bindView(int position, ViewHolder holder, T item);
    
    @Override
    public Filter getFilter() {
        if (mFilter == null) mFilter = new JavaBeanFilter();
        return mFilter;
    }
    
    public void setFilterMatcher(FilterMatcher<T> matcher) {
        mMatcher = matcher;
    }
    
    public interface FilterMatcher<T> {
        
        boolean match(T item, CharSequence constraint);
    }
    
    private class JavaBeanFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (mOriginalValues == null)
            {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<T>(mObjects);
                }
            }
            
            ArrayList<T> list;
            if (TextUtils.isEmpty(constraint))
            {
                list = mOriginalValues;
                mOriginalValues = null;
            }
            else
            {
                list = new ArrayList<T>(mOriginalValues.size());
                for (T value : mOriginalValues)
                {
                    if (mMatcher.match(value, constraint))
                    {
                        list.add(value);
                    }
                }
                
                list.trimToSize();
            }
            
            return generateResult(list);
        }
        
        private FilterResults generateResult(List<T> result) {
            FilterResults results = new FilterResults();
            results.values = result;
            results.count = result.size();
            
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mObjects = (List<T>) results.values;
            notifyDataSetChanged();
        }
    }

    /**
     * 配合{@link CursorLoader}使用
     */
    public static abstract class JavaBeanCursorAdapter extends CursorAdapter {

        private final LayoutInflater mInflater;

        private final int mResource;

        public JavaBeanCursorAdapter(Context context, int resource) {
            super(context, null, 0);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mResource = resource;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View convertView = mInflater.inflate(mResource, parent, false);
            convertView.setTag(new ViewHolder(convertView));
            return convertView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();

            if (holder != null)
            {
                bindView(holder, cursor);
            }
        }

        protected abstract void bindView(ViewHolder holder, Cursor cursor);
    }

    /**
     * 缓存View，提高效率
     */
    public static final class ViewHolder {

        private final View convertView;

        private final SparseArray<View> views;

        public ViewHolder(View convertView) {
            this.convertView = convertView;
            views = new SparseArray<View>();
        }

        public <T extends View> T getView(int viewId) {
            return retrieveView(viewId);
        }

        public void removeView(int viewId) {
            views.remove(viewId);
        }
        
        public void setTextView(int viewId, CharSequence text) {
            ((TextView) retrieveView(viewId)).setText(text);
        }
        
        public void setImageView(int viewId, int resId) {
            ((ImageView) retrieveView(viewId)).setImageResource(resId);
        }

        public void setAlpha(int viewId, float alpha) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                retrieveView(viewId).setAlpha(alpha);
            }
            else
            {
                AlphaAnimation anim = new AlphaAnimation(alpha, alpha);
                anim.setDuration(0);
                anim.setFillAfter(true);
                retrieveView(viewId).startAnimation(anim);
            }
        }
        
        public void setVisible(int viewId, boolean shown) {
            retrieveView(viewId).setVisibility(shown ? View.VISIBLE : View.GONE);
        }

        public void setVisibility(int viewId, int visibility) {
            retrieveView(viewId).setVisibility(visibility);
        }

        public View getConvertView() {
            return convertView;
        }

        @SuppressWarnings("unchecked")
        private <T extends View> T retrieveView(int viewId) {
            View view = views.get(viewId);
            if (view == null)
            {
                view = convertView.findViewById(viewId);
                views.append(viewId, view);
            }

            return (T) view;
        }
    }
}