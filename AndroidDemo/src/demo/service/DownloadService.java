package demo.service;

import android.app.IntentService;
import android.content.Intent;

public class DownloadService extends IntentService {
    
    private static final String NAME = "download";

    public DownloadService() {
        super(NAME);
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        
    }
}