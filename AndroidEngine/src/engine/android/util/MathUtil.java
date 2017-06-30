package engine.android.util;

/**
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class MathUtil {

    /**
     * Returns the smallest power of two >= its argument, with several caveats:
     * If the argument is negative but not Integer.MIN_VALUE, the method returns
     * zero. If the argument is > 2^30 or equal to Integer.MIN_VALUE, the method
     * returns Integer.MIN_VALUE. If the argument is zero, the method returns
     * zero.
     */
    public static int roundUpToPowerOfTwo(int i) {
        i--; // If input is a power of two, shift its high-order bit right

        // "Smear" the high-order bit all the way to the right
        i |= i >>> 1;
        i |= i >>> 2;
        i |= i >>> 4;
        i |= i >>> 8;
        i |= i >>> 16;

        return i + 1;
    }
    
    /**
     * 计算进度值
     */
    public static float getPercent(float min, float max, float value) {
        float span = max - min;
        return (value - min) / span;
    }

    /**
     * 获取序列数组（依次从最小值排列至最大值）
     */
    public static int[] getSequence(int min, int max) {
        int[] sequence = new int[max - min + 1];
        for (int i = 0; i < sequence.length; i++)
        {
            sequence[i] = min + i;
        }

        return sequence;
    }
}