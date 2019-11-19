package engine.android.util.os;

import engine.android.core.util.LogFactory.LOG;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.File;

/**
 * 音频功能辅助类<p>
 * 需要声明权限<uses-permission android:name="android.permission.RECORD_AUDIO" />
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class AudioUtil {
    
    /**
     * 返回一个录音封装接口
     */
    public static AudioRecorder record() {
        return new engine.android.util.os.AudioRecorder();
    }
    
    /**
     * 返回一个语音播放接口
     */
    public static AudioPlayer play() {
        return new engine.android.util.os.AudioPlayer();
    }
    
    public interface AudioRecorder {

        /**
         * 开始录音
         */
        void start();

        /**
         * 停止录音
         * 
         * @return 录音文件，如为Null表示录音失败
         */
        File stop();
    }
    
    public interface AudioPlayer {
        
        /**
         * 开始播放
         * 
         * @param file 音频文件
         */
        MediaPlayer start(File file);
        
        /**
         * 停止播放
         */
        void stop();
        
        /**
         * 是否正在播放
         * 
         * @param file 音频文件
         */
        boolean isPlaying(File file);
    }
}

class AudioRecorder implements AudioUtil.AudioRecorder {
    
    private MediaRecorder mediaRecorder;
    
    private File recordFile;

    @Override
    public void start() {
        if (mediaRecorder != null)
        {
            // 正在录音
            return;
        }

        MediaRecorder r = mediaRecorder = new MediaRecorder();
        try {
            // 创建临时存储文件
            recordFile = File.createTempFile("record", null);
            // 初始化录音机
            r.setAudioSource(MediaRecorder.AudioSource.MIC);
            r.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            r.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            r.setOutputFile(recordFile.getAbsolutePath());
            r.prepare();
            r.start();
        } catch (Exception e) {
            LOG.log(e);
            if (recordFile != null)
            {
                recordFile.delete();
                recordFile = null;
            }
        } finally {
            if (recordFile != null) recordFile.deleteOnExit();
        }
    }

    @Override
    public File stop() {
        if (mediaRecorder != null)
        {
            try {
                mediaRecorder.stop();
            } catch (Exception e) {
                // 录音时间太短
                if (recordFile != null)
                {
                    recordFile.delete();
                    recordFile = null;
                }
            }
            
            mediaRecorder.release();
            mediaRecorder = null;
            return recordFile;
        }
        
        return null;
    }
}

class AudioPlayer implements AudioUtil.AudioPlayer {
    
    private MediaPlayer mediaPlayer;
    
    private File playFile;

    @Override
    public MediaPlayer start(File file) {
        if (file == null)
        {
            if (mediaPlayer != null) release();
            return null;
        }
        
        if (file.equals(playFile))
        {
            if (!mediaPlayer.isPlaying())
            {
                mediaPlayer.start();
            }
        }
        else
        {
            if (mediaPlayer == null)
            {
                mediaPlayer = new MediaPlayer();
            }
            else
            {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.stop();
                }
                
                mediaPlayer.reset();
            }
            
            try {
                mediaPlayer.setDataSource((playFile = file).getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                LOG.log(e);
                release();
            }
        }
        
        return mediaPlayer;
    }
    
    private void release() {
        mediaPlayer.release();
        mediaPlayer = null;
        playFile = null;
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) mediaPlayer.stop();
    }

    @Override
    public boolean isPlaying(File file) {
        if (mediaPlayer == null) return false;
        return (file == null || file.equals(playFile)) && mediaPlayer.isPlaying();
    }
}