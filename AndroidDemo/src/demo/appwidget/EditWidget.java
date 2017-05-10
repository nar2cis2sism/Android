package demo.appwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RemoteViews;

import demo.android.R;
import engine.android.util.image.ImageUtil;

public class EditWidget extends Activity implements OnClickListener {
	
	private int appWidgetId;
	private SharedPreferences sp;
	private EditText et;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		
		appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
		//恢复数据
		sp = getSharedPreferences("widget", MODE_PRIVATE);
		et = (EditText) findViewById(R.id.EditText02);
		et.setText(sp.getString("DAT_" + appWidgetId, ""));
		
		//设置按钮监听器
		ImageButton button = (ImageButton) findViewById(R.id.ImageButton01);
		button.setOnClickListener(this);
		button = (ImageButton) findViewById(R.id.ImageButton02);
		button.setOnClickListener(this);
		button = (ImageButton) findViewById(R.id.ImageButton03);
		button.setOnClickListener(this);
		button = (ImageButton) findViewById(R.id.ImageButton04);
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		//储存数据
		Editor e = sp.edit();
		e.putString("DAT_" + appWidgetId, et.getText().toString());
		e.commit();
		
		//获取Widget窗体控件
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget);
		//设置布局内容
		views.setImageViewBitmap(R.id.my_widget_img, ImageUtil.drawable2Bitmap(v.getBackground()));
		
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		//更新Widget
		manager.updateAppWidget(appWidgetId, views);
		

		finish();
	}
}