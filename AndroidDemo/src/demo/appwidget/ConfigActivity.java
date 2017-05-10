package demo.appwidget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RemoteViews;

import demo.android.R;
import engine.android.util.image.ImageUtil;

public class ConfigActivity extends Activity implements OnClickListener {
	
	private int appWidgetId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			//获取Widget的ID
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
		{
			//如果Widget的ID无效，则返回
			finish();
		}
		else
		{
			setContentView(R.layout.config);
			
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
	}

	@Override
	public void onClick(View v) {
		//储存数据
		EditText et = (EditText) findViewById(R.id.EditText02);
		Editor e = getSharedPreferences("widget", MODE_PRIVATE).edit();
		e.putString("DAT_" + appWidgetId, et.getText().toString());
		e.commit();
		
		//获取Widget窗体控件
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget);
		//设置布局内容
		views.setImageViewBitmap(R.id.my_widget_img, ImageUtil.drawable2Bitmap(v.getBackground()));
		//设置点击事件
		Intent intent = new Intent(this, EditWidget.class);
		/**
		 * 注意这里我们使用intent.setAction(getPackageName() + appWidgetId);为每个widget赋予了独一无二的Action
		 * 否则获得的pendingIntent实际是同一个实例，仅extraData不同，根据创建pendingIntent方法的不同，
		 * extraData可能会被覆盖或者只初始化一次不再改变（getActivity的最后一个参数flags决定）。
		 * 这样我们在pendingIntent中就只能得到第一个新增的widget的Id，或者最后一次新增的widget的Id
		 * 具体参见#NotificationActivity
		 */
		intent.setAction(getPackageName() + appWidgetId);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		views.setOnClickPendingIntent(R.id.my_widget_img, PendingIntent.getActivity(this, 0, intent, 0));
		
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		//更新Widget
		manager.updateAppWidget(appWidgetId, views);
		

		Intent data = new Intent();
		//将Widget的ID透传下去
		data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		//必须设置RESULT_OK才能创建Widget
		setResult(RESULT_OK, data);
		finish();
	}
}