package com.project.app.config;

import android.graphics.Bitmap;

import engine.android.framework.app.image.ImageManager.ImageUrl;
import engine.android.framework.app.image.ImageManager.Transformer;
import engine.android.util.image.AvatarImage;

public class ImageTransformer implements Transformer {
    
    public static final int TYPE_AVATAR = 1;            // 圆形头像

    @Override
    public Bitmap transform(ImageUrl url, Bitmap image) {
        switch (url.type) {
            case TYPE_AVATAR:
                return new AvatarImage(image).get();

            default:
                return image;
        }
    }
}
