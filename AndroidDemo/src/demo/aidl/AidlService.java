package demo.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;

public class AidlService extends Service {
	
	private final IAidl.Stub stub = new IAidl.Stub() {		//实现AIDL接口中各个方法
		
		private String name;

		@Override
		public int getAccountBalance() throws RemoteException {
			return 10000;
		}

		@Override
		public int getCustomerList(String branch, String[] customerList)
				throws RemoteException {
			customerList[0] = name;
			return 0;
		}

		@Override
		public void setOwnerNames(List<String> names) throws RemoteException {
			name = names.get(0);
		}

		@Override
		public void showTest() throws RemoteException {
			System.out.println("aidl");
		}};

	@Override
	public IBinder onBind(Intent intent) {
		//返回AIDL接口实例化对象
		return stub;
	}
}