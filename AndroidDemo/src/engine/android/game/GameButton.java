package engine.android.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import engine.android.game.GameCanvas.TouchEvent;

/**
 * 游戏按钮
 * 
 * @author Daimon
 * @version 3.0
 * @since 5/12/2012
 */

public class GameButton extends Area {

    /***** 按钮样式 *****/

    public static final int ALPHA       = 0;                // 透明样式
    public static final int NONE        = 1;                // 普通样式
    public static final int IMAGE       = 2;                // 图片样式

    /***** 按钮状态 *****/

    private static final int NORMAL     = 0;                // 正常状态
    private static final int PRESSED    = 1;                // 按下状态
    private static final int DISABLED   = 2;                // 无效状态

    private int style;                                      // 按钮样式

    private int state;                                      // 按钮状态

    private ActionListener listener;                        // 动作事件监听器

    private Bitmap normalImage;                             // 正常图片
    private Bitmap pressedImage;                            // 按钮按下时的图片
    private Bitmap disabledImage;                           // 按钮无效时的图片

    public GameButton(Bitmap image) {
        super(image);
        normalImage = image;
    }

    public GameButton(int width, int height) {
        super(width, height);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (state == NORMAL)
        {
            if (style == ALPHA)
            {
                setAlpha(0xff);
            }
            else if (style == IMAGE && normalImage != null)
            {
                setImage(normalImage);
            }
        }
        else if (state == PRESSED)
        {
            if (style == ALPHA)
            {
                setAlpha(0xaa);
            }
            else if (style == IMAGE && pressedImage != null)
            {
                setImage(pressedImage);
            }
        }
        else if (state == DISABLED)
        {
            if (style == ALPHA)
            {
                setAlpha(0xaa);
            }
            else if (style == IMAGE && disabledImage != null)
            {
                setImage(disabledImage);
            }
        }

        super.onDraw(canvas);
    }

    /**
     * 设置画笔透明度
     */

    private void setAlpha(int alpha) {
        Paint paint = getPaint();
        paint.setAlpha(alpha);
        setPaint(paint);
    }

    @Override
    protected boolean onTouchEvent(TouchEvent event) {
        if (state == DISABLED)
        {
            return false;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected boolean mousePressed(int x, int y) {
        if (state == NORMAL && collidesWith(x, y, false))
        {
            state = PRESSED;
            return true;
        }

        return false;
    }

    @Override
    protected boolean mouseReleased(int x, int y) {
        if (state == PRESSED && collidesWith(x, y, false))
        {
            state = NORMAL;
            click();
            return true;
        }

        return false;
    }

    @Override
    protected boolean mouseDragged(int x, int y) {
        if (state == PRESSED)
        {
            if (collidesWith(x, y, false))
            {
                return true;
            }
            else
            {
                state = NORMAL;
            }
        }

        return false;
    }

    /**
     * 模拟按钮单击（可供用户调用）
     */

    public void click() {
        if (listener != null)
        {
            listener.actionPerformed(this);
        }
    }

    /**
     * 添加动作事件监听器
     */

    public void addActionListener(ActionListener listener) {
        if (listener == null)
        {
            throw new NullPointerException();
        }

        this.listener = listener;
    }

    /**
     * 移除动作事件监听器
     */

    public void removeActionListener() {
        listener = null;
    }

    /**
     * 判断按钮是否有效
     */

    public boolean isEnabled() {
        return state != DISABLED;
    }

    /**
     * 设置按钮状态
     * 
     * @param isEnabled 是否有效
     */

    public void setEnabled(boolean isEnabled) {
        if (isEnabled)
        {
            state = NORMAL;
        }
        else
        {
            state = DISABLED;
        }
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public Bitmap getNormalImage() {
        return normalImage;
    }

    public void setNormalImage(Bitmap normalImage) {
        this.normalImage = normalImage;
    }

    public Bitmap getPressedImage() {
        return pressedImage;
    }

    public void setPressedImage(Bitmap image) {
        pressedImage = image;
    }

    public Bitmap getDisabledImage() {
        return disabledImage;
    }

    public void setDisabledImage(Bitmap image) {
        disabledImage = image;
    }

    /**
     * 按钮动作事件监听器
     */

    public static interface ActionListener {

        /**
         * 动作事件处理
         * 
         * @param source 事件发生源
         */

        public void actionPerformed(GameButton source);

    }
}