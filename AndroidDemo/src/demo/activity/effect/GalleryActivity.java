package demo.activity.effect;

import android.app.Activity;
import android.os.Bundle;

import demo.android.R;
import demo.widget.MyGallery;
import demo.widget.MyGallery.ImageAdapter;

public class GalleryActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		
		int[] images = {
				R.drawable.img0001, R.drawable.img0030, R.drawable.img0100, 
                R.drawable.img0130, R.drawable.img0200, R.drawable.img0230, 
                R.drawable.img0300, R.drawable.img0330, R.drawable.img0354};
        
        ImageAdapter adapter = new ImageAdapter(this, images);

        MyGallery gallery = (MyGallery) findViewById(R.id.gallery);
        gallery.setAdapter(adapter);
	}
}