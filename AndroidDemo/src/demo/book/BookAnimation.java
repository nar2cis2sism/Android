package demo.book;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import demo.book.BookImage.PageIndex;

/**
 * 翻页动画
 * @author yanhao
 * @version 1.0
 */

public abstract class BookAnimation {
	
	private BookImageManager manager;
	
	protected int width,height;
	
	protected int startX,startY,endX,endY;
	
	private Mode mode = Mode.NoScrolling;
	
	protected Direction direction;
	
	protected float speed;
	
	BookAnimation(BookImageManager manager) {
		this.manager = manager;
	}
	
	final Mode getMode()
	{
		return mode;
	}
	
	/**
	 * 停止翻页
	 */
	
	final void stop()
	{
		mode = Mode.NoScrolling;
		speed = 0;
	}
	
	/**
	 * 翻页启动设置
	 * @param direction
	 * @param width
	 * @param height
	 */
	
	final void setup(Direction direction, int width, int height)
	{
		this.direction = direction;
		this.width = width;
		this.height = height;
	}

	/**
	 * 开始手动翻页
	 * @param x,y 起始位置
	 */
	
	final void startManualScrolling(int x, int y)
	{
		if (!mode.auto)
		{
			mode = Mode.ManualScrolling;
			endX = startX = x;
			endY = startY = y;
		}
	}
	
	/**
	 * 手动翻页到指定位置
	 * @param x,y
	 */
	
	final void scrollManuallyTo(int x, int y)
	{
		if (mode == Mode.ManualScrolling)
		{
			endX = x;
			endY = y;
		}
	}
	
	/**
	 * 开始动画翻页
	 * @param x,y 起始位置
	 * @param speed 翻页速率
	 */
	
	final void startAnimatedScrolling(int x, int y, int speed)
	{
		if (mode != Mode.ManualScrolling)
		{
			return;
		}
		
		if (getPageIndex(x, y) == PageIndex.current)
		{
			return;
		}
		
		int distance = direction.isHorizontal ? x - startX : y - startY;
		int min = direction.isHorizontal ? (width > height ? width / 4 : width / 3) : (height > width ? height / 4 : height / 3);
		boolean forward = Math.abs(distance) > min;
		mode = forward ? Mode.AnimatedScrollingForward : Mode.AnimatedScrollingBackward;
		
		if (getPageIndex() == PageIndex.previous)
		{
			forward = !forward;
		}

		float velocity = 15;
		
		switch (direction) {
		case rightToLeft:
		case up:
			this.speed = forward ? -velocity : velocity;
			break;
		case leftToRight:
		case down:
			this.speed = forward ? velocity : -velocity;
			break;

		default:
			break;
		}
		
		startAnimatedScrolling(speed);
	}
	
	/**
	 * 开始动画翻页
	 * @param pageIndex
	 * @param x,y 起始位置
	 * @param speed 翻页速率
	 */
	
	final void startAnimatedScrolling(PageIndex pageIndex, Integer x, Integer y, int speed)
	{
		if (mode.auto)
		{
			return;
		}
		
		stop();
		mode = Mode.AnimatedScrollingForward;

		float velocity = 15;
		
		switch (direction) {
		case rightToLeft:
		case up:
			this.speed = pageIndex == PageIndex.next ? -velocity : velocity;
			break;
		case leftToRight:
		case down:
			this.speed = pageIndex == PageIndex.next ? velocity : -velocity;
			break;

		default:
			break;
		}
		
		setupAnimatedScrolling(x, y);
		startAnimatedScrolling(speed);
	}
	
	/**
	 * 动画翻页启动设置
	 * @param x,y 起始位置
	 */
	
	protected abstract void setupAnimatedScrolling(Integer x, Integer y);
	
	/**
	 * 开始动画翻页
	 * @param speed 翻页速率
	 */

	protected abstract void startAnimatedScrolling(int speed);
	
	/**
	 * 动画翻页实现
	 */

	protected abstract void onAnimatedScrolling();

	/**
	 * 是否正在翻页
	 * @return
	 */
	
	public final boolean isScrolling()
	{
		return mode != Mode.NoScrolling;
	}
	
	/**
	 * 动画绘制
	 * @param canvas
	 */
	
	final void draw(Canvas canvas)
	{
		manager.setSize(width, height);
		onAnimatedDraw(canvas);
	}
	
	/**
	 * 翻页动画绘制
	 * @param canvas
	 */
	
	protected abstract void onAnimatedDraw(Canvas canvas);
	
	/**
	 * 返回指定位置翻页的索引
	 * @param x,y
	 * @return
	 */

	protected abstract PageIndex getPageIndex(int x, int y);
	
	/**
	 * 返回当前位置翻页的索引
	 * @return
	 */

	final PageIndex getPageIndex()
	{
		return getPageIndex(endX, endY);
	}
	
	/**
	 * 返回当前书页内容拷贝
	 * @return
	 */
	
	protected final Bitmap getBookImageFrom()
	{
		return manager.getBookImage(PageIndex.current);
	}
	
	/**
	 * 返回翻开书页内容拷贝
	 * @return
	 */
	
	protected final Bitmap getBookImageTo()
	{
		return manager.getBookImage(getPageIndex());
	}
	
	/**
	 * 翻页模式
	 * @author yanhao
	 * @version 1.0
	 */
	
	public static enum Mode {
		
		NoScrolling(false),								//没有翻页
		ManualScrolling(false),							//手动翻页
		AnimatedScrollingForward(true),					//动画向前翻页
		AnimatedScrollingBackward(true);				//动画往回翻页
		
		public final boolean auto;						//是否自动翻页
		
		private Mode(boolean auto) {
			this.auto = auto;
		}
	}
	
	/**
	 * 翻页方向
	 * @author yanhao
	 * @version 1.0
	 */
	
	public static enum Direction {
		
		leftToRight(true), rightToLeft(true), up(false), down(false);
		
		public final boolean isHorizontal;				//是否水平方向
		
		private Direction(boolean isHorizontal) {
			this.isHorizontal = isHorizontal;
		}
	}
	
	/**
	 * 翻页动画类型
	 * @author yanhao
	 * @version 1.0
	 */
	
	public static enum BookAnimationType {
		
		none, curl, slide, shift
		
	}
}