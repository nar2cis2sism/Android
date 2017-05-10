package demo.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RingtoneActivity extends ListActivity {

	private static final int SMS_RINGTONE_PICKED 	= 0;
	private static final int PHONE_RINGTONE_PICKED 	= 1;
	private static final int ALARM_RINGTONE_PICKED 	= 2;
	private static final int SDCARD_RINGTONE_PICKED = 3;
	
	private static final String[] titles
	= {"选择短信铃声", "选择手机铃声", "选择闹钟铃声", "选择SDcard中的铃声"};
	
	final String[] texts = new String[titles.length];
	
	Uri sms_uri;
	Uri phone_uri;
	Uri alarm_uri;
	Uri sdcard_uri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		update();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
		case SMS_RINGTONE_PICKED:
			//选择短信铃声
			pickSmsRingtone();
			break;
		case PHONE_RINGTONE_PICKED:
			//选择手机铃声
			pickPhoneRingtone();
			break;
		case ALARM_RINGTONE_PICKED:
			//选择闹钟铃声
			pickAlarmRingtone();
			break;
		case SDCARD_RINGTONE_PICKED:
			//选择SDcard中的铃声
			pickSDcardRingtone();
			break;

		default:
			break;
		}
	}
	
	private void pickSmsRingtone()
	{
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        //是否显示默认的手机铃声
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        //是否显示静音
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        //显示铃声类型
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        //设置默认的短信铃声
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

    	//设置当前选择的铃声
        if (sms_uri != null)
        {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, sms_uri);
        }
        else
        {
        	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        
        startActivityForResult(intent, SMS_RINGTONE_PICKED);
	}
	
	private void pickPhoneRingtone()
	{
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        //是否显示默认的手机铃声
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        //是否显示静音
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        //显示铃声类型
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        //设置默认的手机铃声
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));

    	//设置当前选择的铃声
        if (phone_uri != null)
        {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, phone_uri);
        }
        else
        {
        	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        }
        
        startActivityForResult(intent, PHONE_RINGTONE_PICKED);
	}
	
	private void pickAlarmRingtone()
	{
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        //是否显示默认的手机铃声
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        //是否显示静音
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        //显示铃声类型
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        //设置默认的闹钟铃声
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

    	//设置当前选择的铃声
        if (alarm_uri != null)
        {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, alarm_uri);
        }
        else
        {
        	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        }
        
        startActivityForResult(intent, ALARM_RINGTONE_PICKED);
	}
	
	private void pickSDcardRingtone()
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("audio/*");
//		intent.setType("audio/aac");//如果不想使用录音
        
        startActivityForResult(intent, SDCARD_RINGTONE_PICKED);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
		{
			return;
		}
		
		switch (requestCode) {
		case SMS_RINGTONE_PICKED:
			//选择短信铃声
			sms_uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if (sms_uri == null)
			{
				texts[SMS_RINGTONE_PICKED] = "静音";
			}
			else
			{
				Ringtone ringtone =  RingtoneManager.getRingtone(this, sms_uri);
				texts[SMS_RINGTONE_PICKED] = ringtone.getTitle(this);
			}
			
			break;
		case PHONE_RINGTONE_PICKED:
			//选择手机铃声
			phone_uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if (phone_uri == null)
			{
				texts[PHONE_RINGTONE_PICKED] = "静音";
			}
			else
			{
				Ringtone ringtone =  RingtoneManager.getRingtone(this, phone_uri);
				texts[PHONE_RINGTONE_PICKED] = ringtone.getTitle(this);
			}
			
			break;
		case ALARM_RINGTONE_PICKED:
			//选择闹钟铃声
			alarm_uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if (alarm_uri == null)
			{
				texts[ALARM_RINGTONE_PICKED] = "静音";
			}
			else
			{
				Ringtone ringtone =  RingtoneManager.getRingtone(this, alarm_uri);
				texts[ALARM_RINGTONE_PICKED] = ringtone.getTitle(this);
			}
			
			break;
		case SDCARD_RINGTONE_PICKED:
			//选择SDcard中的铃声
			sdcard_uri = data.getData();
			if (sdcard_uri == null)
			{
				texts[SDCARD_RINGTONE_PICKED] = "静音";
			}
			else
			{
				Ringtone ringtone =  RingtoneManager.getRingtone(this, sdcard_uri);
				texts[SDCARD_RINGTONE_PICKED] = ringtone.getTitle(this);
			}
			
			break;

		default:
			break;
		}
		
		update();
	}
	
	private void update()
	{
		List<Map<String, String>> data = new ArrayList<Map<String,String>>();
		for (int i = 0; i < titles.length; i++)
		{
			Map<String, String> map = new HashMap<String, String>();
			map.put("title", titles[i]);
			map.put("text", texts[i]);
			data.add(map);
		}
		
		setListAdapter(new SimpleAdapter(this, data, android.R.layout.simple_list_item_2, 
				new String[]{"title", "text"}, 
				new int[]{android.R.id.text1, android.R.id.text2}));
	}
}