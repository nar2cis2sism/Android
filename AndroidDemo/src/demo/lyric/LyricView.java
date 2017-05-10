package demo.lyric;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 歌词视图
 * @author yanhao
 * @version 1.0
 */

public class LyricView extends TextView {
	
	private Paint normal;							//正常显示画笔
	private Paint highlight;						//高亮显示画笔
	
	private List<Lyric> LyricList;					//歌词列表
	private int index;								//歌词索引
	
	private int width,height;						//窗口大小
	private int lineHeight;							//行高
	
	public LyricView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LyricView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LyricView(Context context) {
		super(context);
		init();
	}
	
	/**
	 * 初始化
	 */
	
	private void init()
	{
		normal = new Paint(getPaint());
		normal.setColor(Color.GREEN);
		normal.setTextAlign(Paint.Align.CENTER);
		
		highlight = new Paint(normal);
		highlight.setColor(Color.YELLOW);
		
		lineHeight = getLineHeight();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w;
		height = h;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (LyricList == null || index < 0 || index > LyricList.size() - 1)
		{
			super.onDraw(canvas);
		}
		else
		{
			int y = height / 2;
			
			canvas.drawText(LyricList.get(index).getLyric(), width / 2, y, highlight);
			
			//绘制之前的歌词
			for (int i = index - 1; i >= 0; i--)
			{
				y -= lineHeight;
				canvas.drawText(LyricList.get(i).getLyric(), width / 2, y, normal);
			}
			
			y = height / 2;
			//绘制之后的歌词
			for (int i = index + 1, size = LyricList.size(); i < size; i++)
			{
				y += lineHeight;
				canvas.drawText(LyricList.get(i).getLyric(), width / 2, y, normal);
			}
		}
	}

	public void setLyricList(List<Lyric> lyricList) {
		LyricList = lyricList;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setLyricTime(long lyricTime)
	{
		if (LyricList == null || LyricList.isEmpty())
		{
			index = 0;
		}
		else
		{
			int size = LyricList.size();
			if (lyricTime < LyricList.get(0).getLyricTime())
			{
				index = 0;
			}
			else if (lyricTime >= LyricList.get(size - 1).getLyricTime())
			{
				index = size - 1;
			}
			else
			{
				for (int i = 0; i < size - 1; i++)
				{
					if (lyricTime >= LyricList.get(i).getLyricTime() && lyricTime < LyricList.get(i + 1).getLyricTime())
					{
						index = i;
					}
				}
			}
		}
	}
}