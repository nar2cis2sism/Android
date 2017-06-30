package engine.android.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * 游戏地图（砖块拼接类）
 * 
 * @author Daimon
 * @version 3.0
 * @since 9/11/2012
 */

public class TiledLayer extends Layer {

    private Bitmap sourceImage;                                 // 源图片
    private int numberOfTiles;                                  // 砖块数量

    private int[] tileSetX, tileSetY;                           // 砖块定位
    private int cellWidth, cellHeight;                          // 砖块大小

    private final int rows, cols;                               // 行列数
    private final int[][] cellMatrix;                           // 砖块填充矩阵（包括静态和动画砖块）

    private int[] anim_to_static;                               // 动画砖块查询表
    private int numOfAnimTiles;                                 // 动画砖块数量

    private final Rect src = new Rect(), dst = new Rect();      // 绘制区域

    /**
     * 创建地图
     * 
     * @param rows,cols 地图大小（行列数）
     * @param image 源图片
     * @param tileWidth,tileHeight 砖块大小
     */

    public TiledLayer(int rows, int cols, Bitmap image, int tileWidth, int tileHeight) {
        super(cols < 1 || tileWidth < 1 ? -1 : cols * tileWidth,
              rows < 1 || tileHeight < 1 ? -1 : rows * tileHeight);
        if ((image.getWidth() % tileWidth != 0)
        ||  (image.getHeight() % tileHeight != 0))
        {
            throw new IllegalArgumentException();
        }

        this.rows = rows;
        this.cols = cols;
        cellMatrix = new int[rows][cols];
        // 计算砖块数量
        int num = (image.getWidth() / tileWidth) * (image.getHeight() / tileHeight);
        createStaticSet(image, num + 1, tileWidth, tileHeight, true);
    }

    /**
     * 创建动画砖块
     * 
     * @param staticTileIndex 静态砖块索引
     * @return 动画砖块映射
     */

    public int createAnimatedTile(int staticTileIndex) {
        if (staticTileIndex < 0 || staticTileIndex >= numberOfTiles)
        {
            throw new IndexOutOfBoundsException();
        }

        if (anim_to_static == null)
        {
            anim_to_static = new int[4];
            numOfAnimTiles = 1;
        }
        else if (numOfAnimTiles == anim_to_static.length)
        {
            int[] new_anim = new int[numOfAnimTiles * 2];
            System.arraycopy(anim_to_static, 0, new_anim, 0, numOfAnimTiles);
            anim_to_static = new_anim;
        }

        anim_to_static[numOfAnimTiles] = staticTileIndex;

        return (-(++numOfAnimTiles - 1));
    }

    /**
     * 设置动画砖块
     * 
     * @param animatedTileIndex 动画砖块映射
     * @param staticTileIndex 静态砖块索引
     */

    public void setAnimatedTile(int animatedTileIndex, int staticTileIndex) {
        if (staticTileIndex < 0 || staticTileIndex >= numberOfTiles)
        {
            throw new IndexOutOfBoundsException();
        }

        animatedTileIndex = -animatedTileIndex;
        if (anim_to_static == null
        ||  animatedTileIndex <= 0
        ||  animatedTileIndex >= numOfAnimTiles)
        {
            throw new IndexOutOfBoundsException();
        }

        anim_to_static[animatedTileIndex] = staticTileIndex;
    }

    /**
     * 获取动画砖块当前指向的静态砖块
     * 
     * @param animatedTileIndex 动画砖块映射
     */

    public int getAnimatedTile(int animatedTileIndex) {
        animatedTileIndex = -animatedTileIndex;
        if (anim_to_static == null
        ||  animatedTileIndex <= 0
        ||  animatedTileIndex >= numOfAnimTiles)
        {
            throw new IndexOutOfBoundsException();
        }

        return anim_to_static[animatedTileIndex];
    }

    /**
     * 设置砖块
     * 
     * @param col,row 行列定位
     * @param tileIndex 砖块索引（0为空白）
     */

    public void setCell(int col, int row, int tileIndex) {
        if (col < 0 || col >= cols || row < 0 || row >= rows)
        {
            throw new IndexOutOfBoundsException();
        }

        if (tileIndex > 0)
        {
            // 静态砖块
            if (tileIndex >= numberOfTiles)
            {
                throw new IndexOutOfBoundsException();
            }
        }
        else if (tileIndex < 0)
        {
            // 动画砖块
            if (anim_to_static == null || -tileIndex >= numOfAnimTiles)
            {
                throw new IndexOutOfBoundsException();
            }
        }

        cellMatrix[row][col] = tileIndex;
    }

    /**
     * 获取砖块
     * 
     * @param col,row 行列定位
     * @return 砖块索引（0为空白）
     */

    public int getCell(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows)
        {
            throw new IndexOutOfBoundsException();
        }

        return cellMatrix[row][col];
    }

    /**
     * 填充砖块
     * 
     * @param col,row 行列定位
     * @param numCols,numRows 填充行列数
     * @param tileIndex 砖块索引（0为空白）
     */

    public void fillCells(int col, int row, int numCols, int numRows, int tileIndex) {
        if (col < 0 || col >= cols || row < 0 || row >= rows
        ||  numCols < 0 || col + numCols > cols
        ||  numRows < 0 || row + numRows > rows)
        {
            throw new IndexOutOfBoundsException();
        }

        if (tileIndex > 0)
        {
            // 静态砖块
            if (tileIndex >= numberOfTiles)
            {
                throw new IndexOutOfBoundsException();
            }
        }
        else if (tileIndex < 0)
        {
            // 动画砖块
            if (anim_to_static == null || -tileIndex >= numOfAnimTiles)
            {
                throw new IndexOutOfBoundsException();
            }
        }

        for (int rowCount = row, rowLength = row + numRows; rowCount < rowLength; rowCount++)
        {
            for (int colCount = col, colLength = col + numCols; colCount < colLength; colCount++)
            {
                cellMatrix[rowCount][colCount] = tileIndex;
            }
        }
    }

    public final int getCellWidth() {
        return cellWidth;
    }

    public final int getCellHeight() {
        return cellHeight;
    }

    public final int getCols() {
        return cols;
    }

    public final int getRows() {
        return rows;
    }

    /**
     * 获取Y坐标所在行
     * 
     * @return 超出地图范围则返回-1
     */

    public final int getRow(int y) {
        y -= this.y;
        if (y < 0 || y >= height)
        {
            return -1;
        }

        return y / cellHeight;
    }

    /**
     * 获取X坐标所在列
     * 
     * @return 超出地图范围则返回-1
     */

    public final int getCol(int x) {
        x -= this.x;
        if (x < 0 || x >= width)
        {
            return -1;
        }

        return x / cellWidth;
    }

    /**
     * 返回所在列的X坐标
     */

    public final int getCellX(int col) {
        if (col < 0 || col >= cols)
        {
            throw new IndexOutOfBoundsException();
        }

        return x + col * cellWidth;
    }

    /**
     * 返回所在行的Y坐标
     */

    public final int getCellY(int row) {
        if (row < 0 || row >= rows)
        {
            throw new IndexOutOfBoundsException();
        }

        return y + row * cellHeight;
    }

    /**
     * 重设砖块地图
     * 
     * @param image 源图片
     * @param tileWidth,tileHeight 砖块大小
     */

    public void setStaticTileSet(Bitmap image, int tileWidth, int tileHeight) {
        if (tileWidth < 1 || tileHeight < 1
        || (image.getWidth() % tileWidth != 0)
        || (image.getHeight() % tileHeight != 0))
        {
            throw new IllegalArgumentException();
        }

        sizeChanged(cols * tileWidth, rows * tileHeight);
        // 计算砖块数量
        int num = (image.getWidth() / tileWidth) * (image.getHeight() / tileHeight);
        if (num >= numberOfTiles - 1)
        {
            createStaticSet(image, num + 1, tileWidth, tileHeight, true);
        }
        else
        {
            createStaticSet(image, num + 1, tileWidth, tileHeight, false);
        }
    }

    /**
     * 创建静态砖块集合
     * 
     * @param image 源图片
     * @param num 砖块数量，索引从1开始
     * @param tileWidth,tileHeight 砖块大小
     * @param maintainIndices 是否维持砖块矩阵（无需重置为空白）
     */

    private void createStaticSet(Bitmap image, int num, int tileWidth, int tileHeight,
            boolean maintainIndices) {
        // 获取图片大小
        int imageW = image.getWidth();
        int imageH = image.getHeight();

        sourceImage = image;

        cellWidth = tileWidth;
        cellHeight = tileHeight;
        src.set(0, 0, tileWidth, tileHeight);
        dst.set(src);

        numberOfTiles = num;
        // 定位砖块
        tileSetX = new int[numberOfTiles];
        tileSetY = new int[numberOfTiles];

        if (!maintainIndices)
        {
            for (int row = 0; row < rows; row++)
            {
                for (int col = 0; col < cols; col++)
                {
                    cellMatrix[row][col] = 0;
                }
            }

            anim_to_static = null;
        }

        int currentTile = 1;

        for (int y = 0; y < imageH; y += tileHeight)
        {
            for (int x = 0; x < imageW; x += tileWidth)
            {
                tileSetX[currentTile] = x;
                tileSetY[currentTile] = y;

                currentTile++;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        int tileIndex = 0;
        int y = this.y;
        for (int row = 0; row < rows; row++, y += cellHeight)
        {
            int x = this.x;
            for (int col = 0; col < cols; col++, x += cellWidth)
            {
                tileIndex = cellMatrix[row][col];
                if (tileIndex == 0)
                {
                    // 空白
                    continue;
                }
                else if (tileIndex < 0)
                {
                    // 动画砖块
                    tileIndex = getAnimatedTile(tileIndex);
                }

                setRect(src,
                        tileSetX[tileIndex],
                        tileSetY[tileIndex],
                        cellWidth,
                        cellHeight);
                setRect(dst, x, y, cellWidth, cellHeight);
                canvas.drawBitmap(sourceImage, src, dst, paint);
            }
        }
    }

    /**
     * 像素检测
     * 
     * @param x,y 相对于砖块的坐标
     * @param tileIndex 砖块索引
     */

    boolean doPixelCollision(int x, int y, int tileIndex) {
        x += tileSetX[tileIndex];
        y += tileSetY[tileIndex];
        return sourceImage != null
            && x >= 0
            && y >= 0
            && x < sourceImage.getWidth()
            && y < sourceImage.getHeight()
            && (sourceImage.getPixel(x, y) & 0xff000000) != 0;
    }
}