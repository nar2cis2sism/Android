package demo.account;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Service to handle Account sync. This is invoked with an intent with action
 * ACTION_AUTHENTICATOR_INTENT. It instantiates the syncadapter and returns its
 * IBinder.
 */

public class SyncService extends Service {
	
	private final byte[] mSyncAdapterLock = new byte[0];
	
    private SyncAdapter mSyncAdapter;
    
    @Override
    public void onCreate() {
    	synchronized (mSyncAdapterLock) {
			if (mSyncAdapter == null)
			{
				mSyncAdapter = new SyncAdapter(getApplicationContext(), true);
			}
		}
    }

	@Override
	public IBinder onBind(Intent intent) {
		return mSyncAdapter.getSyncAdapterBinder();
	}

	/**
	 * SyncAdapter implementation for syncing this SyncAdapter contacts to the
	 * platform ContactOperations provider.
	 */
	
	class SyncAdapter extends AbstractThreadedSyncAdapter {

		public SyncAdapter(Context context, boolean autoInitialize) {
			super(context, autoInitialize);
		}

		@Override
		public void onPerformSync(Account account, Bundle extras,
				String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			// TODO Auto-generated method stub
			
		}
	}
}