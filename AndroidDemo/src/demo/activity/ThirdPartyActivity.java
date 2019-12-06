package demo.activity;

import engine.android.util.AndroidUtil;
import engine.android.util.extra.ReflectObject;
import engine.android.util.file.FileManager;
import engine.android.util.io.IOUtil;
import engine.android.util.manager.SDCardManager;
import engine.android.util.secure.ZipUtil;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import demo.j2se.shell.ApkLoader;

import java.io.File;
import java.io.FileOutputStream;

public class ThirdPartyActivity extends ListActivity implements OnItemLongClickListener {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(this, 
                android.R.layout.simple_list_item_1, new String[]{"应用", "游戏"}));
        getListView().setOnItemLongClickListener(this);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (position == 0)
        {
            //应用
            try {
                AndroidUtil.startApp(this, "com.chat");
            } catch (Exception e) {
//                e.printStackTrace();
                //程序未安装
                try {
                    String fileName = "Chat.zip";
                    File dir = SDCardManager.openSDCardAppDir(this);
                    File zipFile = new File(dir, fileName);
                    FileManager.writeFile(zipFile, IOUtil.readStream(getAssets().open("project/" + fileName)), false);
                    
                    ZipUtil.unzip(zipFile, dir.getAbsolutePath());
                    
                    File apk = FileManager.searchFile(dir, "Chat.apk");
                    
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
                    startActivityForResult(intent, 0);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        else if (position == 1)
        {
            //游戏
            startActivity(new Intent(this, TowerDefenceActivity.class));
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0)
        {
            if (resultCode != RESULT_OK)
            {
                try {
                    String apkName = "Chat.apk";
                    
                    File odex = getDir("unshell", MODE_PRIVATE);
                    
                    File apkFile = new File(odex, apkName);
                    
                    if (!FileManager.copyFile(apkFile, 
                            FileManager.searchFile(SDCardManager.openSDCardAppDir(this), apkName)))
                    {
                        throw new Exception("Failed to copy file to:" + apkFile.getAbsolutePath());
                    }
                
                    ApkLoader apkLoader = new ApkLoader(apkFile);
                    apkLoader.configClassLoader();
                    apkLoader.configResourceLoader();

                    startActivityForResult(new Intent().setComponent(
                            new ComponentName(this, "com.chat.MainActivity")), 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            return;
        }
        else if (requestCode == 1)
        {
            try {
                String apkName = "Chat.apk";
                
                File odex = getDir("unshell", MODE_PRIVATE);
                
                File apkFile = new File(odex, apkName);
            
                ApkLoader apkLoader = new ApkLoader(apkFile);
                apkLoader.restoreResourceLoader(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        
            return;
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        if (position == 0)
        {
            startActivity(new Intent(Intent.ACTION_DELETE, 
                    Uri.fromParts("package", "com.chat", null)));
            return true;
        }
        
        return false;
    }
    
    public static class TowerDefenceActivity extends Activity {
        
        private static final int MENU_start = 1;
        private static final int MENU_next  = 2;
        private static final int MENU_pause = 3;
        private static final int MENU_exit  = 4;
        
        private ReflectObject game;
        private Resources gameRes;
        
        private boolean isPause;
        
        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            try {
                String fileName = "TowerDefence.zip";
                File dir = getFilesDir();
                File zipFile = new File(dir, fileName);
                FileManager.createFileIfNecessary(zipFile);
                FileOutputStream fos = new FileOutputStream(zipFile);
                IOUtil.writeStream(getAssets().open("project/" + fileName), fos);
                fos.close();
                ZipUtil.unzip(zipFile, dir.getAbsolutePath());
                
                File apk = FileManager.searchFile(dir, "TowerDefenceActivity.apk");
                
                engine.android.plugin.util.ApkLoader loader = new engine.android.plugin.util.ApkLoader(apk);
                
                Class<?> c = loader.getClassLoader().loadClass("com.tower.TowerDefenceGame");
                Object obj = c.getConstructor(Context.class, Resources.class).newInstance(this, 
                        gameRes = loader.getResources());
                setContentView((View) obj);
                game = new ReflectObject(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(0, MENU_start, 0, getString("MENU_start"));
            menu.add(0, MENU_next,  0, getString("MENU_next"));
            menu.add(0, MENU_pause, 0, getString("MENU_pause"));
            menu.add(0, MENU_exit,  0, getString("MENU_exit"));
            return true;
        }
        
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case MENU_start:
                game.invokeWithoutThrow("resetGame");
                break;
            case MENU_next:
                game.invokeWithoutThrow("nextGame");
                break;
            case MENU_pause:
                game.invokeWithoutThrow(game.getMethod("pause", boolean.class), isPause = !isPause);
                break;
            case MENU_exit:
                finish();
                break;
            }
            
            return true;
        }
        
        @Override
        protected void onDestroy() {
            if (game != null)
            {
                game.invokeWithoutThrow("exit");
            }
            
            super.onDestroy();
        }
        
        /**
         * 使用反射技术加载文本
         * @param stringName 文本名称
         * @return 如无此文本则返回Null
         */
        
        public String getString(String stringName)
        {
            int resourceId = gameRes.getIdentifier("com.tower" + ":string/" + stringName, null, null);
            if (resourceId == 0)
            {
                return null;
            }
            
            return gameRes.getString(resourceId);
        }
    }
}