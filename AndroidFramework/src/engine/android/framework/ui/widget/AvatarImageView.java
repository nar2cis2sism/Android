package engine.android.framework.ui.widget;

import static engine.android.widget.base.CustomView.getDesiredSize;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import engine.android.core.extra.JavaBeanAdapter.ViewHolder;
import engine.android.framework.R;
import engine.android.framework.app.AppGlobal;
import engine.android.framework.app.image.ImageManager.ImageUrl;
import engine.android.util.ui.UIUtil;

/**
 * 头像显示控件
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class AvatarImageView extends ImageView {

    public AvatarImageView(Context context) {
        super(context);
    }

    public AvatarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = getResources().getDimensionPixelSize(R.dimen.avatar_size);
        setMeasuredDimension(getDesiredSize(size, widthMeasureSpec), 
                getDesiredSize(size, heightMeasureSpec));
    }

    /**
     * 显示头像
     * 
     * @param view 需要显示的地方
     * @param url 头像的下载地址
     * @return 将被替换成{@link AvatarImageView}
     */
    public static AvatarImageView display(View view, ImageUrl url) {
        AvatarImageView avatar;
        if (view instanceof AvatarImageView)
        {
            avatar = (AvatarImageView) view;
        }
        else
        {
            avatar = new AvatarImageView(view.getContext());
            UIUtil.replace(view, avatar, view.getLayoutParams());
        }

        avatar.display(url);
        return avatar;
    }

    /**
     * 显示头像（适用于ListView）
     * 
     * @param holder 由于ViewHolder持有View的引用，所以需要即时更新
     * @param viewId 需要显示的地方（将被替换成{@link AvatarImageView}）
     * @param url 头像的下载地址
     */
    public static void display(ViewHolder holder, int viewId, ImageUrl url) {
        AvatarImageView avatar;
        View view = holder.getView(viewId);
        if (view instanceof AvatarImageView)
        {
            avatar = (AvatarImageView) view;
        }
        else
        {
            avatar = new AvatarImageView(view.getContext());
            UIUtil.replace(view, avatar, view.getLayoutParams());
            holder.removeView(viewId);
        }

        avatar.display(url);
    }

    public void display(ImageUrl url) {
        AppGlobal.get(getContext()).getImageManager()
        .display(this, url, getResources().getDrawable(R.drawable.avatar_default));
    }
}