package engine.android.framework.ui.fragment.region;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter.JavaBeanCursorAdapter;
import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.dao.DAOTemplate;
import engine.android.framework.R;
import engine.android.util.ui.CursorLoader;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

class RegionPresenter extends Presenter<RegionFragment> {
    
    private static final int PROVINCE   = 0;        // 省级
    private static final int CITY       = 1;        // 市级
    private static final int REGION     = 2;        // 区级
    
    RegionAdapter adapter;
    RegionLoader  loader;
    
    int level;                                      // 当前地方级别
    String[] regionName = new String[REGION + 1];   // 当前地级名称
    String regionCode;                              // 当前区域编码
    
    Region selected;                                // 已选地区
    String[] selectedRegionCode;                    // 已选地级编码
    
    @Override
    protected void onCreate(Context context) {
        if ((selected = getCallbacks().getData()) != null)
        {
            String regionCode = selected.code;
            selectedRegionCode = new String[REGION + 1];
            selectedRegionCode[PROVINCE] = regionCode.substring(0, 2) + "0000";
            selectedRegionCode[CITY] = regionCode.substring(0, 4) + "00";
            selectedRegionCode[REGION] = regionCode;
        }
        
        adapter = new RegionAdapter(context);
        loader  = new RegionLoader(context);
        getCallbacks().setDataSource(adapter, loader);
    }
    
    public void updateSelectedRegionCode() {
        if (selectedRegionCode != null)
        {
            adapter.selectedRegionCode = selectedRegionCode[level];
        }
    }
    
    public void onListItemClick(int position) {
        Region region = adapter.getItem(position);
        regionName[level] = region.name;
        
        if (level == REGION)
        {
            // 拼凑地区显示名称
            region.name = TextUtils.join(" ", regionName);
            getCallbacks().notifyDataChanged(region);
        }
        else
        {
            level++;
            loader.setRegionCode(regionCode = region.code);
        }
    }

    public boolean onBackPressed() {
        switch (level--) {
            case PROVINCE:
                return false;
            case CITY:
                loader.setRegionCode(null);
                break;
            case REGION:
                loader.setRegionCode(regionCode.substring(0, 2) + "0000");
                break;
        }

        return true;
    }
}

class RegionAdapter extends JavaBeanCursorAdapter {
    
    String selectedRegionCode;                      // 已选区域编码

    public RegionAdapter(Context context) {
        super(context, R.layout.region_item);
    }

    @Override
    protected void bindView(ViewHolder holder, Cursor cursor) {
        Region region = DAOTemplate.convertFromCursor(cursor, Region.class);
        holder.setTextView(R.id.name, region.name);
        holder.setVisible(R.id.selected, region.code.equals(selectedRegionCode));
    }
    
    @Override
    public Region getItem(int position) {
        Cursor cursor = (Cursor) super.getItem(position);
        if (cursor != null)
        {
            return DAOTemplate.convertFromCursor(cursor, Region.class);
        }
        
        return null;
    }
}

class RegionLoader extends CursorLoader {

    public RegionLoader(Context context) {
        super(context, RegionDataBase.open(context));
        setTable(RegionDataBase.TABLE);
        setSelection(Region.REGION_CODE + " LIKE ? AND " + Region.REGION_CODE + " <> ?");
        setRegionCode(null);
    }
    
    public void setRegionCode(String regionCode) {
        if (TextUtils.isEmpty(regionCode))
        {
            // 省级
            setSelectionArgs(new String[] {"%0000", ""});
        }
        else if (regionCode.endsWith("0000"))
        {
            // 市级
            setSelectionArgs(new String[] {regionCode.substring(0, 2) + "%00", regionCode});
        }
        else if (regionCode.endsWith("00"))
        {
            // 区级
            setSelectionArgs(new String[] {regionCode.substring(0, 4) + "%", regionCode});
        }
        
        onContentChanged();
    }
}