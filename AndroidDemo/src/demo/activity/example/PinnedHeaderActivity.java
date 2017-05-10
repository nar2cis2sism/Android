package demo.activity.example;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import demo.activity.example.bean.Email;
import demo.activity.example.bean.Email.EmailDateRange;
import demo.android.R;
import demo.widget.PinnedHeaderListView;
import demo.widget.PinnedHeaderListView.PinnedHeaderAdapter;
import engine.android.core.util.CalendarFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PinnedHeaderActivity extends Activity implements ViewBinder {
	
	List<Email> emailList;
	EmailAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initData();
		
		PinnedHeaderListView lv = new PinnedHeaderListView(this);
		lv.setBackgroundColor(Color.parseColor("#fff2f2f2"));
		lv.setCacheColorHint(Color.parseColor("#fff2f2f2"));
		lv.setDivider(new ColorDrawable(Color.parseColor("#bfbfbf")));
		lv.setDividerHeight(1);
		lv.setFastScrollEnabled(true);
		
		adapter = new EmailAdapter(this, getData(), 
				R.layout.pinned_header_listitem, 
				new String[]{Email.FROM, Email.DATE, Email.SUBJECT, Email.BODY}, 
				new int[]{R.id.from, R.id.date, R.id.subject, R.id.body});
		adapter.setViewBinder(this);
		
		lv.setAdapter(adapter);
		
		lv.setPinnedHeaderView(R.layout.pinned_header_category);
		
		setContentView(lv);
	}
	
	private void initData()
	{
		emailList = new LinkedList<Email>();
		
		String[] subjects = {"s", "1", "RE:2", "Canceled:d", "Canceled:a", "RE:test", "Canceled:1"};
		String[] bodys = {"s", "1", "From:Hyan1 Sent:Friday,July 20,2012 9:15 AM To:Hyan2 Subject:2 2", "Hyan1@asia.qagood.com", "<<Hao Yan.txt>>", "", "a"};
		
		Calendar cal = Calendar.getInstance();
		cal.roll(Calendar.DAY_OF_YEAR, true);
		Random random = new Random();
		
		for (int i = 0; i < 100; i++)
		{
			Email email = new Email();
			email.from = "Hyan2";
			email.date = cal.getTimeInMillis();
			email.subject = subjects[random.nextInt(subjects.length)];
			email.body = bodys[random.nextInt(bodys.length)];
			emailList.add(email);

			cal.roll(Calendar.DAY_OF_YEAR, false);
		}
	}
	
	private List<Map<String, Object>> getData()
	{
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(emailList.size());
		for (Email email : emailList)
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(Email.FROM, email.from);
			map.put(Email.DATE, email.date);
			map.put(Email.SUBJECT, email.subject);
			map.put(Email.BODY, email.body);
			list.add(map);
		}
		
		return list;
	}

	@Override
	public boolean setViewValue(View view, Object data,
			String textRepresentation) {
		switch (view.getId()) {
		case R.id.date:
			TextView tv = (TextView) view;
			long date = ((Long) data).longValue();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(date);
			Calendar today = Calendar.getInstance();
			if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
			&&  cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
			&&  cal.get(Calendar.DATE) == today.get(Calendar.DATE))
			{
				//Today
				tv.setText(CalendarFormat.format(cal, "HH:mm"));
			}
			else
			{
				tv.setText(CalendarFormat.format(cal, "MM-dd"));
			}
			
			return true;
		}
		
		return false;
	}
	
	class EmailAdapter extends SimpleAdapter implements PinnedHeaderAdapter {
		
		public EmailAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			if (view != null)
			{
				View header = (View) view.getTag();
				TextView tv;
				if (header == null)
				{
					header = view.findViewById(R.id.header);
					view.setTag(header);
					tv = (TextView) header.findViewById(R.id.category);
					header.setTag(tv);
				}
				else
				{
					tv = (TextView) header.getTag();
				}
				
				EmailDateRange dateRange = Email.lookupDateRange(getDate(position));
				if (dateRange != null && position > 0 && Email.lookupDateRange(getDate(position - 1)) != dateRange)
				{
					tv.setText(dateRange.getLabel());
					header.setVisibility(View.VISIBLE);
				}
				else
				{
					header.setVisibility(View.GONE);
				}
			}
			
			return view;
		}
		
		@Override
		public boolean getPinnedHeaderState(int position) {
			EmailDateRange dateRange = Email.lookupDateRange(getDate(position));
			if (Email.lookupDateRange(getDate(position + 1)) != dateRange)
			{
				return true;
			}
			
			return false;
		}

		@Override
		public void configurePinnedHeader(View header, int position, float visibleRatio) {
			PinnedHeaderCache cache = (PinnedHeaderCache) header.getTag();
			if (cache == null)
			{
				cache = new PinnedHeaderCache();
				cache.text = (TextView) header.findViewById(R.id.category);
				cache.color = cache.text.getTextColors();
				header.setTag(cache);
			}
			
			String label = null;
			EmailDateRange dateRange = Email.lookupDateRange(getDate(position));
			if (dateRange != null)
			{
				label = dateRange.getLabel();
				header.setVisibility(View.VISIBLE);
			}
			else
			{
				header.setVisibility(View.GONE);
			}
			
			if (!TextUtils.isEmpty(label))
			{
				cache.text.setText(label);
			}
			
			if (visibleRatio == 1.0f)
			{
				cache.text.setTextColor(cache.color);
			}
			else
			{
				int color = cache.color.getDefaultColor();
				cache.text.setTextColor(Color.argb((int) (0xff * visibleRatio), 
						Color.red(color), Color.green(color), Color.blue(color)));
			}
		}
		
		private long getDate(int position)
		{
			return emailList.get(position).date;
		}
		
		final class PinnedHeaderCache {
			
			public TextView text;
			public ColorStateList color;
			
		}
	}
}