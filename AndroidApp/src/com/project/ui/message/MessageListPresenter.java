package com.project.ui.message;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.daimon.yueba.R;
import com.project.app.bean.MessageListItem;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.core.extra.JavaBeanLoader;
import engine.android.framework.ui.util.DateRange;
import engine.android.widget.common.list.PinnedHeaderListView.PinnedHeaderAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class MessageListPresenter extends Presenter<MessageListFragment> {
    
    MessageListAdapter adapter;
    MessageListLoader  loader;
    
    @Override
    protected void onCreate(Context context) {
        adapter = new MessageListAdapter(context);
        loader  = new MessageListLoader(context);
        getCallbacks().setDataSource(adapter, loader);
    }
}

class MessageListAdapter extends JavaBeanAdapter<MessageListItem> implements PinnedHeaderAdapter {

    public MessageListAdapter(Context context) {
        super(context, R.layout.message_list_item);
    }

    @Override
    protected void bindView(int position, ViewHolder holder, MessageListItem item) {
        // 日期分类
        DateRange category = item.category;
        if (category == null
        || (position > 0 && category == getItem(position - 1).category))
        {
            holder.setVisible(R.id.category, false);
        }
        else
        {
            holder.setVisible(R.id.category, true);
            holder.setTextView(R.id.category, category.getLabel());
        }
        
        // 头像
        holder.setImageView(R.id.icon, R.drawable.avatar_default);
        
        // 时间
        holder.setTextView(R.id.note, item.time);

        // 名称
        ((TextView) holder.getView(R.id.title)).setText(item.name);

        // 消息
        ((TextView) holder.getView(R.id.content)).setText(item.message);
    }

    @Override
    public boolean getPinnedHeaderState(int position) {
        return getItem(position + 1).category != getItem(position).category;
    }

    @Override
    public void configurePinnedHeader(View header, int position, float visibleRatio) {
        PinnedHeaderCache cache = (PinnedHeaderCache) header.getTag();
        if (cache == null)
        {
            cache = new PinnedHeaderCache();
            cache.category = ((TextView) header);
            cache.color = cache.category.getTextColors();
            header.setTag(cache);
        }
        
        DateRange category = getItem(position).category;
        if (category != null)
        {
            cache.category.setText(category.getLabel());
            header.setVisibility(View.VISIBLE);
        }
        else
        {
            header.setVisibility(View.GONE);
        }
        
        if (visibleRatio == 1.0f)
        {
            cache.category.setTextColor(cache.color);
        }
        else
        {
            int color = cache.color.getDefaultColor();
            cache.category.setTextColor(Color.argb((int) (0xff * visibleRatio), 
                    Color.red(color), Color.green(color), Color.blue(color)));
        }
    }
    
    private static class PinnedHeaderCache {
        
        public TextView category;
        public ColorStateList color;
    }
}

class MessageListLoader extends JavaBeanLoader<MessageListItem> {

    public MessageListLoader(Context context) {
        super(context);
    }

    @Override
    public Collection<MessageListItem> loadInBackground() {
        List<MessageListItem> list = new ArrayList<MessageListItem>();
        
        // 1
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        
        list.add(new MessageListItem(cal.getTimeInMillis(), 
                "飞信热点", 
                "玩转身边-会讲故事的相机玩转身边-会讲故事的相机"));
        
        // 2
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 22);
        list.add(new MessageListItem(cal.getTimeInMillis(), 
                "短信箱", 
                "查询余额服务：您总账户余额为"));
        
        // 3
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 15);
        list.add(new MessageListItem(cal.getTimeInMillis(), 
                "飞信团队", 
                "Q萌表情 耍帅有礼"));
        
        // 4
        cal.add(Calendar.MONTH, -1);
        list.add(new MessageListItem(cal.getTimeInMillis(), 
                "系统消息", 
                "土豪发奖了！十万支付券"));
        
        // 5
        cal.add(Calendar.DATE, -1);
        list.add(new MessageListItem(cal.getTimeInMillis(), 
                "我的电脑", 
                "[离线]手机轻松传输文件"));
        
        for (int i = 0; i < 10; i++)
        {
            cal.add(Calendar.DATE, -1);
            list.add(new MessageListItem(cal.getTimeInMillis(), 
                    "我的电脑", 
                    "[离线]手机轻松传输文件"));
        }
        
        return list;
    }
}