package demo.activity.test;

import android.app.Activity;
import android.widget.ScrollView;
import android.widget.TextView;

import demo.widget.MyTextView;


public class TestOnBase extends Activity {
    
    protected final StringBuilder sb = new StringBuilder();
    
    protected void log(String content)
    {
        System.out.println(content);
        sb.append(content + "\n");
    }
    
    protected void showContent()
    {
        ScrollView sv = new ScrollView(this);
        TextView tv = new MyTextView(this);
        tv.setText(sb.toString());
        sv.addView(tv);
        setContentView(sv);
    }
}