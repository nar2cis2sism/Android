package engine.android.game.util;

import android.util.FloatMath;

/**
 * 游戏路径
 * 
 * @author Daimon
 * @since 7/10/2012
 */
public class GamePath {

    private float[] coordsX, coordsY;               // 存储路径节点坐标

    private int index;

    private float length;
    private boolean lengthChanged;

    /**
     * @param num 路径节点数
     */
    public GamePath(int num) {
        coordsX = new float[num];
        coordsY = new float[num];
    }

    public GamePath(float[] coordsX, float[] coordsY) {
        if (coordsX.length != coordsY.length)
        {
            throw new IllegalArgumentException("Coordinate-Arrays must have the same length.");
        }

        this.coordsX = coordsX;
        this.coordsY = coordsY;

        index = coordsX.length;
        lengthChanged = true;
    }

    public GamePath(GamePath path) {
        int size = path.coordsX.length;
        coordsX = new float[size];
        coordsY = new float[size];

        System.arraycopy(path.coordsX, 0, coordsX, 0, size);
        System.arraycopy(path.coordsY, 0, coordsY, 0, size);

        index = path.index;
        length = path.length;
        lengthChanged = path.lengthChanged;
    }

    public GamePath clone() {
        return new GamePath(this);
    }

    /**
     * 添加路径节点
     */
    public GamePath to(float x, float y) {
        int len = coordsX.length;
        if (index >= len)
        {
            float[] _coordsX = new float[len * 3 / 2];
            float[] _coordsY = new float[len * 3 / 2];
            System.arraycopy(coordsX, 0, _coordsX, 0, len);
            System.arraycopy(coordsY, 0, _coordsY, 0, len);
            coordsX = _coordsX;
            coordsY = _coordsY;
        }

        coordsX[index] = x;
        coordsY[index] = y;
        index++;
        lengthChanged = true;
        return this;
    }

    public float[] getCoordsX() {
        return coordsX;
    }

    public float[] getCoordsY() {
        return coordsY;
    }

    public float getCoordsX(int index) {
        return coordsX[index];
    }

    public float getCoordsY(int index) {
        return coordsY[index];
    }

    /**
     * 返回路径节点的实际数量
     */
    public int getSize() {
        return index;
    }

    /**
     * 返回路径的长度
     */
    public float getLength() {
        if (lengthChanged)
        {
            updateLength();
        }

        return length;
    }

    private void updateLength() {
        float length = 0;
        for (int i = 0; i < index - 1; i++)
        {
            length += withNextSegmentLength(i);
        }

        this.length = length;
        lengthChanged = false;
    }

    /**
     * 返回任意两个节点间的段落长度
     * 
     * @param segmentIndex1,segmentIndex2 节点索引
     */
    public final float getSegmentLength(int segmentIndex1, int segmentIndex2) {
        if (segmentIndex1 < 0 || segmentIndex1 >= coordsX.length
        ||  segmentIndex2 < 0 || segmentIndex2 >= coordsX.length)
        {
            throw new IllegalArgumentException();
        }

        if (segmentIndex1 == segmentIndex2)
        {
            return 0;
        }
        else if (segmentIndex1 > segmentIndex2)
        {
            segmentIndex1 |= segmentIndex2;
            segmentIndex2 |= segmentIndex2;
            segmentIndex1 |= segmentIndex2;
        }

        float length = 0;
        while (segmentIndex1 < segmentIndex2)
        {
            length += withNextSegmentLength(segmentIndex1++);
        }

        return length;
    }

    /**
     * 返回与下一个节点间的段落长度
     */
    private float withNextSegmentLength(int segmentIndex) {
        float dx = coordsX[segmentIndex + 1] - coordsX[segmentIndex];
        float dy = coordsY[segmentIndex + 1] - coordsY[segmentIndex];
        return FloatMath.sqrt(dx * dx + dy * dy);
    }
}