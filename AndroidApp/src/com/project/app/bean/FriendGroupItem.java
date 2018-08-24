package com.project.app.bean;

import engine.android.widget.extra.MyExpandableListView.ExpandableGroupItem;

import java.util.ArrayList;
import java.util.List;

public class FriendGroupItem implements ExpandableGroupItem<FriendListItem> {
    
    public final String name;                   // 分组名称
    
    private List<FriendListItem> children;
    
    public FriendGroupItem(String name) {
        this.name = name;
    }
    
    public void addChild(FriendListItem item) {
        if (children == null) children = new ArrayList<FriendListItem>();
        children.add(item);
    }

    @Override
    public int getChildrenCount() {
        return children != null ? children.size() : 0;
    }

    @Override
    public FriendListItem getChild(int index) {
        return children.get(index);
    }
}