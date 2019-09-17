package engine.android.framework.util;

import engine.android.core.extra.JavaBeanAdapter;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * 对数据源进行分类
 * 
 * @author Daimon
 * @since 6/6/2016
 */
public abstract class FilterAdapter<T> extends JavaBeanAdapter<T> {

    private static final String FILTER_ALL = new Object().toString();

    private final HashMap<String, List<T>> filterMap = new HashMap<String, List<T>>();

    protected String filter;

    /**
     * @return 分类标签
     */
    protected abstract String filter(T item);

    public FilterAdapter(Context context, int resource) {
        super(context, resource);
    }

    /**
     * 根据标签分类
     */
    public void setFilter(String filter) {
        super.update(filterMap.get(processFilter(this.filter = filter)));
    }

    private String processFilter(String filter) {
        if (TextUtils.isEmpty(filter)) filter = FILTER_ALL;
        return filter;
    }

    @Override
    public void update(Collection<? extends T> collection) {
        filterMap.clear();
        addAll(collection);
    }

    @Override
    public void addAll(Collection<? extends T> collection) {
        if (collection != null)
        {
            for (T item : collection)
            {
                addItem(FILTER_ALL, item);
                String filter = filter(item);
                if (!TextUtils.isEmpty(filter))
                {
                    addItem(filter, item);
                }
            }
        }

        setFilter(filter);
    }
    
    @Override
    public void addAll(T... items) {
        addAll(Arrays.asList(items));
    }

    private void addItem(String filter, T item) {
        List<T> list = filterMap.get(filter);
        if (list == null)
        {
            filterMap.put(filter, list = new ArrayList<T>());
        }

        list.add(item);
    }
}
