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
    private static final int COLOR_ID_REALRED = 0;
    private static final int COLOR_ID_RED = 1;
    private static final int COLOR_ID_PINK = 2;
    private static final int COLOR_ID_SOFTPURPLE = 3;
    private static final int COLOR_ID_PURPLE = 4;
    private static final int COLOR_ID_DEEPPURPLE = 5;
    private static final int COLOR_ID_INDIGO = 6;
    private static final int COLOR_ID_BLUE = 7;
    private static final int COLOR_ID_LIGHTBLUE = 8;
    private static final int COLOR_ID_SOFTBLUE = 9;
    private static final int COLOR_ID_CYAN = 10;
    private static final int COLOR_ID_TEAL = 11;
    private static final int COLOR_ID_PRESTOGREEN = 12;
    private static final int COLOR_ID_REALGREEN = 13;
    private static final int COLOR_ID_REALLIGHTGREEN = 14;
    private static final int COLOR_ID_GREEN = 15;
    private static final int COLOR_ID_LIGHTGREEN = 16;
    private static final int COLOR_ID_LIME = 17;
    private static final int COLOR_ID_YELLOW = 18;
    private static final int COLOR_ID_AMBER = 19;
    private static final int COLOR_ID_ORANGE = 20;
    private static final int COLOR_ID_DEEPORANGE = 21;
    private static final int COLOR_ID_SOFTORANGE = 22;
    private static final int COLOR_ID_BROWN = 23;
    private static final int COLOR_ID_GREY = 24;
    private static final int COLOR_ID_BLUEGREY = 25;
    private static final int COLOR_ID_JVC = 26;
    private static final int COLOR_ID_SOFTBLACK = 27;
    private static final int COLOR_ID_BLACK = 28;

    private static ThemeName themeUsed = ThemeName.LIGHT_THEME;
    private static int colorPrimaryIdUsedForThemeLight = COLOR_ID_INDIGO;
    private static @ColorInt int realTopicNameColorUsedForLightTheme = 0;
    private static @ColorInt int realAltColorUsedForLightTheme = 0;
    private static @ColorInt int realSurveyColorUsedForLightTheme = 0;
    private static @ColorInt int realDeletedColorUsedForLightTheme = 0;

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

    public static void updateColorsUsed(Resources res) {
        int[] arrayOfColors = res.getIntArray(R.array.choicesForPrimaryColor);
        int primaryColorChoosed = PrefsManager.getInt(PrefsManager.IntPref.Names.PRIMARY_COLOR_OF_LIGHT_THEME);
        realTopicNameColorUsedForLightTheme = PrefsManager.getInt(PrefsManager.IntPref.Names.TOPIC_NAME_COLOR_OF_LIGHT_THEME);
        realAltColorUsedForLightTheme = PrefsManager.getInt(PrefsManager.IntPref.Names.ALT_COLOR_OF_LIGHT_THEME);
        realSurveyColorUsedForLightTheme = PrefsManager.getInt(PrefsManager.IntPref.Names.SURVEY_COLOR_OF_LIGHT_THEME);
        realDeletedColorUsedForLightTheme = PrefsManager.getInt(PrefsManager.IntPref.Names.DELETED_COLOR_OF_LIGHT_THEME);

        for (int i = 0; i < arrayOfColors.length; ++i) {
            if (arrayOfColors[i] == primaryColorChoosed) {
                colorPrimaryIdUsedForThemeLight = i;
                return;
            }
        }

        colorPrimaryIdUsedForThemeLight = COLOR_ID_INDIGO;
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
            switch (colorPrimaryIdUsedForThemeLight) {
                case COLOR_ID_REALRED:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsRealRed, true);
                    break;
                case COLOR_ID_RED:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsRed, true);
                    break;
                case COLOR_ID_PINK:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsPink, true);
                    break;
                case COLOR_ID_SOFTPURPLE:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsSoftPurple, true);
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
                case COLOR_ID_SOFTBLUE:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsSoftBlue, true);
                    break;
                case COLOR_ID_CYAN:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsCyan, true);
                    break;
                case COLOR_ID_TEAL:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsTeal, true);
                    break;
                case COLOR_ID_PRESTOGREEN:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsPrestoGreen, true);
                    break;
                case COLOR_ID_REALGREEN:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsRealGreen, true);
                    break;
                case COLOR_ID_REALLIGHTGREEN:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsRealLightGreen, true);
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
                case COLOR_ID_SOFTORANGE:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsSoftOrange, true);
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
                case COLOR_ID_SOFTBLACK:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsSoftBlack, true);
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

    public static int getColorPrimaryIdUsedForThemeLight() {
        return colorPrimaryIdUsedForThemeLight;
    }

    public static boolean getThemeUsedIsDark() {
        return themeUsed == ThemeName.DARK_THEME || themeUsed == ThemeName.BLACK_THEME;
    }

    @ColorInt
    public static int getColorInt(@AttrRes int thisAttrColor, Context fromThisContext) {
        if (themeUsed == ThemeName.LIGHT_THEME) {
            switch (thisAttrColor) {
                case R.attr.themedTopicNameColor:
                    if (realTopicNameColorUsedForLightTheme != 0) {
                        return realTopicNameColorUsedForLightTheme;
                    } else {
                        break;
                    }
                case R.attr.themedAltBackgroundColor:
                    if (realAltColorUsedForLightTheme != 0) {
                        return realAltColorUsedForLightTheme;
                    } else {
                        break;
                    }
                case R.attr.themedSurveyMessageBackgroundColor:
                    if (realSurveyColorUsedForLightTheme != 0) {
                        return realSurveyColorUsedForLightTheme;
                    } else {
                        break;
                    }
                case R.attr.themedDeletedMessageBackgroundColor:
                    if (realDeletedColorUsedForLightTheme != 0) {
                        return realDeletedColorUsedForLightTheme;
                    } else {
                        break;
                    }
            }
        }

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
