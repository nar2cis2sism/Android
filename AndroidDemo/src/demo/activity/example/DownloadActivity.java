//package demo.activity.example;
//
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import demo.android.R;
//import engine.android.core.Forelet;
//import engine.android.util.file.FileDownloader;
//import engine.android.util.file.FileDownloader.DownloadStateListener;
//import engine.android.util.manager.SDCardManager;
//
//import java.io.File;
//
//public class DownloadActivity extends Forelet implements DownloadStateListener {
//	
//	EditText path;
//	Button download;
//	ProgressBar progress;
//	TextView result;
//	
//	FileDownloader fd;
//	
//	final Handler handler = new Handler() {
//		
//		@Override
//		public void handleMessage(Message msg) {
//            progress.setProgress((int) fd.getDownloadSize());
//            result.setText(Math.round(10000.0 * progress.getProgress() / progress.getMax()) / 100.0 + "%");
//            
//            sendEmptyMessageDelayed(0, 100);
//		}
//	};
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.download);
//		
//		path = (EditText) findViewById(R.id.path);
//		download = (Button) findViewById(R.id.download);
//		progress = (ProgressBar) findViewById(R.id.progress);
//		result = (TextView) findViewById(R.id.result);
//		
//		download.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				if ("开始下载".equals(download.getText()))
//				{
//			        download.setText("停止下载");
//			        
//					String downloadUrl = path.getText().toString();
//					File dir = SDCardManager.openSDCardAppDir(getBaseContext());
//					download(downloadUrl, dir);
//				}
//				else
//				{
//				    fd.stopDownload();
//				}
//			}});
//	}
//	
//	private void download(String downloadUrl, File dir)
//	{
//		System.out.println("下载目录：" + dir);
//		fd = new FileDownloader(downloadUrl, dir);
//		fd.config().enableBreakPointResume(true);
//		
//		fd.setStateListener(this);
//		
//		fd.startDownload();
//	}
//
//    @Override
//    public void onStateChanged(FileDownloader fileDownloader, int downloadState,
//            DownloadErrorMessage downloadErrorMessage) {
//        switch (downloadState) {
//            case STATE_DOWNLOADING:
//                long fileSize = fd.getFileSize();
//                System.out.println("文件大小：" + fileSize);
//                progress.setMax((int) fileSize);
//                
//                handler.sendEmptyMessage(0);
//                break;
//            case STATE_FINISH:
//                stopDownload();
//                result.setText("下载完毕");
//                download.setEnabled(false);
//                break;
//            case STATE_STOP:
//            case STATE_FAILURE:
//                stopDownload();
//                break;
//        }
//        
//        if (downloadErrorMessage != null)
//        {
//            result.setText(downloadErrorMessage.toString());
//        }
//    }
//    
//    private void stopDownload() {
//        handler.removeMessages(0);
//        download.setText("开始下载");
//        fd.close();
//    }
//}