//package demo.activity.example;
//
//import android.graphics.drawable.ColorDrawable;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnTouchListener;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.LinearLayout.LayoutParams;
//import android.widget.ListView;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//
//import demo.activity.example.bean.LoginBean;
//import demo.android.R;
//import engine.android.core.Forelet;
//import engine.android.dao.DAOTemplate;
//import engine.android.dao.DAOTemplate.DAOQueryBuilder;
//import engine.android.util.Util;
//
//import org.apache.commons.lang.ArrayUtils;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 登录界面
// * @author yanhao
// * @version 1.0
// */
//
//public class LoginActivity extends Forelet implements OnClickListener {
//	
//	DAOTemplate dao;
//	
//	EditText account_input;						//帐号输入框
//	ImageButton dropdown_button;				//下拉列表按钮
//	EditText password_input;					//密码输入框
//
//	CheckBox rem_pwd;							//记住密码复选框
//	Button login_button;						//登录按钮
//
//	CheckBox yinshen;							//隐身登录复选框
//	CheckBox zhendong;							//开启振动复选框
//	CheckBox qunxiaoxi;							//接收群消息复选框
//	CheckBox jingyin;							//静音登录复选框
//	
//	PopupWindow popup;							//账户列表弹出框
//	PopupAdapter adapter;						//账户列表适配器
//	Map<String, LoginBean> account_map;			//账户查询表<账号, 账户属性>
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.example_login_activity);
//        
//        //帐号输入框
//        account_input = (EditText) findViewById(R.id.account_input);
//        
//        //下拉列表按钮
//        dropdown_button = (ImageButton) findViewById(R.id.dropdown_button);
//        dropdown_button.setOnClickListener(this);
//        
//        //密码输入框
//        password_input = (EditText) findViewById(R.id.password_input);
//        
//        //记住密码复选框
//        rem_pwd = (CheckBox) findViewById(R.id.rem_pwd);
//        
//        //登录按钮
//        login_button = (Button) findViewById(R.id.login_button);
//        login_button.setOnClickListener(this);
//		
//		//隐身登录复选框
//		yinshen = (CheckBox) findViewById(R.id.yinshen);
//		
//		//开启振动复选框
//		zhendong = (CheckBox) findViewById(R.id.zhendong);
//		
//		//接收群消息复选框
//		qunxiaoxi = (CheckBox) findViewById(R.id.qunxiaoxi);
//		
//		//静音登录复选框
//		jingyin = (CheckBox) findViewById(R.id.jingyin);
//		
//		dao = new DAOTemplate(this, "login", 1, null);
//		dao.createTable(LoginBean.class, false);
//		
//		account_map = new HashMap<String, LoginBean>();
//		LoginBean[] loginArray = dao.find(DAOQueryBuilder.create(LoginBean.class), LoginBean[].class);
//		if (!ArrayUtils.isEmpty(loginArray))
//		{
//		    LoginBean bean = null;
//		    for (LoginBean obj : loginArray)
//		    {
//		        account_map.put(obj.account, bean = obj);
//		    }
//			
//			if (bean != null)
//			{
//				set(bean);
//			}
//		}
//	}
//	
//	@Override
//	protected void onDestroy() {
//		if (popup != null)
//		{
//			popup.dismiss();
//		}
//		
//		if (dao != null)
//		{
//			dao.close();
//		}
//		
//        super.onDestroy();
//	}
//	
//	/**
//	 * 设置账户属性
//	 * @param bean
//	 */
//	
//	private void set(LoginBean bean)
//	{
//		account_input.setText(bean.account);
//		password_input.setText(bean.password);
//		rem_pwd.setChecked(Util.getBoolean(bean.rem_pwd));
//		yinshen.setChecked(Util.getBoolean(bean.yinshen));
//		zhendong.setChecked(Util.getBoolean(bean.zhendong));
//		qunxiaoxi.setChecked(Util.getBoolean(bean.qunxiaoxi));
//		jingyin.setChecked(Util.getBoolean(bean.jingyin));
//	}
//
//	@Override
//	public void onClick(View v) {
//		switch (v.getId()) {
//		case R.id.dropdown_button:
//			//下拉列表按钮
//			if (popup == null)
//			{
//				ListView lv = new ListView(this);
//				lv.setAdapter(adapter = new PopupAdapter());
//				popup = new PopupWindow(lv, account_input.getWidth(), LayoutParams.WRAP_CONTENT);
//				// Daimon:点击 PopupWindow之外的区域后， PopupWindow会消失
//				popup.setBackgroundDrawable(new ColorDrawable());
//				popup.setFocusable(true);
//				popup.setOutsideTouchable(true);
//			}
//			
//			if (popup.isShowing())
//			{
//				popup.dismiss();
//			}
//			else
//			{
//				popup.showAsDropDown(account_input);
//			}
//			
//			break;
//		case R.id.login_button:
//			//登录按钮
//			if (TextUtils.isEmpty(account_input.getText()))
//			{
//				//帐号不能为空
//				openMessageDialog("提示", "请您输入帐号后再登录", "确定");
//				return;
//			}
//
//			if (TextUtils.isEmpty(password_input.getText()))
//			{
//				//密码不能为空
//				openMessageDialog("提示", "请您输入密码后再登录", "确定");
//				return;
//			}
//			
//			LoginBean bean = new LoginBean();
//			bean.account = account_input.getText().toString();
//			if (rem_pwd.isChecked())
//			{
//				bean.password = password_input.getText().toString();
//			}
//			
//			bean.rem_pwd = rem_pwd.isChecked() ? "true" : "false";
//			bean.yinshen = yinshen.isChecked() ? "true" : "false";
//			bean.zhendong = zhendong.isChecked() ? "true" : "false";
//			bean.qunxiaoxi = qunxiaoxi.isChecked() ? "true" : "false";
//			bean.jingyin = jingyin.isChecked() ? "true" : "false";
//			
//			login(bean);
//			
//			break;
//
//		default:
//			break;
//		}
//	}
//	
//	private void login(LoginBean bean)
//	{
////		int num = dao.find(bean, "account");
//		long num = dao.find(
//		        DAOQueryBuilder.create(LoginBean.class)
//		        .setWhereClause(DAOExpression.create("account").equal(bean.account)), 
//		        Long.class);
//		if (num < 0)
//		{
//			//查询出错
//			openMessageDialog("提示", "数据库错误", "确定");
//			return;
//		}
//		else if (num == 0)
//		{
//			dao.save(bean);
//		}
//		else
//		{
//		    dao.edit(
//		            DAOSQLBuilder.create(LoginBean.class)
//		            .setWhereClause(DAOExpression.create("account").equal(bean.account)), 
//		            bean);
//		}
//		
//		account_map.put(bean.account, bean);
//		
//		if (adapter != null)
//		{
//			adapter.refresh();
//		}
//		
//		set(new LoginBean());
//	}
//	
//	/**
//	 * 账户列表适配器
//	 * @author yanhao
//	 * @version 1.0
//	 */
//	
//	class PopupAdapter extends BaseAdapter {
//		
//		String[] accounts;					//账户列表（账号）
//		
//		public PopupAdapter() {
//			accounts = account_map.keySet().toArray(new String[0]);
//		}
//		
//		/**
//		 * 刷新数据
//		 */
//		
//		public void refresh()
//		{
//			accounts = account_map.keySet().toArray(new String[0]);
//			notifyDataSetChanged();
//		}
//
//		@Override
//		public int getCount() {
//			return accounts.length;
//		}
//
//		@Override
//		public Object getItem(int position) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(final int position, View convertView, ViewGroup parent) {
//			ViewHolder holder;
//			if (convertView == null)
//			{
//				convertView = LoginActivity.this.getLayoutInflater().inflate(R.layout.example_login_popup, null);
//				holder = new ViewHolder();
//				//账号
//				holder.tv = (TextView) convertView.findViewById(R.id.tv);
//				
//				//删除按钮
//				holder.delete = (ImageButton) convertView.findViewById(R.id.delete);
//				
//				convertView.setTag(holder);
//			}
//			else
//			{
//				holder = (ViewHolder) convertView.getTag();
//			}
//			
//			if (holder != null)
//			{
//				holder.tv.setText(accounts[position]);
//				holder.tv.setOnTouchListener(new OnTouchListener(){
//
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						popup.dismiss();
//						set(account_map.get(accounts[position]));
//						return true;
//					}});
//				holder.delete.setOnClickListener(new OnClickListener(){
//
//					@Override
//					public void onClick(View v) {
//					    dao.remove(
//					            DAOSQLBuilder.create(LoginBean.class)
//					            .setWhereClause(DAOExpression.create("account")
//					                    .equal(account_map.remove(accounts[position]))));
//						refresh();
//						
//						if (!account_map.containsKey(account_input.getText().toString()))
//						{
//							set(new LoginBean());
//						}
//					}});
//			}
//			
//			return convertView;
//		}
//		
//		class ViewHolder {
//			
//			TextView tv;			//账号
//			ImageButton delete;		//删除按钮
//		}
//	}
//}