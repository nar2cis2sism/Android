package engine.android.util.anim;

import engine.android.util.anim.AnimatorBuilder.AnimationListenerAdapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.graphics.Rect;
import android.renderscript.Float2;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * 封装一些功能函数
 * 
 * @author Daimon
 * @since 1/28/2019
 */
public class AnimationUtil {

    /**
     * Make animationView as same as sourceView with position and size into animationRoot
     *
     * @return The visible area of sourceView
     */
    public static Rect makeAnimationView(View sourceView, View animationView, ViewGroup animationRoot) {
        Rect rect = new Rect();
        sourceView.getGlobalVisibleRect(rect);

        animationRoot.addView(animationView, rect.width(), rect.height());
        animationView.setX(rect.left);
        animationView.setY(rect.top);

        return rect;
    }

    /**
     * Make and new animationView as same as sourceView with position and size into animationRoot
     *
     * @param visibleRect The visible area of sourceView
     */
    public static ImageView makeAnimationView(ImageView sourceView, ViewGroup animationRoot, Rect visibleRect) {
        if (visibleRect == null) visibleRect = new Rect();
        sourceView.getGlobalVisibleRect(visibleRect);

        ImageView animationView = new ImageView(sourceView.getContext());
        animationView.setImageDrawable(sourceView.getDrawable());

        animationRoot.addView(animationView, visibleRect.width(), visibleRect.height());
        animationView.setX(visibleRect.left);
        animationView.setY(visibleRect.top);

        return animationView;
    }

    /**
     * 贝塞尔曲线路径
     *
     * @param control 控制点
     */
    public static Path getBezierPath(float fromX, float toX, float fromY, float toY, Float2 control) {
        if (control == null) control = new Float2((toX - fromX) / 4 + fromX, toY);
        Path path = new Path();
        path.moveTo(fromX, fromY);
        path.quadTo(control.x, control.y, toX, toY);
        return path;
    }

    /**
     * 动画之后隐藏view
     */
    public static void disappearAfterAnim(final View view, Animation anim) {
        anim.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 动画之后隐藏view
     */
    public static void disappearAfterAnim(final View view, Animator anim) {
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 动画之后隐藏view
     */
    public static void disappearAfterAnim(ObjectAnimator anim) {
        disappearAfterAnim((View) anim.getTarget(), anim);
    }
}