package engine.android.util.manager;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

/**
 * 音效播放器（超过大约5.6秒的声音就播放不出来）
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public class SoundPlayer {

    private final Context context;

    private SoundPool pool;                 // 声音池

    private SparseIntArray map;             // 声音查询表

    private AudioManager am;                // 音频管理器

    private int maxVolume;                  // 设备最大音量

    private int volume;                     // 设备音量

    public SoundPlayer(Context context) {
        this.context = context.getApplicationContext();
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        // 第一个参数为允许同时播放的最大声音数量
        pool = new SoundPool(100, AudioManager.STREAM_MUSIC, 100);
        map = new SparseIntArray();

        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 加载声音资源
     * 
     * @param id 声音标识
     * @param raw 声音文件引用
     */
    public void load(int id, int raw) {
        map.put(id, pool.load(context, raw, 1));
    }

    /**
     * 播放声音
     * 
     * @param id 声音标识
     * @param loop 循环播放次数（0为不循环，-1为无限循环）
     */
    public void play(int id, int loop) {
        pool.play(map.get(id), volume, volume, 1, loop, 1);
    }

    /**
     * 增大音量
     */
    public void increaseVolume(int value) {
        if (volume >= maxVolume)
        {
            return;
        }

        volume += value;
        if (volume > maxVolume)
        {
            volume = maxVolume;
        }

        setVolume();
    }

    /**
     * 减小音量
     */
    public void decreaseVolume(int value) {
        if (volume <= 0)
        {
            return;
        }

        volume -= value;
        if (volume < 0)
        {
            volume = 0;
        }

        setVolume();
    }

    public int getVolume() {
        return volume;
    }

    /**
     * 设置音量
     */
    private void setVolume() {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
    }

    public int getMaxVolume() {
        return maxVolume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
        setVolume();
    }
}