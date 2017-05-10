package demo.activity.effect;

import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class BlazeActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(new BlazeView(this));
    }
}

class BlazeView extends SurfaceView implements Callback {
    
    private static final class BlazeRandom {
        
        private int[] rands = new int[1024];
        private int id;
        private Date date = new Date();
        private Random random = new Random(date.getDate() * date.getSeconds());
        
        public BlazeRandom() {
            for (int i = 0; i < rands.length; i++)
            {
                rands[i] = random.nextInt(Integer.MAX_VALUE);
            }
        }
        
        public int nextInt() {
            if (id >= rands.length)
            {
                id = 0;
            }
            
            return rands[id++];
        }
    }
    
    private static final BlazeRandom random = new BlazeRandom();
    
    private static final int[] palette = new int[512];
    
    static
    {
        int i;
        int index;
        double alpha = 0;
        
        //生成调色板
        palette[0] = Color.argb(0, 0, 0, 0);//黑色
        
        for (i = 1; i < 70; i++)
        {
            index = i >> 1;
            palette[i] = Color.argb((int) (alpha++ / 512 * 255), 
                    index + 25, random.nextInt() % 10, random.nextInt() % 10);//接近黑色
        }
        
        for (i = 70; i < 110; i++)
        {
            index = i >> 1;
            palette[i] = Color.argb((int) (alpha++ / 512 * 255), 
                    index + 25, index - 25, random.nextInt() % 10);//比上面淡一些
        }
        
        for (i = 110; i < 190; i++)
        {
            index = i >> 1;
            palette[i] = Color.argb((int) (alpha++ / 512 * 255), 
                    index + 75, index, random.nextInt() % 5);//接近深橘红色
        }
        
        for (i = 190; i < 400; i++)
        {
            index = i >> 1;
            palette[i] = Color.argb((int) (alpha++ / 512 * 255), 
                    (index + 70 > 255) ? 255 : (index + 70), index + 30, random.nextInt() % 10);//橘红色
        }
        
        for (i = 400; i < 512; i++)
        {
            index = i >> 1;
            palette[i] = Color.argb((int) (alpha++ / 512 * 255), 
                    index, index - random.nextInt() % 25, random.nextInt() % 5);//接近黄色
        }
    }
    
    private class BlazeThread extends Thread {
        
        private boolean isRunning;
        
        public void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }
        
        @Override
        public void run() {
            Canvas canvas = null;
            try {
                while (isRunning)
                {
                    canvas = holder.lockCanvas();
                    if (canvas != null)
                    {
                        render(canvas);
                        makeBlaze();
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
    
    private BlazeThread refreshThread;
    
    SurfaceHolder holder;
    
    private int width,height;
    
    private int[] blazeBuffer;
    private int[] blazePalette;

    public BlazeView(Context context) {
        super(context);
        (holder = getHolder()).addCallback(this);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        blazeBuffer = new int[(width = w) * (height = h)];
        blazePalette = new int[blazeBuffer.length];
        for (int i = 0; i < blazeBuffer.length; i++)
        {
            blazeBuffer[i] = 0;
            blazePalette[i] = 0;
        }
    }
    
    void render(Canvas canvas)
    {
        canvas.drawBitmap(blazeBuffer, 0, width, 0, 0, width, height, true, null);
    }
    
    /**
     * 制造火焰效果
     */
    
    void makeBlaze()
    {
        int wh = width * (height - 2) - 1;
        int rand = random.nextInt();
        int tmp;
        for (int i = 0; i < width; i += rand % 3)
        {
            tmp = wh + i;
            if (rand % 2 != 0)
            {
                blazePalette[tmp] = 511;
            }
            else
            {
                blazePalette[tmp] = 0;
            }
            
            blazeBuffer[tmp] = palette[blazePalette[tmp]];
            rand = random.nextInt();
        }

        int x,y;
        int w = width - 1;
        int h = height - 1;
        int offset,value;
        int widthAdd1 = width + 1;
        int widthSub1 = width - 1;
        for (y = 1; y < h; y++)
        {
            for (x = 1; x < w; x++)
            {
                offset = y * width + x;
                value = (
                        blazePalette[offset - width] 
                      + blazePalette[offset + width] 
                      + blazePalette[offset + 1] 
                      + blazePalette[offset - 1] 
                      + blazePalette[offset - widthAdd1] 
                      + blazePalette[offset - widthSub1] 
                      + blazePalette[offset + widthSub1] 
                      + blazePalette[offset + widthAdd1]
                      ) >> 3;
            
                if (value > 0)
                {
                    --value;
                    tmp = offset - width;
                    blazeBuffer[tmp] = palette[blazePalette[tmp] = value];
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (refreshThread != null)
        {
            refreshThread.interrupt();
        }
        
        refreshThread = new BlazeThread();
        refreshThread.setRunning(true);
        refreshThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        refreshThread.setRunning(false);
        try {
            refreshThread.join();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        
        refreshThread = null;
    }
}