package com.project.ui.module.beside;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.project.R;
import com.project.app.bean.AppListItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import engine.android.core.extra.JavaBeanAdapter;
import engine.android.core.extra.JavaBeanLoader;
import engine.android.framework.app.service.LocalServiceBinder;
import engine.android.framework.ui.BaseFragment.Presenter;
import engine.android.util.AndroidUtil;
import engine.android.util.StringUtil.AlphaComparator;
import engine.android.util.ui.UIUtil;
import engine.android.widget.CheckableItem;

public class AppListPresenter extends Presenter {
    
    AppListLoader loader;
    AppListAdapter adapter;
    
    LocalServiceBinder<AppListService> serviceBinder;
    private final HashMap<String, Object> disableMap            // [key为包名]
    = new HashMap<String, Object>();
    
    @Override
    public void onCreate(Context context) {
        loader = new AppListLoader(context);
        adapter = new AppListAdapter(context);
        ((AppListFragment) getCallbacks()).setDataSource(adapter, loader);
        
        serviceBinder = new LocalServiceBinder<AppListService>(context);
        serviceBinder.bindAndStartService(AppListService.class);
    }
    
    @Override
    public void onDestroy() {
        serviceBinder.unbindService();
    }
    
    public void disable(int position) {
        AppListItem item = adapter.getItem(position);
        String packageName = item.appInfo.packageName;
        if (disableMap.containsKey(packageName))
        {
            disableMap.remove(packageName);
            item.isDisabled = false;
        }
        else
        {
            disableMap.put(packageName, null);
            item.isDisabled = true;
        }
        
        adapter.notifyDataSetChanged();
        serviceBinder.getService().disable(item);
    }
}

class AppListLoader extends JavaBeanLoader<AppListItem> {
    
    private final AlphaComparator<AppListItem> ALPHA_COMPARATOR
    = new AlphaComparator<AppListItem>() {
        
        @Override
        public String toString(AppListItem obj) {
            return obj.label;
        }
    };
    
    private final PackageManager pm;

    public AppListLoader(Context context) {
        super(context);
        pm = context.getPackageManager();
    }

    @Override
    public Collection<AppListItem> loadInBackground() {
        List<PackageInfo> list = AndroidUtil.getAllApps(getContext(), false);
        List<AppListItem> items = new ArrayList<AppListItem>(list.size());
        String self = getContext().getPackageName();
        for (PackageInfo packageInfo : list)
        {
            if (!TextUtils.equals(packageInfo.packageName, self))
            {
                items.add(new AppListItem(pm, packageInfo));
            }
        }
        
        Collections.sort(items, ALPHA_COMPARATOR);
        
        return items;
    }
}

class AppListAdapter extends JavaBeanAdapter<AppListItem> {
    
    private boolean processMode;

    public AppListAdapter(Context context) {
        super(context, R.layout.base_list_item_1);
    }
    
    public void setProcessMode(boolean processMode) {
        this.processMode = processMode;
        notifyDataSetChanged();
    }
    
    @Override
    protected View newView(int position, ViewGroup parent) {
        View view = super.newView(position, parent);
        
        // Launcher icon固定为48dp大小
        int size = AndroidUtil.dp2px(getContext(), 48);
        view.findViewById(R.id.icon).setLayoutParams(new RelativeLayout.LayoutParams(size, size));
        
        // 对勾图标
        ImageView check = new ImageView(getContext());
        check.setImageResource(R.drawable.check_red);
        View note = view.findViewById(R.id.note);
        UIUtil.replace(note, check, note.getLayoutParams());
        
        CheckableItem item = new CheckableItem(getContext());
        item.addView(view);
        
        return item;
    }

    @Override
    protected void bindView(int position, ViewHolder holder, AppListItem item) {
        // 应用程序图标
        ((ImageView) holder.getView(R.id.icon)).setImageDrawable(item.icon);
        // 应用程序名称
        holder.setTextView(R.id.subject, item.label);
        // 禁止后台进程
        holder.setVisible(R.id.note, processMode && item.isDisabled);
    }
}