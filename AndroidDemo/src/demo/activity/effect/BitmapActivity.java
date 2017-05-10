package demo.activity.effect;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import demo.android.R;
import engine.android.util.AndroidUtil;
import engine.android.util.image.ImageUtil;
import engine.android.util.image.ImageUtil.ImageDecoder;

import java.util.LinkedList;
import java.util.List;

public class BitmapActivity extends Activity {
    
    private static final int imageResource = R.drawable.image;
    
    boolean flag;
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        showMain();
    }
    
    private void showMain() {
        setContentView(R.layout.main);
        
        final ListView lv = (ListView) findViewById(R.id.list);
        List<String> list = new LinkedList<String>();
        list.add("图片圆角");
        list.add("图片倒影");
        list.add("旋转图片");
        list.add("图片反转");
        list.add("灰度效果");
        list.add("粉笔效果");
        list.add("滤镜效果");
        list.add("底片效果");
        list.add("线条效果");
        list.add("亮度对比度");
        list.add("怀旧效果");
        list.add("光照效果");
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
        lv.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                bitmap1(arg2);
            }});
    }
    
    private void bitmap1(int position) {
        flag = true;
        setContentView(R.layout.bitmap1);
        final ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
        final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        
        int resourceId = imageResource;
        if (position == 0)
        {
            resourceId = R.drawable.img0200;
        }
        
        DisplayMetrics dm = AndroidUtil.getResolution(this);
        int width = dm.widthPixels * 3 / 4;
        int height = width * 2 / 3;
        
        final Bitmap image = ImageDecoder.decodeResource(getResources(), resourceId, width, height, true);
        imageView1.setImageBitmap(image);
        
        switch (position) {
            case 0:
                //图片圆角
                imageView2.setImageBitmap(ImageUtil.getRoundImage(image, 10));
                break;
            case 1:
                //图片倒影
                imageView2.setImageBitmap(ImageUtil.getReflectedImage(image));
                break;
            case 2:
                //旋转图片
                imageView2.setImageBitmap(ImageUtil.rotate(image, 90));
                break;
            case 3:
                //图片反转
                imageView2.setImageBitmap(ImageUtil.mirror(image, true));
                break;
            case 4:
                //灰度效果
                imageView2.setImageBitmap(ImageUtil.getGrayImage(image));
                break;
            case 5:
                //粉笔效果
                imageView2.setImageBitmap(ImageUtil.crayon(image));
                break;
            case 6:
                //滤镜效果
                imageView2.setImageBitmap(ImageUtil.filter(image));
                break;
            case 7:
                //底片效果
                imageView2.setImageBitmap(ImageUtil.negative(image));
                break;
            case 8:
                //线条效果
                imageView2.setImageBitmap(ImageUtil.lines(image));
                break;
            case 9:
                //亮度对比度
                imageView2.setImageBitmap(ImageUtil.changeLight(image, 0.2, 0));
                break;
            case 10:
                //怀旧效果
                imageView2.setImageBitmap(ImageUtil.remember(image));
                break;
            case 11:
                //光照效果
                imageView2.setImageBitmap(ImageUtil.sunshine(image, image.getWidth() / 2, image.getHeight() / 2));
                break;

            default:
                break;
        }
    }
    
    @Override
    public void onBackPressed() {
        if (flag)
        {
            flag = false;
            showMain();
        }
        else
        {
            super.onBackPressed();
        }
    }
}