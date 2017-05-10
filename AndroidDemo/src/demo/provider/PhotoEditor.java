package demo.provider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import demo.android.R;

public class PhotoEditor extends ImageView {
	
	private boolean isHavingPhoto;							//是否设置了图片

	public PhotoEditor(Context context) {
		super(context);
	}

	public PhotoEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PhotoEditor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean isHavingPhoto() {
		return isHavingPhoto;
	}

	public void setPhoto(Bitmap photo) {
		if (photo != null)
		{
			setImageBitmap(photo);
			isHavingPhoto = true;
		}
		else
		{
			setImageResource(R.drawable.editcontact_headicon);
			isHavingPhoto = false;
		}
	}
	
	public Bitmap getPhoto()
	{
		if (isHavingPhoto)
		{
			return ((BitmapDrawable) getDrawable()).getBitmap();
		}
		else
		{
			return null;
		}
	}
}