package demo.activity.example;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;

import demo.activity.example.bean.Child;
import demo.activity.example.bean.Group;
import demo.android.R;
import engine.android.core.Forelet;

import java.util.ArrayList;
import java.util.List;

public class MyExpandableListActivity extends Forelet {
	
	ExpandableListView lv;
	
	List<Group> list;
	
	Toast toast;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_expandable_activity);
		
		lv = (ExpandableListView) findViewById(R.id.lv);
		
		list = new ArrayList<Group>();
		
		initData();
		
		MyExpandableListAdapter adapter = new MyExpandableListAdapter();

		lv.setAdapter(adapter);
		
		//展开所有组
		for (int i = 0, size = adapter.getGroupCount(); i < size; i++)
		{
			lv.expandGroup(i);
		}
		
		lv.setOnChildClickListener(new OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if (toast != null)
				{
					toast.cancel();
					toast.setText(groupPosition + ":" + childPosition);
				}
				else
				{
					toast = Toast.makeText(MyExpandableListActivity.this, groupPosition + ":" + childPosition, Toast.LENGTH_SHORT);
				}

				toast.show();
				return true;
			}});
	}
	
	private void initData()
	{
		//第一组数据
		Group group = new Group();
		group.name = "金融帐户";
		
		Child child = new Child();
		child.name = "银行卡(CNY)";
		child.text = "银行卡";
		child.money = "￥0.00";
		group.add(child);
		
		list.add(group);

		//第二组数据
		group = new Group();
		group.name = "虚拟帐户";
		
		child = new Child();
		child.name = "饭卡(CNY)";
		child.text = "储值卡";
		child.money = "￥0.00";
		group.add(child);
		
		child = new Child();
		child.name = "财付通(CNY)";
		child.text = "在线支付";
		child.money = "￥0.00";
		group.add(child);
		
		child = new Child();
		child.name = "公交卡(CNY)";
		child.text = "储值卡";
		child.money = "￥0.00";
		group.add(child);
		
		child = new Child();
		child.name = "支付宝(CNY)";
		child.text = "在线支付";
		child.money = "￥0.00";
		group.add(child);
		
		list.add(group);

		//第三组数据
		group = new Group();
		group.name = "现金帐户";
		
		child = new Child();
		child.name = "现金(CNY)";
		child.text = "现金口袋";
		child.money = "￥0.00";
		group.add(child);
		
		child = new Child();
		child.name = "其他(CNY)";
		child.text = "现金口袋";
		child.money = "￥0.00";
		group.add(child);
		
		list.add(group);
	}
	
	class MyExpandableListAdapter extends BaseExpandableListAdapter {

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return list.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(R.layout.my_expandable_child_item, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.money = (TextView) convertView.findViewById(R.id.money);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			if (holder != null)
			{
				Child child = list.get(groupPosition).get(childPosition);
				holder.name.setText(child.name);
				holder.text.setText(child.text);
				holder.money.setText(child.money);
			}
			
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return list.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return list.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return list.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView name;
			if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(R.layout.my_expandable_group_item, null);
				name = (TextView) convertView.findViewById(R.id.name);
				convertView.setTag(name);
			}
			else
			{
				name = (TextView) convertView.getTag();
			}
			
			name.setText(list.get(groupPosition).name);
			
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
	
	static class ViewHolder {
		
		TextView name;
		TextView text;
		TextView money;
		
	}
}