package demo.activity.example;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import demo.android.R;

/**
 * 自定义标签界面
 * @author yanhao
 * @version 1.0
 */

public class MyTabActivity extends ActivityGroup {
	
	LocalActivityManager lam;
	
	FrameLayout frame;
	RadioGroup menuBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_tab_activity);
		
		lam = getLocalActivityManager();
		
		frame = (FrameLayout) findViewById(R.id.frame);
		menuBar = (RadioGroup) findViewById(R.id.menuBar);
		
		menuBar.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.tab_1:
					//首页
					frame.removeAllViews();
//					frame.addView(lam.startActivity("tab_1", new Intent(MyTabActivity.this, LoginActivity.class)).getDecorView());
					break;
				case R.id.tab_2:
					//信息
					frame.removeAllViews();
					ImageView iv = new ImageView(MyTabActivity.this);
					iv.setImageResource(R.drawable.img0001);
					frame.addView(iv);
					break;

				default:
					break;
				}
			}});
	}
}