package demo.provider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;

import demo.android.R;
import demo.provider.MyContentProvider.Contacts;
import demo.provider.MyContentProvider.Groups;
import engine.android.core.ApplicationManager;
import engine.android.core.util.CalendarFormat;
import engine.android.util.image.ImageUtil;
import engine.android.util.manager.SDCardManager;
import engine.android.util.ui.MyValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditContactActivity extends Activity {

	//用来标识请求照相功能的activity
	private static final int CAMERA_WITH_DATA = 3023;

	//用来标识请求gallery的activity
	private static final int PHOTO_PICKED_WITH_DATA = 3021;
	
	//各个组件
	private EditText name;							//姓名
	private PhotoEditor headIcon;					//头像
	private EditText phone;							//号码
	private Spinner group;							//组
	private Button birthday;						//生日
	private EditText address;						//住址
	private EditText email;							//邮箱
	private EditText description;					//好友描述 
	
	private Button ok;								//确定
	private Button cancel;							//取消

	private File photoFile;							//照相机拍照得到的图片
	
	private int year, monthOfYear, dayOfMonth;
	
	//activity处于两种状态，插入状态或者编辑状态
	private static final int STATE_INSERT 	= 0;	//插入状态
	private static final int STATE_EDIT		= 1;	//编辑状态
	private String editContactName;					//编辑联系人名称
	
	private int state;								//记录当前的状态
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editcontact);
		
		Intent intent = getIntent();
		String action = intent.getAction();
		if (Intent.ACTION_INSERT.equals(action))
		{
			state = STATE_INSERT;
		}
		else if (Intent.ACTION_EDIT.equals(action))
		{
			state = STATE_EDIT;
			editContactName = intent.getStringExtra("editContactName");
		}
		else
		{
			finish();
			return;
		}
		
		name = (EditText) findViewById(R.id.name);
		headIcon = (PhotoEditor) findViewById(R.id.headIcon);
		phone = (EditText) findViewById(R.id.phone);
		group = (Spinner) findViewById(R.id.group);
		birthday = (Button) findViewById(R.id.birthday);
		address = (EditText) findViewById(R.id.address);
		email = (EditText) findViewById(R.id.email);
		description = (EditText) findViewById(R.id.description);
		
		ok = (Button) findViewById(R.id.ok);
		cancel = (Button) findViewById(R.id.cancel);
		
		initListener();
		initData();
	}
	
	private void initListener()
	{
		//图像上的监听
		headIcon.setOnClickListener(photoEditorListener);
		//生日按钮监听
		birthday.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new DatePickerDialog(EditContactActivity.this, new OnDateSetListener(){

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						setBirthday(year, monthOfYear, dayOfMonth);
					}}, year, monthOfYear - 1, dayOfMonth).show();
			}
		});
		
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveContact();
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void initData()
	{
		group.setPrompt("加入分组");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, getAllGroups());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		group.setAdapter(adapter);
		
		if (state == STATE_INSERT)
		{
			Calendar date = Calendar.getInstance();
			setBirthday(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
		}
		else
		{
			MyContact contact = getContact(editContactName);
			if (contact != null)
			{
				name.setText(contact.name);
				byte[] bs = contact.headIcon;
				if (bs != null)
				{
					headIcon.setPhoto(BitmapFactory.decodeByteArray(bs, 0, bs.length));
				}
				
				phone.setText(contact.phone);
				group.setSelection(adapter.getPosition(contact.group));
				setBirthday(contact.birthday);
				address.setText(contact.address);
				email.setText(contact.email);
				description.setText(contact.description);
			}
		}
	}
	
	private void setBirthday(int year, int monthOfYear, int dayOfMonth)
	{
		birthday.setText(new StringBuilder()
			.append(this.year = year)
			.append("-")
			.append(this.monthOfYear = monthOfYear + 1)
			.append("-")
			.append(this.dayOfMonth = dayOfMonth));
	}
	
	private void setBirthday(String birthday)
	{
		String[] strs = birthday.split("-");
		this.birthday.setText(new StringBuilder()
		.append(year = Integer.parseInt(strs[0]))
		.append("-")
		.append(monthOfYear = Integer.parseInt(strs[1]))
		.append("-")
		.append(dayOfMonth = Integer.parseInt(strs[2])));
	}
	
	/**
	 * 得到所有的组
	 */
	
	private List<String> getAllGroups()
	{
		List<String> list = new ArrayList<String>();
		Cursor c = getContentResolver().query(
				Groups.CONTENT_URI, new String[]{Groups.GROUP_NAME}, null, null, Groups.GROUP_NAME);
		if (c != null)
		{
			while (c.moveToNext())
			{
				list.add(c.getString(c.getColumnIndexOrThrow(Groups.GROUP_NAME)));
			}
			
			c.close();
		}
		
		return list;
	}
	
	/**
	 * 保存联系人
	 */
	
	private void saveContact()
	{
		String contact_name = name.getText().toString().trim();
		byte[] contact_headIcon = ImageUtil.image2bytes(headIcon.getPhoto());
		String contact_phone = phone.getText().toString().trim();
		String contact_group = group.getSelectedItem().toString();
		String contact_birthday = birthday.getText().toString();
		String contact_address = address.getText().toString().trim();
		String contact_email = email.getText().toString().trim();
		String contact_description = description.getText().toString().trim();
		
		//检查数据的有效性
		if (TextUtils.isEmpty(contact_name))
		{
			ApplicationManager.showMessage("名字不能为空");
		}
		else if (TextUtils.isEmpty(contact_phone))
		{
			ApplicationManager.showMessage("号码不能为空");
		}
		else if (TextUtils.isEmpty(contact_email))
		{
			ApplicationManager.showMessage("邮箱不能为空");
		}
		else if (!MyValidator.validate(contact_email, MyValidator.EMAIL_ADDRESS))
		{
			ApplicationManager.showMessage("Email格式有误");
		}
		else
		{
			MyContact contact = new MyContact();
			contact.name = contact_name;
			contact.headIcon = contact_headIcon;
			contact.phone = contact_phone;
			contact.group = contact_group;
			contact.birthday = contact_birthday;
			contact.address = contact_address;
			contact.email = contact_email;
			contact.description = contact_description;
			
			if (state == STATE_INSERT)
			{
				addContact(contact);
				ApplicationManager.showMessage("新建联系人成功");
			}
			else
			{
				updateContact(editContactName, contact);
				ApplicationManager.showMessage("联系人更新成功");
			}

			finish();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK)
		{
			switch (requestCode) {
			case CAMERA_WITH_DATA:
				//照相机程序返回的,再次调用图片剪辑程序去修剪图片
				try {
					//首先将相机拍下的照片放到媒体库里面
					String url = Media.insertImage(getContentResolver(), 
							photoFile.getAbsolutePath(), photoFile.getName(), null);
					
					Intent intent = new Intent("com.android.camera.action.CROP");
			    	intent.setData(Uri.parse(url));
			    	
			    	intent.putExtra("crop", "true");
			    	intent.putExtra("aspectX", 1);
			    	intent.putExtra("aspectY", 1);
			    	intent.putExtra("outputX", 80);
			    	intent.putExtra("outputY", 80);
			    	intent.putExtra("scale", true);
			    	intent.putExtra("noFaceDetection", true);
			    	intent.putExtra("return-data", true);
			    	
			    	startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
				} catch (Exception e1) {
					ApplicationManager.showMessage("剪辑照片出错");
				}
				
				break;
			case PHOTO_PICKED_WITH_DATA:
				//调用Gallery返回的
				try {
					Bitmap image = data.getParcelableExtra("data");
					headIcon.setPhoto(image);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				break;
			}
		}
	}
	
	private OnClickListener photoEditorListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (headIcon.isHavingPhoto())
			{
				//当前已经有了照片
				//Wrap our context to inflate list items using correct theme
				Context context = new ContextThemeWrapper(EditContactActivity.this, android.R.style.Theme_Light);
				String[] strs = {"使用此头像", "删除头像", "更改头像"};
				ListAdapter adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, strs);
				Dialog dialog = new AlertDialog.Builder(context)
		        .setTitle("好友头像")
		        .setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
						case 0:
							//什么也不做
							break;
						case 1:
							//删除图像
							headIcon.setPhoto(null);
							break;
						case 2:
							//替换图像
							pickPhoto();
							break;
						}
					}
				})
		        .setPositiveButton("返回", null).create();
				dialog.show();
			}
			else
			{
				pickPhoto();
			}
		}
		
		private void pickPhoto()
		{
			//Wrap our context to inflate list items using correct theme
			Context context = new ContextThemeWrapper(EditContactActivity.this, android.R.style.Theme_Light);
			String[] strs = {"拍照", "从图库中选择一张"};
			ListAdapter adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, strs);
			Dialog dialog = new AlertDialog.Builder(context)
	        .setTitle("好友头像")
	        .setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					switch (which) {
					case 0:
						if (SDCardManager.isEnabled())
						{
							takePhoto();
						}
						else
						{
							ApplicationManager.showMessage("SD卡无效");
						}
						
						break;
					case 1:
						//从相册中去获取
						try {
							//Launch picker to choose photo for selected contact
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.setType("image/*");
							
							intent.putExtra("crop", "true");
					    	intent.putExtra("aspectX", 1);
					    	intent.putExtra("aspectY", 1);
					    	intent.putExtra("outputX", 80);
					    	intent.putExtra("outputY", 80);
					    	intent.putExtra("scale", true);
					    	intent.putExtra("noFaceDetection", true);
					    	intent.putExtra("return-data", true);
					    	
							startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
						} catch (Exception e) {
							ApplicationManager.showMessage("手机里面没有照片");
						}
						
						break;
					}
				}
			})
	        .setPositiveButton("返回", null).create();
			dialog.show();
		}
		
		/**
		 * 拍照获取图片
		 */
		
		private void takePhoto()
		{
			try {
				//Launch camera to take photo for selected contact
				File dir = SDCardManager.openSDCardFile("DCIM/Camera");
				if (!dir.exists())
				{
					dir.mkdirs();
				}
				
				photoFile = new File(dir, getPhotoName());
				if (!photoFile.exists())
				{
					photoFile.createNewFile();
				}
				
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				startActivityForResult(intent, CAMERA_WITH_DATA);
			} catch (Exception e) {
				ApplicationManager.showMessage("相机不可用");
			}
		}
		
		/**
		 * 用当前时间给取得的图片命名
		 */
		
		private String getPhotoName()
		{
			return CalendarFormat.format(Calendar.getInstance(), "yyyy-MM-dd HH.mm.ss") + ".jpg";
		}
	};
	
	/**
	 * 添加一个联系人
	 * @param contact
	 */
	
	private void addContact(MyContact contact)
	{
		ContentValues values = new ContentValues();
		values.put(Contacts.NAME, contact.name);
		values.put(Contacts.HEAD_ICON, contact.headIcon);
		values.put(Contacts.PHONE, contact.phone);
		values.put(Contacts.BIRTHDAY, contact.birthday);
		values.put(Contacts.ADDRESS, contact.address);
		values.put(Contacts.EMAIL, contact.email);
		values.put(Contacts.DESCRIPTION, contact.description);
		values.put(Contacts.GROUP_NAME, contact.group);
		getContentResolver().insert(Contacts.CONTENT_URI, values);
	}
	
	/**
	 * 更新联系人
	 * @param name
	 * @param contact
	 */
	
	private void updateContact(String name, MyContact contact)
	{
		ContentValues values = new ContentValues();
		values.put(Contacts.NAME, contact.name);
		values.put(Contacts.HEAD_ICON, contact.headIcon);
		values.put(Contacts.PHONE, contact.phone);
		values.put(Contacts.BIRTHDAY, contact.birthday);
		values.put(Contacts.ADDRESS, contact.address);
		values.put(Contacts.EMAIL, contact.email);
		values.put(Contacts.DESCRIPTION, contact.description);
		values.put(Contacts.GROUP_NAME, contact.group);
		getContentResolver().update(Contacts.CONTENT_URI, values, Contacts.NAME + "=?", new String[]{name});
	}
	
	private MyContact getContact(String name)
	{
		MyContact contact = null;
		Cursor c = getContentResolver().query(
				Contacts.CONTENT_URI, 
				null, 
				Contacts.NAME + "=?", 
				new String[]{name}, 
				null);
		if (c != null)
		{
			if (c.moveToFirst())
			{
				contact = new MyContact();
				contact.name = c.getString(c.getColumnIndexOrThrow(Contacts.NAME));
				contact.headIcon = c.getBlob(c.getColumnIndexOrThrow(Contacts.HEAD_ICON));
				contact.phone = c.getString(c.getColumnIndexOrThrow(Contacts.PHONE));
				contact.group = c.getString(c.getColumnIndexOrThrow(Contacts.GROUP_NAME));
				contact.birthday = c.getString(c.getColumnIndexOrThrow(Contacts.BIRTHDAY));
				contact.address = c.getString(c.getColumnIndexOrThrow(Contacts.ADDRESS));
				contact.email = c.getString(c.getColumnIndexOrThrow(Contacts.EMAIL));
				contact.description = c.getString(c.getColumnIndexOrThrow(Contacts.DESCRIPTION));
			}
			
			c.close();
		}
		
		return contact;
	}
	
	private static class MyContact {
		
		public String name;								//姓名
		public byte[] headIcon;							//头像
		public String phone;							//号码
		public String group;							//组
		public String birthday;							//生日
		public String address;							//住址
		public String email;							//邮箱
		public String description;						//好友描述 
	}
}