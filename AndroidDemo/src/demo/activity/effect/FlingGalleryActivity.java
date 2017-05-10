package demo.activity.effect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import demo.android.R;
import demo.android.ui.ViewAnimationUtil;
import demo.android.ui.ViewAnimationUtil.AnimationType;
import demo.widget.FlingGallery;
import demo.widget.FlingLayout;
import demo.widget.FlingLayout.OnViewChangeListener;

public class FlingGalleryActivity extends Activity implements OnViewChangeListener {
	
	final String[] labels = {"View1", "View2", "View3", "View4", "View5"};
	final int[] colors = {Color.argb(100, 200, 0, 0), 
						  Color.argb(100, 0, 200, 0),
						  Color.argb(100, 0, 0, 200),
						  Color.argb(100, 200, 200, 0),
						  Color.argb(100, 200, 0, 200)};
	
	FlingGallery gallery;
	
	CheckBox check;
	
	/////////
    
    FlingLayout gallery2;
    
    ImageView page1;
    ImageView page2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		showFlingGallery1();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add("换一种实现方式");
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    showFlingGallery2();
	    item.setVisible(false);
	    return super.onOptionsItemSelected(item);
	}
	
	private void showFlingGallery1()
	{
	    gallery = new FlingGallery(this);
        gallery.setAdapter(new FlingGalleryAdapter());
        gallery.setPaddingWidth(5);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, 
                LinearLayout.LayoutParams.FILL_PARENT);
        params.weight = 1;
        params.setMargins(10, 10, 10, 10);
        
        layout.addView(gallery, params);
        
        check = new CheckBox(this);
        check.setText("Gallery is Circular");
        check.setTextSize(30);
        check.setChecked(true);
        check.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                gallery.setGalleryCircular(isChecked);
            }});
        
        layout.addView(check);
        
        setContentView(layout);
	}
    
    private void showFlingGallery2()
    {
        gallery2 = new FlingLayout(this);
        gallery2.setOnViewChangeListener(this);
        
        View view1 = new View(this);
        view1.setBackgroundResource(R.drawable.fling_gallery_bg);
        gallery2.addView(view1, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        View view2 = new View(this);
        view2.setBackgroundResource(R.drawable.fling_gallery_bg);
        gallery2.addView(view2, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(gallery2, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.rightMargin = 20;
        params.bottomMargin = 20;
        
        page1 = new ImageView(this);
        page1.setImageResource(R.drawable.page1);
        layout.addView(page1, params);
        
        page2 = new ImageView(this);
        page2.setImageResource(R.drawable.page2);
        page2.setVisibility(View.GONE);
        layout.addView(page2, params);
        
        setContentView(layout);
    }
	
	class FlingGalleryAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return labels.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return new FlingGalleryItem(FlingGalleryActivity.this, position);
		}
	}
	
	class FlingGalleryItem extends LinearLayout {
		
		EditText et;
		TextView tv1;
		TextView tv2;
		Button btn1;
		Button btn2;

		public FlingGalleryItem(Context context, int position) {
			super(context);
			
			setOrientation(VERTICAL);
			setBackgroundColor(colors[position]);
			
			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			et = new EditText(context);
			addView(et, params);
			
			tv1 = new TextView(context);
			tv1.setText(labels[position]);
			tv1.setTextSize(30);
			tv1.setGravity(Gravity.LEFT);
			addView(tv1, params);
			
			btn1 = new Button(context);
			btn1.setText("<<");
			btn1.setGravity(Gravity.LEFT);
			btn1.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					gallery.movePrevious();
				}});
			addView(btn1, params);
			
			btn2 = new Button(context);
			btn2.setText(">>");
			btn2.setGravity(Gravity.RIGHT);
			btn2.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					gallery.moveNext();
				}});
			addView(btn2, params);
			
			tv2 = new TextView(context);
			tv2.setText(labels[position]);
			tv2.setTextSize(30);
			tv2.setGravity(Gravity.RIGHT);
			addView(tv2, params);
		}
	}

    @Override
    public void OnViewChanged(int childIndex) {
        if (childIndex == 0)
        {
            ViewAnimationUtil.startAnimation(AnimationType.ANIMATION_ROTATE_UP, page1, page2, 500);
        }
        else
        {
            ViewAnimationUtil.startAnimation(AnimationType.ANIMATION_ROTATE_DOWN, page2, page1, 500);
        }
    }
}