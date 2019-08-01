package engine.android.framework.ui.presenter;

import android.content.Context;
import android.media.MediaPlayer;

import engine.android.core.BaseFragment;
import engine.android.core.BaseFragment.Presenter;

/**
 * 背景音乐管理
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class AudioPresenter extends Presenter<BaseFragment> {
    
    private final int rawRes;
    
    private MediaPlayer mediaPlayer;
    
    public AudioPresenter(int rawRes) {
        this.rawRes = rawRes;
    }
    
    public AudioPresenter(MediaPlayer mediaPlayer) {
        this(0);
        this.mediaPlayer = mediaPlayer;
    }
    
    @Override
    protected void onCreate(Context context) {
        if (rawRes != 0)
        {
            mediaPlayer = MediaPlayer.create(context, rawRes);
            mediaPlayer.setLooping(true);
        }
    }
    
    @Override
    protected void onStart() {
        mediaPlayer.start();
    }
    
    @Override
    protected void onStop() {
        mediaPlayer.pause();
    }
    
    @Override
    protected void onDestroy() {
        if (rawRes != 0)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
    
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}