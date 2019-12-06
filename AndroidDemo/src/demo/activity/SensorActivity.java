package demo.activity;

import engine.android.util.extra.SafeThread;
import engine.android.util.extra.SafeThread.SafeRunnable;
import engine.android.util.manager.MySensorManager;
import engine.android.util.manager.MySensorManager.OrientationSensorListener;
import engine.android.util.manager.MySensorManager.RotateSensorListener;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import demo.activity.SensorActivity.BlowListener.BlowCallback;

import java.util.concurrent.atomic.AtomicBoolean;

public class SensorActivity extends Activity implements Runnable {
    
    MySensorManager sm;                                 //我的手机动作感应管理器
    
    TextView tv;
    
    BlowListener bl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        setContentView(tv);
        
        sm = new MySensorManager(this);
        sm.addSensorListener(new ShakeSensorListener());
        sm.addSensorListener(new CompassSensorListener());
        
        bl = new BlowListener(new BlowCallback() {
            
            @Override
            public void blow() {
                runOnUiThread(SensorActivity.this);
            }
        });
    }
    
    @Override
    public void run() {
        Toast.makeText(this, "吹了我一下！", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        sm.register();
        bl.startListen();
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        sm.unregister();
        bl.stopListen();
        super.onStop();
    }
    
    class ShakeSensorListener extends RotateSensorListener {

        @Override
        public void notifyRotate(int rotateX, int rotateY, float speed) {
            if (speed > 250)
            {
                Toast.makeText(SensorActivity.this, "检测到摇晃，执行操作！", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    class CompassSensorListener extends OrientationSensorListener {

        @Override
        public void notifyOrientation(float degree) {
            String direction = "";
            int angle = 0;
            if (Math.abs(degree) <= 5)
            {
                direction = "北";
                angle = (int) FloatMath.ceil(Math.abs(degree));
            }
            else if (degree > 5 && degree < 85)
            {
                direction = "东北";
                angle = (int) FloatMath.ceil(90 - degree);
            }
            else if (degree >= 85 && degree <= 95)
            {
                direction = "东";
                angle = (int) FloatMath.ceil(Math.abs(90 - degree));
            }
            else if (degree > 95 && degree < 175)
            {
                direction = "东南";
                angle = (int) FloatMath.ceil(degree - 90);
            }
            else if (degree >= 175 || degree <= -175)
            {
                direction = "南";
                angle = (int) FloatMath.ceil(Math.abs(180 - Math.abs(degree)));
            }
            else if (degree > -175 && degree < -95)
            {
                direction = "西南";
                angle = (int) FloatMath.ceil(-degree - 90);
            }
            else if (degree >= -95 && degree <= -85)
            {
                direction = "西";
                angle = (int) FloatMath.ceil(Math.abs(90 + degree));
            }
            else if (degree > -85 && degree < -5)
            {
                direction = "西北";
                angle = (int) FloatMath.ceil(90 + degree);
            }
            
            tv.setText("吹一吹，摇一摇\n" + direction + ":" + angle);
        }
    }
    
    static class BlowListener extends SafeRunnable {
        
        private final SafeThread thread = new SafeThread();
        
        //到达该值之后 触发事件
        private static final int BLOW_MAXIMUM = 3000;
        
        private static final int SAMPLE_RATE_IN_HZ = 44100;
        private int bufferSize;
        private AudioRecord ar;
        
        private int number;
        private int value;
        private long currentTime;
        private long time;
        
        private BlowCallback callback;
        
        public BlowListener(BlowCallback callback) {
            this.callback = callback;
            bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE_IN_HZ, 
                    AudioFormat.CHANNEL_IN_MONO, 
                    AudioFormat.ENCODING_PCM_16BIT);
            ar = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, 
                    SAMPLE_RATE_IN_HZ, 
                    AudioFormat.CHANNEL_IN_MONO, 
                    AudioFormat.ENCODING_PCM_16BIT, 
                    bufferSize);
        }
        
        public void startListen()
        {
            thread.startThread(this);
        }
        
        public void stopListen()
        {
            thread.forceStopThread();
        }
        
        private void reset()
        {
            number = 0;
            value = 0;
            time = 0;
        }
        
        @Override
        public void run(AtomicBoolean isRunning) {
            try {
                byte[] buffer = new byte[bufferSize];
                ar.startRecording();
                while (isRunning.get())
                {
                    currentTime = System.currentTimeMillis();
                    int num = ar.read(buffer, 0, bufferSize);
                    if (num <= 0)
                    {
                        continue;
                    }

                    number++;
                    int sum = 0;
                    for (int i = 0; i < num; i++)
                    {
                        sum += buffer[i] * buffer[i];
                    }
                    
                    value += sum / num;
                    time += System.currentTimeMillis() - currentTime;
                    
                    if (time >= 500 || number > 5)
                    {
                        int total = value / number;
                        System.out.println(total);
                        if (total > BLOW_MAXIMUM)
                        {
                            //吹一吹
                            if (callback != null)
                            {
                                callback.blow();
                            }
                        }
                        
                        reset();
                    }
                    
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                reset();
                ar.stop();
                ar.release();
            }
        }
        
        public static interface BlowCallback {
            
            public void blow();
        }
    }
}