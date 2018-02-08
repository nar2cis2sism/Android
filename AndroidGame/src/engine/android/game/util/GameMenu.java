package engine.android.game.util;

import static engine.android.util.RectUtil.setRect;

import android.graphics.Canvas;
import android.graphics.Paint;

import engine.android.game.GameCanvas.TouchEvent;
import engine.android.game.layer.Area;
import engine.android.game.layer.Sprite;

/**
 * 游戏菜单
 * 
 * @author Daimon
 * @version N
 * @since 9/4/2012
 */
public abstract class GameMenu extends Area {

    private Paint backgroundPaint;                      // 背景绘制

    private int selectedIndex = -1;

    private OnMenuItemSelectListener selectListener;

    private OnMenuItemClickListener clickListener;

    private boolean enableDrag;                         // 拖曳开关

    private boolean seleteMode;                         // 选择模式

    private boolean dragMode;                           // 拖曳模式

    private int dragX, dragY;

    public GameMenu(int width, int height) {
        super(width, height);
    }

    /**
     * 使能拖曳
     * 
     * @param enable 是否允许拖曳菜单
     */
    public void enableDrag(boolean enable) {
        enableDrag = enable;
    }

    public void setBackground(int color) {
        if (backgroundPaint == null)
        {
            backgroundPaint = new Paint();
        }

        backgroundPaint.setColor(color);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (backgroundPaint != null)
        {
            canvas.drawRect(setRect(null, x, y, width, height), backgroundPaint);
        }
        
        if (image != null)
        {
            canvas.drawBitmap(image, x, y, paint);
        }

        for (int i = 0, size = getSpriteNum(); i < size; i++)
        {
            drawMenuItem(canvas, getSpriteByIndex(i), i == selectedIndex);
        }
    }

    /**
     * 绘制菜单项
     * 
     * @param selected 是否选中
     */
    protected abstract void drawMenuItem(Canvas canvas, Sprite menuItem, boolean selected);

    @Override
    protected boolean mousePressed(int x, int y) {
        if (collidesWith(x, y, false))
        {
            seleteMode = true;
            return true;
        }

        return false;
    }

    @Override
    protected boolean mouseDragged(int x, int y) {
        if (seleteMode)
        {
            selectMenuItem(x, y);
            return true;
        }

        if (dragMode)
        {
            move(x - dragX, y - dragY);
            dragX = x;
            dragY = y;
            return true;
        }

        return false;
    }

    @Override
    protected boolean mouseReleased(int x, int y) {
        if (seleteMode)
        {
            selectMenuItem(x, y);
            seleteMode = false;
            return true;
        }

        if (dragMode)
        {
            dragMode = false;
            return true;
        }

        return false;
    }

    @Override
    protected boolean onAdvancedTouchEvent(TouchEvent event) {
        int x = (int) event.getTriggerX();
        int y = (int) event.getTriggerY();
        if (collidesWith(x, y, false))
        {
            switch (event.getAction()) {
                case ACTION_CLICK:
                    clickMenuItem(x, y);
                    return true;
                case ACTION_LONG_PRESS:
                    if (enableDrag)
                    {
                        seleteMode = false;
                        dragMode = true;
                        dragX = x;
                        dragY = y;
                        move(2, 2);
                        return true;
                    }
            }
        }

        return false;
    }

    private void selectMenuItem(int x, int y) {
        for (int i = 0, size = getSpriteNum(); i < size; i++)
        {
            Sprite menuItem = getSpriteByIndex(i);
            if (menuItem.collidesWith(x, y, false) && i != selectedIndex)
            {
                selectedIndex = i;
                selectMenuItem(menuItem);
                return;
            }
        }
    }

    private void clickMenuItem(int x, int y) {
        for (int i = 0, size = getSpriteNum(); i < size; i++)
        {
            Sprite menuItem = getSpriteByIndex(i);
            if (menuItem.collidesWith(x, y, false))
            {
                if (clickListener != null)
                {
                    clickListener.onMenuItemClick(menuItem);
                }

                return;
            }
        }
    }

    /**
     * 选取菜单项
     */
    public void selectMenuItem(int index) {
        if (index < 0 || index >= getSpriteNum())
        {
            throw new IndexOutOfBoundsException();
        }

        selectMenuItem(getSpriteByIndex(selectedIndex = index));
    }

    private void selectMenuItem(Sprite menuItem) {
        if (selectListener != null)
        {
            selectListener.onMenuItemSelecte(menuItem);
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setOnMenuItemSelectListener(OnMenuItemSelectListener selectListener) {
        this.selectListener = selectListener;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * 菜单项选择监听器
     */
    public interface OnMenuItemSelectListener {

        void onMenuItemSelecte(Sprite menuItem);
    }

    /**
     * 菜单项点击监听器
     */
    public interface OnMenuItemClickListener {

        void onMenuItemClick(Sprite menuItem);
    }
}