package demo.activity.effect;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import demo.android.R;
import demo.widget.ImageZoomView;
import demo.widget.ImageZoomView.SimpleZoomListener;
import demo.widget.ImageZoomView.ZoomState;

public class ImageZoomActivity extends Activity {

    private static final int MENU_RESET = 0;
	
	ZoomState state;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ImageZoomView view = new ImageZoomView(this);
		view.setBackgroundResource(R.drawable.img0230);
		view.setZoomImage(BitmapFactory.decodeResource(getResources(), R.drawable.zoom_image));
		view.setOnTouchListener(new SimpleZoomListener(state = view.getZoomState()));
		setContentView(view);
		
		resetZoomState();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_RESET, 0, "Reset");
        
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_RESET:
			resetZoomState();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Reset zoom state and notify observers
	 */
	
	private void resetZoomState()
	{
		state.setPanX(0.5f);
		state.setPanY(0.5f);
		state.setZoom(1.0f);
		state.notifyDataChanged();
	}
}