package com.project.ui.message;

import engine.android.core.BaseFragment.Presenter;
import engine.android.core.extra.JavaBeanAdapter;
import engine.android.core.util.CalendarFormat.DateRange;
import engine.android.dao.util.JavaBeanLoader;
import engine.android.framework.ui.widget.AvatarImageView;
import engine.android.util.ui.UIUtil;
import engine.android.widget.common.list.PinnedHeaderListView.PinnedHeaderAdapter;
import engine.android.widget.common.text.BadgeView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimon.yueba.R;
import com.project.app.MyApp;
import com.project.app.bean.MessageListItem;
import com.project.storage.MyDAOManager;
import com.project.storage.dao.FriendDAO;
import com.project.storage.dao.MessageDAO;
import com.project.storage.db.Friend;
import com.project.storage.db.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class MessageListPresenter extends Presenter<MessageListFragment> {
    
    MessageListAdapter adapter;
    MessageListLoader loader;
    
    @Override
    protected void onCreate(Context context) {
        getCallbacks().setDataSource(adapter = new MessageListAdapter(context),
                loader = new MessageListLoader(context));
    }
}

class MessageListAdapter extends JavaBeanAdapter<MessageListItem> implements PinnedHeaderAdapter {

    public MessageListAdapter(Context context) {
        super(context, R.layout.message_list_item);
    }
    
    @Override
    protected View newView(int position, LayoutInflater inflater, ViewGroup parent) {
        View root = super.newView(position, inflater, parent);
        View icon = root.findViewById(R.id.icon);
        // 头像
        AvatarImageView avatar = new AvatarImageView(getContext());
        UIUtil.replace(icon, avatar, icon.getLayoutParams());
        // 未读徽章
        BadgeView badge = new BadgeView(getContext(), avatar);
        root.findViewById(R.id.icon).setTag(badge);
        
        return root;
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
        // 未读徽章
        BadgeView badge = (BadgeView) holder.getView(R.id.icon).getTag();
        if (item.unreadCount != 0)
        {
            badge.setText(String.valueOf(item.unreadCount));
            badge.show();
        }
        else
        {
            badge.hide();
        }
        // 头像
        ((AvatarImageView) badge.getTarget()).display(item.getFriendAvatarUrl());
        // 名称
        holder.setTextView(R.id.title, item.name);
        // 消息
        holder.setTextView(R.id.content, item.message);
        // 时间
        holder.setTextView(R.id.note, item.timeText);
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
        super(context, MyDAOManager.getDAO());
        listen(Message.class);
    }

    @Override
    public Collection<MessageListItem> loadInBackground() {
        List<Message> messages = MessageDAO.getMessageList();
        if (messages != null)
        {
            ArrayList<MessageListItem> list = new ArrayList<MessageListItem>(messages.size());
            for (Message message : messages)
            {
                Friend friend = FriendDAO.getFriendByAccount(message.account);
                MessageListItem item = new MessageListItem(message, friend);
                item.unreadCount = MessageDAO.getUnreadMessageCount(message.account);
                list.add(item);
            }

            test(list);
            return list;
        }
        
        return null;
    }
    
    private void test(List<MessageListItem> list) {
        if (MyApp.global().getConfig().isOffline())
        {
            list.addAll(testData());
            Collections.sort(list, new Comparator<MessageListItem>() {

                @Override
                public int compare(MessageListItem lhs, MessageListItem rhs) {
                    return (int) ((rhs.time - lhs.time) >> 32);
                }
            });
        }
    }
    
    private static List<MessageListItem> testData() {
        ArrayList<MessageListItem> list = new ArrayList<MessageListItem>();
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
        // 6
        cal.add(Calendar.DATE, -1);
        list.add(new MessageListItem(cal.getTimeInMillis(), 
                "我的电脑", 
                "[离线]手机轻松传输文件"));
        // 7
        cal.add(Calendar.DATE, -1);
        list.add(new MessageListItem(cal.getTimeInMillis(), 
                "我的电脑", 
                "[离线]手机轻松传输文件"));
        // 8
        cal.add(Calendar.DATE, -1);
        list.add(new MessageListItem(cal.getTimeInMillis(), 
                "我的电脑", 
                "[离线]手机轻松传输文件"));
        // 9
        cal.add(Calendar.DATE, -1);
        list.add(new MessageListItem(cal.getTimeInMillis(), 
                "我的电脑", 
                "[离线]手机轻松传输文件"));
        
        return list;
    }
}