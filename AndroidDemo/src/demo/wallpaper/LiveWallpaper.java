package demo.wallpaper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import engine.android.game.Area;
import engine.android.game.Box;
import engine.android.game.FrameSprite;
import engine.android.game.Layer;
import engine.android.game.Sprite;
import engine.android.util.Util;
import engine.android.util.image.ImageUtil;

import java.util.List;
import java.util.Random;

public class LiveWallpaper extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new LiveWallpaperEngine();
    }
    
    public class LiveWallpaperEngine extends Engine implements OnSharedPreferenceChangeListener {

        private SurfaceHolder holder;
        private LayerManager lm;
        
        private Area bg;                                //背景
        private Sprite mountain;                        //山脉
        private Sprite stars;                           //星星
        private Sprite moon;                            //月亮
        private Sprite branchLeft,branchRight;          //树枝
        private Sprite sakuraLeft,sakuraRight;          //树枝上的樱花
        
        private static final int flower_timer = 40;
        private int flower_timer_count;
        private Bitmap flower1,flower2;                 //花朵
        private static final int petal_timer = 60;
        private int petal_timer_count;
        private Bitmap petal1,petal2;                   //花瓣
        
        private final Random random = new Random();
        
        private int width,height;
        private boolean visible;
        
        private SharedPreferences sp;
        private boolean dynamic;
        
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            setTouchEventsEnabled(true);
            
            holder = getSurfaceHolder();
            lm = new LayerManager();
            
            loadImages();
            
            sp = getSharedPreferences("wallpaper", MODE_PRIVATE);
            sp.registerOnSharedPreferenceChangeListener(this);
            
            dynamic = sp.getBoolean("dynamic", dynamic);
        }
        
        private void loadImages()
        {
            flower1 = loadImage("flower1.png");
            flower2 = loadImage("flower2.png");
            petal1 = loadImage("petal1.png");
            petal2 = loadImage("petal2.png");
        }
        
        private Bitmap loadImage(String name)
        {
            return BitmapFactory.decodeStream(getClass().getResourceAsStream("/assets/wallpaper/" + name));
        }
        
        @Override
        public void onDestroy() {
            sp.unregisterOnSharedPreferenceChangeListener(this);
        }
        
        @Override
        public void onVisibilityChanged(boolean visible) {
            if (this.visible = visible)
            {
                renderHandler.sendEmptyMessage(0);
            }
            else
            {
                renderHandler.removeMessages(0);
            }
        }
        
        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
        }
        
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xOffsetStep, float yOffsetStep, int xPixelOffset,
                int yPixelOffset) {
            lm.setPosition(xPixelOffset, yPixelOffset);
        }
        
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                int width, int height) {
            lm.setViewWindow(0, 0, this.width = width, this.height = height);
            
            moon.setPosition(width - moon.getWidth(), 70);
        }
        
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            Area bg = new Area(loadImage("bg.png"));
            lm.append(bg);
            
            mountain = new Sprite(loadImage("mountain.png"));
            mountain.setPosition(100, 200);
            bg.addSprite(mountain);
            
            stars = new Sprite(loadImage("stars.png"));
            bg.addSprite(stars);
            
            moon = new Sprite(loadImage("moon.png"));
            bg.addSprite(moon);
            
            branchLeft = new Sprite(loadImage("branch_left.png"));
            bg.addSprite(branchLeft);
            
            branchRight = new Sprite(loadImage("branch_right.png"));
            branchRight.setPosition(bg.getWidth() - branchRight.getWidth(), 0);
            bg.addSprite(branchRight);

            sakuraLeft = new Sprite(loadImage("sakura_left.png"));
            bg.addSprite(sakuraLeft);
            
            sakuraRight = new Sprite(loadImage("sakura_right.png"));
            sakuraRight.setPosition(bg.getWidth() - sakuraRight.getWidth(), 0);
            bg.addSprite(sakuraRight);
            
            lm.append(this.bg = new Area(bg.getWidth(), bg.getHeight()));
        }
        
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            lm.clear();
        }
        
        private Handler renderHandler = new Handler(){
            
            public void handleMessage(android.os.Message msg) {
                render();
                if (visible)
                {
                    sendEmptyMessageDelayed(0, 20);
                }
            };
        };
        
        private Canvas canvas;                      //绘制画布
        
        void render()
        {
            try {
                //锁住画布
                canvas = holder.lockCanvas();
                if (canvas != null)
                {
                    if (dynamic)
                    {
                        onLogic();
                    }
                    
                    lm.render(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null)
                {
                    //刷新绘制
                    holder.unlockCanvasAndPost(canvas);
                    canvas = null;
                }
            }
        }
        
        private void onLogic()
        {
            if (++flower_timer_count >= flower_timer)
            {
                Sprite flower;
                if (Util.getRandom(random, 0, 10) < 5)
                {
                    flower = new Flower(flower1);
                }
                else
                {
                    flower = new Flower(flower2);
                }
                
                flower.setPosition(Util.getRandom(random, 0, width * 2 - flower.getWidth()), 0);
                bg.addSprite(flower);
                
                flower_timer_count = 0;
            }
            
            if (++petal_timer_count >= petal_timer)
            {
                Sprite petal;
                if (Util.getRandom(random, 0, 10) < 5)
                {
                    petal = new Petal(petal1, 35, 35);
                }
                else
                {
                    petal = new Petal(petal2, 35, 35);
                }
                
                petal.setPosition(Util.getRandom(random, 0, width * 2 - petal.getWidth()), 0);
                bg.addSprite(petal);
                
                petal_timer_count = 0;
            }
            
            for (int i = 0, size = bg.getSpriteNum(); i < size; i++)
            {
                Sprite s = bg.getSpriteByIndex(i);
                if (s.getY() > height)
                {
                    bg.removeSprite(s);
                    i--;
                    size--;
                }
            }
        }

        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            dynamic = sp.getBoolean("dynamic", dynamic);
            if (!dynamic)
            {
                bg.clear();
                flower_timer_count = 0;
                petal_timer_count = 0;
            }
        }
    }

    private static class LayerManager {
        
        private Point position = new Point();                       //画布位置
        
        private List<Layer> layers = new Box<Layer>();              //图层容器
        
        private int viewX,viewY,viewWidth,viewHeight;               //可视区域
        
        /**
         * 设置画布位置
         */
        
        public void setPosition(int x, int y)
        {
            position.set(x, y);
        }
        
        /**
         * 添加图层
         */
        
        public void append(Layer l)
        {
            remove(l);
            layers.add(l);
        }
        
        /**
         * 移除图层
         */
        
        public void remove(Layer l)
        {
            if (l == null)
            {
                throw new NullPointerException();
            }
            
            layers.remove(l);
        }
        
        /**
         * 清空图层
         */
        
        public void clear()
        {
            layers.clear();
        }
        
        /**
         * 画布渲染
         */
        
        void render(Canvas canvas)
        {
            canvas.save();
            
            canvas.clipRect(viewX, viewY, viewX + viewWidth, viewY + viewHeight);
            canvas.translate(position.x, position.y);
            
            for (Layer l : layers)
            {
                l.onDraw(canvas);
            }
            
            canvas.restore();
        }
        
        /**
         * 设置窗口显示区域（相对于手机屏幕）
         */
        
        public final void setViewWindow(int x, int y, int width, int height)
        {
            if (width < 0 || height < 0)
            {
                throw new IllegalArgumentException();
            }
            
            viewX = x;
            viewY = y;
            viewWidth = width;
            viewHeight = height;
        }
    }
    
    private static class Flower extends Sprite {
        
        private float angle;

        public Flower(Bitmap image) {
            super(image);
        }
        
        @Override
        public void onDraw(Canvas canvas) {
            ImageUtil.drawRotateImage(canvas, getImage(), angle, centerX(), centerY(), 
                    getWidth() / 2, getHeight() / 2);
            
            move(0, 2);
            angle = ++angle % 360;
        }
    }
    
    private static class Petal extends FrameSprite {
        
        private static final int[] sequence = Util.getSequence(0, 44);

        public Petal(Bitmap image, int frameWidth, int frameHeight) {
            super(image, frameWidth, frameHeight);
            setFrameSequence(sequence);
        }
        
        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            move(0, 1);
            nextFrame();
        }
    }
}