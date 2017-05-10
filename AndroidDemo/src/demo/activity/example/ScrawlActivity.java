package demo.activity.example;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import demo.android.R;
import demo.widget.ColorPickerView;
import demo.widget.NotePad;
import demo.widget.ColorPickerView.OnColorPickerListener;
import demo.widget.NotePad.OnNotePadListener;

import java.util.LinkedList;
import java.util.List;

public class ScrawlActivity extends Activity implements OnNotePadListener {
    
    List<Bitmap> list = new LinkedList<Bitmap>();
    
    GridView gv;
    GridAdapter adapter;
    
    Paint paint;
    MaskFilter emboss;
    MaskFilter blur;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrawl);
        
        gv = (GridView) findViewById(R.id.gv);
        gv.setAdapter(adapter = new GridAdapter());
        
        NotePad np = (NotePad) findViewById(R.id.np);
        np.setOnNotePadListener(this);
        
        Button delete = (Button) findViewById(R.id.delete);
        delete.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (!list.isEmpty())
                {
                    list.remove(list.size() - 1);
                    refresh();
                }
            }
        });

        paint = np.getNotePaint();
        emboss = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 6, 3.5f);
        blur = new BlurMaskFilter(8, Blur.NORMAL);
    }

    @Override
    public void onNoteToBitmap(Bitmap note) {
        list.add(note);
        refresh();
    }
    
    private void refresh()
    {
        adapter.notifyDataSetChanged();
    }
    
    private class GridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size() + 1;
        }

        @Override
        public Bitmap getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv;
            if (convertView == null)
            {
                convertView = iv = new ImageView(getBaseContext());
                iv.setBackgroundResource(R.drawable.scrawl_grid_bg);
                iv.setLayoutParams(new GridView.LayoutParams(100, 100));
                iv.setScaleType(ScaleType.FIT_CENTER);
            }
            else
            {
                iv = (ImageView) convertView;
            }
            
            if (position < list.size())
            {
                iv.setImageBitmap(list.get(position));
            }
            else
            {
                iv.setImageResource(R.drawable.scrawl_grid_edit);
            }
            
            return convertView;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "颜色选取");
        menu.add(0, 2, 0, "浮雕效果");
        menu.add(0, 3, 0, "模糊效果");
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case 1:
            //颜色选取
            new ColorPickerDialog(this).show();
            break;
        case 2:
            //浮雕效果
            if (paint.getMaskFilter() != emboss)
            {
                paint.setMaskFilter(emboss);
            }
            else
            {
                paint.setMaskFilter(null);
            }
            
            break;
        case 3:
            //模糊效果
            if (paint.getMaskFilter() != blur)
            {
                paint.setMaskFilter(blur);
            }
            else
            {
                paint.setMaskFilter(null);
            }
            
            break;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    class ColorPickerDialog extends Dialog implements OnColorPickerListener {

        public ColorPickerDialog(Context context) {
            super(context);
        }
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            ColorPickerView cpv = new ColorPickerView(getContext());
            cpv.setInitialColor(paint.getColor());
            cpv.setOnColorPickerListener(this);
            
            setTitle("Pick a Color");
            setContentView(cpv, new LayoutParams(250, 250));
        }

        @Override
        public void onColorPicked(int color) {
            paint.setColor(color);
            dismiss();
        }

        @Override
        public void onColorChanged(int color) {
            // TODO Auto-generated method stub
            
        }
    }
}