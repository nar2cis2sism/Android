package engine.android.widget.extra;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import engine.android.core.extra.JavaBeanAdapter.ViewHolder;

/**
 * 自定义ExpandableListView，因为其存在些许bug.
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class MyExpandableListView extends ExpandableListView {
    
    /** 折叠其它项，只保留一项展开 **/
    public static final int FLAG_COLLAPSE_OTHER = 1;
    /** 展开项时不滑动 **/
    public static final int FLAG_NO_SCROLL      = 2;
    
    private int mFlags;

    private OnGroupExpandListener mOnGroupExpandListener;
    private OnGroupClickListener mOnGroupClickListener;
    
    public MyExpandableListView(Context context) {
        super(context);
    }

    public MyExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setFlags(int flags) {
        mFlags = flags;
        setOnGroupExpandListener();
        setOnGroupClickListener();
    }
    
    private boolean hasFlag(int flag) {
        return (mFlags & flag) != 0;
    }
    
    @Override
    public void setOnGroupExpandListener(OnGroupExpandListener onGroupExpandListener) {
        mOnGroupExpandListener = onGroupExpandListener;
        setOnGroupExpandListener();
    }
    
    private void setOnGroupExpandListener() {
        super.setOnGroupExpandListener(hasFlag(FLAG_COLLAPSE_OTHER)
                ? listener : mOnGroupExpandListener);
    }
    
    @Override
    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener) {
        mOnGroupClickListener = onGroupClickListener;
        setOnGroupClickListener();
    }
    
    private void setOnGroupClickListener() {
        super.setOnGroupClickListener(hasFlag(FLAG_NO_SCROLL)
                ? listener : mOnGroupClickListener);
    }
    
    private final Listener listener = new Listener();
    
    private class Listener implements OnGroupExpandListener, OnGroupClickListener {
        
        private int expandedGroup = -1;

        @Override
        public void onGroupExpand(int groupPosition) {
            // 只允许展开一项
            if (expandedGroup != groupPosition) collapseGroup(expandedGroup);
            expandedGroup = groupPosition;
            
            if (mOnGroupExpandListener != null)
                mOnGroupExpandListener.onGroupExpand(groupPosition);
        }

        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            if (mOnGroupClickListener != null
            &&  mOnGroupClickListener.onGroupClick(parent, v, groupPosition, id))
                return true;
            
            if (parent.isGroupExpanded(groupPosition))
            {
                parent.collapseGroup(groupPosition);
            }
            else
            {
                parent.expandGroup(groupPosition);
            }

            // 系统默认滑动到展开项，返回true阻止
            return true;
        }
    }
    
    public interface ExpandableGroupItem<Child> {
        
        int getChildrenCount();
        
        Child getChild(int index);
    }
    
    public static abstract class BaseExpandableListAdapter
    <Group extends ExpandableGroupItem<Child>, Child>
    extends android.widget.BaseExpandableListAdapter {
        
        private final Context mContext;

        private final LayoutInflater mInflater;

        private final List<Group> mObjects = new ArrayList<Group>();

        public BaseExpandableListAdapter(Context context) {
            mInflater = LayoutInflater.from(mContext = context);
        }
        
        public Context getContext() {
            return mContext;
        }

        public LayoutInflater getLayoutInflater() {
            return mInflater;
        }

        public void update(Collection<? extends Group> collection) {
            mObjects.clear();
            if (collection != null) mObjects.addAll(collection);
            notifyDataSetChanged();
        }

        public void update(ExpandableListView listView, Collection<? extends Group> collection) {
            // 系统bug:当有多个group展开时更新数据会出现item重复现象，故在此之前先折叠所有项
            int size = mObjects.size();
            for (int i = 0; i < size; i++)
            {
                if (listView.isGroupExpanded(i)) listView.collapseGroup(i);
            }
            
            update(collection);
        }
        
        public List<Group> getItems() {
            return mObjects;
        }

        @Override
        public int getGroupCount() {
            return mObjects.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return getGroup(groupPosition).getChildrenCount();
        }

        @Override
        public Group getGroup(int groupPosition) {
            return mObjects.get(groupPosition);
        }

        @Override
        public Child getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).getChild(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null)
            {
                (convertView = newGroupView(groupPosition, parent))
                .setTag(holder = new ViewHolder(convertView));
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            if (holder != null)
            {
                bindGroupView(groupPosition, isExpanded, holder, getGroup(groupPosition));
            }

            return convertView;
        }
        
        protected abstract View newGroupView(int groupPosition, ViewGroup parent);

        protected abstract void bindGroupView(int groupPosition, boolean isExpanded, 
                ViewHolder holder, Group item);

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}