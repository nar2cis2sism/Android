package demo.activity.example;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import demo.android.R;
import demo.widget.FirstLetterBar;
import demo.widget.FirstLetterBar.FirstLetterAdapter;
import demo.widget.FirstLetterBar.OnFirstLetterChangedListener;
import engine.android.core.ApplicationManager;
import engine.android.util.file.FileManager;
import engine.android.util.io.IOUtil;
import engine.android.util.manager.SDCardManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirstLetterActivity extends ListActivity implements 
OnFirstLetterChangedListener, OnTouchListener, TextWatcher {
    
    SQLiteDatabase db;
    
    List<String> firstLetters;
    
    TextView overlay;
    FirstLetterBar firstLetterBar;
    
    Adapter adapter;
    Map<String, ArrayAdapter<String>> data;
    ArrayAdapter<String> searchData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_letter_activity);
        
        if (!SDCardManager.isEnabled())
        {
            new AlertDialog.Builder(this)
            .setMessage("请准备SD卡")
            .setPositiveButton("确定",
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    finish();
                }
            }).create().show();
            
            return;
        }
        
        try {
            String name = "city";
            File file = new File(getPackageName(), name);
            
            File out = SDCardManager.openSDCardFile(file.getAbsolutePath());
            FileManager.createFileIfNecessary(out);
            
            FileOutputStream fos = new FileOutputStream(out);
            IOUtil.writeStream(getAssets().open(name), fos);
            fos.close();
            
            db = SDCardManager.openSDCardDatabase(file.getAbsolutePath());
            
            firstLetters = Arrays.asList(FirstLetterBar.firstLetters);
            
            overlay = (TextView) findViewById(R.id.overlay);
            
            firstLetterBar = (FirstLetterBar) findViewById(R.id.firstLetterBar);
            firstLetterBar.setOnTouchListener(this);
            firstLetterBar.setOnFirstLetterChangedListener(this);
            
            setListAdapter(adapter = new Adapter(this));
            
            data = new HashMap<String, ArrayAdapter<String>>();
            
            searchData = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            
            EditText search = (EditText) findViewById(R.id.search);
            search.addTextChangedListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private class Adapter extends FirstLetterAdapter {

        public Adapter(Context context) {
            super(context);
        }

        @Override
        public ListAdapter getData(String firstLetter) {
            if (!data.containsKey(firstLetter))
            {
                List<String> list = new ArrayList<String>();
                if (FirstLetterBar.firstLetters[0].equals(firstLetter))
                {
                    list.add("上海");
                }
                else
                {
                    Cursor c = db.query("city", new String[]{"name"}, "pinyin like ?", 
                            new String[]{firstLetter + "%"}, null, null, "pinyin");
                    if (c != null)
                    {
                        while (c.moveToNext())
                        {
                            String name = c.getString(c.getColumnIndexOrThrow("name"));
                            if (!TextUtils.isEmpty(name))
                            {
                                list.add(name);
                            }
                        }
                        
                        c.close();
                    }
                }

                data.put(firstLetter, new ArrayAdapter<String>(getBaseContext(), 
                        android.R.layout.simple_list_item_1, list));
            }
            
            return data.get(firstLetter);
        }

        @Override
        public String getTitle(String firstLetter) {
            if (FirstLetterBar.firstLetters[0].equals(firstLetter))
            {
                return "当前城市";
            }
            
            return firstLetter;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            String text = parent.getItemAtPosition(position).toString();
            ApplicationManager.showMessage(text);
        }
    }

    @Override
    public void onFirstLetterChanged(String firstLetter) {
        overlay.setText(firstLetter);
        overlay.setVisibility(View.VISIBLE);
        if (getListAdapter() == adapter)
        {
            getListView().setSelection(firstLetters.indexOf(firstLetter));
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
            overlay.setVisibility(View.GONE);
            break;
        }
        
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void afterTextChanged(Editable s) {
        search(s.toString());
    }
    
    private void search(String text)
    {
        if (TextUtils.isEmpty(text))
        {
            setListAdapter(adapter);
        }
        else
        {
            searchData.setNotifyOnChange(false);
            searchData.clear();
            Cursor c = db.query("city", new String[]{"name"}, "pinyin like ? or name like ?", 
                    new String[]{text + "%", text + "%"}, null, null, "pinyin");
            if (c != null)
            {
                while (c.moveToNext())
                {
                    String name = c.getString(c.getColumnIndexOrThrow("name"));
                    if (!TextUtils.isEmpty(name))
                    {
                        searchData.add(name);
                    }
                }
                
                c.close();
            }

            searchData.notifyDataSetChanged();
            setListAdapter(searchData);
        }
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        adapter.onItemClick(l, v, position, id);
    }
    
    @Override
    public void setListAdapter(ListAdapter adapter) {
        super.setListAdapter(adapter);
        firstLetterBar.setVisibility(adapter.isEmpty() ? View.GONE : View.VISIBLE);
    }
}