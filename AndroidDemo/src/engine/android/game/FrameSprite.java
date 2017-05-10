package engine.android.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * 游戏精灵（逐帧动画）
 * 
 * @author Daimon
 * @version 3.0
 * @since 7/9/2012
 */

public class FrameSprite extends Sprite {

    private int numberFrames;                                   // 帧数量

    private int[] frameCoordsX, frameCoordsY;                   // 每帧定位
    private int srcFrameWidth, srcFrameHeight;                  // 每帧大小

    private int[] frameSequence;                                // 每帧切换顺序
    private int sequenceIndex;                                  // 当前帧的顺序索引

    private boolean customSequenceDefined;                      // 是否自定义帧顺序

    private final Rect src = new Rect(), dst = new Rect();      // 绘制区域

    public FrameSprite(Bitmap image, int frameWidth, int frameHeight) {
        super(frameWidth, frameHeight);
        if (frameWidth < 1 || frameHeight < 1
        || (image.getWidth() % frameWidth != 0)
        || (image.getHeight() % frameHeight != 0))
        {
            throw new IllegalArgumentException();
        }

        initFrames(image, frameWidth, frameHeight, false);
    }

    /**
     * 帧初始化
     * 
     * @param image 源图片
     * @param fWidth,fHeight 每帧大小
     * @param maintainCurFrame 是否维持当前显示帧（无需重置为第一帧）
     */

    private void initFrames(Bitmap image, int fWidth, int fHeight, boolean maintainCurFrame) {
        // 获取图片大小
        int imageW = image.getWidth();
        int imageH = image.getHeight();
        // 计算帧数量
        int numHorizontalFrames = imageW / fWidth;
        int numVerticalFrames = imageH / fHeight;

        this.image = image;

        srcFrameWidth = fWidth;
        srcFrameHeight = fHeight;

        numberFrames = numHorizontalFrames * numVerticalFrames;
        // 定位帧
        frameCoordsX = new int[numberFrames];
        frameCoordsY = new int[numberFrames];

        if (!maintainCurFrame)
        {
            sequenceIndex = 0;
        }

        if (!customSequenceDefined)
        {
            frameSequence = new int[numberFrames];
        }

        int currentFrame = 0;

        for (int y = 0; y < imageH; y += fHeight)
        {
            for (int x = 0; x < imageW; x += fWidth)
            {
                frameCoordsX[currentFrame] = x;
                frameCoordsY[currentFrame] = y;

                if (!customSequenceDefined)
                {
                    frameSequence[currentFrame] = currentFrame;
                }

                currentFrame++;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (image != null)
        {
            setRect(src,
                    frameCoordsX[frameSequence[sequenceIndex]],
                    frameCoordsY[frameSequence[sequenceIndex]],
                    srcFrameWidth,
                    srcFrameHeight);
            setRect(dst, x, y, srcFrameWidth, srcFrameHeight);
            canvas.drawBitmap(image, src, dst, paint);
        }
    }

    @Override
    public void setImage(Bitmap image) {
        setImage(image, srcFrameWidth, srcFrameHeight);
    }

    @Override
    protected boolean doPixelCollision(int x, int y) {
        return super.doPixelCollision(
                frameCoordsX[frameSequence[sequenceIndex]] + x,
                frameCoordsY[frameSequence[sequenceIndex]] + y);
    }

    /**
     * 设置当前显示帧
     * 
     * @param sequenceIndex 帧的顺序索引
     */

    public void setFrame(int sequenceIndex) {
        if (sequenceIndex < 0 || sequenceIndex >= frameSequence.length)
        {
            throw new IndexOutOfBoundsException();
        }

        this.sequenceIndex = sequenceIndex;
    }

    /**
     * 获取当前帧的顺序索引
     */

    public final int getFrame() {
        return sequenceIndex;
    }

    /**
     * 获取帧数量
     */

    public final int getRawFrameCount() {
        return numberFrames;
    }

    /**
     * 获取帧显示序列的长度
     */

    public final int getFrameSequenceLength() {
        return frameSequence.length;
    }

    /**
     * 切换到下一帧
     */

    public void nextFrame() {
        sequenceIndex = (sequenceIndex + 1) % frameSequence.length;
    }

    /**
     * 切换到前一帧
     */

    public void prevFrame() {
        sequenceIndex = (sequenceIndex + frameSequence.length - 1) % frameSequence.length;
    }

    /**
     * 设置帧序列
     */

    public void setFrameSequence(int[] sequence) {
        if (sequence == null)
        {
            // 重置帧序列
            sequenceIndex = 0;
            customSequenceDefined = false;
            frameSequence = new int[numberFrames];
            for (int i = 0; i < numberFrames; i++)
            {
                frameSequence[i] = i;
            }

            return;
        }

        if (sequence.length < 1)
        {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < sequence.length; i++)
        {
            if (sequence[i] < 0 || sequence[i] >= numberFrames)
            {
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        sequenceIndex = 0;
        customSequenceDefined = true;
        frameSequence = new int[sequence.length];
        System.arraycopy(sequence, 0, frameSequence, 0, sequence.length);
    }

    /**
     * 重设显示帧
     * 
     * @param image
     * @param frameWidth,frameHeight 帧大小
     */

    public void setImage(Bitmap image, int frameWidth, int frameHeight) {
        if (frameWidth < 1 || frameHeight < 1
        || (image.getWidth() % frameWidth != 0)
        || (image.getHeight() % frameHeight != 0))
        {
            throw new IllegalArgumentException();
        }

        int num = (image.getWidth() / frameWidth) * (image.getHeight() / frameHeight);
        boolean maintainCurFrame = true;
        if (num < numberFrames)
        {
            maintainCurFrame = false;
            customSequenceDefined = false;
        }

        if (!(srcFrameWidth == frameWidth && srcFrameHeight == frameHeight))
        {
            // 重新设置大小
            sizeChanged(frameWidth, frameHeight);
        }

        initFrames(image, frameWidth, frameHeight, maintainCurFrame);
    }
}