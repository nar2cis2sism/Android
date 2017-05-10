package demo.activity.test;

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

public class TestActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ListView lv = (ListView) findViewById(R.id.list);
        List<String> list = new LinkedList<String>();
        list.add("TestOnMethod");
        list.add("硬件信息");
        list.add("工具类测试");
        list.add("日期时间格式化");
        list.add("加密解密");
        list.add("触摸事件");
        list.add("数据库");
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
        lv.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                switch (arg2) {
                case 0:
                    //TestOnMethod
                    startActivity(new Intent(TestActivity.this, TestOnMethod.class));
                    break;
                case 1:
                    //硬件信息
                    startActivity(new Intent(TestActivity.this, TestOnHardWare.class));
                    break;
                case 2:
                    //工具类测试
                    startActivity(new Intent(TestActivity.this, TestOnUtil.class));
                    break;
                case 3:
                    //日期时间格式化
                    startActivity(new Intent(TestActivity.this, TestOnDateTime.class));
                    break;
                case 4:
                    //加密解密
                    startActivity(new Intent(TestActivity.this, TestOnCrypto.class));
                    break;
                case 5:
                    //触摸事件
                    startActivity(new Intent(TestActivity.this, TestOnTouch.class));
                    break;
                case 6:
                    //数据库
                    startActivity(new Intent(TestActivity.this, TestOnDataBase.class));
                    break;

                default:
                    break;
                }
            }});
    }
}