package demo.widget;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 自定义文本视图（修正文本显示换行不均匀）
 * @author Daimon
 * @version 3.0
 * @since 12/15/2013
 */

public class MyTextView extends TextView {
	
	private String[] textShow;
	
	private int textWidth, textHeight;

	public MyTextView(Context context) {
		super(context);
	}

	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	
    	if (TextUtils.isEmpty(getText()))
    	{
    		return;
    	}
    
    	int width = getMeasuredWidth();
    	int height = getMeasuredHeight();
    	if (textShow == null)
    	{
    		if (textWidth <= 0)
    		{
    			textWidth = width - getCompoundPaddingLeft() - getCompoundPaddingRight();
    			if (textWidth <= 0)
    			{
    				return;
    			}
    		}
    		
    		count(getText().toString());
    		setMeasuredDimension(width, textHeight);
    	}
    	else if (height != textHeight)
    	{
    		setMeasuredDimension(width, textHeight);
    	}
    }

    @Override
	protected void onDraw(Canvas canvas) {
		if (textShow != null)
		{
		    TextPaint paint = getPaint();
		    int color = paint.getColor();
		    paint.setColor(getCurrentTextColor());
			
			int ypos = getLineHeight();
			int x = getCompoundPaddingLeft();
			int y = getExtendedPaddingTop() + ypos - 2;
			
			for (int i = 0; i < textShow.length; i++)
			{
				canvas.drawText(textShow[i], x, y, paint);
				y += ypos;
			}
			
			paint.setColor(color);
		}
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		if (!TextUtils.isEmpty(text) && textWidth > 0)
		{
			count(text.toString());
		}
		else
		{
			textShow = null;
		}
	}
	
	private void count(String text)
	{
		textShow = split(text, textWidth, getPaint());
		textHeight = getExtendedPaddingTop() + textShow.length * getLineHeight() + getExtendedPaddingBottom() + 3;
	}
	
	/**
	 * 自动分割文本
	 * @param text 需要分割的文本
	 * @param width 可显示宽度
	 * @param paint 画笔，用来根据字体测量宽度用
	 * @return 分割好的字符串数组
	 */
	
	private static String[] split(String text, float width, Paint paint)
	{
		float count = 0;
		List<String> list = new LinkedList<String>();
		int len = text.length();
		char c;
		for (int i = 0; i < len;)
		{
			c = text.charAt(i);
			if (c == '\n')
			{
				list.add(text.substring(0, i));
				text = text.substring(i + 1);
				len = text.length();
				i = 0;
			}
			else if (c == '\r')
			{
				c = text.charAt(i + 1);
				if (c == '\n')
				{
					list.add(text.substring(0, i));
					text = text.substring(i + 2);
					len = text.length();
					i = 0;
				}
				else
				{
					text = text.substring(0, i) + text.substring(i + 1);
				}
			}
			else
			{
				if (width > 0)
				{
					count = paint.measureText(text, 0, i + 1);
					if (count > width)
					{
						list.add(text.substring(0, i));
						text = text.substring(i);
						len = text.length();
						i = 0;
						continue;
					}
				}
				
				i++;
			}
		}
		
		list.add(text);
		return list.toArray(new String[list.size()]);
	}
}