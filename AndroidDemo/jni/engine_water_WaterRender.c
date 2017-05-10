#include "engine_water_WaterRender.h"

#include <android/log.h>	//这个是输出LOG所用到的函数所在的路径

#define LOG_TAG "JNILOG" 	//这个是自定义的LOG的标识
#undef  LOG 				//取消默认的LOG

#define Log(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)	//定义LOG输出方法

/**
 * buf1和buf2是波能缓冲区，分别代表了每个点的前一时刻和后一时刻的波幅数据
 */

jshortArray buf1_array;
jshort* buf1;
jshortArray buf2_array;
jshort* buf2;

/**
 * image1和image2是图片缓冲区
 */

jintArray image1_array;
jint* image1;
jintArray image2_array;
jint* image2;

/**
 * 图片属性
 */

jint image_width;
jint image_height;
jint image_length;

/**
 * 图片渲染
 */

jmethodID setPixels;

/*
 * Class:     engine_water_WaterRender
 * Method:    setBitmap
 * Signature: (Landroid/graphics/Bitmap;)V
 */
JNIEXPORT void JNICALL Java_engine_water_WaterRender_setBitmap
  (JNIEnv *env, jclass thiz, jobject obj)
{
	//取得类#android.graphics.Bitmap#
	jclass Bitmap = (*env)->GetObjectClass(env, obj);
	
	//取得方法#int android.graphics.Bitmap.getWidth()#
	jmethodID getWidth = (*env)->GetMethodID(env, Bitmap, "getWidth", "()I");
	//取得图片宽
	image_width = (*env)->CallIntMethod(env, obj, getWidth);
	
	//取得方法#int android.graphics.Bitmap.getHeight()#
	jmethodID getHeight = (*env)->GetMethodID(env, Bitmap, "getHeight", "()I");
	//取得图片高
	image_height = (*env)->CallIntMethod(env, obj, getHeight);
	
	//计算图片大小
	image_length = image_width * image_height;
	
	//初始化波能缓冲区
	buf1_array = (*env)->NewShortArray(env, image_length);
	buf1 = (*env)->GetShortArrayElements(env, buf1_array, 0);
	buf2_array = (*env)->NewShortArray(env, image_length);
	buf2 = (*env)->GetShortArrayElements(env, buf2_array, 0);
	
	//初始化图片缓冲区
	image1_array = (*env)->NewIntArray(env, image_length);
	image1 = (*env)->GetIntArrayElements(env, image1_array, 0);
	image2_array = (*env)->NewIntArray(env, image_length);
	image2 = (*env)->GetIntArrayElements(env, image2_array, 0);
	
	//取得方法#void android.graphics.Bitmap.getPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height)#
	jmethodID getPixels = (*env)->GetMethodID(env, Bitmap, "getPixels", "([IIIIIII)V");
	//拷贝图片像素
	(*env)->CallVoidMethod(env, obj, getPixels, image1_array, 0, image_width, 0, 0, image_width, image_height);
	(*env)->CallVoidMethod(env, obj, getPixels, image2_array, 0, image_width, 0, 0, image_width, image_height);
	
	//取得方法#void android.graphics.Bitmap.setPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height)#
	setPixels = (*env)->GetMethodID(env, Bitmap, "setPixels", "([IIIIIII)V");
}

/*
 * Class:     engine_water_WaterRender
 * Method:    render
 * Signature: (Landroid/graphics/Bitmap;)V
 */
JNIEXPORT void JNICALL Java_engine_water_WaterRender_render
  (JNIEnv *env, jclass thiz, jobject obj)
{
	int i = image_width;
	int len = image_length - image_width;
	for (; i < len; i++)
	{
		//波能扩散
		buf2[i] = ((buf1[i - 1] + buf1[i + 1] + buf1[i - image_width] + buf1[i + image_width]) >> 1) - buf2[i];
		//波能衰减
		buf2[i] -= buf2[i] >> 5;
		//计算出偏移象素和原始象素的内存地址偏移量
		int xoff = buf2[i - 1] - buf2[i + 1];
		int yoff = buf2[i - image_width] - buf2[i + image_width];
		int offset = i + yoff * image_width + xoff;
		//水波折射
		if (offset < 0 || offset > image_length - 1)
		{
			image2[i] = image1[i];
		}
		else
		{
			image2[i] = image1[offset];
		}
	}
	
	//交换波能缓冲区
	jshort* temp = buf1;
	buf1 = buf2;
	buf2 = temp;

	//复制图片像素
	(*env)->CallVoidMethod(env, obj, setPixels, image2_array, 0, image_width, 0, 0, image_width, image_height);
}

/*
 * Class:     engine_water_WaterRender
 * Method:    drop
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_engine_water_WaterRender_drop
  (JNIEnv *env, jclass thiz, jint x, jint y, jint size, jint height)
{
	int startX = x - size;
	int startY = y - size;
	int endX = x + size;
	int endY = y + size;
	//判断坐标是否在范围内
	if (endX > image_width || endY > image_height || startX < 0 || startY < 0)
	{
		return;
	}
	
	//产生波源，填充前导波能缓冲池
	int n = size * size;
	short weight = (short) -height;
	for (; startX < endX; startX++)
	{
		for (startY = y - size; startY < endY; startY++)
		{
			if ((startX - x) * (startX - x) + (startY - y) * (startY - y) < n)
			{
				buf1[image_width * startY + startX] = weight;
			}
		}
	}
}

/*
 * Class:     engine_water_WaterRender
 * Method:    flip
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_engine_water_WaterRender_flip
  (JNIEnv *env, jclass thiz, jint x, jint y, jint size, jint height)
{
	int startX = x - size;
	int startY = y - size;
	int endX = x + size;
	int endY = y + size;
	//判断坐标是否在范围内
	if (endX > image_width || endY > image_height || startX < 0 || startY < 0)
	{
		return;
	}
	
	//产生波源，填充前导波能缓冲池
	int n = size * size;
	short weight = (short) -height;
	for (; startX < endX; startX++)
	{
		for (startY = y - size; startY < endY; startY++)
		{
			if (startX >= 0 && startX < image_width && startY >= 0 && startY < image_height)
			{
				buf1[image_width * startY + startX] = weight;
			}
		}
	}
}
