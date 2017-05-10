package engine.android.game.util;

import android.graphics.Point;

import engine.android.game.TiledLayer;

import java.util.LinkedList;

/**
 * A*寻路算法
 * 
 * @author Daimon
 * @version 3.0
 * @since 9/12/2012
 */

public class AStar {

    private static class AStarNode {

        Point pos;                          // 该节点的位置

        int G;                              // 从起点移动到该节点的移动代价
        int H;                              // 从该节点移动到终点的估算成本
        int F;                              // 上面两者之和

        AStarNode parent;                   // 此节点的父节点
        AStarNode prev;                     // 在open或者next链表中的上一个节点
        AStarNode next;                     // 在open或者next链表中的下一个节点

        int modified;                       // 记录该节点是否被修改过，1:空|2:open|4:close
    }

    private static final int G_MAX = Integer.MAX_VALUE;

    private final AStarNode[][] nodes;      // 对应地图中每个节点

    private AStarNode open;                 // 保存没有处理的按估计值排序的节点

    private AStarNode close;                // 保存处理过的节点

    private final AStarNode[] modified;     // 保存修改过的节点

    private final int width, height;        // 地图的大小

    private final int size;                 // 地图的面积

    private int modified_;                  // modified数组的指针

    private int openNum;                    // open链表中的节点数目

    private int closeNum;                   // close链表中的节点数目

    private int openMaxNum;                 // open链表中最多节点数目

    private int closeMaxNum;                // close链表中最多节点数目

    private int directionMask;              // 要搜索方向的标志，0-7位为从上开始顺时针八个方向

    private Point minPos;                   // 终点或最接近终点的位置

    private int endX, endY;                 // 终点坐标

    protected final TiledLayer map;         // 游戏地图

    public AStar(TiledLayer map) {
        this.map = map;

        width = map.getCols();
        height = map.getRows();
        size = width * height;

        openMaxNum = closeMaxNum = size;
        directionMask = 0x55;

        modified = new AStarNode[size];
        nodes = new AStarNode[height][];
        for (int i = 0; i < height; i++)
        {
            nodes[i] = new AStarNode[width];
            for (int j = 0; j < width; j++)
            {
                nodes[i][j] = new AStarNode();
                nodes[i][j].pos = new Point(j, i);
                nodes[i][j].G = G_MAX;
            }
        }
    }

    public void setOpenMaxNum(int openMaxNum) {
        this.openMaxNum = openMaxNum;
    }

    public void setCloseMaxNum(int closeMaxNum) {
        this.closeMaxNum = closeMaxNum;
    }

    /**
     * 设置允许移动的方向标志，默认为上左下右四个方向
     * 
     * @param directionMask 0-7位为从上开始顺时针八个方向
     */

    public void setDirectionMask(int directionMask) {
        this.directionMask = directionMask;
    }

    /**
     * 待处理节点入open队列，根据F值插入排序（升序）
     */

    private void addToOpenQueue(AStarNode node) {
        node.modified |= 2; // 记录open标志
        int F = node.F;
        AStarNode p, n;
        int i = 0;
        for (p = null, n = open; n != null && i <= openNum; p = n, n = n.next, i++)
        {
            if (F <= n.F)
            {
                break;
            }
        }

        if (i > openNum)
        {
            return;
        }

        node.next = n;
        node.prev = p;
        if (p != null)
        {
            p.next = node;
        }
        else
        {
            open = node;
        }

        if (n != null)
        {
            n.prev = node;
        }

        openNum++;
    }

    /**
     * 选择F值最小的节点出open队列
     */

    private boolean getFromOpenQueue() {
        if (open == null)
        {
            return false;
        }

        AStarNode node = open;
        open = open.next;
        if (open != null)
        {
            open.prev = null;
        }

        if ((node.modified & 4) != 0)
        {
            // 已经在close中了
            return false;
        }

        // 放入close队列
        node.next = close;
        node.prev = null;
        node.modified &= ~2; // 清除open标志
        node.modified |= 4;  // 记录close标志
        if (close != null)
        {
            close.prev = node;
        }

        close = node;
        openNum--;
        closeNum++;
        return true;
    }

    /**
     * 释放close队列栈顶节点
     */

    private boolean getFromCloseQueue() {
        if (close != null)
        {
            close.modified &= ~4; // 清除close标志
            close = close.next;
            if (close != null)
            {
                close.prev = null;
            }

            closeNum--;
            return true;
        }

        return false;
    }

    /**
     * 还原修改过的所有节点
     */

    private void clearModifiedNodes() {
        for (int i = 0; i < modified_; i++)
        {
            modified[i].modified = 0;
            modified[i].G = G_MAX;
        }

        modified_ = 0;
        openNum = closeNum = 0;
        open = close = null;
    }

    /**
     * 尝试移动到指定坐标
     * 
     * @return 是否可行
     */

    private boolean tryMove(int x, int y, AStarNode parent) {
        if (!isMoveAble(x, y))
        {
            // 如果地图无法通过则退出
            return false;
        }

        AStarNode node = nodes[y][x];

        if ((node.modified & 4) != 0)
        {
            // 已在close中忽略
            return false;
        }

        int G = parent.G + getG(parent.pos.x, parent.pos.y, x, y);

        if ((node.modified & 2) != 0)
        {
            // 已在open中判断G值是否更小
            if (G >= node.G)
            {
                return true;
            }

            // 从open中清除重新插入
            if (node.next != null)
            {
                node.next.prev = node.prev;
            }

            if (node.prev != null)
            {
                node.prev.next = node.next;
            }
            else
            {
                open = node.next;
            }

            openNum--;
            node.modified = 1;
            node.parent = parent;
            node.F = (node.G = G) + node.H;
            addToOpenQueue(node);
        }
        else
        {
            if (node.modified == 0)
            {
                // 记录这个修改过的点以还原
                modified[modified_++] = node;
            }

            node.modified = 1;
            node.parent = parent;
            node.F = (node.G = G) + (node.H = getH(x, y, endX, endY));
            addToOpenQueue(node);
        }

        return true;
    }

    /**
     * 检查地图是否可以移动到此位置<br>
     * 可自行修改算法
     */

    protected boolean isMoveAble(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height)
        {
            return false;
        }

        return map.getCell(x, y) == 0;
    }

    /**
     * 计算某一点向各方向移动的代价<br>
     * 可自行修改算法
     */

    protected int getG(int x1, int y1, int x2, int y2) {
        return 1;
    }

    /**
     * 计算两点之间移动的估算成本<br>
     * 可自行修改算法
     */

    protected int getH(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return (dx + dy) << 3;
    }

    /**
     * 路径寻找主函数
     * 
     * @param startX,startY 起点坐标
     * @param endX,endY 终点坐标
     * @return 是否成功寻到路径
     */

    public final boolean findPath(int startX, int startY, int endX, int endY) {
        AStarNode root;
        int minH;
        boolean ok = true;
        int x, y;

        clearModifiedNodes();
        root = nodes[startY][startX];
        root.G = 0;
        root.F = root.H = getH(startX, startY, endX, endY);
        root.parent = null;
        root.modified = 1;
        modified[modified_++] = root;
        addToOpenQueue(root);

        minPos = new Point(startX, startY);
        minH = root.H;
        this.endX = endX;
        this.endY = endY;

        while (ok)
        {
            // 取出open队列F值最小的节点放入close中
            ok = getFromOpenQueue();
            // 得到刚才取出的节点
            root = close;

            if (!ok || root == null)
            {
                return false;
            }

            x = root.pos.x;
            y = root.pos.y;
            if (root.H < minH)
            {
                // 找到一个估计离终点最近的点
                minH = root.H;
                minPos = root.pos;
            }

            if (x == endX && y == endY)
            {
                // 如果走到终点了
                minPos = root.pos;
                return true;
            }
            else
            {
                boolean b = false;
                if ((directionMask & 0x01) != 0) b |= tryMove(x, y - 1, root);
                if ((directionMask & 0x02) != 0) b |= tryMove(x + 1, y - 1, root);
                if ((directionMask & 0x04) != 0) b |= tryMove(x + 1, y, root);
                if ((directionMask & 0x08) != 0) b |= tryMove(x + 1, y + 1, root);
                if ((directionMask & 0x10) != 0) b |= tryMove(x, y + 1, root);
                if ((directionMask & 0x20) != 0) b |= tryMove(x - 1, y + 1, root);
                if ((directionMask & 0x40) != 0) b |= tryMove(x - 1, y, root);
                if ((directionMask & 0x80) != 0) b |= tryMove(x - 1, y - 1, root);

                if (!b)
                {
                    // 如果不是通路则从close中取
                    if (!getFromCloseQueue())
                    {
                        return false;
                    }
                }
            }

            if (openNum >= openMaxNum || closeNum >= closeMaxNum)
            {
                return true;
            }
        }

        return ok;
    }

    /**
     * 返回从起点移动到终点所需的步数
     */

    public int getStep() {
        return nodes[minPos.y][minPos.x].G;
    }

    /**
     * 返回从起点移动到终点经过的路径，包含起点和终点（也可能是最接近终点的点）
     */

    public Point[] getPath() {
        LinkedList<Point> list = new LinkedList<Point>();
        AStarNode p;
        int i = 0;
        for (p = nodes[minPos.y][minPos.x]; p != null && i < size; p = p.parent, i++)
        {
            list.addFirst(p.pos);
        }

        return list.toArray(new Point[list.size()]);
    }

    /**
     * 返回各路径点对应的方向
     * 
     * @return 0-7为从上开始顺时针八个方向，8代表无方向
     */

    public static int[] getDirection(Point... pos) {
        if (pos == null || pos.length == 0)
        {
            return null;
        }

        int len = pos.length;
        int[] inc2r = { 7, 0, 1, 6, 8, 2, 5, 4, 3, 0 };
        int[] dirs = new int[len];
        int x = pos[len - 1].x;
        int y = pos[len - 1].y;
        int ix, iy;
        for (int i = len - 1; i >= 0; i--)
        {
            ix = pos[i].x;
            iy = pos[i].y;
            dirs[i] = inc2r[(y - iy + 1) * 3 + x - ix + 1];
            x = ix;
            y = iy;
        }

        return dirs;
    }
}