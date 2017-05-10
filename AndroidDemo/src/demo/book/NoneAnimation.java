package demo.book;

import android.graphics.Canvas;
import android.graphics.Paint;

import demo.book.BookImage.PageIndex;

/**
 * 无翻书效果（直接翻页）
 * @author yanhao
 * @version 1.0
 */

class NoneAnimation extends BookAnimation {
	
	private final Paint myPaint = new Paint();

	NoneAnimation(BookImageManager manager) {
		super(manager);
	}

	@Override
	protected PageIndex getPageIndex(int x, int y) {
		if (direction == null)
		{
			return PageIndex.current;
		}
		
		switch (direction) {
		case leftToRight:
			return startX < x ? PageIndex.next : PageIndex.previous;
		case rightToLeft:
			return startX < x ? PageIndex.previous : PageIndex.next;
		case up:
			return startY < y ? PageIndex.previous : PageIndex.next;
		case down:
			return startY < y ? PageIndex.next : PageIndex.previous;

		default:
			return PageIndex.current;
		}
	}

	@Override
	protected void onAnimatedDraw(Canvas canvas) {
		canvas.drawBitmap(getBookImageFrom(), 0, 0, myPaint);
	}

	@Override
	protected void onAnimatedScrolling() {
		if (getMode().auto)
		{
			stop();
		}
	}

	@Override
	protected void setupAnimatedScrolling(Integer x, Integer y) {
		if (direction.isHorizontal)
		{
			startX = speed < 0 ? width : 0;
			endX = width - startX;
			endY = startY = 0;
		}
		else
		{
			endX = startX = 0;
			startY = speed < 0 ? height : 0;
			endY = height - startY;
		}
	}

	@Override
	protected void startAnimatedScrolling(int speed) {
		// TODO Auto-generated method stub
		
	}
}