package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
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
    private static final int COLOR_ID_DEFAULT = 29;

    private static ThemeName themeUsed = ThemeName.LIGHT_THEME;
    private static boolean toolbarTextColorIsInvertedForThemeLight = false;
    private static int primaryColorIdUsedForThemeLight = COLOR_ID_INDIGO;
    private static int topicNameAndAccentColorIdUsedForThemeLight = COLOR_ID_DEFAULT;
    private static @ColorInt int realHeaderColorUsedForLightTheme = 0;
    private static @ColorInt int realAltColorUsedForLightTheme = 0;
    private static @ColorInt int realSurveyColorUsedForLightTheme = 0;
    private static @ColorInt int realDeletedColorUsedForLightTheme = 0;

    private static @ColorInt int brightenColor(@ColorInt int color) {
        int a = Color.alpha(color);
        int r = (int)Math.round((Color.red(color) + 255) / 2.);
        int g = (int)Math.round((Color.green(color) + 255) / 2.);
        int b = (int)Math.round((Color.blue(color) + 255) / 2.);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    public static void updateThemeUsed() {
        String themeStringId = PrefsManager.getString(PrefsManager.StringPref.Names.THEME_USED);

        switch (themeStringId) {
            case "0":
                themeUsed = ThemeName.LIGHT_THEME;
                break;
            case "1":
                themeUsed = ThemeName.GREY_THEME;
                break;
            case "2":
                themeUsed = ThemeName.DARK_THEME;
                break;
            case "3":
                themeUsed = ThemeName.BLACK_THEME;
                break;
            default:
                themeUsed = ThemeName.LIGHT_THEME;
                break;
        }
    }

    public static void updateToolbarTextColor() {
        toolbarTextColorIsInvertedForThemeLight = PrefsManager.getBool(PrefsManager.BoolPref.Names.INVERT_TOOLBAR_TEXT_COLOR);
    }

    public static void updateColorsUsed(Resources res) {
        int[] arrayOfPrimaryColors = res.getIntArray(R.array.choicesForPrimaryColor);
        int[] arrayOfTopicNameAndAccentColors = res.getIntArray(R.array.choicesForTopicNameAndAccentColor);
        int primaryColorChoosed = PrefsManager.getInt(PrefsManager.IntPref.Names.PRIMARY_COLOR_OF_LIGHT_THEME);
        int topicNameAndAccentColorChoosed = PrefsManager.getInt(PrefsManager.IntPref.Names.TOPIC_NAME_AND_ACCENT_COLOR_OF_LIGHT_THEME);
        realHeaderColorUsedForLightTheme = PrefsManager.getInt(PrefsManager.IntPref.Names.HEADER_COLOR_OF_LIGHT_THEME);
        realSurveyColorUsedForLightTheme = PrefsManager.getInt(PrefsManager.IntPref.Names.SURVEY_COLOR_OF_LIGHT_THEME);
        realDeletedColorUsedForLightTheme = PrefsManager.getInt(PrefsManager.IntPref.Names.DELETED_COLOR_OF_LIGHT_THEME);

        if (PrefsManager.getBool(PrefsManager.BoolPref.Names.BRIGHTEN_ALT_COLOR)) {
            realAltColorUsedForLightTheme = brightenColor(PrefsManager.getInt(PrefsManager.IntPref.Names.ALT_COLOR_OF_LIGHT_THEME));
        } else {
            realAltColorUsedForLightTheme = PrefsManager.getInt(PrefsManager.IntPref.Names.ALT_COLOR_OF_LIGHT_THEME);
        }

        primaryColorIdUsedForThemeLight = -1;
        topicNameAndAccentColorIdUsedForThemeLight = -1;

        for (int i = 0; i < arrayOfPrimaryColors.length; ++i) {
            if (arrayOfPrimaryColors[i] == primaryColorChoosed) {
                primaryColorIdUsedForThemeLight = i;
                break;
            }
        }
        for (int i = 0; i < arrayOfTopicNameAndAccentColors.length; ++i) {
            if (arrayOfTopicNameAndAccentColors[i] == topicNameAndAccentColorChoosed) {
                topicNameAndAccentColorIdUsedForThemeLight = i;
                break;
            }
        }

        if (primaryColorIdUsedForThemeLight == -1) {
            primaryColorIdUsedForThemeLight = COLOR_ID_INDIGO;
        }
        if (topicNameAndAccentColorIdUsedForThemeLight == -1) {
            topicNameAndAccentColorIdUsedForThemeLight = COLOR_ID_DEFAULT;
        }
    }

    public static void changeActivityTheme(Activity thisActivity) {
        switch (themeUsed) {
            case LIGHT_THEME:
                thisActivity.setTheme(R.style.AppTheme_Light_Real);
                break;
            case GREY_THEME:
                thisActivity.setTheme(R.style.AppTheme_Grey_Real);
                break;
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

    public static void changeActivityToolbarTextColor(Activity thisActivity) {
        if (themeUsed == ThemeName.LIGHT_THEME) {
            if (toolbarTextColorIsInvertedForThemeLight) {
                thisActivity.getTheme().applyStyle(R.style.ToolbarIsLight, true);
            } else {
                thisActivity.getTheme().applyStyle(R.style.ToolbarIsDark, true);
            }
        }
    }

    public static void changeActivityThemedColorsIfNeeded(Activity thisActivity) {
        if (themeUsed == ThemeName.LIGHT_THEME) {
            switch (primaryColorIdUsedForThemeLight) {
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
                case COLOR_ID_DEFAULT:
                default:
                    thisActivity.getTheme().applyStyle(R.style.PrimaryColorIsIndigo, true);
                    break;
            }

            switch (topicNameAndAccentColorIdUsedForThemeLight) {
                case COLOR_ID_REALRED:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsRealRed, true);
                    break;
                case COLOR_ID_RED:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsRed, true);
                    break;
                case COLOR_ID_PINK:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsPink, true);
                    break;
                case COLOR_ID_SOFTPURPLE:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsSoftPurple, true);
                    break;
                case COLOR_ID_PURPLE:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsPurple, true);
                    break;
                case COLOR_ID_DEEPPURPLE:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsDeepPurple, true);
                    break;
                case COLOR_ID_INDIGO:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsIndigo, true);
                    break;
                case COLOR_ID_BLUE:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsBlue, true);
                    break;
                case COLOR_ID_LIGHTBLUE:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsLightBlue, true);
                    break;
                case COLOR_ID_SOFTBLUE:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsSoftBlue, true);
                    break;
                case COLOR_ID_CYAN:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsCyan, true);
                    break;
                case COLOR_ID_TEAL:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsTeal, true);
                    break;
                case COLOR_ID_PRESTOGREEN:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsPrestoGreen, true);
                    break;
                case COLOR_ID_REALGREEN:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsRealGreen, true);
                    break;
                case COLOR_ID_REALLIGHTGREEN:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsRealLightGreen, true);
                    break;
                case COLOR_ID_GREEN:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsGreen, true);
                    break;
                case COLOR_ID_LIGHTGREEN:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsLightGreen, true);
                    break;
                case COLOR_ID_LIME:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsLime, true);
                    break;
                case COLOR_ID_YELLOW:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsYellow, true);
                    break;
                case COLOR_ID_AMBER:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsAmber, true);
                    break;
                case COLOR_ID_ORANGE:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsOrange, true);
                    break;
                case COLOR_ID_DEEPORANGE:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsDeepOrange, true);
                    break;
                case COLOR_ID_SOFTORANGE:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsSoftOrange, true);
                    break;
                case COLOR_ID_BROWN:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsBrown, true);
                    break;
                case COLOR_ID_GREY:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsGrey, true);
                    break;
                case COLOR_ID_BLUEGREY:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsBlueGrey, true);
                    break;
                case COLOR_ID_JVC:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsJVC, true);
                    break;
                case COLOR_ID_SOFTBLACK:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsSoftBlack, true);
                    break;
                case COLOR_ID_BLACK:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsBlack, true);
                    break;
                case COLOR_ID_DEFAULT:
                default:
                    thisActivity.getTheme().applyStyle(R.style.TopicNameAndAccentColorIsDefault, true);
                    break;
            }
        }
    }

    public static ThemeName getThemeUsed() {
        return themeUsed;
    }

    public static boolean getToolbarTextColorIsInvertedForThemeLight() {
        return toolbarTextColorIsInvertedForThemeLight;
    }

    public static int getHeaderColorUsedForThemeLight() {
        return realHeaderColorUsedForLightTheme;
    }

    public static int getPrimaryColorIdUsedForThemeLight() {
        return primaryColorIdUsedForThemeLight;
    }

    public static int getTopicNameAndAccentColorIdUsedForThemeLight() {
        return topicNameAndAccentColorIdUsedForThemeLight;
    }

    public static boolean currentThemeUseDarkColors() {
        return themeUsed != ThemeName.LIGHT_THEME;
    }

    @ColorInt
    public static int getColorInt(@AttrRes int thisAttrColor, Context fromThisContext) {
        if (themeUsed == ThemeName.LIGHT_THEME) {
            switch (thisAttrColor) {
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

    public static Drawable getDrawable(@AttrRes int thisAttrDrawable, Context fromThisContext) {
        if (fromThisContext != null) {
            TypedValue typedValue = new TypedValue();
            boolean valueIsFound = fromThisContext.getTheme().resolveAttribute(thisAttrDrawable, typedValue, true);

            if (valueIsFound) {
                return fromThisContext.getResources().getDrawable(typedValue.resourceId);
            }
        }

        return new ColorDrawable(Color.TRANSPARENT);
    }

    public enum ThemeName {
        LIGHT_THEME, GREY_THEME, DARK_THEME, BLACK_THEME
    }
}
