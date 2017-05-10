package demo.android.ui;

import android.app.Activity;
import android.os.Handler;

import demo.android.ui.util.SystemUiHider;
import demo.android.ui.util.SystemUiHider.OnVisibilityChangeListener;


/**
 * An agent of full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @author Daimon
 * @version 3.0
 * @since 11/15/2013
 * @see SystemUiHider
 */

public class FullScreenProxy {

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private Handler mHideHandler = new Handler();
    private Runnable mHideRunnable = new Runnable() {

        @Override
        public void run() {
            hide();
        }
    };

    public FullScreenProxy(Activity activity) {
        this(activity, HIDER_FLAGS);
    }

    /**
     * @param activity
     *            The activity whose window's system UI should be controlled by
     *            this class.
     * @param flags
     *            Either 0 or any combination of 
     *            {@link SystemUiHider#FLAG_FULLSCREEN},
     *            {@link SystemUiHider#FLAG_HIDE_NAVIGATION}, and
     *            {@link SystemUiHider#FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES}.
     */

    public FullScreenProxy(Activity activity, int flags) {
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(
                activity, 
                activity.getWindow().getDecorView(),
                flags);
        mSystemUiHider.setup();
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    public void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Returns whether or not the system UI is visible.
     */
    public boolean isVisible() {
        return mSystemUiHider.isVisible();
    }

    /**
     * Hide the system UI.
     */
    public void hide() {
        mSystemUiHider.hide();
    }

    /**
     * Show the system UI.
     */
    public void show() {
        mSystemUiHider.show();
    }

    /**
     * Toggle the visibility of the system UI.
     */
    public void toggle() {
        mSystemUiHider.toggle();
    }

    /**
     * Registers a callback, to be triggered when the system UI visibility
     * changes.
     */
    public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
        mSystemUiHider.setOnVisibilityChangeListener(listener);
    }
}