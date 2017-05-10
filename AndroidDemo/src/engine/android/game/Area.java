package engine.android.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import engine.android.game.GameCanvas.TouchEvent;

import java.util.List;
import java.util.ListIterator;

/**
 * 游戏区域
 * 
 * @author Daimon
 * @version 3.0
 * @since 5/11/2012
 */

public class Area extends Sprite {

    private final List<Sprite> sprites = new Box<Sprite>();     // 精灵容器

    public Area(int width, int height) {
        super(width, height);
    }

    public Area(Bitmap image) {
        super(image);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Sprite sprite : sprites)
        {
            sprite.paint(canvas);
        }
    }

    @Override
    protected boolean onTouchEvent(TouchEvent event) {
        for (ListIterator<Sprite> iter = sprites.listIterator(sprites.size()); iter.hasPrevious();)
        {
            if (iter.previous().dispatchTouchEvent(event))
            {
                return true;
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * 添加精灵
     */

    public void addSprite(Sprite sprite) {
        removeSprite(sprite);
        sprites.add(sprite);
    }

    /**
     * 添加精灵
     * 
     * @param index 精灵添加位置
     */

    public void addSprite(int index, Sprite sprite) {
        removeSprite(sprite);
        sprites.add(index, sprite);
    }

    /**
     * 查找精灵索引
     * 
     * @return 如无此精灵则返回-1
     */

    public int findSpriteIndex(Sprite sprite) {
        return sprites.indexOf(sprite);
    }

    /**
     * 通过精灵名称查找精灵
     * 
     * @param name 精灵名称
     * @return 查找到的精灵，如没找到则返回Null
     */

    public Sprite getSpriteByName(String name) {
        if (name != null)
        {
            for (Sprite sprite : sprites)
            {
                if (name.equals(sprite.getName()))
                {
                    return sprite;
                }
            }
        }

        return null;
    }

    /**
     * 通过精灵属性查找精灵
     * 
     * @param tag 精灵属性
     * @return 查找到的精灵，如没找到则返回Null
     */

    public Sprite getSpriteByTag(Object tag) {
        if (tag != null)
        {
            for (Sprite sprite : sprites)
            {
                if (tag.equals(sprite.getTag()))
                {
                    return sprite;
                }
            }
        }

        return null;
    }

    /**
     * 通过精灵索引查找精灵
     * 
     * @param index 精灵索引
     * @return 查找到的精灵，如没找到则返回Null
     */

    public Sprite getSpriteByIndex(int index) {
        if (index < 0 || index >= sprites.size())
        {
            return null;
        }

        return sprites.get(index);
    }

    /**
     * 移除精灵
     * 
     * @param name 精灵名称
     * @return 被移除的精灵
     */

    public Sprite removeSprite(String name) {
        Sprite sprite = getSpriteByName(name);
        if (sprite != null)
        {
            sprites.remove(sprite);
        }

        return sprite;
    }

    /**
     * 移除精灵
     * 
     * @param sprite 需移除的精灵
     */

    public void removeSprite(Sprite sprite) {
        if (sprite == null)
        {
            throw new NullPointerException();
        }

        sprites.remove(sprite);
    }

    /**
     * 获取精灵的数量
     */

    public int getSpriteNum() {
        return sprites.size();
    }

    /**
     * 清空区域内的所有精灵
     */

    public void clear() {
        sprites.clear();
    }

    @Override
    public Area setPosition(int x, int y) {
        return move(x - this.x, y - this.y);
    }

    @Override
    public Area move(int dx, int dy) {
        if (dx == 0 && dy == 0)
        {
            return this;
        }

        super.move(dx, dy);
        for (Sprite sprite : sprites)
        {
            sprite.move(dx, dy);
        }

        return this;
    }
}