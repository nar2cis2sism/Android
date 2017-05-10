package demo.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * 账户管理器
 * @author Daimon
 * @version 3.0
 * @since 9/28/2012
 */

public class MyAccountManager {
	
	private Context context;
	
	public MyAccountManager(Context context) {
		this.context = context;
	}
	
	/**
	 * 添加账户<br>
	 * 需要声明权限<uses-permission android:name="android.permission.AUTHENTICATE_ACCOUNTS" />
	 * @param name 账户名称
	 */
	
	public boolean addAccount(String name)
	{
		AccountManager am = AccountManager.get(context);
		return am.addAccountExplicitly(new Account(name, context.getPackageName()), null, null);
	}
	
	/**
	 * 获取账户<br>
	 * 需要声明权限<uses-permission android:name="android.permission.GET_ACCOUNTS" />
	 * @param name 账户名称
	 */
	
	public Account getAccount(String name)
	{
		AccountManager am = AccountManager.get(context);
		Account[] accounts = am.getAccountsByType(context.getPackageName());
		for (Account account : accounts)
		{
			if (account.name.equals(name))
			{
				return account;
			}
		}
		
		return null;
	}
}