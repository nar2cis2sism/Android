package demo.activity.test;

import static engine.android.core.util.CalendarFormat.ABBREV_ALL;
import static engine.android.core.util.CalendarFormat.ABBREV_WEEKDAY;
import static engine.android.core.util.CalendarFormat.NO_MONTH_DAY;
import static engine.android.core.util.CalendarFormat.NO_YEAR;
import static engine.android.core.util.CalendarFormat.SHOW_WEEKDAY;
import static engine.android.core.util.CalendarFormat.SHOW_YEAR;

import android.os.Bundle;
import android.provider.Settings;

import engine.android.core.util.CalendarFormat;

import java.util.Calendar;
import java.util.Locale;

public class TestOnDateTime extends TestOnBase {
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        long inTimeInMillis = System.currentTimeMillis();

        log("根据语言设置");
        final Locale locale = getResources().getConfiguration().locale;
        log(locale.getDisplayName(locale));
        log(CalendarFormat.formatDateTimeByLocale(this, inTimeInMillis, SHOW_YEAR, true));
        log(CalendarFormat.formatDateByLocale(inTimeInMillis, SHOW_YEAR | NO_MONTH_DAY));
        log(CalendarFormat.formatDateByLocale(inTimeInMillis, NO_YEAR));
        log(CalendarFormat.formatDateByLocale(inTimeInMillis, SHOW_YEAR | SHOW_WEEKDAY | ABBREV_ALL));
        log(CalendarFormat.formatDateByLocale(inTimeInMillis, SHOW_YEAR | SHOW_WEEKDAY));
        log(CalendarFormat.formatDateByLocale(inTimeInMillis, SHOW_YEAR | SHOW_WEEKDAY | ABBREV_WEEKDAY));

        log("\n");
        log("根据手机设置");
        log(Settings.System.getString(getContentResolver(), Settings.System.DATE_FORMAT));
        log(CalendarFormat.formatDateTimeBySetting(this, inTimeInMillis, 0, true));
        log(CalendarFormat.formatDateBySetting(this, inTimeInMillis, SHOW_WEEKDAY));
        log(CalendarFormat.formatDateBySetting(this, inTimeInMillis, SHOW_WEEKDAY | ABBREV_WEEKDAY));

        log("\n");
        log("固定格式");
        log(CalendarFormat.format(Calendar.getInstance(), "yyyy-MM-dd EEEE HH:mm:ss.SSS\n%tF %tA %tT.%tL\n%cF %cE %cr"));
        
        showContent();
    }
}