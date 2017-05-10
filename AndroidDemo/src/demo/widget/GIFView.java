package demo.widget;

import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Daimon
 * @version 4.0
 * @since 5/7/2014
 */

public class GIFView extends ImageView {
	
	private Movie movie;
	private long startTime;

	public GIFView(Context context) {
		super(context);
	}

	public GIFView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GIFView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	private void init(Movie movie) {
		this.movie = movie;
		startTime = 0;
	}
	
	@Override
	public void setImageResource(int resId) {
		if (resId != getResource())
		{
			try {
				init(Movie.decodeStream(getResources().openRawResource(resId)));
			} catch (Exception e) {
				e.printStackTrace();
				movie = null;
			}
			
			super.setImageResource(resId);
		}
	}
	
	private int getResource() {
		try {
			Field field = getClass().getSuperclass().getDeclaredField("mResource");
			field.setAccessible(true);
			return field.getInt(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	@Override
	public void setImageURI(Uri uri) {
		try {
			if (uri != null)
			{
				if ("content".equals(uri.getScheme()))
				{
					init(Movie.decodeStream(getContext().getContentResolver().openInputStream(uri)));
				}
				else
				{
					init(Movie.decodeFile(uri.toString()));
				}
				
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		movie = null;
		super.setImageURI(uri);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (movie != null)
		{
			long time = SystemClock.uptimeMillis();
			if (startTime == 0)
			{
				//first time
				startTime = time;
			}
			
			int duration = movie.duration();
			if (duration == 0)
			{
				duration = 1000;
			}
			
			movie.setTime((int) ((time - startTime) % duration));
			movie.draw(canvas, 0, 0);
			invalidate();
		}
		else
		{
			super.onDraw(canvas);
		}
	}
}