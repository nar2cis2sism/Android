package engine.android.game.util;

import android.graphics.Canvas;

import engine.android.game.Layer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * 地图数据管理器
 * 
 * @author Daimon
 * @version 3.0
 * @since 6/6/2012
 */

public final class MapDataManager {

    private final List<int[][]> list = new LinkedList<int[][]>();   // 地图数据表

    private int index;                                              // 当前地图索引

    /**
     * 加载地图<br>
     * 数据格式参照raw/map.txt
     * 
     * @param is 数据流
     */

    public int[][] loadMap(InputStream is) throws Exception {
        List<int[]> list = new LinkedList<int[]>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String str;
        while ((str = br.readLine()) != null)
        {
            String[] strs = str.split(",");
            int[] a = new int[strs.length];
            for (int i = 0; i < strs.length; i++)
            {
                a[i] = Integer.parseInt(strs[i].trim());
            }

            list.add(a);
        }

        return list.toArray(new int[list.size()][]);
    }

    /**
     * 添加地图
     * 
     * @param mapData 地图数据
     */

    public void add(int[][] mapData) {
        list.add(mapData);
    }

    /**
     * 获取当前地图数据
     */

    public int[][] getMap() {
        if (index < 0 || index >= list.size())
        {
            return null;
        }

        return list.get(index);
    }

    /**
     * 设置当前地图
     */

    public void setMap(int index) {
        this.index = index;
    }

    /**
     * 获取地图数量
     */

    public int getNum() {
        return list.size();
    }

    /**
     * 自定义游戏地图
     */

    public static abstract class MapView extends Layer {

        private int[][] mapData;                    // 地图数据

        public MapView() {
            super(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        /**
         * 设置地图数据
         */

        public void setMapData(int[][] mapData) {
            int len = mapData.length;
            this.mapData = new int[len][];
            for (int i = 0, size = len; i < size; i++)
            {
                len = mapData[i].length;
                this.mapData[i] = new int[len];
                System.arraycopy(mapData[i], 0, this.mapData[i], 0, len);
            }
        }

        /**
         * 获取地图数据
         */

        public int[][] getMapData() {
            return mapData;
        }

        /**
         * 获取地图特定位置的标志
         */

        public int getFlag(int row, int col) {
            return mapData[row][col];
        }

        /**
         * 设置地图特定位置的标志
         */

        public void setFlag(int row, int col, int flag) {
            mapData[row][col] = flag;
        }

        @Override
        public void onDraw(Canvas canvas) {
            if (mapData != null)
            {
                for (int i = 0; i < mapData.length; i++)
                {
                    for (int j = 0; j < mapData[i].length; j++)
                    {
                        drawMap(canvas, i, j, mapData[i][j]);
                    }
                }
            }
        }

        /**
         * 地图绘制方法
         */

        protected abstract void drawMap(Canvas canvas, int row, int col, int flag);
    }
}