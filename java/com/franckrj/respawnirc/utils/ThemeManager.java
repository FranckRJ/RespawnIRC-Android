package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.util.TypedValue;

import com.franckrj.respawnirc.R;

public class ThemeManager {
    private static final int COLOR_ID_RED = 0;
    private static final int COLOR_ID_PINK = 1;
    private static final int COLOR_ID_PURPLE = 2;
    private static final int COLOR_ID_DEEPPURPLE = 3;
    private static final int COLOR_ID_INDIGO = 4;
    private static final int COLOR_ID_BLUE = 5;
    private static final int COLOR_ID_LIGHTBLUE = 6;
    private static final int COLOR_ID_CYAN = 7;
    private static final int COLOR_ID_TEAL = 8;
    private static final int COLOR_ID_GREEN = 9;
    private static final int COLOR_ID_LIGHTGREEN = 10;
    private static final int COLOR_ID_LIME = 11;
    private static final int COLOR_ID_YELLOW = 12;
    private static final int COLOR_ID_AMBER = 13;
    private static final int COLOR_ID_ORANGE = 14;
    private static final int COLOR_ID_DEEPORANGE = 15;
    private static final int COLOR_ID_BROWN = 16;
    private static final int COLOR_ID_GREY = 17;
    private static final int COLOR_ID_BLUEGREY = 18;
    private static final int COLOR_ID_JVC = 19;
    private static final int COLOR_ID_BLACK = 20;

    private static ThemeName themeUsed = ThemeName.LIGHT_THEME;
    private static int colorPrimaryIdUsed = COLOR_ID_INDIGO;

    public static void updateThemeUsed() {
        String themeStringId = PrefsManager.getString(PrefsManager.StringPref.Names.THEME_USED);

        switch (themeStringId) {
            case "1":
                themeUsed = ThemeName.DARK_THEME;
                break;
            case "2":
                themeUsed = ThemeName.BLACK_THEME;
                break;
            default:
                themeUsed = ThemeName.LIGHT_THEME;
                break;
        }
    }

    public static void updatePrimaryColorUsed(Resources res) {
        int[] arrayOfColors = res.getIntArray(R.array.choicesForPrimaryColor);
        int primaryColorChoosed = PrefsManager.getInt(PrefsManager.IntPref.Names.PRIMARY_COLOR_OF_LIGHT_THEME);

        for (int i = 0; i < arrayOfColors.length; ++i) {
            if (arrayOfColors[i] == primaryColorChoosed) {
                colorPrimaryIdUsed = i;
                return;
            }
        }

        colorPrimaryIdUsed = COLOR_ID_INDIGO;
    }

    public static void changeActivityTheme(Activity thisActivity) {
        switch (themeUsed) {
            case DARK_THEME:
                thisActivity.setTheme(R.style.AppTheme_Dark_Real);
                break;
            case BLACK_THEME:
                thisActivity.setTheme(R.style.AppTheme_Black_Real);
                break;
            default:
                thisActivity.setTheme(R.style.AppTheme_Light_Real);
                break;
        }
    }

    public static void changeActivityPrimaryColorIfNeeded(Activity thisActivity) {
        if (themeUsed == ThemeName.LIGHT_THEME) {
            switch (colorPrimaryIdUsed) {
                case COLOR_ID_RED:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsRed, true);
                    break;
                case COLOR_ID_PINK:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsPink, true);
                    break;
                case COLOR_ID_PURPLE:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsPurple, true);
                    break;
                case COLOR_ID_DEEPPURPLE:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsDeepPurple, true);
                    break;
                case COLOR_ID_INDIGO:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsIndigo, true);
                    break;
                case COLOR_ID_BLUE:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsBlue, true);
                    break;
                case COLOR_ID_LIGHTBLUE:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsLightBlue, true);
                    break;
                case COLOR_ID_CYAN:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsCyan, true);
                    break;
                case COLOR_ID_TEAL:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsTeal, true);
                    break;
                case COLOR_ID_GREEN:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsGreen, true);
                    break;
                case COLOR_ID_LIGHTGREEN:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsLightGreen, true);
                    break;
                case COLOR_ID_LIME:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsLime, true);
                    break;
                case COLOR_ID_YELLOW:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsYellow, true);
                    break;
                case COLOR_ID_AMBER:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsAmber, true);
                    break;
                case COLOR_ID_ORANGE:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsOrange, true);
                    break;
                case COLOR_ID_DEEPORANGE:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsDeepOrange, true);
                    break;
                case COLOR_ID_BROWN:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsBrown, true);
                    break;
                case COLOR_ID_GREY:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsGrey, true);
                    break;
                case COLOR_ID_BLUEGREY:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsBlueGrey, true);
                    break;
                case COLOR_ID_JVC:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsJVC, true);
                    break;
                case COLOR_ID_BLACK:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsBlack, true);
                    break;
                default:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsIndigo, true);
                    break;
            }
        }
    }

    public static ThemeName getThemeUsed() {
        return themeUsed;
    }

    public static int getColorPrimaryIdUsed() {
        return colorPrimaryIdUsed;
    }

    public static boolean getThemeUsedIsDark() {
        return themeUsed == ThemeName.DARK_THEME || themeUsed == ThemeName.BLACK_THEME;
    }

    @ColorInt
    public static int getColorInt(@AttrRes int thisAttrColor, Context fromThisContext) {
        if (fromThisContext != null) {
            TypedValue typedValue = new TypedValue();
            boolean valueIsFound = fromThisContext.getTheme().resolveAttribute(thisAttrColor, typedValue, true);

            if (valueIsFound) {
                return typedValue.data;
            }
        }

        return 0;
    }

    @DrawableRes
    public static int getDrawableRes(DrawableName thisDrawable) {
        if (getThemeUsedIsDark()) {
            switch (thisDrawable) {
                case SHADOW_DRAWER:
                    return R.drawable.shadow_drawer_dark;
                case EXPAND_MORE:
                    return R.drawable.ic_expand_more_dark;
                case EXPAND_LESS:
                    return R.drawable.ic_expand_less_dark;
                case ARROW_DROP_DOWN:
                    return R.drawable.ic_arrow_drop_down_dark;
                case CONTENT_SEND:
                    return R.drawable.ic_msg_send_dark;
                case CONTENT_EDIT:
                    return R.drawable.ic_msg_send_edit_dark;
                case TOPIC_LOCK_ICON:
                    return R.drawable.icon_topic_lock_dark;
                case DELETED_IMAGE:
                    return R.drawable.image_deleted_dark;
                case DOWNLOAD_IMAGE:
                    return R.drawable.image_download_dark;
                default:
                    return R.drawable.ic_arrow_drop_down_dark;
            }
        } else {
            switch (thisDrawable) {
                case SHADOW_DRAWER:
                    return R.drawable.shadow_drawer_light;
                case EXPAND_MORE:
                    return R.drawable.ic_expand_more_light;
                case EXPAND_LESS:
                    return R.drawable.ic_expand_less_light;
                case ARROW_DROP_DOWN:
                    return R.drawable.ic_arrow_drop_down_light;
                case CONTENT_SEND:
                    return R.drawable.ic_msg_send_light;
                case CONTENT_EDIT:
                    return R.drawable.ic_msg_send_edit_light;
                case TOPIC_LOCK_ICON:
                    return R.drawable.icon_topic_lock_light;
                case DELETED_IMAGE:
                    return R.drawable.image_deleted_light;
                case DOWNLOAD_IMAGE:
                    return R.drawable.image_download_light;
                default:
                    return R.drawable.ic_arrow_drop_down_light;
            }
        }
    }

    public enum DrawableName {
        SHADOW_DRAWER,
        EXPAND_MORE, EXPAND_LESS,
        ARROW_DROP_DOWN,
        CONTENT_SEND, CONTENT_EDIT,
        TOPIC_LOCK_ICON,
        DELETED_IMAGE, DOWNLOAD_IMAGE
    }

    public enum ThemeName {
        LIGHT_THEME, DARK_THEME, BLACK_THEME
    }
}
