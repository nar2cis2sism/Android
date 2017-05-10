package demo.activity.example;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorTreeAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import demo.android.R;
import demo.provider.MyContentProvider.Contacts;
import demo.provider.MyContentProvider.Groups;
import demo.provider.MyContentProvider.Intents.Edit;
import demo.provider.MyContentProvider.Intents.Insert;
import engine.android.core.ApplicationManager;

public class ContactsActivity extends ExpandableListActivity {
	
	//长按分组上的 菜单
	public static final int MENU_GROUP_ADD 			= Menu.FIRST;
	public static final int MENU_GROUP_DELETE 		= Menu.FIRST + 1;
	public static final int MENU_GROUP_MODIFY 		= Menu.FIRST + 2;
	public static final int MENU_GROUP_ADDCONTACT 	= Menu.FIRST + 3;
	
	//长按联系人菜单
	public static final int MENU_CONTACT_DELETE		= Menu.FIRST;
	public static final int MENU_CONTACT_MODIFY		= Menu.FIRST + 1;
	public static final int MENU_CONTACT_MOVE		= Menu.FIRST + 2;
	
	PopupWindow popup;							
	Button btn_sms;			
	Button btn_email;			
	Button btn_call;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_activity);
		
		getExpandableListView().setBackgroundResource(R.drawable.contacts_bg);
		
		//拖动时避免出现黑色
		getExpandableListView().setCacheColorHint(0);
		//去掉每项下面的黑线(分割线)
		getExpandableListView().setDivider(null);
        //自定义下拉图标
        getExpandableListView().setGroupIndicator(getResources().getDrawable(R.drawable.contacts_group_icon));
        
		registerForContextMenu(getExpandableListView());
        
		initAdapter();
        
        initPopupWindow();
	}
	
	private void initAdapter()
	{
        setListAdapter(new ContactsAdapter(getAllGroups(), this, true));
	}
	
	private void initPopupWindow()
	{
		View view = getLayoutInflater().inflate(R.layout.contacts_popup_window, null);
		popup = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		btn_sms = (Button) view.findViewById(R.id.btn_sms);
		btn_email = (Button) view.findViewById(R.id.btn_email);
		btn_call = (Button) view.findViewById(R.id.btn_call);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
		{
			//在组上长按
			String title = ((TextView) info.targetView.findViewById(R.id.name)).getText().toString();
			menu.setHeaderTitle(title);
			menu.add(0, MENU_GROUP_ADD, 0, "添加分组");
    		menu.add(0, MENU_GROUP_DELETE, 0, "删除分组");
    		menu.add(0, MENU_GROUP_MODIFY, 0, "重命名");
    		menu.add(0, MENU_GROUP_ADDCONTACT, 0, "添加联系人");
		}
		else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			//在联系人上长按
			String title = ((TextView) info.targetView.findViewById(R.id.name)).getText().toString();
			Drawable icon = ((ImageView) info.targetView.findViewById(R.id.headIcon)).getDrawable();
			menu.setHeaderTitle(title);
			menu.setHeaderIcon(icon);
    		menu.add(0, MENU_CONTACT_DELETE, 0, "删除联系人");
    		menu.add(0, MENU_CONTACT_MODIFY, 0, "编辑联系人");
    		menu.add(0, MENU_CONTACT_MOVE, 0, "移动联系人到...");
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
		{
			final String name = ((TextView) info.targetView.findViewById(R.id.name)).getText().toString();
			switch (item.getItemId()) {
			case MENU_GROUP_ADD:
				//添加分组
				final EditText view = new EditText(this);
				Dialog dialog = new AlertDialog.Builder(this)
		        .setTitle("添加组")
		        .setView(view)
		        .setPositiveButton("确定",
		        new DialogInterface.OnClickListener()
		        {
		            public void onClick(DialogInterface dialog, int whichButton)
		            {
		            	String groupName = view.getText().toString().trim();
		            	if (!TextUtils.isEmpty(groupName))
		            	{
		            		//添加新的组到数据库
		            		Cursor c = getAllGroups();
		            		if (c != null && c.moveToFirst())
		            		{
		            			while (c.moveToNext())
		            			{
		            				if (groupName.equals(getGroupName(c)))
		            				{
		            					ApplicationManager.showMessage(groupName + "已存在！");
		            					c.close();
		            					return;
		            				}
		            			}
			            		
		            			c.close();
			            		addGroup(groupName);
		            		}
		            	}
		            }
		        })
		        .setNegativeButton("取消", null).create();
				dialog.show();
				
				break;
			case MENU_GROUP_DELETE:
				//删除分组
				dialog = new AlertDialog.Builder(this)
		        .setTitle("确定要删除该组和该组内的所有联系人吗?")
		        .setPositiveButton("确定",
		        new DialogInterface.OnClickListener()
		        {
		            public void onClick(DialogInterface dialog, int whichButton)
		            {
		            	deleteGroup(name);
		            	deleteContactsByGroupName(name);
		            }
		        })
		        .setNegativeButton("取消", null).create();
				dialog.show();
				
				break;
			case MENU_GROUP_MODIFY:
				//重命名
				final EditText view1 = new EditText(this);
				view1.setText(name);
				dialog = new AlertDialog.Builder(this)
		        .setTitle("请输入新的组名")
		        .setView(view1)
		        .setPositiveButton("确定",
		        new DialogInterface.OnClickListener()
		        {
		            public void onClick(DialogInterface dialog, int whichButton)
		            {
		            	String groupName = view1.getText().toString().trim();
		            	if (!TextUtils.isEmpty(groupName) && !groupName.equals(name))
		            	{
		            		//添加新的组到数据库
		            		Cursor c = getAllGroups();
		            		if (c != null && c.moveToFirst())
		            		{
		            			while (c.moveToNext())
		            			{
		            				if (groupName.equals(getGroupName(c)))
		            				{
		            					ApplicationManager.showMessage(groupName + "已存在！");
		            					c.close();
		            					return;
		            				}
		            			}
			            		
		            			c.close();
		            			updateGroup(name, groupName);
		            			updateContactGroupName(name, groupName);
		            		}
		            	}
		            }
		        })
		        .setNegativeButton("取消", null).create();
				dialog.show();
				
				break;
			case MENU_GROUP_ADDCONTACT:
				//添加联系人
				Intent intent = new Intent(Insert.ACTION);
				intent.setType(Insert.Type);
				startActivity(intent);
				break;

			default:
				break;
			}
		}
		else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			final String name = ((TextView) info.targetView.findViewById(R.id.name)).getText().toString();
			switch (item.getItemId()) {
			case MENU_CONTACT_DELETE:
				//删除联系人
				Dialog dialog = new AlertDialog.Builder(this)
		        .setTitle("确定要删除联系人吗？")
		        .setPositiveButton("确定",
		        new DialogInterface.OnClickListener()
		        {
		            public void onClick(DialogInterface dialog, int whichButton)
		            {
		            	deleteContactByName(name);
		            }
		        })
		        .setNegativeButton("取消", null).create();
				dialog.show();
				
				break;
			case MENU_CONTACT_MODIFY:
				//编辑联系人
				Intent intent = new Intent(Edit.ACTION);
				intent.setType(Edit.Type);
				intent.putExtra("editContactName", name);
				startActivity(intent);
				break;
			case MENU_CONTACT_MOVE:
				//移动联系人到...
				final Cursor c = getGroupsExcept(checkContactGroup(name));
				dialog = new AlertDialog.Builder(this)
		        .setTitle("移动联系人到...")
		        .setSingleChoiceItems(c, -1, Groups.GROUP_NAME, 
		        		new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (c != null)
						{
							if (c.moveToPosition(which))
							{
								updateContact(name, c.getString(c.getColumnIndexOrThrow(Groups.GROUP_NAME)));
							}
							
							c.close();
						}
						
						dialog.dismiss();
					}
				}).create();
				dialog.show();
				
				break;

			default:
				break;
			}
		}
		else
		{
			return super.onContextItemSelected(item);
		}
		
		return true;
	}
	
	class ContactsAdapter extends CursorTreeAdapter {

		LayoutInflater inflater;
		
		public ContactsAdapter(Cursor cursor, Context context,
				boolean autoRequery) {
			super(cursor, context, autoRequery);
			inflater = LayoutInflater.from(context);
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			return getContactsByGroupName(getGroupName(groupCursor));
		}

		@Override
		protected View newGroupView(Context context, Cursor cursor,
				boolean isExpanded, ViewGroup parent) {
			return inflater.inflate(R.layout.contacts_group_layout, null);
		}

		@Override
		protected void bindGroupView(View view, Context context, Cursor cursor,
				boolean isExpanded) {
			TextView name = (TextView) view.findViewById(R.id.name);
			String groupName = getGroupName(cursor);
			name.setText(groupName);

			TextView count = (TextView) view.findViewById(R.id.count);
			int groupCount = getContactsCountByGroupName(groupName);
			count.setText("[" + groupCount + "]");
		}

		@Override
		protected View newChildView(Context context, Cursor cursor,
				boolean isLastChild, ViewGroup parent) {
			return inflater.inflate(R.layout.contacts_contact_layout, null);
		}

		@Override
		protected void bindChildView(View view, Context context, Cursor cursor,
				boolean isLastChild) {
			ImageView headIcon = (ImageView) view.findViewById(R.id.headIcon);
			headIcon.setImageBitmap(getContactHeadIcon(
					cursor.getBlob(cursor.getColumnIndexOrThrow(Contacts.HEAD_ICON))));

			TextView name = (TextView) view.findViewById(R.id.name);
			name.setText(cursor.getString(cursor.getColumnIndexOrThrow(Contacts.NAME)));

			TextView description = (TextView) view.findViewById(R.id.description);
			description.setText(cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DESCRIPTION)));
			description.setSelected(true);
			
			final String phone = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.PHONE));
			final String email = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.EMAIL));

			ImageView edit = (ImageView) view.findViewById(R.id.edit);
			edit.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (popup.isShowing())
					{
						popup.dismiss();
					}
					else
					{
						popup.showAsDropDown(v);
						
						btn_sms.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								popup.dismiss();
								Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
						    	intent.putExtra("sms_body", "呵呵！好久不见");
						    	startActivity(intent);
							}
						});
						btn_email.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								popup.dismiss();
								startActivity(new Intent(Intent.ACTION_SENDTO, 
						    			Uri.parse("mailto:" + email)));
							}
						});
						btn_call.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								popup.dismiss();
								startActivity(new Intent(Intent.ACTION_DIAL, 
						    			Uri.parse("tel:" + phone)));
							}
						});
					}
				}
			});
		}
	}
	
	static String getGroupName(Cursor groupCursor)
	{
		return groupCursor.getString(groupCursor.getColumnIndexOrThrow(Groups.GROUP_NAME));
	}
	
	final String[] groupProjection = {
			Groups._ID,
			Groups.GROUP_NAME
			};
	
	/**
	 * 查找所有组
	 */
	
	private Cursor getAllGroups()
	{
		return getContentResolver().query(Groups.CONTENT_URI, groupProjection, null, null, Groups.GROUP_NAME);
	}
	
	final String[] contactProjection = {
			Contacts._ID,
			Contacts.HEAD_ICON, 
			Contacts.NAME, 
			Contacts.DESCRIPTION, 
			Contacts.PHONE, 
			Contacts.EMAIL
			};
	
	/**
	 * 查找给定组的所有联系人
	 */
	
	private Cursor getContactsByGroupName(String groupName)
	{
		return getContentResolver().query(
				Contacts.CONTENT_URI, 
				contactProjection, 
				Contacts.GROUP_NAME + "=?", 
				new String[]{groupName}, 
				Contacts.NAME);
	}
	
	/**
	 * 统计给定组的人数
	 * @param groupName 组名
	 */
	
	private int getContactsCountByGroupName(String groupName)
	{
		int count = 0;
		Cursor c = getContentResolver().query(
				Contacts.CONTENT_URI, 
				new String[]{Contacts._ID}, 
				Groups.GROUP_NAME + "=?", 
				new String[]{groupName}, 
				null);
		if (c != null)
		{
			count = c.getCount();
			c.close();
		}
		
		return count;
	}
	
	/**
	 * 获取联系人头像
	 */
	
	private Bitmap getContactHeadIcon(byte[] bs)
	{
		if (bs != null)
		{
			return BitmapFactory.decodeByteArray(bs, 0, bs.length);
		}
		else
		{
			return BitmapFactory.decodeResource(getResources(), R.drawable.login_head_default);
		}
	}
	
	/**
	 * 删除一个联系人
	 * @param name 联系人名称
	 */
	
	private void deleteContactByName(String name)
	{
		getContentResolver().delete(Contacts.CONTENT_URI, Contacts.NAME + "=?", new String[]{name});
	}
	
	/**
	 * 查找所有组
	 * @param groupName 不包含此组名
	 * @return
	 */
	
	private Cursor getGroupsExcept(String groupName)
	{
		return getContentResolver().query(
				Groups.CONTENT_URI, 
				groupProjection, 
				Groups.GROUP_NAME + "!=?", 
				new String[]{groupName}, 
				null);
	}
	
	/**
	 * 查询联系人在哪个组
	 * @param name 联系人名称
	 * @return 组名
	 */
	
	private String checkContactGroup(String name)
	{
		String groupName = null;
		Cursor c = getContentResolver().query(
				Contacts.CONTENT_URI, 
				new String[]{Contacts.GROUP_NAME}, 
				Contacts.NAME + "=?", 
				new String[]{name}, 
				null);
		if (c != null)
		{
			if (c.moveToFirst())
			{
				groupName = c.getString(c.getColumnIndexOrThrow(Contacts.GROUP_NAME));
			}
			
			c.close();
		}
		
		return groupName;
	}
	
	/**
	 * 更新联系人所在组
	 * @param name 联系人名称
	 * @param groupName 新组名
	 */
	
	private void updateContact(String name, String groupName)
	{
		ContentValues values = new ContentValues();
		values.put(Contacts.GROUP_NAME, groupName);
		getContentResolver().update(
				Contacts.CONTENT_URI, 
				values, 
				Contacts.NAME + "=?", 
				new String[]{name});
	}
	
	/**
	 * 新建一个组
	 * @param groupName 组名
	 */
	
	private void addGroup(String groupName)
	{
		ContentValues values = new ContentValues();
		values.put(Groups.GROUP_NAME, groupName);
		getContentResolver().insert(Groups.CONTENT_URI, values);
	}
	
	/**
	 * 删除一个组
	 * @param groupName 组名
	 */
	
	private void deleteGroup(String groupName)
	{
		getContentResolver().delete(Groups.CONTENT_URI, Groups.GROUP_NAME + "=?", new String[]{groupName});
	}
	
	/**
	 * 删除一个组内所有联系人
	 * @param groupName 组名
	 */
	
	private void deleteContactsByGroupName(String groupName)
	{
		getContentResolver().delete(Contacts.CONTENT_URI, Contacts.GROUP_NAME + "=?", new String[]{groupName});
	}
	
	/**
	 * 更新组名
	 */
	
	private void updateGroup(String oldGroupName, String newGroupName)
	{
		ContentValues values = new ContentValues();
		values.put(Groups.GROUP_NAME, newGroupName);
		getContentResolver().update(
				Groups.CONTENT_URI, 
				values, 
				Groups.GROUP_NAME + "=?", 
				new String[]{oldGroupName});
	}
	
	/**
	 * 更新联系人所在组名
	 */
	
	private void updateContactGroupName(String oldGroupName, String newGroupName)
	{
		ContentValues values = new ContentValues();
		values.put(Contacts.GROUP_NAME, newGroupName);
		getContentResolver().update(
				Contacts.CONTENT_URI, 
				values, 
				Contacts.GROUP_NAME + "=?", 
				new String[]{oldGroupName});
	}
}