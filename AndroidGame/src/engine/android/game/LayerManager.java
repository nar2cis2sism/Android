package engine.android.game;

import android.graphics.Canvas;
import android.graphics.Point;

import engine.android.game.GameCanvas.TouchEvent;

import java.util.List;
import java.util.ListIterator;

/**
 * 图层管理器
 * 
 * @author Daimon
 * @version 3.0
 * @since 8/13/2012
 */

public class LayerManager {

    protected final GameCanvas gc;                              // 游戏画布

    private Point position = new Point();                       // 画布位置

    private List<Layer> layers = new Box<Layer>();              // 图层容器

    private int viewX, viewY, viewWidth, viewHeight;            // 可视区域

    LayerManager(GameCanvas gc) {
        this.gc = gc;
        setViewWindow(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * 设置画布位置
     */

    public void setPosition(int x, int y) {
        position.set(x, y);
    }

    /**
     * 返回画布位置
     */

    public Point getPosition() {
        return position;
    }

    /**
     * 移动画布位置
     */

    public void translate(int dx, int dy) {
        position.offset(dx, dy);
    }

    /**
     * 添加图层
     */

    public void append(Layer l) {
        remove(l);
        layers.add(l);
    }

    /**
     * 插入图层
     */

    public void insert(Layer l, int index) {
        remove(l);
        layers.add(index, l);
    }

    /**
     * 获取图层
     * 
     * @param index 图层索引
     */

    public Layer getLayerAt(int index) {
        return layers.get(index);
    }

    /**
     * 获取图层数量
     */

    public int getSize() {
        return layers.size();
    }

    /**
     * 移除图层
     */

    public void remove(Layer l) {
        if (l == null)
        {
            throw new NullPointerException();
        }

        layers.remove(l);
    }

    /**
     * 清空图层
     */

    public void clear() {
        layers.clear();
    }

    /**
     * 画布渲染
     */

    void render(Canvas canvas) {
        canvas.save();

        canvas.clipRect(viewX, viewY, viewX + viewWidth, viewY + viewHeight);
        canvas.translate(position.x, position.y);
        if (gc.isScreenAutoAdapt)
        {
            canvas.scale(gc.scaleX, gc.scaleY);
        }

        for (Layer l : layers)
        {
            l.paint(canvas);
        }

        canvas.restore();
    }

    /**
     * 触屏事件处理
     * 
     * @param event 触屏事件
     */

    void touchAction(TouchEvent event) {
        event.move(-position.x, -position.y);
        for (ListIterator<Layer> iter = layers.listIterator(layers.size()); iter.hasPrevious();)
        {
            if (iter.previous().dispatchTouchEvent(event))
            {
                return;
            }
        }
    }

    /**
     * 设置窗口显示区域（相对于手机屏幕）
     */

    public final void setViewWindow(int x, int y, int width, int height) {
        if (width < 0 || height < 0)
        {
            throw new IllegalArgumentException();
        }

        viewX = x;
        viewY = y;
        viewWidth = width;
        viewHeight = height;
    }

    /**
     * 返回画布的宽度，代替{@link GameCanvas#getWidth()}
     */

    public final int getWidth() {
        return gc.width;
    }

    /**
     * 返回画布的高度，代替{@link GameCanvas#getHeight()}
     */

    public final int getHeight() {
        return gc.height;
    }
}