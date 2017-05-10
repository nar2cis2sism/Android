package demo.book;

import demo.book.BookImage.PageIndex;

abstract class SimpleAnimation extends BookAnimation {

	private float mySpeedFactor;

	SimpleAnimation(BookImageManager manager) {
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
	protected void onAnimatedScrolling() {
		if (!getMode().auto)
		{
			return;
		}
		
		switch (direction) {
		case leftToRight:
			endX -= speed;
			break;
		case rightToLeft:
			endX += speed;
			break;
		case up:
			endY += speed;
			break;
		case down:
			endY -= speed;
			break;

		default:
			break;
		}
		
		int bound;
		if (getMode() == Mode.AnimatedScrollingForward)
		{
			bound = direction.isHorizontal ? width : height;
		}
		else
		{
			bound = 0;
		}
		
		if (speed > 0)
		{
			if (getScrollingShift() >= bound)
			{
				if (direction.isHorizontal)
				{
					endX = startX + bound;
				}
				else
				{
					endY = startY + bound;
				}
				
				stop();
				return;
			}
		}
		else
		{
			if (getScrollingShift() <= -bound)
			{
				if (direction.isHorizontal)
				{
					endX = startX - bound;
				}
				else
				{
					endY = startY - bound;
				}
				
				stop();
				return;
			}
		}
		
		speed *= mySpeedFactor;
	}

	@Override
	protected void setupAnimatedScrolling(Integer x, Integer y) {
		if (x == null || y == null)
		{
			if (direction.isHorizontal)
			{
				x = speed < 0 ? width: 0;
				y = 0;
			}
			else
			{
				x = 0;
				y = speed < 0 ? height: 0;
			}
		}
		
		endX = startX = x;
		endY = startY = y;
	}

	@Override
	protected void startAnimatedScrolling(int speed) {
		mySpeedFactor = (float)Math.pow(1.5, 0.25 * speed);
		onAnimatedScrolling();
	}
	
	/**
	 * 返回翻页偏移量
	 * @return
	 */
	
	private int getScrollingShift()
	{
		return direction.isHorizontal ? endX - startX : endY - startY;
	}
}