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

    private SparseIntArray rawMap;          // 资源查询表

    private SparseIntArray soundMap;        // 声音查询表

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
        rawMap = new SparseIntArray();
        soundMap = new SparseIntArray();

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
        rawMap.append(id, raw);
        soundMap.append(id, pool.load(context, raw, 1));
    }
    
    /**
     * 卸载声音资源
     * 
     * @param id 声音标识
     */
    public void unload(int id) {
        rawMap.delete(id);
        int index = soundMap.indexOfKey(id);
        if (index >= 0)
        {
            pool.unload(soundMap.valueAt(index));
            soundMap.removeAt(index);
        }
    }

    /**
     * 播放声音
     * 
     * @param id 声音标识
     * @param loop 循环播放次数（0为不循环，-1为无限循环）
     * @return non-zero streamID if successful, zero if failed
     */
    public int play(int id, int loop) {
        int streamID = pool.play(soundMap.get(id), 1, 1, 1, loop, 1);
        // 需要重新加载资源，否则播放一次后就没声音了
        int raw = rawMap.get(id);
        if (raw != 0) load(id, raw);
        return streamID;
    }
    
    /**
     * 释放资源
     */
    public void release() {
        pool.release();
    }

    public final SoundPool getPool() {
        return pool;
    }

    /**
     * 控制手机媒体音量
     */
    public void setVolume(int volume) {
        this.volume = volume;
        setupVolume();
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

        setupVolume();
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

        setupVolume();
    }

    public int getVolume() {
        return volume;
    }

    public int getMaxVolume() {
        return maxVolume;
    }

    /**
     * 设置音量
     */
    private void setupVolume() {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
    }
}