package engine.android.util.listener;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.OrientationEventListener;

import engine.android.util.AndroidUtil;

/**
 * 屏幕方向感应监听器
 * 
 * @author Daimon
 * @version N
 * @since 3/1/2013
 */
public abstract class MyOrientationEventListener extends OrientationEventListener {

    public static enum ScreenOrientation {

        SCREEN_ORIENTATION_UNSPECIFIED,
        SCREEN_ORIENTATION_PORTRAIT,
        SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
        SCREEN_ORIENTATION_REVERSE_PORTRAIT,
        SCREEN_ORIENTATION_LANDSCAPE;

        public static ScreenOrientation getScreenOrientation(int requestedOrientation) {
            switch (requestedOrientation) {
                case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                    return SCREEN_ORIENTATION_UNSPECIFIED;
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    return SCREEN_ORIENTATION_PORTRAIT;
                case 8/* ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE */:
                    return SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                case 9/* ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT */:
                    return SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                    return SCREEN_ORIENTATION_LANDSCAPE;

                default:
                    return SCREEN_ORIENTATION_UNSPECIFIED;
            }
        }
    }

    private ScreenOrientation screenOrientation
    = ScreenOrientation.SCREEN_ORIENTATION_UNSPECIFIED;

    private OrientationCompensation orientationCompensation;

    public MyOrientationEventListener(Context context) {
        super(context);
        init();
    }

    public MyOrientationEventListener(Context context, int rate) {
        super(context, rate);
        init();
    }

    private void init() {
        if (AndroidUtil.getVersion() >= 9 && Camera.getNumberOfCameras() > 0)
        {
            final CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(0, cameraInfo);

            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT)
            {
                orientationCompensation = new OrientationCompensation() {

                    @Override
                    public int compensateOrientation(int orientation) {
                        return orientation -= 360 - (90 + cameraInfo.orientation);
                    }
                };
            }
            else
            {
                orientationCompensation = new OrientationCompensation() {

                    @Override
                    public int compensateOrientation(int orientation) {
                        return orientation -= 90 - cameraInfo.orientation;
                    }
                };
            }
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation != ORIENTATION_UNKNOWN)
        {
            if (orientationCompensation != null)
            {
                orientation = orientationCompensation.compensateOrientation(orientation);

                // normalize to 0 - 359 range
                while (orientation >= 360)
                {
                    orientation -= 360;
                }

                while (orientation < 0)
                {
                    orientation += 360;
                }
            }

            if ((orientation >= 0 && orientation < 45) || (orientation >= 315 && orientation < 360))
            {
                screenOrientation = ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT;
            }
            else if (orientation >= 45 && orientation < 135)
            {
                screenOrientation = ScreenOrientation.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
            else if (orientation >= 135 && orientation < 225)
            {
                screenOrientation = ScreenOrientation.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
            else if (orientation >= 225 && orientation < 315)
            {
                screenOrientation = ScreenOrientation.SCREEN_ORIENTATION_LANDSCAPE;
            }

            onOrientationChanged(orientation, screenOrientation);
        }
    }

    public static final int toOrientation(ScreenOrientation screenOrientation) {
        return (screenOrientation.ordinal() - 1) * 90;
    }

    protected abstract void onOrientationChanged(int orientation,
            ScreenOrientation screenOrientation);

    private static interface OrientationCompensation {

        public int compensateOrientation(int orientation);
    }

    @Override
    public void enable() {
        super.enable();
    }

    @Override
    public void disable() {
        super.disable();
    }
}