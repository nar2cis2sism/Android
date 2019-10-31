package engine.android.util.file;

/**
 * 描述文件的大小及单位
 * 
 * @author Daimon
 * @since 9/26/2012
 */
public class FileSize {

    private float size;

    private Unit unit;

    public FileSize(float size) {
        this(size, Unit.B);
    }

    public FileSize(float size, Unit unit) {
        this.size = size;
        this.unit = unit;
    }

    public float getSize() {
        return size;
    }

    public Unit getUnit() {
        return unit;
    }

    /**
     * 换算成对应单位大小
     */
    public FileSize toUnit(Unit unit) {
        size = size * this.unit.size / (this.unit = unit).size;
        return this;
    }

    /**
     * 单位自动转换
     * 
     * @see #convert(long, int)
     */
    public static FileSize convert(long size) {
        return convert(size, 0);
    }

    /**
     * 单位自动转换
     * 
     * @param size 以字节为单位
     * @param threshold 默认1024，即小于此阀值的大小转换成对应单位数值
     */
    public static FileSize convert(long size, int threshold) {
        if (threshold <= 0) threshold = 1024;
        for (Unit unit : Unit.values())
        {
            if (size < threshold * unit.size)
            {
                return new FileSize(size).toUnit(unit);
            }
        }

        throw new IllegalArgumentException("大小已超出承受范围");
    }

    @Override
    public FileSize clone() {
        return new FileSize(size, unit);
    }

    private static final long KB = 1024;

    private static long SIZE = 1;

    public static enum Unit {

        B(SIZE), K(SIZE *= KB), M(SIZE *= KB), G(SIZE *= KB), T(SIZE *= KB), P(SIZE *= KB);

        private final long size;

        private Unit(long size) {
            this.size = size;
        }

        public long size() {
            return size;
        }
    }
}