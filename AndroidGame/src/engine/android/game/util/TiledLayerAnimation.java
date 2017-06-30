package engine.android.game.util;

import android.util.SparseArray;

import engine.android.game.GameCanvas.GameResource;
import engine.android.game.GameEngine;
import engine.android.game.TiledLayer;

/**
 * 游戏地图动画
 * 
 * @author Daimon
 * @version 3.0
 * @since 9/11/2012
 */

public class TiledLayerAnimation extends GameEngine {

    private final TiledLayer map;                               // 游戏地图

    private final SparseArray<AnimatedTile> anim_map;           // 动画集合查询表

    private long interval;                                      // 动画帧时间差

    public TiledLayerAnimation(TiledLayer map) {
        this.map = map;
        anim_map = new SparseArray<AnimatedTile>();
    }

    /**
     * 创建动画砖块
     * 
     * @param staticTileIndex 静态砖块索引
     * @return 动画砖块映射
     */

    public int createAnimatedTile(int... staticTileIndex) {
        if (staticTileIndex == null || staticTileIndex.length < 2)
        {
            throw new IllegalArgumentException();
        }

        int anim = createAnimatedTile(staticTileIndex[0]);
        for (int i = 1; i < staticTileIndex.length; i++)
        {
            addAnimatedTile(anim, staticTileIndex[i]);
        }

        return anim;
    }

    /**
     * 创建动画砖块
     * 
     * @param staticTileIndex 静态砖块索引
     * @return 动画砖块映射
     */

    private int createAnimatedTile(int staticTileIndex) {
        int anim = map.createAnimatedTile(staticTileIndex);
        anim_map.append(anim, new AnimatedTile(staticTileIndex));
        return anim;
    }

    /**
     * 添加动画砖块
     * 
     * @param animatedTileIndex 动画砖块映射
     * @param staticTileIndex 静态砖块索引
     */

    private void addAnimatedTile(int animatedTileIndex, int staticTileIndex) {
        AnimatedTile tile = anim_map.get(animatedTileIndex);
        if (tile != null)
        {
            tile.addAnimatedTile(staticTileIndex);
            return;
        }

        throw new IndexOutOfBoundsException();
    }

    /**
     * 动画砖块
     */

    private static class AnimatedTile {

        private int[] tiles;

        private int num;

        private int tileIndex;

        public AnimatedTile(int staticTileIndex) {
            addAnimatedTile(staticTileIndex);
        }

        public void addAnimatedTile(int staticTileIndex) {
            if (tiles == null)
            {
                tiles = new int[2];
            }
            else if (num == tiles.length)
            {
                int[] new_tiles = new int[num * 2];
                System.arraycopy(tiles, 0, new_tiles, 0, num);
                tiles = new_tiles;
            }

            tiles[num++] = staticTileIndex;
        }

        public int increaseAndGet() {
            return tiles[tileIndex = ++tileIndex % num];
        }
    }

    @Override
    protected void doEngine() {
        for (int i = 0, size = anim_map.size(); i < size; i++)
        {
            map.setAnimatedTile(anim_map.keyAt(i), anim_map.valueAt(i).increaseAndGet());
        }
    }

    /**
     * 播放动画
     */

    @Override
    public void start() {
        if (interval == 0)
        {
            interval = GameResource.getGame().getAnimationInterval();
        }

        start(0, interval);
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
        if (isRunning())
        {
            start();
        }
    }
}