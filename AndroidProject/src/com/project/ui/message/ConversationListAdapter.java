//package com.project.ui.module.message;
//
//import android.content.Context;
//import android.content.res.ColorStateList;
//import android.graphics.Color;
//import android.view.View;
//import android.widget.TextView;
//
//import com.project.R;
//import com.project.bean.ui.ConversationListItem;
//import com.project.bean.ui.ConversationListItem.DateRange;
//import com.project.widget.PinnedHeaderListView.PinnedHeaderAdapter;
//
//import engine.android.core.util.extra.JavaBeanAdapter;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
//public class ConversationListAdapter extends JavaBeanAdapter<ConversationListItem>
//implements PinnedHeaderAdapter {
//
//    public ConversationListAdapter(Context context) {
//        super(context, R.layout.base_list_item_with_category, testData());
//    }
//
//    @Override
//    protected void bindView(int position, ViewHolder holder,
//            ConversationListItem item) {
//        // 日期分类
//        DateRange category = item.category;
//        if (category == null
//        || (position > 0 && category == getItem(position - 1).category))
//        {
//            holder.setVisibility(R.id.category_container, View.GONE);
//        }
//        else
//        {
//            ((TextView) holder.getView(R.id.category)).setText(category.getLabel());
//            holder.setVisibility(R.id.category_container, View.VISIBLE);
//        }
//        
//        // 图标
//        holder.setVisibility(R.id.icon, View.GONE);
//        
//        // 日期显示
//        ((TextView) holder.getView(R.id.note)).setText(item.date);
//
//        // 标题
//        ((TextView) holder.getView(R.id.title)).setText(item.title);
//
//        // 内容
//        ((TextView) holder.getView(R.id.content)).setText(item.content);
//    }
//    
//    @Override
//    public boolean getPinnedHeaderState(int position) {
//        return getItem(position + 1).category != getItem(position).category;
//    }
//
//    @Override
//    public void configurePinnedHeader(View header, int position, float visibleRatio) {
//        PinnedHeaderCache cache = (PinnedHeaderCache) header.getTag();
//        if (cache == null)
//        {
//            cache = new PinnedHeaderCache();
//            cache.category = ((TextView) header.findViewById(R.id.category));
//            cache.color = cache.category.getTextColors();
//            header.setTag(cache);
//        }
//        
//        DateRange category = getItem(position).category;
//        if (category != null)
//        {
//            cache.category.setText(category.getLabel());
//            header.setVisibility(View.VISIBLE);
//        }
//        else
//        {
//            header.setVisibility(View.GONE);
//        }
//        
//        if (visibleRatio == 1.0f)
//        {
//            cache.category.setTextColor(cache.color);
//        }
//        else
//        {
//            int color = cache.color.getDefaultColor();
//            cache.category.setTextColor(Color.argb((int) (0xff * visibleRatio), 
//                    Color.red(color), Color.green(color), Color.blue(color)));
//        }
//    }
//    
//    private static class PinnedHeaderCache {
//        
//        public TextView category;
//        public ColorStateList color;
//    }
//
//    private static List<ConversationListItem> testData() {
//        List<ConversationListItem> list = new ArrayList<ConversationListItem>();
//        
//        // 1
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY, 12);
//        cal.set(Calendar.MINUTE, 0);
//        
//        ConversationListItem item = new ConversationListItem(cal.getTimeInMillis(), 
//                "飞信热点", 
//                "玩转身边-会讲故事的相机玩转身边-会讲故事的相机");
//        list.add(item);
//        
//        // 2
//        cal.set(Calendar.HOUR_OF_DAY, 8);
//        cal.set(Calendar.MINUTE, 22);
//        item = new ConversationListItem(cal.getTimeInMillis(), 
//                "短信箱", 
//                "查询余额服务：您总账户余额为");
//        list.add(item);
//        
//        // 3
//        cal.add(Calendar.DATE, -1);
//        cal.set(Calendar.HOUR_OF_DAY, 15);
//        cal.set(Calendar.MINUTE, 15);
//        item = new ConversationListItem(cal.getTimeInMillis(), 
//                "飞信团队", 
//                "Q萌表情 耍帅有礼");
//        list.add(item);
//        
//        // 4
//        cal.add(Calendar.MONTH, -1);
//        item = new ConversationListItem(cal.getTimeInMillis(), 
//                "系统消息", 
//                "土豪发奖了！十万支付券");
//        list.add(item);
//        
//        // 5
//        cal.add(Calendar.DATE, -1);
//        item = new ConversationListItem(cal.getTimeInMillis(), 
//                "我的电脑", 
//                "[离线]手机轻松传输文件");
//        list.add(item);
//        
//        for (int i = 0; i < 10; i++)
//        {
//            cal.add(Calendar.DATE, -1);
//            item = new ConversationListItem(cal.getTimeInMillis(), 
//                    "我的电脑", 
//                    "[离线]手机轻松传输文件");
//            list.add(item);
//        }
//        
//        return list;
//    }
//}