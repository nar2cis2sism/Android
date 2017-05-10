package demo.book;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

class SlideAnimation extends SimpleAnimation {
	
	private final Paint myPaint = new Paint();

	SlideAnimation(BookImageManager manager) {
		super(manager);
	}

	@Override
	protected void onAnimatedDraw(Canvas canvas) {
		canvas.drawBitmap(getBookImageTo(), 0, 0, myPaint);
		myPaint.setColor(Color.rgb(127, 127, 127));
		if (direction.isHorizontal)
		{
			int dX = endX - startX;
			canvas.drawBitmap(getBookImageFrom(), dX, 0, myPaint);
			if (dX > 0 && dX < width)
			{
				canvas.drawLine(dX, 0, dX, height + 1, myPaint);
			}
			else if (dX < 0 && dX > -width)
			{
				canvas.drawLine(dX + width, 0, dX + width, height + 1, myPaint);
			}
		}
		else
		{
			int dY = endY - startY;
			canvas.drawBitmap(getBookImageFrom(), 0, dY, myPaint);
			if (dY > 0 && dY < height)
			{
				canvas.drawLine(0, dY, width + 1, dY, myPaint);
			}
			else if (dY < 0 && dY > -height)
			{
				canvas.drawLine(0, dY + height, width + 1, dY + height, myPaint);
			}
		}
	}
}