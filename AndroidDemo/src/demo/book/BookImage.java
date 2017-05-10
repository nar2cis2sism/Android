package demo.book;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * 书页接口
 * @author yanhao
 * @version 1.0
 */

public interface BookImage {
	
	/**
	 * 书页绘制
	 * @param canvas
	 * @param pageIndex
	 */
	
	public void onDraw(Canvas canvas, PageIndex pageIndex);
	
	/**
	 * 触屏事件处理
	 * @see View#onTouchEvent(MotionEvent)
	 */
	
	public boolean onTouchEvent(MotionEvent event);
	
	/**
	 * 判断是否能翻页
	 * @param pageIndex
	 * @return 是否允许翻页
	 */
	
	public boolean onScrollingEnabled(PageIndex pageIndex);
	
	/**
	 * 翻页结束
	 * @param pageIndex
	 */
	
	public void onScrollingFinished(PageIndex pageIndex);
	
	/**
	 * 书页索引
	 * @author yanhao
	 * @version 1.0
	 */
	
	public static enum PageIndex {
		
		previous, current, next;
		
		/**
		 * 返回下一页（如没有则返回Null）
		 * @return
		 */
		
		PageIndex getNext()
		{
			switch (this) {
			case previous:
				return current;
			case current:
				return next;

			default:
				return null;
			}
		}
		
		/**
		 * 返回上一页（如没有则返回Null）
		 * @return
		 */
		
		PageIndex getPrevious()
		{
			switch (this) {
			case next:
				return current;
			case current:
				return previous;

			default:
				return null;
			}
		}
	}
}