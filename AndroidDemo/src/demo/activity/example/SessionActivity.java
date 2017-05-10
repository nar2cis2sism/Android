package demo.activity.example;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import demo.activity.example.bean.SessionBean;
import demo.android.R;
import engine.android.core.Forelet;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话界面
 * @author yanhao
 * @version 1.0
 */

public class SessionActivity extends Forelet {
	
	SessionAdapter adapter;						//会话列表适配器
	List<SessionBean> list;						//会话属性列表
	boolean[] isExpand;							//会话展开状态
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initData();
		
		ListView lv = new ListView(this);
		lv.setAdapter(adapter = new SessionAdapter());
		lv.setBackgroundColor(Color.WHITE);
		lv.setCacheColorHint(Color.TRANSPARENT);
		lv.setDividerHeight(0);
		lv.setSelector(getResources().getDrawable(R.drawable.session_selector));
		setContentView(lv);
	}
	
	/**
	 * 初始化数据
	 */
	
	private void initData()
	{
		list = new ArrayList<SessionBean>();
		
		SessionBean bean = new SessionBean();
		bean.title = "我们都是幸运的人";
		bean.status = true;
		
		bean.start_time = "2011-04-11";
		bean.end_time = "2011-05-21";
		bean.to_name = "张北，李西，王东，谭北，居中正";
		bean.content = "在这个世界里，我们都是一群幸运的人，因为上天怜惜，赐与我们一副完整的身体，和健全的头脑，让我们在这世上走一遭，当是幸运无比的人。";
		bean.from_name = "国政";
		list.add(bean);
		
		bean = new SessionBean();
		bean.title = "生活的大城市";
		bean.status = false;
		
		bean.start_time = "2011-03-11";
		bean.end_time = "2011-05-11";
		bean.to_name = "黑三，王二";
		bean.content = "不知道为什么，我在这个城市，找不到任何活着的意义，因为我不明白，我在这里的意义。";
		bean.from_name = "张零";
		list.add(bean);
		
		bean = new SessionBean();
		bean.title = "向往中的生活";
		bean.status = false;
		
		bean.start_time = "2010-11-02";
		bean.end_time = "2010-12-01";
		bean.to_name = "刘王，齐皇，塞帝";
		bean.content = "向往着田园的生活，却奋斗在喧嚣的都市。";
		bean.from_name = "王皇帝";
		list.add(bean);
		
		bean = new SessionBean();
		bean.title = "不知道为什么";
		bean.status = true;
		
		bean.start_time = "2011-01-21";
		bean.end_time = "2011-06-27";
		bean.to_name = "毛东";
		bean.content = "你说真的可以吗？";
		bean.from_name = "未知";
		list.add(bean);
		
		bean = new SessionBean();
		bean.title = "世界无限大";
		bean.status = false;
		
		bean.start_time = "2001-07-11";
		bean.end_time = "2001-05-21";
		bean.to_name = "中美日";
		bean.content = "这个世界真的很大，青山绿水，山川高原，有着无数的地方，等待我们去探索。\r\n" +
				"这个世界真的很大，琴棋书画，衣食住行，每一样都着独特的魅力，让我们去学习。\r\n" +
				"这个世界，真的很大。";
		bean.from_name = "乡";
		list.add(bean);
		
		bean = new SessionBean();
		bean.title = "信念";
		bean.status = true;
		
		bean.start_time = "2011-04-11";
		bean.end_time = "2011-05-21";
		bean.to_name = "信念";
		bean.content = "当你抱着一个信念，意志坚定的走下去，即便神魔，也不敢说什么，可惜如今太多的人面对残酷的现实，丢失了信仰，如此的你怎么去谈梦想。";
		bean.from_name = "信仰";
		list.add(bean);
		
		bean = new SessionBean();
		bean.title = "磨和难";
		bean.status = true;
		
		bean.start_time = "2011-04-11";
		bean.end_time = "2011-05-21";
		bean.to_name = "痛苦，挫折";
		bean.content = "曾常常听到这样的话语，幸福是建立在物质需求之上的，每每听这句话，便觉得很刺耳，终其原因，" +
				"是因为幸福之所以幸福，是因为经历过风雨，这个世界因为有了峭壁，才觉得平原的广阔，因为有了干旱，才渴望雨水的滋润。";
		bean.from_name = "幸福";
		list.add(bean);
		
		bean = new SessionBean();
		bean.title = "我们都是幸运的人";
		bean.status = true;
		
		bean.start_time = "2011-04-11";
		bean.end_time = "2011-05-21";
		bean.to_name = "张北，李西，王东，谭北，居中正";
		bean.content = "在这个世界里，我们都是一群幸运的人，因为上天怜惜，赐与我们一副完整的身体，和健全的头脑，让我们在这世上走一遭，当是幸运无比的人。";
		bean.from_name = "国政";
		list.add(bean);
		
		bean = new SessionBean();
		bean.title = "我们都是幸运的人";
		bean.status = true;
		
		bean.start_time = "2011-04-11";
		bean.end_time = "2011-05-21";
		bean.to_name = "张北，李西，王东，谭北，居中正";
		bean.content = "在这个世界里，我们都是一群幸运的人，因为上天怜惜，赐与我们一副完整的身体，和健全的头脑，让我们在这世上走一遭，当是幸运无比的人。";
		bean.from_name = "国政";
		list.add(bean);
		
		bean = new SessionBean();
		bean.title = "我们都是幸运的人";
		bean.status = true;
		
		bean.start_time = "2011-04-11";
		bean.end_time = "2011-05-21";
		bean.to_name = "张北，李西，王东，谭北，居中正";
		bean.content = "在这个世界里，我们都是一群幸运的人，因为上天怜惜，赐与我们一副完整的身体，和健全的头脑，让我们在这世上走一遭，当是幸运无比的人。";
		bean.from_name = "国政";
		list.add(bean);
		
		bean = new SessionBean();
		bean.title = "我们都是幸运的人";
		bean.status = true;
		
		bean.start_time = "2011-04-11";
		bean.end_time = "2011-05-21";
		bean.to_name = "张北，李西，王东，谭北，居中正";
		bean.content = "在这个世界里，我们都是一群幸运的人，因为上天怜惜，赐与我们一副完整的身体，和健全的头脑，让我们在这世上走一遭，当是幸运无比的人。";
		bean.from_name = "国政";
		list.add(bean);
		
		isExpand = new boolean[list.size()];
	}
	
	/**
	 * 会话列表适配器
	 * @author yanhao
	 * @version 1.0
	 */
	
	class SessionAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			SessionListItem view = null;
			if (convertView == null)
			{
				view = new SessionListItem(SessionActivity.this);
			}
			else
			{
				view = (SessionListItem) convertView;
			}
			
			if (view != null)
			{
				view.refresh(position);
			}
			
			return view;
		}
		
		/**
		 * 会话列表项
		 * @author yanhao
		 * @version 1.0
		 */
		
		class SessionListItem extends LinearLayout {
			
			LinearLayout title_bg;						//标题背景
			TextView title;								//标题
			ImageView status;							//状态（未读：已读）
			
			LinearLayout detail;						//内容详情
			TextView start_time;						//起始时间
			TextView end_time;							//结束时间
			TextView to_name;							//接收人名称
			TextView content;							//消息内容
			TextView from_name;							//发送人名称
			
			public SessionListItem(Context context) {
				super(context);
				
				LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.example_session_listitem, null);
				//标题背景
				title_bg = (LinearLayout) layout.findViewById(R.id.title_bg);

				//标题
				title = (TextView) layout.findViewById(R.id.title);

				//状态（未读：已读）
				status = (ImageView) layout.findViewById(R.id.status);

				//内容详情
				detail = (LinearLayout) layout.findViewById(R.id.detail);

				//起始时间
				start_time = (TextView) layout.findViewById(R.id.start_time);

				//结束时间
				end_time = (TextView) layout.findViewById(R.id.end_time);

				//接收人名称
				to_name = (TextView) layout.findViewById(R.id.to_name);

				//消息内容
				content = (TextView) layout.findViewById(R.id.content);

				//发送人名称
				from_name = (TextView) layout.findViewById(R.id.from_name);
				
				addView(layout);
			}
			
			public void refresh(final int position)
			{
				final SessionBean bean = list.get(position);
				//标题背景
				title_bg.setBackgroundResource(position % 2 == 0 ? R.drawable.session_title_bg_0 : R.drawable.session_title_bg_1);
				title_bg.setOnTouchListener(new OnTouchListener(){

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_UP)
						{
							isExpand[position] = !isExpand[position];
							refresh(position);
							return false;
						}
						
						return true;
					}});

				//标题
				title.setText(bean.title);
				title.setTextColor(position % 2 == 0 ? Color.parseColor("#FF3D8CB8") : Color.WHITE);

				//状态（未读：已读）
				status.setImageResource(bean.status ? R.drawable.session_status_true : R.drawable.session_status_false);
				status.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						if (bean.status)
						{
							Toast.makeText(SessionActivity.this, "该任务已完成", Toast.LENGTH_SHORT).show();
						}
						else
						{
							bean.status = true;
							status.setImageResource(R.drawable.session_status_true);
						}
					}});

				//内容详情
				detail.setVisibility(isExpand[position] ? VISIBLE : GONE);
				
				if (isExpand[position])
				{
					//起始时间
					start_time.setText(bean.start_time);

					//结束时间
					end_time.setText(bean.end_time);

					//接收人名称
					to_name.setText(bean.to_name);

					//消息内容
					content.setText(bean.content);

					//发送人名称
					from_name.setText(bean.from_name);
				}
			}
		}
	}
}