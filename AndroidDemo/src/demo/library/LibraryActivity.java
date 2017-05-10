package demo.library;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import demo.android.R;

import java.util.LinkedList;
import java.util.List;

public class LibraryActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ListView lv = (ListView) findViewById(R.id.list);
        List<String> list = new LinkedList<String>();
        list.add("Android-Flip：可以实现类似FlipBoard那种华丽丽的翻页");
        list.add("Explosion：可以实现类似MIUI卸载APP时的炸裂效果");
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
        lv.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                switch (position) {
                case 0:
                    // Android-Flip
                    startActivity(new Intent(LibraryActivity.this, Android_Flip.class));
                    break;
                case 1:
                    // Explosion
                    startActivity(new Intent(LibraryActivity.this, ExplosionActivity.class));
                    break;
                }
            }});
    }
}
