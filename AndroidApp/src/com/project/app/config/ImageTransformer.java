package com.project.app.config;

import engine.android.framework.app.image.ImageManager.Transformer;
import engine.android.util.image.AsyncImageLoader.ImageUrl;
import engine.android.util.image.AvatarImage;

import android.graphics.Bitmap;

public class ImageTransformer implements Transformer {
    
    public static final int TYPE_AVATAR = 1;            // 圆形头像

    @Override
    public Bitmap transform(ImageUrl url, Bitmap image) {
        switch (url.getType()) {
            case TYPE_AVATAR:
                return new AvatarImage(image).get();

            default:
                return image;
        }
    }
}
