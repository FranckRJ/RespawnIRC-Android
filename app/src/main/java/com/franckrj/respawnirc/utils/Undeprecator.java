package com.franckrj.respawnirc.utils;

import android.content.res.Resources;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.text.Spanned;
import android.webkit.WebSettings;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import java.security.InvalidParameterException;

public class Undeprecator {
    @ColorInt
    public static int resourcesGetColor(Resources resources, @ColorRes int colorId) {
        if (Build.VERSION.SDK_INT >= 23) {
            return resources.getColor(colorId, null);
        } else {
            //noinspection deprecation
            return resources.getColor(colorId);
        }
    }

    public static void drawableSetColorFilter(Drawable drawable, @ColorInt int color, @NonNull PorterDuff.Mode mode) {
        if (mode != PorterDuff.Mode.SRC_ATOP && mode != PorterDuff.Mode.SRC_IN && mode != PorterDuff.Mode.OVERLAY) {
            throw new InvalidParameterException();
        }

        if (Build.VERSION.SDK_INT >= 29) {
            if (mode == PorterDuff.Mode.SRC_ATOP) {
                drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
            } else if (mode == PorterDuff.Mode.SRC_IN) {
                drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_IN));
            } else if (mode == PorterDuff.Mode.OVERLAY) {
                drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.OVERLAY));
            }
        } else {
            drawable.setColorFilter(color, mode);
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

    public static void webSettingsSetSaveFormData(WebSettings settings, boolean newVal) {
        //noinspection deprecation
        settings.setSaveFormData(newVal);
    }

    public static void webSettingsSetSavePassword(WebSettings settings, boolean newVal) {
        //noinspection deprecation
        settings.setSavePassword(newVal);
    }

    public static void vibratorVibrate(Vibrator vibratorService, long[] pattern, int repeat) {
        if (Build.VERSION.SDK_INT >= 26) {
            vibratorService.vibrate(VibrationEffect.createWaveform(pattern, repeat));
        } else {
            //noinspection deprecation
            vibratorService.vibrate(pattern, repeat);
        }
    }
}
