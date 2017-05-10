package demo.activity.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import demo.android.R;
import demo.widget.HorizontalListView;

import java.util.ArrayList;
import java.util.List;

public class HorizontalActivity extends Activity implements OnItemClickListener {
    
    VerticalListAdapter verticalAdapter;
    HorizontalListAdapter horizontalAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horizontal);
        
        ListView vertical_list = (ListView) findViewById(R.id.vertical_list);
        vertical_list.setAdapter(verticalAdapter = new VerticalListAdapter());
        vertical_list.setOnItemClickListener(this);
        HorizontalListView horizontal_list = (HorizontalListView) findViewById(R.id.horizontal_list);
        horizontal_list.setAdapter(horizontalAdapter = new HorizontalListAdapter());
        horizontal_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ViewData data = horizontalAdapter.getItem(position);
                if (data.id != -1)
                {
                    verticalAdapter.setChecked(data.id, false);
                    horizontalAdapter.remove(data);
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        boolean isChecked = verticalAdapter.isChecked(position);
        verticalAdapter.setChecked(position, isChecked = !isChecked);
        ViewData data = verticalAdapter.getItem(position);
        if (isChecked)
        {
            horizontalAdapter.add(data);
        }
        else
        {
            horizontalAdapter.remove(data);
        }
    }
    
    private class VerticalListAdapter extends BaseAdapter {
        
        List<ViewData> list;
        
        public VerticalListAdapter() {
            list = new ArrayList<ViewData>();
            for (int i = 0; i < 100; i++)
            {
                ViewData vd = new ViewData();
                vd.id = i;
                vd.name = "name" + i;
                vd.head = getHead(i);
                list.add(vd);
            }
        }
        
        private int getHead(int id)
        {
            switch (id % 9) {
            case 0:
                return R.drawable.img0354;
            case 1:
                return R.drawable.img0001;
            case 2:
                return R.drawable.img0030;
            case 3:
                return R.drawable.img0100;
            case 4:
                return R.drawable.img0130;
            case 5:
                return R.drawable.img0200;
            case 6:
                return R.drawable.img0230;
            case 7:
                return R.drawable.img0300;
            case 8:
                return R.drawable.img0330;

            default:
                return 0;
            }
        }
        
        public boolean isChecked(int position)
        {
            return list.get(position).isChecked;
        }
        
        public void setChecked(int id, boolean isChecked)
        {
            list.get(id).isChecked = isChecked;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public ViewData getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return list.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.vertical_list_item, null);
                holder = new ViewHolder();
                holder.isChecked = (CheckBox) convertView.findViewById(R.id.isChecked);
                holder.head = (ImageView) convertView.findViewById(R.id.head);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            
            if (holder != null)
            {
                ViewData data = list.get(position);
                holder.isChecked.setChecked(data.isChecked);
                holder.head.setImageResource(data.head);
                holder.name.setText(data.name);
            }
            
            return convertView;
        }
        
        private class ViewHolder {
            
            CheckBox isChecked;
            ImageView head;
            TextView name;
        }
    }
    
    private class HorizontalListAdapter extends BaseAdapter {
        
        List<ViewData> list;
        
        public HorizontalListAdapter() {
            list = new ArrayList<ViewData>();
            ViewData vd = new ViewData();
            vd.id = -1;
            vd.head = R.drawable.none;
            list.add(vd);
        }
        
        public void add(ViewData data)
        {
            list.add(list.size() - 1, data);
            notifyDataSetChanged();
        }
        
        public void remove(ViewData data)
        {
            list.remove(data);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public ViewData getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return list.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.horizontal_list_item, null);
                holder = new ViewHolder();
                holder.head = (ImageView) convertView.findViewById(R.id.head);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            
            if (holder != null)
            {
                ViewData data = list.get(position);
                holder.head.setImageResource(data.head);
            }
            
            return convertView;
        }
        
        private class ViewHolder {
            
            ImageView head;
        }
    }
    
    private static class ViewData {
        
        int id;
        String name;
        int head;
        boolean isChecked;
        
    }
}