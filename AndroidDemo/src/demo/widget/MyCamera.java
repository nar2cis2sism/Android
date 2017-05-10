package demo.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import engine.android.util.AndroidUtil;

import java.io.IOException;
import java.util.List;

/**
 * 自定义照相机（支持录制视频）<br>
 * 需要声明权限<uses-permission android:name="android.permission.CAMERA" />
 * @author Daimon
 * @version 3.0
 * @since 4/11/2012
 */

public class MyCamera extends SurfaceView implements Callback, OnGlobalLayoutListener {
	
	private SurfaceHolder holder;								//相机处理器
	
	private Camera camera;										//照相机
	
	private boolean isPreview;									//预览模式
	
	private ImageCallback iCallback;							//自定义拍照回调接口
	
	private VideoCallback vCallback;							//自定义视频流回调接口（实际指的是预览帧）
	
	private CameraCallback cCallback;							//自定义照相机回调接口
	
	private MediaRecorder recorder;								//视频记录器
	
	private boolean isRecording;								//视频录制模式
	
	private String location;									//视频录制文件路径
	
	private boolean paused;                                     //暂停开关
	
	private int width, height;
	
    private int numOfCamera = 1;
	private static final int UNAVAILABLE_CAMERAID = -1;
	private int backCameraId = UNAVAILABLE_CAMERAID;
	private int frontCameraId = UNAVAILABLE_CAMERAID;
	private int cameraId = 0;
	
	private Point pictureSize;
	private PictureSizeSetting pictureSizeSetting;

	public MyCamera(Context context) {
		super(context);
		init();
	}
	
	public MyCamera(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyCamera(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init()
	{
		//添加回调函数
		(holder = getHolder()).addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置类型
		setKeepScreenOn(true);
		
		getViewTreeObserver().addOnGlobalLayoutListener(this);
		
		if (AndroidUtil.getVersion() >= 9)
		{
		    numOfCamera = Camera.getNumberOfCameras();
	        CameraInfo cameraInfo = new CameraInfo();
	        for (int i = 0; i < numOfCamera; i++)
	        {
	            Camera.getCameraInfo(i, cameraInfo);
	            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK && backCameraId == UNAVAILABLE_CAMERAID)
	            {
	                backCameraId = i;
	            }
	            else if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT && frontCameraId == UNAVAILABLE_CAMERAID)
                {
                    frontCameraId = i;
                }
	        }
		}
	}
	
	/**
	 * 暂停使用相机（外部调用）
	 * @param paused true：释放相机资源 - false：laze launch camera
	 */
	
	public final void setPaused(boolean paused)
	{
        if (this.paused = paused)
        {
            releaseCamera();
        }
    }
	
	public void setPictureSize(int width, int height)
	{
	    if (width < height)
	    {
	        width  = width ^ height;
	        height = width ^ height;
	        width  = width ^ height;
	    }
	    
	    pictureSize = new Point(width, height);
	}
    
    public void setPictureSize(PictureSizeSetting setting)
    {
        pictureSizeSetting = setting;
    }
	
	/**
	 * 初始化相机
	 */
	
	private void initCamera()
	{
	    if (camera == null)
        {
            try {
                camera = Camera.open();//开启相机，不能放在构造函数中，不然不会显示画面
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            cameraOpened(camera);
            
            if (cCallback != null)
            {
                cCallback.CameraCreated(camera);
            }
        }
	}
	
	/**
	 * 释放相机资源
	 */
	
	private void releaseCamera()
	{
	    if (camera != null)
	    {
	        closeCamera();
	        
	        if (cCallback != null)
	        {
	            cCallback.CameraDestroyed();
	        }
	    }
	}

	/**
	 * 判断是否支持摄像功能（仅判断后置摄像头）
	 */
	
	public static boolean supportCamera(Context context)
	{
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}
    
    /**
     * 获取摄像头数量
     */
    
    public int getNumberOfCameras()
    {
        return numOfCamera;
    }
    
    /**
     * 判断是否支持前置摄像头
     */
	
	public boolean supportFrontCamera()
	{
	    return frontCameraId != UNAVAILABLE_CAMERAID;
	}
    
    /**
     * 打开前置摄像头
     */
	
	public void openFrontCamera()
	{
	    if (frontCameraId != UNAVAILABLE_CAMERAID)
	    {
	        openCamera(frontCameraId);
	    }
	}
	
	/**
	 * 打开后置摄像头
	 */
    
    public void openBackCamera()
    {
        if (backCameraId != UNAVAILABLE_CAMERAID)
        {
            openCamera(backCameraId);
        }
    }
    
    /**
     * 切换摄像头
     */
    
    public void switchCamera()
    {
        if (numOfCamera > 1)
        {
            openCamera(++cameraId % numOfCamera);
        }
    }
    
    private void openCamera(int cameraId)
    {
        closeCamera();
        try {
            camera = Camera.open(this.cameraId = cameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        cameraOpened(camera);
        
        if (cCallback != null)
        {
            cCallback.CameraChanged(cameraId, camera);
        }
        
        //开始预览
        showPreview(true);
    }
	
	public void setImageCallback(ImageCallback iCallback)
	{
		this.iCallback = iCallback;
	}
	
	/**
	 * 此功能主要用来实现实时视频
	 */
	
	public void setVideoCallback(VideoCallback vCallback)
	{
		this.vCallback = vCallback;
	}
	
	public void setVideoPath(String location)
	{
		this.location = location;
	}
    
    public void setCameraCallback(CameraCallback cCallback)
    {
        this.cCallback = cCallback;
    }
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_CAMERA)
		{
			takePicture();
			return true;
		}
		
		return super.onKeyUp(keyCode, event);
	}
	
	/**
	 * 拍照
	 */
    
    public synchronized void takePicture()
    {
        if (camera != null)
        {
            if (isPreview && !isRecording)
            {
                camera.takePicture(shutter, null, jpeg);
                isPreview = false;
            }
        }
    }
	
	/**
	 * 显示预览图像
	 * @param start 显示开关
	 */
	
	public synchronized void showPreview(boolean start)
	{
		if (start)
		{
			if (!isPreview)
			{
				if (camera != null)
				{
					camera.startPreview();
					isPreview = true;
				}
			}
		}
		else
		{
			if (isPreview)
			{
				if (camera != null)
				{
					camera.stopPreview();
					isPreview = false;
				}
			}
		}
	}
	
	/**
	 * 录制视频
	 * @param start 录制开关
	 */
	
	public synchronized void recordVideo(boolean start)
	{
		if (start)
		{
			if (!isRecording)
			{
				if (prepareRecorder())
				{
					recorder.start();
					isRecording = true;
				}
			}
		}
		else
		{
			if (isRecording)
			{
				if (recorder != null)
				{
					recorder.stop();
					releaseRecorder();
				}
			}
		}
	}
	
	private boolean prepareRecorder()
	{
		if (recorder == null)
		{
			recorder = new MediaRecorder();
			
			if (camera != null)
			{
				camera.unlock();
				recorder.setCamera(camera);
			}
			
			recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
			
			recorder.setOutputFile(location);
			recorder.setMaxDuration(30 * 1000);//30秒
			recorder.setMaxFileSize(30 * 1024);//30K
			
			recorder.setPreviewDisplay(holder.getSurface());
			
			try {
				recorder.prepare();
			} catch (Exception e) {
				e.printStackTrace();
				releaseRecorder();
				return false;
			}
		}
		
		return true;
	}
	
	private void releaseRecorder()
	{
		if (recorder != null)
		{
			recorder.reset();
			recorder.release();
			recorder = null;
			if (camera != null)
			{
				camera.lock();
			}

			isRecording = false;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		//开始预览
		showPreview(true);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        System.out.println("surfaceCreated");
        if (!paused)
        {
            initCamera();
        }
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println("surfaceDestroyed");
        if (!paused)
        {
            releaseCamera();
        }
	}
    
    private void cameraOpened(Camera camera)
    {
        if (camera != null)
        {
            //初始化相机参数
            Parameters params = camera.getParameters();
            List<Integer> pictureFormats = params.getSupportedPictureFormats();
            if (pictureFormats.contains(PixelFormat.JPEG))
            {
                params.setPictureFormat(PixelFormat.JPEG);//设置照片格式
                params.setJpegQuality(100);//设置照片质量
            }
            else
            {
                params.setPictureFormat(pictureFormats.get(0));
            }
            
            params.setRotation(0);//设置照片旋转角度(此方法在某些手机上无用)
            camera.setParameters(params);
            
            updateCameraSize(camera);

            try {
                //设置预览
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (vCallback != null)
            {
                //捕获视频流,Android默认为压缩的YUV420SP格式
                camera.setPreviewCallback(pc);
            }
        }
    }
    
    private void updateCameraSize(Camera camera)
    {
        //设置摄像头参数
        Parameters params = camera.getParameters();
        Size screenSize = camera.new Size(width, height);
        System.out.println("screenSize：" + screenSize.width + "*" + screenSize.height);
        
        //判断相片分辨率
        List<Size> list = params.getSupportedPictureSizes();
        Size pictureSize = getOptimalPictureSize(list);
        if (pictureSize == null)
        {
            pictureSize = screenSize;
        }
        
        //判断预览窗口分辨率
        list = params.getSupportedPreviewSizes();
        Size previewSize = getOptimalPreviewSize(list, pictureSize);
        if (previewSize == null)
        {
            //确保分辨率为8的倍数
            previewSize = camera.new Size(screenSize.width >> 3 << 3, screenSize.height >> 3 << 3);
        }
        
        System.out.println("pictureSize：" + pictureSize.width + "*" + pictureSize.height);
        params.setPictureSize(pictureSize.width, pictureSize.height);
        System.out.println("previewSize：" + previewSize.width + "*" + previewSize.height);
        params.setPreviewSize(previewSize.width, previewSize.height);
        camera.setParameters(params);
        //更新布局尺寸
        updateLayout(previewSize);
    }
    
    private Size getOptimalPictureSize(List<Size> list)
    {
        if (pictureSize != null)
        {
            Size size = camera.new Size(pictureSize.x, pictureSize.y);
            if (list.contains(size))
            {
                return size;
            }
        }
        
        if (pictureSizeSetting != null)
        {
            Size size = pictureSizeSetting.getPictureSize(list);
            if (size != null)
            {
                return size;
            }
        }

        int w = Integer.MAX_VALUE, h = Integer.MAX_VALUE;
        int newW, newH;
        //找一个最小的分辨率
        for (Size size : list)
        {
            newW = size.width;
            newH = size.height;
            
            if (newW < 320)
            {
                continue;
            }
            
            if (newW < w || newH < h)
            {
                w = newW;
                h = newH;
            }
        }
        
        if (w > 0 && h > 0)
        {
            return camera.new Size(w, h);
        }
        
        return null;
    }
	
	private Size getOptimalPreviewSize(List<Size> list, Size pictureSize)
	{
		int w = 0, h = 0, diff = Integer.MAX_VALUE;
		int newW, newH, newDiff;
		//找一个符合相片比例的最小的分辨率
		for (Size size : list)
		{
		    newW = size.width;
            newH = size.height;
			
			if (newW < 640)
			{
				continue;
			}
			
			newDiff = (int) Math.abs(((float) newW / newH - 
			        (float) pictureSize.width / pictureSize.height) * 100);
			if (newDiff == diff && newW < w)
			{
				w = newW;
				h = newH;
			}
			else if (newDiff < diff)
			{
				w = newW;
				h = newH;
				diff = newDiff;
			}
		}
        
        if (w > 0 && h > 0)
        {
            return camera.new Size(w, h);
        }
        
        return null;
	}
	
	private void updateLayout(Size previewSize)
    {
	    final int width = this.width;
	    final int height = this.height;
        
        LayoutParams params = getLayoutParams();
        if (params == null)
        {
            params = new LayoutParams(width, height);
        }
    
        int previewWidth = previewSize.width;
        int previewHeight = previewSize.height;
        
        if (width < height)
        {
            previewWidth  = previewWidth ^ previewHeight;
            previewHeight = previewWidth ^ previewHeight;
            previewWidth  = previewWidth ^ previewHeight;
        }
        
        if (width * previewHeight > height * previewWidth)
        {
            final int scaledWidth = previewWidth * height / previewHeight;
            params.width = scaledWidth;
            params.height = height;
        }
        else
        {
            final int scaleHeight = previewHeight * width / previewWidth;
            params.width = width;
            params.height = scaleHeight;
        }
    
        System.out.println("surfaceSize：" + params.width + "*" + params.height);
        setLayoutParams(params);
    }

    private void closeCamera()
    {
        if (camera != null)
        {
            recordVideo(false);
            showPreview(false);
            camera.setPreviewCallback(null);
            camera.release();//释放相机资源
            camera = null;
        }
    }

    /**
	 * 在相机快门关闭时候的回调接口，通过这个接口来通知用户快门关闭的事件<br>
	 * 普通相机在快门关闭的时候都会发出响声，根据需要可以在该回调接口中定义各种动作，例如：使设备震动
	 */
	
	private ShutterCallback shutter = new ShutterCallback() {

		@Override
		public void onShutter() {
			System.out.println("shutter");
		}};
		
	/**
	 * 拍照的回调接口<br>
	 * 获取照片
	 */
		
	private PictureCallback jpeg = new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if (iCallback != null && data != null)
			{
			    Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
    			iCallback.takePicture(image);
			}
		}
	};
	
	/**
	 * 视频流回调接口<br>
	 * 获取帧图片
	 */
		
	private PreviewCallback pc = new PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (vCallback != null && data != null && camera != null)
			{
				Size size = camera.getParameters().getPreviewSize();
				byte[] rgbBuf = new byte[size.width * size.height * 3];
				decodeYUV420SP(rgbBuf, data, size.width, size.height);
				int[] colors = new int[size.width * size.height];
				for (int i = 0; i < colors.length; i++)
				{
					colors[i] = Color.rgb(rgbBuf[i * 3] & 0xff, rgbBuf[i * 3 + 1] & 0xff, rgbBuf[i * 3 + 2] & 0xff);
				}
				
				vCallback.takeFrame(Bitmap.createBitmap(colors, size.width, size.height, Config.RGB_565));
			}
		}};
		
	/**
	 * 把YUV420SP的视频流转换成RGB格式的图像，用于图像识别
	 * @param rgbBuf
	 * @param yuv420sp
	 * @param width
	 * @param height
	 */
	
	private static void decodeYUV420SP(byte[] rgbBuf, byte[] yuv420sp, int width, int height)
	{
		if (rgbBuf == null)
		{
			throw new NullPointerException("buffer 'rgbBuf' is null");
		}
		
		int frameSize = width * height;
		if (rgbBuf.length < frameSize * 3)
		{
			throw new IllegalArgumentException("buffer 'rgbBuf' size " + rgbBuf.length + " < minimum " + frameSize * 3);
		}
		
		if (yuv420sp == null)
		{
			throw new NullPointerException("buffer 'yuv420sp' is null");
		}

		if (yuv420sp.length < frameSize * 3 / 2)
		{
			throw new IllegalArgumentException("buffer 'yuv420sp' size " + yuv420sp.length + " < minimum " + frameSize * 3 / 2);
		}
		
		int uvp,u,v,y;
		int y1192,r,g,b;
		for (int i, j = 0, yp = 0; j < height; j++)
		{
			uvp = frameSize + (j >> 1) * width;
			u = 0;
			v = 0;
			for (i = 0; i < width; i++, yp++)
			{
				y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
				{
					y = 0;
				}
				
				if ((i & 1) == 0)
				{
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}
				
				y1192 = 1192 * y;
				r = (y1192 + 1634 * v);
				g = (y1192 - 833 * v - 400 * u);
				b = (y1192 + 2066 * u);
				
				if (r < 0)
				{
					r = 0;
				}
				else if (r > 262143)
				{
					r = 262143;
				}
				
				if (g < 0)
				{
					g = 0;
				}
				else if (g > 262143)
				{
					g = 262143;
				}
				
				if (b < 0)
				{
					b = 0;
				}
				else if (b > 262143)
				{
					b = 262143;
				}
				
				rgbBuf[yp * 3] = (byte)(r >> 10);
				rgbBuf[yp * 3 + 1] = (byte)(g >> 10);
				rgbBuf[yp * 3 + 2] = (byte)(b >> 10);
			}
		}
	}
	
	/**
	 * 自定义拍照回调接口
	 * @author Daimon
	 * @version 3.0
	 * @since 4/11/2012
	 */
	
	public static interface ImageCallback {
		
		public void takePicture(Bitmap image);
		
	}
	
	/**
	 * 自定义视频流回调接口
	 * @author Daimon
	 * @version 3.0
	 * @since 4/11/2012
	 */
	
	public static interface VideoCallback {
		
		public void takeFrame(Bitmap image);
		
	}
    
    /**
     * 自定义照相机回调接口
     * @author Daimon
     * @version 3.0
     * @since 4/11/2012
     */
	
	public static interface CameraCallback {
	    
	    /**
	     * 创建默认（后置）摄像头并打开
	     */
	    
	    public void CameraCreated(Camera camera);
        
        /**
         * 切换摄像头并打开
         */
	    
	    public void CameraChanged(int cameraId, Camera camera);
        
        /**
         * 关闭摄像头并销毁
         */
        
        public void CameraDestroyed();
	}
	
	/**
	 * 自定义相片尺寸设置
     * @author Daimon
     * @version 3.0
     * @since 3/26/2013
	 */
	
	public static interface PictureSizeSetting {
	    
	    public Size getPictureSize(List<Size> list);
	}

    @Override
    public void onGlobalLayout() {
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
        
        width = getWidth();
        height = getHeight();
    }
}