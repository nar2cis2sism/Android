package demo.activity.example.bean;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import android.text.format.DateFormat;

public class Email {
	
	public static final String FROM 	= "from";
	
	public static final String DATE 	= "date";
	
	public static final String SUBJECT 	= "subject";
	
	public static final String BODY 	= "body";
	
	public String from;
	
	public long date;
	
	public String subject;
	
	public String body;
	
	public static class EmailDateRange {
		
		private long start;
		private long end;
		private String label;
		
		EmailDateRange(long start, long end, String label) {
			this.start = start;
			this.end = end;
			this.label = label;
		}
		
		boolean includes(long time)
		{
			return start <= time && time < end;
		}
		
		public String getLabel() {
			return label;
		}
	}
	
	private static final List<EmailDateRange> dateList;
	
	static
	{
		dateList = new LinkedList<EmailDateRange>();
		
		//Today
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long today = cal.getTimeInMillis();
		
		long start = today;
		cal.roll(Calendar.DAY_OF_MONTH, true);
		long end = cal.getTimeInMillis();
		EmailDateRange dateRange = new EmailDateRange(start, end, "Today");
		dateList.add(dateRange);
		
		//Yesterday
		cal.setTimeInMillis(today);
		end = today;
		cal.roll(Calendar.DAY_OF_MONTH, false);
		start = cal.getTimeInMillis();
		dateRange = new EmailDateRange(start, end, "Yesterday");
		dateList.add(dateRange);
		
		//days from this week
		cal.setTimeInMillis(today);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int firstDayOfWeek = cal.getFirstDayOfWeek();
		if (dayOfWeek != firstDayOfWeek)
		{
			cal.roll(Calendar.DAY_OF_MONTH, false);
			while (cal.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek)
			{
				end = cal.getTimeInMillis();
				cal.roll(Calendar.DAY_OF_MONTH, false);
				start = cal.getTimeInMillis();
				String label = DateFormat.format("EEEE", start).toString();
				dateRange = new EmailDateRange(start, end, label);
				dateList.add(dateRange);
			}
		}
		
		//last week...
		cal.setTimeInMillis(today);
		cal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
		end = cal.getTimeInMillis();
		cal.roll(Calendar.WEEK_OF_YEAR, false);
		start = cal.getTimeInMillis();
		dateRange = new EmailDateRange(start, end, "Last Week");
		dateList.add(dateRange);
		
		//two weeks ago...
		end = start;
		cal.roll(Calendar.WEEK_OF_YEAR, false);
		start = cal.getTimeInMillis();
		dateRange = new EmailDateRange(start, end, "Two Weeks Ago");
		dateList.add(dateRange);
		
		//three weeks ago...
		end = start;
		cal.roll(Calendar.WEEK_OF_YEAR, false);
		start = cal.getTimeInMillis();
		dateRange = new EmailDateRange(start, end, "Three Weeks Ago");
		dateList.add(dateRange);
		
		//earlier this month...
		end = start;
		cal.setTimeInMillis(today);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		start = cal.getTimeInMillis();
		if (start < end)
		{
			dateRange = new EmailDateRange(start, end, "Earlier this Month");
			dateList.add(dateRange);
		}
		
		//last month...
		end = start;
		cal.roll(Calendar.MONTH, false);
		start = cal.getTimeInMillis();
		dateRange = new EmailDateRange(start, end, "Last Month");
		dateList.add(dateRange);
		
		//older
		dateRange = new EmailDateRange(0, start, "Older");
		dateList.add(dateRange);
	}
	
	public static EmailDateRange lookupDateRange(long time)
	{
		for (EmailDateRange dateRange : dateList)
		{
			if (dateRange.includes(time))
			{
				return dateRange;
			}
		}
		
		return null;
	}
}