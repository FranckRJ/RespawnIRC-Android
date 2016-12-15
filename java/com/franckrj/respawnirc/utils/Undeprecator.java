package com.franckrj.respawnirc.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

public class Undeprecator {
    public static void viewSetBackgroundDrawable(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(drawable);
        } else {
            //noinspection deprecation
            view.setBackgroundDrawable(drawable);
        }
    }

    @ColorInt
    public static int resourcesGetColor(Resources resources, @ColorRes int colorId) {
        if (Build.VERSION.SDK_INT >= 23) {
            return resources.getColor(colorId, null);
        } else {
            //noinspection deprecation
            return resources.getColor(colorId);
        }
    }

    public static Drawable resourcesGetDrawable(Resources resources, @DrawableRes int drawableId) {
        if (Build.VERSION.SDK_INT >= 21) {
            return resources.getDrawable(drawableId, null);
        } else {
            //noinspection deprecation
            return resources.getDrawable(drawableId);
        }
    }

    public static Spanned htmlFromHtml(String source) {
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            return Html.fromHtml(source);
        }
    }

    public static Spanned htmlFromHtml(String source, Html.ImageGetter imageGetter, Html.TagHandler tagHandler) {
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY, imageGetter, tagHandler);
        } else {
            //noinspection deprecation
            return Html.fromHtml(source, imageGetter, tagHandler);
        }
    }
}