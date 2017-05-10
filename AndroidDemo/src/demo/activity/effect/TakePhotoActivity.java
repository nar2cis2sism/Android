package demo.activity.effect;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import demo.android.R;
import demo.widget.MyCamera;
import demo.widget.RotateImageView;
import demo.widget.ToggleSwitcher;
import demo.widget.MyCamera.CameraCallback;
import demo.widget.MyCamera.ImageCallback;
import demo.widget.MyCamera.PictureSizeSetting;
import demo.widget.ToggleSwitcher.OnSwitchChangeListener;
import engine.android.core.ApplicationManager;
import engine.android.util.AlarmTimer.IdleTimer;
import engine.android.util.AndroidUtil;
import engine.android.util.RectUtil;
import engine.android.util.image.ImageUtil;
import engine.android.util.listener.MyOrientationEventListener;
import engine.android.util.listener.MyOrientationEventListener.ScreenOrientation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TakePhotoActivity extends Activity implements CameraCallback, Runnable {
    
    MyOrientationEventListener orientationEventListener;
    CameraUtil cameraUtil;
    IdleTimer idleTimer;

    ImageView photo;
    View preview;
    MyPreview myPreview;
    MyCamera camera;
    RotateImageView switch_camera;
    RotateImageView flash_mode;
    RotateImageView take_photo;
    View flash_mode_view;
    ToggleSwitcher switch_mode;
    RotateImageView camera_mode;
    RotateImageView video_mode;
	
	boolean isPreview;
	
	ScreenOrientation screenOrientation_init;
	ScreenOrientation screenOrientation_last;
    int degrees;
	
	boolean supportFocus = false;
	
	/**
                正在聚焦中->不能再次聚焦
                正在聚焦中->拍照->聚焦结束后自动拍照
                正在聚焦中->不能切换相机
                
                聚焦之前->拍照->先聚焦后拍照
                聚焦之后->拍照->直接拍照
                
                正在拍照中->不能再次拍照
                正在拍照中->不能聚焦->拍照结束后聚焦复原
                正在拍照中->不能操作其他按钮
                
                正在切换相机->不能再次切换
                正在切换相机->不能聚焦
                正在切换相机->不能拍照
                正在切换相机->不能操作其他按钮
	 */
	
	AtomicBoolean focusing = new AtomicBoolean();
	AtomicBoolean focused = new AtomicBoolean();
	AtomicBoolean takingPicture = new AtomicBoolean();
	AtomicBoolean switchingCamera = new AtomicBoolean();
	AtomicBoolean takePictureAfterFocus = new AtomicBoolean();
	
	static final String FLASH_MODE = "flash_mode";
	List<Map<String, Object>> flashData;
	SimpleAdapter flashAdapter;

	boolean isFrontCamera = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//为了向下兼容，必须强制横屏（竖屏需要修正预览方向）
		AndroidUtil.setLandscapeMode(this);
		
		if (!MyCamera.supportCamera(this))
		{
		    ApplicationManager.showMessage("设备不支持摄像功能");
		    finish();
		    return;
		}
		
		orientationEventListener = new MyOrientationEventListener(this) {
		    
		    @Override
		    public void onOrientationChanged(int orientation,
		            ScreenOrientation screenOrientation) {
		        if (screenOrientation_last != screenOrientation)
		        {
		            screenOrientation_last = screenOrientation;
		            rotateView(switch_camera, orientation, screenOrientation);
		            rotateView(flash_mode, orientation, screenOrientation);
		            rotateView(take_photo, orientation, screenOrientation);
		            rotateView(camera_mode, orientation, screenOrientation);
		            rotateView(video_mode, orientation, screenOrientation);
	                myPreview.invalidate();
		        }
		    }
		};
		
		idleTimer = IdleTimer.getInstance(this, getClass().getName());
        
        setContentView(R.layout.take_photo);
        
        initView();
        initListener();
        initData();

		showCamera();
	}
	
	private void initView()
	{
        photo = (ImageView) findViewById(R.id.photo);
        preview = (View) findViewById(R.id.preview);
        myPreview = new MyPreview(this);
        ((ViewGroup) findViewById(R.id.myPreview)).addView(myPreview, 
                new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        camera = (MyCamera) findViewById(R.id.camera);
        switch_camera = (RotateImageView) findViewById(R.id.switch_camera);
        flash_mode = (RotateImageView) findViewById(R.id.flash_mode);
        take_photo = (RotateImageView) findViewById(R.id.take_photo);
        flash_mode_view = (View) findViewById(R.id.flash_mode_view);
        switch_mode = (ToggleSwitcher) findViewById(R.id.switch_mode);
        camera_mode = (RotateImageView) findViewById(R.id.camera_mode);
        video_mode = (RotateImageView) findViewById(R.id.video_mode);
	}
	
	private void initListener()
	{
	    camera.setCameraCallback(this);
	    camera.setImageCallback(new ImageCallback() {
            
            @Override
            public void takePicture(Bitmap image) {
                focused.getAndSet(false);
                myPreview.resetFocusArea();
                myPreview.resetFocusSuccess();
                showImage(cameraUtil.rotateImage(image, screenOrientation_last));
            }
        });
	    
	    switch_camera.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (focusing.get() || takingPicture.get())
                {
                    return;
                }
                
                if (switchingCamera.compareAndSet(false, true))
                {
                    if (isFlashShowing())
                    {
                        showFlash(false);
                    }
                    
                    if (isFrontCamera = !isFrontCamera)
                    {
                        camera.openFrontCamera();
                    }
                    else
                    {
                        camera.openBackCamera();
                    }
                }
            }
        });
        
	    flash_mode.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (takingPicture.get())
                {
                    return;
                }
                
                showFlash(!isFlashShowing());
            }
        });
        
	    take_photo.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (isFlashShowing())
                {
                    showFlash(false);
                    return;
                }
                
                if (switchingCamera.get())
                {
                    return;
                }
                
                //拍照
                if (takingPicture.compareAndSet(false, true))
                {
                    if (!supportFocus)
                    {
                        takePicture();
                        return;
                    }
                    
                    if (focusing.get())
                    {
                        takePictureAfterFocus.getAndSet(true);
                        return;
                    }
                    
                    if (focused.get())
                    {
                        takePicture();
                    }
                    else
                    {
                        takePictureAfterFocus.getAndSet(true);
                        myPreview.autoFocus();
                    }
                }
            }
        });
	    
	    switch_mode.setOnSwitchChangeListener(new OnSwitchChangeListener() {
            
            @Override
            public void onSwitchChanged(ToggleSwitcher switcher, boolean isChecked) {
                if (isChecked)
                {
                    //Video mode
                }
                else
                {
                    //Camera mode
                }
            }
        });
	}
	
	private void initData()
	{
        screenOrientation_last = screenOrientation_init = 
                ScreenOrientation.getScreenOrientation(getRequestedOrientation());

        Display display = getWindowManager().getDefaultDisplay();
        final int width = display.getWidth();
        final int height = display.getHeight();
        
        camera.setPictureSize(new PictureSizeSetting() {
            
            @Override
            public Size getPictureSize(List<Size> list) {
                Size optimalSize = null;
                int w = 0, sw = Math.abs(w - width), diff = Integer.MAX_VALUE;
                int newW, newH, newDiff;
                //找一个最接近屏幕分辨率的分辨率
                for (Size size : list)
                {
                    newW = size.width;
                    newH = size.height;
                    
                    newDiff = (int) Math.abs(((float) newW / newH - 
                            (float) width / height) * 100);
                    if (newDiff == diff && Math.abs(newW - width) < sw)
                    {
                        optimalSize = size;
                        w = newW;
                        sw = Math.abs(w - width);
                    }
                    else if (newDiff < diff)
                    {
                        optimalSize = size;
                        w = newW;
                        sw = Math.abs(w - width);
                        diff = newDiff;
                    }
                }
                
                return optimalSize;
            }
        });
        
        if (AndroidUtil.getVersion() >= 8)
        {
            int rotation = display.getRotation();
            
            switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
            }
        }
        
	    if (camera.getNumberOfCameras() < 2 || !camera.supportFrontCamera())
	    {
	        switch_camera.setVisibility(View.GONE);
	    }
	}
	
	private void showCamera()
	{
		switchPreview(true);
		camera.showPreview(true);
		//预览界面显示之后方可拍照
        takingPicture.getAndSet(false);
	}
	
	private void showImage(Bitmap image)
	{
        switchPreview(false);
		photo.setImageBitmap(image);
	}
	
	private void switchPreview(boolean isPreview)
	{
	    this.isPreview = isPreview;
	    preview.setVisibility(isPreview ? View.VISIBLE : View.GONE);
	    photo.setVisibility(!isPreview ? View.VISIBLE : View.GONE);
	}
	
	/*-----------------------------------------------
	 * 这样设计的目的是因为以下方法会被多次调用，为了保证相机不被频繁打开和释放
	 */
	
	@Override
    protected void onResume() {
        System.out.println("onResume");
        idleTimer.start(2, TimeUnit.MINUTES, this);
        camera.setPaused(false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        System.out.println("onPause");
        idleTimer.cancel();
        camera.setPaused(true);
        super.onPause();
    }

    @Override
	protected void onStart() {
        System.out.println("onStart");
        if (screenOrientation_init != ScreenOrientation.SCREEN_ORIENTATION_UNSPECIFIED)
        {
            orientationEventListener.enable();
        }
        
	    super.onStart();
	}
    
    @Override
    protected void onRestart() {
        System.out.println("onRestart");
        super.onRestart();
    }
	
	@Override
    protected void onStop() {
        System.out.println("onStop");
        orientationEventListener.disable();
        super.onStop();
    }
	
	@Override
	protected void onDestroy() {
	    idleTimer.clear();
	    super.onDestroy();
	}
	
	@Override
	public void onUserInteraction() {
	    idleTimer.poke();
	}

    @Override
	public void onBackPressed() {
		if (!isPreview)
		{
			showCamera();
		}
		else
		{
			super.onBackPressed();
		}
	}

    @Override
    public void CameraCreated(Camera camera) {
        System.out.println("CameraCreated");
        if (camera == null)
        {
            if (this.camera.supportFrontCamera())
            {
                switch_camera.setVisibility(View.GONE);
                this.camera.openFrontCamera();
            }
            else
            {
                ApplicationManager.showMessage("开启照相机失败");
                finish();
            }
        }
        else
        {
            initFromCamera(0, camera);
        }
    }

    @Override
    public void CameraChanged(int cameraId, Camera camera) {
        System.out.println("CameraChanged");
        if (camera == null)
        {
            ApplicationManager.showMessage("开启照相机失败");
            if (!switch_camera.isShown())
            {
                finish();
                return;
            }
        }
        else
        {
            initFromCamera(cameraId, camera);
            myPreview.invalidate();
        }
        
        ApplicationManager.getHandler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                switchingCamera.getAndSet(false);
            }
        }, 100);
    }
    
    private void initFromCamera(int cameraId, Camera camera)
    {
        myPreview.resetFocusArea();
        myPreview.resetFocusSuccess();

        System.out.println("cameraLayoutSize：" + this.camera.getWidth() + "*" + this.camera.getHeight());
        
        cameraUtil = new CameraUtil(cameraId, camera);
        supportFocus = cameraUtil.initFocus();
        if (!cameraUtil.supportFlash())
        {
            flash_mode.setVisibility(View.GONE);
        }
        else
        {
            flash_mode.setVisibility(View.VISIBLE);
            initFlashListView(camera.getParameters().getSupportedFlashModes());
            refreshFlashData(cameraUtil.getFlashMode());
        }
    }
    
    private void initFlashListView(List<String> flashModes)
    {
        ListView flash_mode_list = (ListView) findViewById(R.id.flash_mode_list);
        flash_mode_list.setAdapter(getFlashListAdapter(flashModes));
        flash_mode_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Map<String, Object> map = flashData.get(position);
                String flashMode = (String) map.get(FLASH_MODE);
                
                if (!flashMode.equals(cameraUtil.getFlashMode()))
                {
                    refreshFlashData(flashMode);
                    
                    cameraUtil.setFlashMode(flashMode);
                }
                
                showFlash(false);
            }
        });
    }
    
    private ListAdapter getFlashListAdapter(List<String> flashModes)
    {
        String[] from = new String[]{"flash_icon", "flash_text", "flash_choice"};
        int[] to = new int[]{R.id.flash_icon, R.id.flash_text, R.id.flash_choice};
        
        flashData = new ArrayList<Map<String, Object>>();
        if (flashModes.contains(Parameters.FLASH_MODE_OFF))
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(from[0], R.drawable.camera_flash_off);
            map.put(from[1], "关");
            map.put(FLASH_MODE, Parameters.FLASH_MODE_OFF);
            flashData.add(map);
        }
        
        if (flashModes.contains(Parameters.FLASH_MODE_ON))
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(from[0], R.drawable.camera_flash_on);
            map.put(from[1], "开");
            map.put(FLASH_MODE, Parameters.FLASH_MODE_ON);
            flashData.add(map);
        }
        
        if (flashModes.contains(Parameters.FLASH_MODE_AUTO))
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(from[0], R.drawable.camera_flash_auto);
            map.put(from[1], "自动闪光");
            map.put(FLASH_MODE, Parameters.FLASH_MODE_AUTO);
            flashData.add(map);
        }
        
        return flashAdapter = new SimpleAdapter(this, flashData, R.layout.take_photo_flash_list_item, from, to);
    }
    
    @Override
    public void CameraDestroyed() {
        System.out.println("CameraDestroyed");
        supportFocus = false;
        
        focusing.getAndSet(false);
        focused.getAndSet(false);
        takingPicture.getAndSet(false);
        switchingCamera.getAndSet(false);
        takePictureAfterFocus.getAndSet(false);
        
        isFrontCamera = false;
    }
    
    void autoFocus(Rect focusArea)
    {
        if (focusing.compareAndSet(false, true))
        {
            cameraUtil.setFocusArea(focusArea);
            cameraUtil.getCamera().autoFocus(autoFocus);
        }
    }
    
    private AutoFocusCallback autoFocus = new AutoFocusCallback() {
        
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            focusing.getAndSet(false);
            focused.getAndSet(true);
            myPreview.setFocusSuccess(success);
            if (takePictureAfterFocus.compareAndSet(true, false))
            {
                takePicture();
            }
        }
    };
    
    void takePicture()
    {
        camera.takePicture();
    }
    
    void refreshFlashData(String flashMode)
    {
        for (Map<String, Object> map : flashData)
        {
            if (((String) map.get(FLASH_MODE)).equals(flashMode))
            {
                map.put("flash_choice", true);
                flash_mode.setImageResource((Integer) map.get("flash_icon"));
            }
            else
            {
                map.put("flash_choice", false);
            }
        }
        
        flashAdapter.notifyDataSetChanged();
    }
    
    void showFlash(boolean show)
    {
        flash_mode_view.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    boolean isFlashShowing()
    {
        return flash_mode_view.isShown();
    }
    
    void rotateView(RotateImageView view, int orientation, ScreenOrientation screenOrientation)
    {
        ScreenOrientation screenOrientationOfView = (ScreenOrientation) view.getTag();
        if (screenOrientationOfView == null)
        {
            view.setTag(screenOrientationOfView = screenOrientation_init);
        }
        
        if (screenOrientationOfView != screenOrientation)
        {
            view.setTag(screenOrientation);
            int orientationOfView = MyOrientationEventListener.toOrientation(screenOrientationOfView);
            boolean cw = orientation < orientationOfView;
            int angle = Math.abs(orientation - orientationOfView);
            if (angle > 180)
            {
                cw = !cw;
                angle = 360 - angle;
            }
            
            angle = (angle + 45) / 90 * 90;
            
            view.rotateDegree(cw, angle, 400);
        }
    }

    private final class CameraUtil {
        
        private int cameraId;
        private Camera camera;
        
        public CameraUtil(int cameraId, Camera camera) {
            this.cameraId = cameraId;
            this.camera = camera;
            System.out.println(camera.getParameters().flatten());
            setCameraDisplayOrientation();
        }
        
        public Camera getCamera()
        {
            return camera;
        }
        
        /**
         * 修正预览方向（解决竖屏模式成像左倾90度的问题）
         */
        
        private void setCameraDisplayOrientation()
        {
            if (screenOrientation_init == ScreenOrientation.SCREEN_ORIENTATION_LANDSCAPE)
            {
                return;
            }
            
            if (AndroidUtil.getVersion() >= 9)
            {
                CameraInfo cameraInfo = new CameraInfo();
                Camera.getCameraInfo(cameraId, cameraInfo);
                
                int orientation;
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT)
                {
                    orientation = (cameraInfo.orientation + degrees) % 360;
                    orientation = (360 - orientation) % 360;  // compensate the mirror
                }
                else
                {
                    // back-facing
                    orientation = (cameraInfo.orientation - degrees + 360) % 360;
                }
                
                System.out.println("修正预览方向：" + orientation);
                camera.setDisplayOrientation(orientation);
            }
        }
        
        /**
         * 支持区域对焦
         */
        
        public boolean initFocus()
        {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS))
            {
                return false;
            }
            
            Parameters params = camera.getParameters();
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Parameters.FOCUS_MODE_AUTO))
            {
                params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            }
            else if (focusModes.contains(Parameters.FOCUS_MODE_MACRO))
            {
                params.setFocusMode(Parameters.FOCUS_MODE_MACRO);
            }
            else
            {
                return false;
            }
            
            camera.setParameters(params);
            return true;
        }
        
        /**
         * 设置对焦区域
         */
        
        public void setFocusArea(Rect focusArea)
        {
            if (AndroidUtil.getVersion() < 14)
            {
                return;
            }

            Parameters params = camera.getParameters();
            if (params.getMaxNumFocusAreas() > 0)
            {
                List<Area> focusAreas = params.getFocusAreas();
                if (focusAreas == null)
                {
                    focusAreas = new ArrayList<Camera.Area>(1);
                }
                else
                {
                    focusAreas.clear();
                }
                
                focusAreas.add(new Area(focusArea, 1000));
                params.setFocusAreas(focusAreas);
                
                camera.setParameters(params);
            }
        }
        
        /**
         * 是否支持闪光灯
         */
        
        public boolean supportFlash()
        {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            {
                return false;
            }

            return camera.getParameters().getSupportedFlashModes() != null;
        }
        
        public void setFlashMode(String value)
        {
            Parameters params = camera.getParameters();
            params.setFlashMode(value);
            camera.setParameters(params);
        }
        
        public String getFlashMode()
        {
            return camera.getParameters().getFlashMode();
        }
        
        /**
         * 前置摄像头成像需要修正方向（补偿{@link Camera.Parameters#setRotation}设置无效，需要手动旋转图片）
         */
        
        public Bitmap rotateImage(Bitmap image, ScreenOrientation screenOrientation)
        {
            if (AndroidUtil.getVersion() >= 9)
            {
                CameraInfo cameraInfo = new CameraInfo();
                Camera.getCameraInfo(cameraId, cameraInfo);

                int rotation;
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT)
                {
                    rotation = (cameraInfo.orientation + degrees) % 360;
                    rotation = (360 - rotation) % 360;  // compensate the mirror
                    if (screenOrientation == ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT 
                    ||  screenOrientation == ScreenOrientation.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
                    {
                        rotation = (180 + rotation) % 360;
                    }
                }
                else
                {
                    // back-facing
                    rotation = (cameraInfo.orientation - degrees + 360) % 360;
                }
                
                if (rotation != 0)
                {
                    System.out.println("相片修正角度：" + rotation);
                    Bitmap rotateImage = ImageUtil.rotate(image, rotation);
                    image.recycle();
                    image = rotateImage;
                }
            }
            
            return image;
        }
    }
    
    private class MyPreview extends View {
        
        static final int FOCUS_HALF_WIDTH = 37;
        static final int FOCUS_HALF_HEIGHT = 25;
        
        Paint paint;
        
        Point centerPoint;
        
        Rect focusArea;
        Rect focusBounds;
        
        Rect screen_coordinate;
        Rect focusArea_coordinate;
        
        boolean isPortrait;
        
        public MyPreview(Context context) {
            super(context);
            init();
        }
        
        private void init()
        {
            paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(5);
        }
        
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            
            centerPoint = new Point(w / 2, h / 2);
            
            int hw = AndroidUtil.dp2px(getContext(), FOCUS_HALF_WIDTH);
            int hh = AndroidUtil.dp2px(getContext(), FOCUS_HALF_HEIGHT);
            
            focusArea = new Rect(centerPoint.x - hw, centerPoint.y - hh, centerPoint.x + hw, centerPoint.y + hh);
            int gap = (int) (paint.getStrokeWidth()) + 1;
            focusBounds = new Rect(gap, gap, w - gap, h - gap);
            
            screen_coordinate = new Rect(0, 0, w, h);
            focusArea_coordinate = new Rect(-1000, -1000, 1000, 1000);
            
            isPortrait = isPortrait();
        }
        
        private void moveFocusAreaTo(int centerX, int centerY)
        {
            focusArea.offsetTo(centerX - focusArea.width() / 2, centerY - focusArea.height() / 2);
            RectUtil.keepRectInBounds(focusArea, focusBounds);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (supportFocus && !isFlashShowing())
            {
                if (isPortrait() && !isPortrait)
                {
                    isPortrait = true;
                    focusArea = RectUtil.rotateRect(focusArea, 90);
                }
                else if (!isPortrait() && isPortrait)
                {
                    isPortrait = false;
                    focusArea = RectUtil.rotateRect(focusArea, 90);
                }
                
                canvas.drawRect(focusArea, paint);
            }
        }
        
        private boolean isPortrait()
        {
            return screenOrientation_last == ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT
                || screenOrientation_last == ScreenOrientation.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (supportFocus && !focusing.get() && !takingPicture.get() && !switchingCamera.get())
            {
                int x = (int) event.getX();
                int y = (int) event.getY();
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    if (isFlashShowing())
                    {
                        break;
                    }
                    
                    if (focusBounds.contains(x, y))
                    {
                        resetFocusSuccess();
                        moveFocusAreaTo(x, y);
                        invalidate();
                    }
                    
                    break;
                case MotionEvent.ACTION_UP:
                    if (isFlashShowing())
                    {
                        showFlash(false);
                        break;
                    }
                    
                    //focus
                    autoFocus();
                    break;
                }
                
                return true;
            }
            
            return super.onTouchEvent(event);
        }
        
        public void autoFocus()
        {
            TakePhotoActivity.this.autoFocus(RectUtil.transformRect(screen_coordinate, focusArea_coordinate, focusArea));
        }
        
        public void resetFocusArea()
        {
            moveFocusAreaTo(centerPoint.x, centerPoint.y);
        }
        
        public void setFocusSuccess(boolean success)
        {
            paint.setColor(success ? Color.GREEN : Color.RED);
            invalidate();
        }
        
        public void resetFocusSuccess()
        {
            paint.setColor(Color.WHITE);
        }
    }

    @Override
    public void run() {
        finish();
    }
}