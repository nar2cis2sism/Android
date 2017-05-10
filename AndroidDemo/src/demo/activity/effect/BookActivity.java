package demo.activity.effect;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import demo.android.R;
import demo.book.BookImage;
import demo.book.BookView;
import demo.book.BookAnimation.BookAnimationType;

public class BookActivity extends Activity {
	
	BookView view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        view = new BookView(this);
        view.setBookImage(new Book());
        setContentView(view);
        
        view.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN)
				{
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
					{
						view.goToPreviousPage();
						return true;
					}
					else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					{
						view.goToNextPage();
						return true;
					}
				}
				
				return false;
			}});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "none");
		menu.add(0, 1, 0, "curl");
		menu.add(0, 2, 0, "slide");
		menu.add(0, 3, 0, "shift");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			view.setBookAnimationType(BookAnimationType.none);
			break;
		case 1:
			view.setBookAnimationType(BookAnimationType.curl);
			break;
		case 2:
			view.setBookAnimationType(BookAnimationType.slide);
			break;
		case 3:
			view.setBookAnimationType(BookAnimationType.shift);
			break;

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
    
    class Book implements BookImage {
    	
    	final int[] resource = {R.drawable.img0001, R.drawable.img0030, R.drawable.img0100};
    	
    	final Bitmap[] images = new Bitmap[resource.length];
    	
    	int index;

		@Override
		public void onDraw(Canvas canvas, PageIndex pageIndex) {
			int index = this.index;
			switch (pageIndex) {
			case previous:
				index--;
				break;
			case next:
				index++;
				break;

			default:
				break;
			}
			
			canvas.drawBitmap(getImage(index), 0, 0, null);
		}
		
		private Bitmap getImage(int index)
		{
			if (images[index] == null)
			{
				images[index] = BitmapFactory.decodeResource(getResources(), resource[index]);
			}
			
			return images[index];
		}

		@Override
		public boolean onScrollingEnabled(PageIndex pageIndex) {
			if (pageIndex == PageIndex.previous && index == 0)
			{
				return false;
			}
			else if (pageIndex == PageIndex.next && index == images.length - 1)
			{
				return false;
			}
			else
			{
				return true;
			}
		}

		@Override
		public void onScrollingFinished(PageIndex pageIndex) {
			switch (pageIndex) {
			case previous:
				index--;
				break;
			case next:
				index++;
				break;

			default:
				break;
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// TODO Auto-generated method stub
			return false;
		}
    }
}