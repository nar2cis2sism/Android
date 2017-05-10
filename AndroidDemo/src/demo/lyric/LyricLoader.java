package demo.lyric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 歌词加载器
 * @author yanhao
 * @version 1.0
 */

public class LyricLoader {
	
	private String title;													//歌曲名
	
	private String artist;													//歌手名
	
	private String album;													//歌曲被收录的专辑
	
	private String author;													//LRC歌词文件的制作者
	
	private String offset;													//时间补偿值（正值表示整体提前，负值相反）
	
	private List<Lyric> LyricList = new ArrayList<Lyric>();					//歌词列表
	
	/**
	 * 加载歌词
	 * @param is
	 * @throws IOException
	 */
	
	public void load(InputStream is) throws IOException
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			String s;
			while ((s = br.readLine()) != null)
			{
				s = s.replace("[", "");
				String[] strs = s.split("]");
				if (strs.length == 1)
				{
					if (isLyricTime(strs[0]))
					{
						Lyric lrc = new Lyric();
						lrc.setLyricTime(getLyricTime(strs[0]));
						lrc.setLyric("");
						LyricList.add(lrc);
					}
					else
					{
						strs = strs[0].split(":");
						if (strs.length == 2)
						{
							setProperty(strs[0], strs[1]);
						}
					}
				}
				else if (strs.length > 1)
				{
					Lyric lrc = new Lyric();
					lrc.setLyricTime(getLyricTime(strs[0]));
					lrc.setLyric(strs[strs.length - 1]);
					LyricList.add(lrc);
				}
			}
		} finally {
			if (br != null)
			{
				br.close();
			}
		}
	}
	
	/**
	 * 计算显示时间
	 * @param LyricTime
	 * @return -1为读取时间出错
	 */
	
	private long getLyricTime(String LyricTime)
	{
		LyricTime = LyricTime.replace(".", ":");
		String[] strs = LyricTime.split(":");
		long millisecond = -1;
		if (strs.length > 1)
		{
			int minute = Integer.parseInt(strs[0]);
			int second = Integer.parseInt(strs[1]);
			millisecond = (minute * 60 + second) * 1000;
		}
		
		if (strs.length > 2)
		{
			millisecond += Integer.parseInt(strs[2]) * 10;
		}
    	
		return millisecond;
	}
	
	/**
	 * 判断是否歌词时间
	 * @param LyricTime
	 * @return
	 */
	
	private boolean isLyricTime(String LyricTime)
	{
		return LyricTime.matches("^\\d{2}:\\d{2}([:.]\\d{2})?$");
	}
	
	/**
	 * 设置属性
	 * @param name
	 * @param value
	 */
	
	private void setProperty(String name, String value)
	{
		if ("ti".equals(name))
		{
			title = value;
		}
		else if ("ar".equals(name))
		{
			artist = value;
		}
		else if ("al".equals(name))
		{
			album = value;
		}
		else if ("by".equals(name))
		{
			author = value;
		}
		else if ("offset".equals(name))
		{
			offset = value;
		}
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public String getAuthor() {
		return author;
	}

	public String getOffset() {
		return offset;
	}

	public List<Lyric> getLyricList() {
		return LyricList;
	}
}