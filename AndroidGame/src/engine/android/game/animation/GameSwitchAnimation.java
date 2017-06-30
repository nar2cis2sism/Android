package engine.android.game.animation;

import android.graphics.Bitmap;

import engine.android.game.GameAnimation;

/**
 * 图片切换动画
 * 
 * @author Daimon
 * @version 3.0
 * @since 6/7/2012
 */

public class GameSwitchAnimation extends GameAnimation {

    private final Bitmap[] images;                      // 图片帧数组
    private final int numberFrames;                     // 图片帧数量

    private int[] frameSequence;                        // 图片帧切换顺序
    private int sequenceIndex;                          // 当前帧的顺序索引

    public GameSwitchAnimation(Bitmap... images) {
        if (images == null || images.length == 0)
        {
            throw new IllegalArgumentException();
        }

        frameSequence = new int[numberFrames = (this.images = images).length];
        for (int i = 0; i < numberFrames; i++)
        {
            frameSequence[i] = i;
        }
    }

    /**
     * 设置帧序列（需在动画启动前调用）
     * 
     * @param sequence
     */

    public void setFrameSequence(int[] sequence) {
        if (isRunning())
        {
            return;
        }

        if (sequence == null)
        {
            // 重置帧序列
            sequenceIndex = 0;
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
        frameSequence = new int[sequence.length];
        System.arraycopy(sequence, 0, frameSequence, 0, sequence.length);
    }

    @Override
    protected boolean onAnimation() {
        if (sequenceIndex == -1)
        {
            sequenceIndex = 0;
            return false;
        }

        if (isReverse())
        {
            prevFrame();
            if (sequenceIndex == 0)
            {
                return true;
            }
        }
        else
        {
            nextFrame();
            if (sequenceIndex == frameSequence.length - 1)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * 切换到下一帧
     */

    private void nextFrame() {
        sequenceIndex = (sequenceIndex + 1) % frameSequence.length;
    }

    /**
     * 切换到前一帧
     */

    private void prevFrame() {
        sequenceIndex = (sequenceIndex + frameSequence.length - 1) % frameSequence.length;
    }

    @Override
    protected void onAnimationAfter() {
        if (fillEnabled)
        {
            if (fillAfter)
            {
                sequenceIndex = frameSequence.length - 1;
            }
            else if (fillBefore)
            {
                sequenceIndex = 0;
            }
        }
    }

    @Override
    protected void onAnimationBefore() {
        sequenceIndex = -1;
    }

    @Override
    protected long getPeriod() {
        if (interval == 0)
        {
            return duration / frameSequence.length;
        }
        else
        {
            return interval;
        }
    }

    /**
     * 获取当前帧的顺序索引
     */

    public int getFrame() {
        return sequenceIndex;
    }

    /**
     * 获取当前帧图片
     */

    public Bitmap getImage() {
        return images[frameSequence[sequenceIndex]];
    }

    /**
     * 获取帧数量
     */

    public int getRawFrameCount() {
        return numberFrames;
    }

    /**
     * 获取帧显示序列的长度
     */

    public int getFrameSequenceLength() {
        return frameSequence.length;
    }
}