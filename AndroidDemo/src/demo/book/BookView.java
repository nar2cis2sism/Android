package demo.book;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import demo.book.BookAnimation.BookAnimationType;
import demo.book.BookAnimation.Direction;
import demo.book.BookImage.PageIndex;

/**
 * 书页界面
 * @author yanhao
 * @version 1.0
 */

public class BookView extends View {
	
	private Paint paint = new Paint();
	
	private BookImageManager manager = new BookImageManager(this);
	
	private BookImage bookImage;
	
	private boolean isTouch;
	private int pressedX,pressedY;
	private boolean isScroll;
	
	/***** 设置 *****/
	
	private boolean isHorizontal = true;
	
	private int speed = 4;
	
	private BookAnimationType type = BookAnimationType.curl;
	
	private boolean isClickAutoScroll = true;

	public BookView(Context context) {
		super(context);
		setFocusableInTouchMode(true);
	}
	
	/**
	 * 设置书页内容
	 * @param bookImage
	 */
	
	public void setBookImage(BookImage bookImage) {
		this.bookImage = bookImage;
	}
	
	/**
	 * 设置横向翻页/竖向翻页
	 * @param isHorizontal
	 */
	
	public void setHorizontal(boolean isHorizontal) {
		this.isHorizontal = isHorizontal;
	}
	
	/**
	 * 设置翻页速率[1-10]
	 * @param speed
	 */
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	/**
	 * 设置翻页动画类型
	 * @param type
	 */
	
	public void setBookAnimationType(BookAnimationType type) {
		this.type = type;
	}
	
	/**
	 * 设置点击自动翻页
	 * @param isClickAutoScroll
	 */
	
	public void setClickAutoScroll(boolean isClickAutoScroll) {
		this.isClickAutoScroll = isClickAutoScroll;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		manager.getBookAnimation().stop();
		if (isTouch)
		{
			isTouch = false;
			bookImage.onScrollingFinished(PageIndex.current);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (manager.getBookAnimation().isScrolling())
		{
			onDrawInScrolling(canvas);
		}
		else
		{
			onDrawStatic(canvas);
		}
	}
	
	private void onDrawInScrolling(Canvas canvas)
	{
		BookAnimation anim = manager.getBookAnimation();
		BookAnimation.Mode mode = anim.getMode();
		
		anim.onAnimatedScrolling();
		if (anim.isScrolling())
		{
			anim.draw(canvas);
			if (anim.getMode().auto)
			{
				postInvalidate();
			}
		}
		else
		{
			switch (mode) {
			case AnimatedScrollingForward:
				PageIndex index = anim.getPageIndex();
				manager.shift(index == PageIndex.next);
				bookImage.onScrollingFinished(index);
				break;
			case AnimatedScrollingBackward:
				bookImage.onScrollingFinished(PageIndex.current);
				break;

			default:
				break;
			}
			
			onDrawStatic(canvas);
		}
	}
	
	private void onDrawStatic(Canvas canvas)
	{
		manager.setSize(getWidth(), getHeight());
		canvas.drawBitmap(manager.getBookImage(PageIndex.current), 0, 0, paint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (bookImage.onTouchEvent(event))
		{
			return true;
		}
		
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isTouch = true;
			pressedX = x;
			pressedY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
			boolean move = Math.abs(x - pressedX) > slop || Math.abs(y - pressedY) > slop;
			if (!isScroll)
			{
				if (move)
				{
					startManualScrolling(pressedX, pressedY);
					isScroll = true;
				}
			}
			else
			{
				scrollManuallyTo(x, y);
			}
			
			break;
		case MotionEvent.ACTION_UP:
			if (!isScroll)
			{
				click(x, y);
			}
			else
			{
				startAnimatedScrolling(x, y, speed);
			}
			
			isTouch = false;
			isScroll = false;
			break;

		default:
			break;
		}

		return true;
	}
	
	/**
	 * 开始手动翻页
	 * @param x,y 起始位置
	 */

	private void startManualScrolling(int x, int y)
	{
		BookAnimation anim = manager.getBookAnimation();
		anim.setup(isHorizontal ? Direction.rightToLeft : Direction.up, getWidth(), getHeight());
		anim.startManualScrolling(x, y);
	}
	
	/**
	 * 手动翻页到指定位置
	 * @param x,y
	 */

	private void scrollManuallyTo(int x, int y)
	{
		BookAnimation anim = manager.getBookAnimation();
		if (bookImage.onScrollingEnabled(anim.getPageIndex(x, y)))
		{
			anim.scrollManuallyTo(x, y);
			postInvalidate();
		}
	}
	
	/**
	 * 开始动画翻页
	 * @param x,y 起始位置
	 * @param speed 翻页速率
	 */

	private void startAnimatedScrolling(int x, int y, int speed)
	{
		BookAnimation anim = manager.getBookAnimation();
		if (!bookImage.onScrollingEnabled(anim.getPageIndex(x, y)))
		{
			anim.stop();
			return;
		}
		
		anim.startAnimatedScrolling(x, y, speed);
		postInvalidate();
	}
	
	/**
	 * 点击事件
	 * @param x,y 点击位置
	 */
	
	private void click(int x, int y)
	{
		if (!isClickAutoScroll)
		{
			return;
		}
		
		PageIndex pageIndex;
		if (isHorizontal)
		{
			pageIndex = x <= getWidth() / 2 ? PageIndex.previous : PageIndex.next;
		}
		else
		{
			pageIndex = y <= getHeight() / 2 ? PageIndex.previous : PageIndex.next;
		}
		
		startAnimatedScrolling(pageIndex, x, y, isHorizontal ? Direction.rightToLeft : Direction.up, speed);
	}

	void onDraw(Bitmap image, PageIndex pageIndex)
	{
		if (bookImage == null)
		{
			return;
		}
		
		bookImage.onDraw(new Canvas(image), pageIndex);
	}
	
	/**
	 * 动画翻页
	 * @param pageIndex
	 * @param x,y
	 * @param direction
	 * @param speed
	 */

	private void startAnimatedScrolling(PageIndex pageIndex, Integer x, Integer y, Direction direction, int speed)
	{
		if (pageIndex == PageIndex.current || !bookImage.onScrollingEnabled(pageIndex))
		{
			return;
		}
		
		BookAnimation anim = manager.getBookAnimation();
		anim.setup(direction, getWidth(), getHeight());
		anim.startAnimatedScrolling(pageIndex, x, y, speed);
		if (anim.getMode().auto)
		{
			postInvalidate();
		}
	}

	private void startAnimatedScrolling(PageIndex pageIndex, Direction direction, int speed)
	{
		startAnimatedScrolling(pageIndex, null, null, direction, speed);
	}
	
	/**
	 * 向前翻页
	 */
	
	public void goToPreviousPage()
	{
		startAnimatedScrolling(PageIndex.previous, isHorizontal ? Direction.rightToLeft : Direction.up, speed);
	}

	/**
	 * 向后翻页
	 */
	
	public void goToNextPage()
	{
		startAnimatedScrolling(PageIndex.next, isHorizontal ? Direction.rightToLeft : Direction.up, speed);
	}
	
	/**
	 * 返回翻页动画类型
	 * @return
	 */
	
	public BookAnimationType getBookAnimationType()
	{
		return type;
	}
	
	/**
	 * 重置
	 */
	
	public void reset()
	{
		manager.reset();
		postInvalidate();
	}
}