package demo.book;

import android.graphics.Bitmap;

import demo.book.BookAnimation.BookAnimationType;
import demo.book.BookImage.PageIndex;

/**
 * 书页管理器
 * @author yanhao
 * @version 1.0
 */

public class BookImageManager {
	
	private final int SIZE = 2;
	
	private final Bitmap[] bookImages = new Bitmap[SIZE];
	
	private final PageIndex[] pageIndexs = new PageIndex[SIZE];
	
	private int width, height;
	
	private BookView bookView;
	
	private BookAnimation anim;
	
	private BookAnimationType type;
	
	BookImageManager(BookView bookView) {
		this.bookView = bookView;
	}
	
	/**
	 * 设置书页尺寸
	 * @param w,h
	 */
	
	void setSize(int w, int h)
	{
		if (width != w || height != h)
		{
			width = w;
			height = h;
			for (int i = 0; i < SIZE; i++)
			{
				bookImages[i] = null;
				pageIndexs[i] = null;
			}
			
			System.gc();
			System.gc();
			System.gc();
		}
	}
	
	/**
	 * 返回书页内容拷贝
	 * @param pageIndex
	 * @return
	 */
	
	Bitmap getBookImage(PageIndex pageIndex)
	{
		for (int i = 0; i < SIZE; i++)
		{
			if (pageIndex == pageIndexs[i])
			{
				return bookImages[i];
			}
		}
		
		int index = getIndex(pageIndex);
		pageIndexs[index] = pageIndex;
		if (bookImages[index] == null)
		{
			bookImages[index] = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		}
		
		bookView.onDraw(bookImages[index], pageIndex);
		return bookImages[index];
	}
	
	private int getIndex(PageIndex pageIndex)
	{
		for (int i = 0; i < SIZE; i++)
		{
			if (pageIndexs[i] == null)
			{
				return i;
			}
		}

		for (int i = 0; i < SIZE; i++)
		{
			if (pageIndexs[i] != PageIndex.current)
			{
				return i;
			}
		}

		throw new RuntimeException("That's impossible");
	}
	
	void reset()
	{
		for (int i = 0; i < SIZE; i++)
		{
			pageIndexs[i] = null;
		}
	}
	
	/**
	 * 翻页
	 * @param forward 是否向前翻页
	 */
	
	void shift(boolean forward)
	{
		for (int i = 0; i < SIZE; i++)
		{
			if (pageIndexs[i] == null)
			{
				continue;
			}
			
			pageIndexs[i] = forward ? pageIndexs[i].getPrevious() : pageIndexs[i].getNext();
		}
	}
	
	/**
	 * 返回翻页动画
	 * @return
	 */
	
	BookAnimation getBookAnimation()
	{
		BookAnimationType type = bookView.getBookAnimationType();
		if (anim == null || this.type != type)
		{
			switch (this.type = type) {
			case none:
				anim = new NoneAnimation(this);
				break;
			case curl:
				anim = new CurlAnimation(this);
				break;
			case slide:
				anim = new SlideAnimation(this);
				break;
			case shift:
				anim = new ShiftAnimation(this);
				break;

			default:
				break;
			}
		}
		
		return anim;
	}
}