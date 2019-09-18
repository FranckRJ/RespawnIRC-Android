package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;

public class CustomImageGetter implements Html.ImageGetter {
    private Activity parentActivity;
    private Drawable deletedDrawable;
    private ImageDownloader downloaderForImage;
    private int stickerSize = -1;

    public CustomImageGetter(Activity newParentActivity, Drawable newDeletedDrawable, ImageDownloader newDownloaderForImage) {
        parentActivity = newParentActivity;
        deletedDrawable = newDeletedDrawable;
        downloaderForImage = newDownloaderForImage;
    }

    public void setStickerSize(int newStickerSize) {
        stickerSize = newStickerSize;
    }

    @Override
    public Drawable getDrawable(String source) {
        if (!source.startsWith("http")) {
            Drawable drawable;
            int resId;
            Resources res = parentActivity.getResources();
            boolean needToBeBig = false;

            if (source.startsWith("big-")) {
                source = source.substring(("big-").length());
                needToBeBig = true;
            }
            resId = res.getIdentifier(source.substring(0, source.lastIndexOf(".")), "drawable", parentActivity.getPackageName());

            try {
                if (needToBeBig) {
                    Bitmap tmpBitmap = BitmapFactory.decodeResource(res, resId);
                    tmpBitmap = Bitmap.createScaledBitmap(tmpBitmap, tmpBitmap.getWidth() * 2, tmpBitmap.getHeight() * 2, false);
                    drawable = new BitmapDrawable(res, tmpBitmap);
                } else {
                    drawable = parentActivity.getDrawable(resId);
                    if (drawable == null) {
                        throw new NullPointerException();
                    }
                }
            } catch (Exception e) {
                drawable = deletedDrawable;
            }

            if (source.startsWith("sticker_") && stickerSize >= 0 && drawable != deletedDrawable) {
                drawable.setBounds(0, 0, stickerSize, stickerSize);
            } else {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }

            return drawable;
        } else if (source.startsWith("http://image.noelshack.com/minis/") && downloaderForImage != null) {
            return downloaderForImage.getDrawableFromLink(source, true, false, false);
        } else if (source.startsWith("http://image.noelshack.com/fichiers-xs/") && downloaderForImage != null) {
            return downloaderForImage.getDrawableFromLink(source, true, true, true);
        } else {
            return deletedDrawable;
        }
    }
}
