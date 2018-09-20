package engine.android.framework.ui.extra.region;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ListView;

import engine.android.framework.R;
import engine.android.framework.ui.BaseListFragment;
import engine.android.framework.ui.extra.SinglePaneActivity.OnBackListener;
import engine.android.util.AndroidUtil;
import engine.android.util.ui.ListViewState;
import engine.android.widget.component.TitleBar;

/**
 * 地区选择界面
 * 
 * @author Daimon
 */
public class RegionFragment extends BaseListFragment implements OnBackListener {
    
    RegionPresenter presenter;
    
    ListViewState state;
    boolean restoreList;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = addPresenter(RegionPresenter.class);
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(R.string.region_title)
        .show();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        root.setBackgroundResource(R.color.divider_category);
        return root;
    }
    
    @Override
    protected void setupListView(ListView listView) {
        View header = new View(getContext());
        header.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtil.dp2px(getContext(), 16)));
        View footer = new View(getContext());
        footer.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtil.dp2px(getContext(), 32)));
        
        listView.addHeaderView(header, null, false);
        listView.addFooterView(footer, null, false);
        listView.setFooterDividersEnabled(false);
        
        state = new ListViewState(listView);
    }
    
    @Override
    protected void notifyDataSetChanged() {
        presenter.updateSelectedRegionCode();
        if (restoreList)
        {
            state.restore();
            restoreList = false;
        }
        else
        {
            getListView().setSelection(0);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        state.save();
        presenter.onListItemClick(position);
    }
    
    @Override
    public boolean onBackPressed() {
        restoreList = true;
        return presenter.onBackPressed();
    }
    
    /**
     * 监听地区变化
     * 
     * @param region 已选地区
     */
    public void setListener(Region region, Listener<Region> listener) {
        super.setListener(region, listener);
    };
    
    public void notifyDataChanged(Region region) {
        super.notifyDataChanged(region);
        finish();
    }
}