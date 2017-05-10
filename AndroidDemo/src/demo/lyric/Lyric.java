package demo.lyric;

/**
 * 歌词
 * @author yanhao
 * @version 1.0
 */

public class Lyric {
	
	private String Lyric;						//歌词内容
	
	private long LyricTime;						//显示时间

	public String getLyric() {
		return Lyric;
	}

	public void setLyric(String lyric) {
		Lyric = lyric;
	}

	public long getLyricTime() {
		return LyricTime;
	}

	public void setLyricTime(long lyricTime) {
		LyricTime = lyricTime;
	}
}