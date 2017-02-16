package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Html;

public class CustomImageGetter implements Html.ImageGetter {
    private Activity parentActivity = null;
    private Drawable deletedDrawable = null;
    private ImageDownloader downloaderForImage = null;

    public CustomImageGetter(Activity newParentActivity, Drawable newDeletedDrawable, ImageDownloader newDownloaderForImage) {
        parentActivity = newParentActivity;
        deletedDrawable = newDeletedDrawable;
        downloaderForImage = newDownloaderForImage;
    }

    @Override
    public Drawable getDrawable(String source) {
        if (!source.startsWith("http")) {
            Drawable drawable;
            Resources res = parentActivity.getResources();
            int resID = res.getIdentifier(source.substring(0, source.lastIndexOf(".")), "drawable", parentActivity.getPackageName());

            try {
                drawable = Undeprecator.resourcesGetDrawable(res, resID);
            } catch (Exception e) {
                drawable = deletedDrawable;
            }
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

            return drawable;
        } else if (source.startsWith("http://image.noelshack.com/minis") && downloaderForImage != null) {
            return downloaderForImage.getDrawableFromLink(source);
        } else {
            return deletedDrawable;
        }
    }
}
