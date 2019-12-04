//package demo.activity.effect;
//
//import android.app.Activity;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.OnTouchListener;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ScrollView;
//
//import demo.android.R;
//import engine.android.core.ApplicationManager;
//import engine.android.dao.util.Page;
//import engine.android.util.image.AsyncImageLoader;
//import engine.android.util.image.AsyncImageLoader.ImageCallback;
//import engine.android.util.image.AsyncImageLoader.ImageDownloader;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//public class PhotoFlowActivity extends Activity implements OnTouchListener {
//	
//	private static final int COLUMN = 3;				//显示列数
//	private static final int COUNT = 15;				//每次加载图片数量
//	private static final String photo_path = "images";	//图片路径
//
//	private int columnWidth;
//	private AsyncImageLoader imageLoader;
//	
//	private ScrollView root;
//	private LinearLayout container;
//	
//	private List<String> photo_fileName;
//	private Page page = new Page(COUNT);
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.photo_flow);
//
//		columnWidth = getWindowManager().getDefaultDisplay().getWidth() / COLUMN;
//		imageLoader = new AsyncImageLoader();
//		
//		setupView();
//		initData();
//		
//		//第一次加载
//		addItem();
//	}
//	
//	@Override
//	protected void onDestroy() {
//        if (imageLoader != null)
//        {
//            imageLoader.release();
//        }
//        
//	    super.onDestroy();
//	}
//	
//	private void setupView()
//	{
//		root = (ScrollView) findViewById(R.id.root);
//		root.setOnTouchListener(this);
//		
//		container = (LinearLayout) findViewById(R.id.container);
//		
//		for (int i = 0; i < COLUMN; i++)
//		{
//			LinearLayout layout = new LinearLayout(this);
//			layout.setOrientation(LinearLayout.VERTICAL);
//			layout.setPadding(2, 2, 2, 2);
//			
//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//					columnWidth, LayoutParams.WRAP_CONTENT);
//			
//			layout.setLayoutParams(params);
//			
//			container.addView(layout);
//		}
//	}
//	
//	private void initData()
//	{
//		try {
//			//加载所有图片路径
//			photo_fileName = Arrays.asList(getAssets().list(photo_path));
//			page.setTotalRecord(photo_fileName.size());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private void addItem()
//	{
//		if (page.isLastPage())
//		{
//			ApplicationManager.showMessage("已到最后一页");
//			return;
//		}
//		
//		for (int i = page.getBeginRecord(); i < page.getEndRecord(); i++)
//		{
//			addPhoto(photo_fileName.get(i), i % COLUMN);
//		}
//		
//		page.nextPage();
//	}
//	
//	private void addPhoto(String fileName, int column)
//	{
//		final ImageView iv = new ImageView(this);
//		((LinearLayout) container.getChildAt(column)).addView(iv);
//		
//		imageLoader.loadImage(fileName, new ImageDownloader() {
//            
//            @Override
//            public Bitmap imageLoading(Object url) {
//                try {
//                    return BitmapFactory.decodeStream(getAssets().open(photo_path + "/" + url));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                
//                return null;
//            }
//        }, new ImageCallback() {
//            
//            @Override
//            public void imageLoaded(Object url, Bitmap image) {
//                if (image != null)
//                {
//                    //调整高度
//                    LayoutParams params = iv.getLayoutParams();
//                    params.height = image.getHeight() * columnWidth / image.getWidth();
//                    iv.setLayoutParams(params);
//                    
//                    iv.setImageBitmap(image);
//                }
//            }
//        });
//	}
//
//	@Override
//	public boolean onTouch(View v, MotionEvent event) {
//		switch (event.getAction()) {
//		case MotionEvent.ACTION_UP:
//	        if (root.getScrollY() == container.getMeasuredHeight() - root.getHeight())
//	        {
//	        	//滚动到底部
//	        	addItem();
//	        }
//		}
//		
//		return false;
//	}
//}