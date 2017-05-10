package engine.water;

import android.graphics.Bitmap;

/**
 * 水波纹渲染器
 * @author yanhao
 * @version 1.0
 */

public class WaterRender {
	
	/**
	 * 装载动态库"libwater.so"
	 */
	
	static
	{
		System.loadLibrary("water");
	}
	
	/**
	 * 设置源图片
	 * @param src
	 */
	
	public static native void setBitmap(Bitmap src);
	
	/**
	 * 绘制目标图片
	 * @param dst
	 */
	
    public static native void render(Bitmap dst);
    
    /**
     * 水滴溅射效果
     * @param x,y 波源位置
     * @param size 波源半径
     * @param height 波源能量
     */
    
    public static native void drop(int x, int y, int size, int height);
    
    /**
     * 水波轻滑效果
     * @param x,y 波源位置
     * @param size 波源半径
     * @param height 波源能量
     */
    
    public static native void flip(int x, int y, int size, int height);
    
    /**
     * 4.0以上需实现JNI_OnLoad
     */

}