package demo.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * 自定义滚动视图（实现反弹效果）
 * @author Daimon
 * @version 3.0
 * @since 6/29/2012
 */

public class MyScrollView extends ScrollView {
	
	private View child;							//唯一的子视图
	
	private float y;							//滚动垂直坐标
	
	private Rect rect = new Rect();				//用于缓存恢复

	public MyScrollView(Context context) {
		super(context);
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onFinishInflate() {
		if (getChildCount() > 0)
		{
			child = getChildAt(0);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (child != null)
		{
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				y = ev.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				float y = ev.getY();
				int deltaY = (int) ((this.y - y) * 0.5);
				this.y = y;

				//当滚动到最上或者最下时就不会再滚动，这时移动布局
	            if (isNeedMove())
	            {
	                if (rect.isEmpty())
	                {
	                    //保存正常的布局位置
	                	rect.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());

	                }
	                
	                //移动布局
	                child.layout(child.getLeft(), child.getTop() - deltaY, 
	                		child.getRight(), child.getBottom() - deltaY);
	            }
	            
				break;
			case MotionEvent.ACTION_UP:
				reset();
				break;

			default:
				break;
			}
		}
		
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 是否需要移动布局
	 */
	
    private boolean isNeedMove()
    {
        int offset = child.getMeasuredHeight() - getHeight();
        int scrollY = getScrollY();
        if (scrollY == 0 || scrollY == offset)
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * 重置布局
     */
    
    private void reset()
    {
    	if (!rect.isEmpty())
    	{
    		//开启移动动画
            TranslateAnimation anim = new TranslateAnimation(0, 0, child.getTop(), rect.top);
            anim.setDuration(300);
            child.startAnimation(anim);
            
            //设置回到正常的布局位置
            child.layout(rect.left, rect.top, rect.right, rect.bottom);

            rect.setEmpty();
    	}
    }
}