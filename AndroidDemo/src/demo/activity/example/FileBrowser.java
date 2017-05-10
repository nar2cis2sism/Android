package demo.activity.example;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import demo.android.R;
import engine.android.core.ApplicationManager;
import engine.android.util.file.FileManager;
import engine.android.util.file.FileManager.FileInfo;
import engine.android.util.image.ImageCache;
import engine.android.util.image.ImageUtil;
import engine.android.util.manager.SDCardManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FileBrowser extends Activity implements OnItemClickListener, OnClickListener {
    
    FileManager fm;
    File currentDir;                        //当前目录
    List<FileInfo> fileList;                //当前目录下的文件列表
    
    TextView path_tv;
    ListView fileList_lv;
    
    FileAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filebrowser);
        
        fm = new FileManager(this);
        fileList = new LinkedList<FileInfo>();
        setCurrentDir(SDCardManager.openSDCardFile(""));
        
        initView();
        initData();
        setupView();
    }
    
    private void initView()
    {
        path_tv = (TextView) findViewById(R.id.path);
        fileList_lv = (ListView) findViewById(R.id.fileList);
    }
    
    private void initData()
    {
        adapter = new FileAdapter();
    }
    
    private void setupView()
    {
        path_tv.setText(currentDir.getPath());
        fileList_lv.setAdapter(adapter);
        fileList_lv.setScrollBarStyle(ListView.SCROLLBARS_INSIDE_INSET);
        fileList_lv.setHeaderDividersEnabled(true);
        fileList_lv.setOnItemClickListener(this);
        
        findViewById(R.id.toobar_up).setOnClickListener(this);
    }
    
    public void setCurrentDir(File currentDir) {
        fileList.clear();
        File[] list = (this.currentDir = currentDir).listFiles();
        if (list != null)
        {
            for (File file : list)
            {
                fileList.add(new FileInfo(file));
            }
            
            Collections.sort(fileList);
        }
    }
    
    public void notifyCurrentDirChanged()
    {
        path_tv.setText(currentDir.getPath());
        adapter.notifyDataSetChanged();
    }
    
    private class FileAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return fileList.size();
        }

        @Override
        public Object getItem(int position) {
            return fileList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.fileitem, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.size = (TextView) convertView.findViewById(R.id.size);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            
            if (holder != null)
            {
                FileInfo fileInfo = fileList.get(position);
                holder.icon.setImageBitmap(getFileIcon(fileInfo));
                holder.name.setText(fileInfo.getName());
                holder.size.setText(fileInfo.isDirectory() ? "" : fileInfo.size());
                holder.date.setText(fileInfo.date());
            }
            
            return convertView;
        }
        
        private ImageCache<Integer> imageCache = new ImageCache<Integer>();                      //图片缓存
        
        private Bitmap getFileIcon(FileInfo fileInfo)
        {
            int type = fileInfo.getType();
            if (type == FileInfo.APK)
            {
                Drawable d = fileInfo.getAPKIcon(getBaseContext());
                if (d != null)
                {
                    return ImageUtil.drawable2Bitmap(d);
                }
            }
            
            Bitmap icon = imageCache.get(type);
            if (icon == null)
            {
                String fileName = "filebrowser/";
                switch (type) {
                case FileInfo.DIRECTORY:
                    fileName += "directory.png";
                    break;
                case FileInfo.TEXT:
                    fileName += "text.png";
                    break;
                case FileInfo.HTML:
                    fileName += "html.png";
                    break;
                case FileInfo.MOVIE:
                    fileName += "movie.png";
                    break;
                case FileInfo.MUSIC:
                    fileName += "music.png";
                    break;
                case FileInfo.PHOTO:
                    fileName += "photo.png";
                    break;
                case FileInfo.APK:
                    fileName += "apk.png";
                    break;
                case FileInfo.ZIP:
                    fileName += "zip.png";
                    break;
                case FileInfo.UNKNOWN:
                    fileName += "unknown.png";
                    break;
                default:
                    return null;
                }
                
                try {
                    icon = BitmapFactory.decodeStream(getAssets().open(fileName));
                    if (icon != null)
                    {
                        imageCache.put(type, icon);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            return icon;
        }
        
        private class ViewHolder {
            
            ImageView icon;
            TextView name;
            TextView size;
            TextView date;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        FileInfo fileInfo = fileList.get(position);
        if (fileInfo.isDirectory())
        {
            setCurrentDir(fileInfo);
            notifyCurrentDirChanged();
        }
        else
        {
            try {
                if (!fm.openFile(fileInfo))
                {
                    ApplicationManager.showMessage("没找到适合打开这个文件的程序");
                }
            } catch (ActivityNotFoundException e) {
                ApplicationManager.showMessage("打开文件出错");
            } catch (FileNotFoundException e) {
                ApplicationManager.showMessage("无效文件");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.toobar_up:
            upLevel();
            
            break;
        }
    }
    
    @Override
    public void onBackPressed()
    {
        upLevel();
    }
    
    private void upLevel()
    {
        File parent = currentDir.getParentFile();
        if (parent != null)
        {
            setCurrentDir(parent);
            notifyCurrentDirChanged();
        }
        else
        {
            super.onBackPressed();
        }
    }
}