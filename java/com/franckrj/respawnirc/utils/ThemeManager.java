package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import com.franckrj.respawnirc.R;

public class ThemeManager {
    private static ThemeName themeUsed = ThemeName.LIGHT_THEME;

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

    public static ThemeName getThemeUsed() {
        return themeUsed;
    }

    public static boolean getThemeUsedIsDark() {
        return themeUsed == ThemeName.DARK_THEME || themeUsed == ThemeName.BLACK_THEME;
    }

    @ColorRes
    public static int getColorRes(ColorName thisColor) {
        if (getThemeUsedIsDark()) {
            switch (thisColor) {
                case COLOR_PRIMARY:
                    return R.color.colorPrimaryThemeDark;
                case COLOR_PRIMARY_DARK:
                    return R.color.colorPrimaryDarkerThemeDark;
                case COLOR_ACCENT:
                    return R.color.colorAccentThemeDark;
                case DEFAULT_BACKGROUND_COLOR:
                    if (themeUsed == ThemeName.BLACK_THEME) {
                        return R.color.defaultBackgroundColorThemeBlack;
                    } else {
                        return R.color.defaultBackgroundColorThemeDark;
                    }
                case DARKER_BACKGROUND_COLOR:
                    if (themeUsed == ThemeName.BLACK_THEME) {
                        return R.color.darkerBackgroundColorThemeBlack;
                    } else {
                        return R.color.darkerBackgroundColorThemeDark;
                    }
                case MORE_DARKER_BACKGROUND_COLOR:
                    if (themeUsed == ThemeName.BLACK_THEME) {
                        return R.color.moreDarkerBackgroundColorThemeBlack;
                    } else {
                        return R.color.moreDarkerBackgroundColorThemeDark;
                    }
                case ALT_BACKGROUND_COLOR:
                    if (themeUsed == ThemeName.BLACK_THEME) {
                        return R.color.altBackgroundColorThemeBlack;
                    } else {
                        return R.color.altBackgroundColorThemeDark;
                    }
                case DELETED_MESSAGE_BACKGROUND_COLOR:
                    return R.color.deletedMessagesBackgroundColorThemeDark;
                case LINK_COLOR:
                    return R.color.linkColorThemeDark;
                case TOPIC_NAME_COLOR:
                    return R.color.topicNameColorThemeDark;
                case COLOR_QUOTE_BACKGROUND:
                    if (themeUsed == ThemeName.BLACK_THEME) {
                        return R.color.colorQuoteBackgroundThemeBlack;
                    } else {
                        return R.color.colorQuoteBackgroundThemeDark;
                    }
                case COLOR_PSEUDO_USER:
                    return R.color.colorPseudoUserThemeDark;
                case COLOR_PSEUDO_OTHER_MODE_FORUM:
                    return R.color.colorPseudoOtherModeForumThemeDark;
                case COLOR_PSEUDO_OTHER_MODE_IRC:
                    return R.color.colorPseudoOtherModeIRCThemeDark;
                case COLOR_PSEUDO_MODO:
                    return R.color.colorPseudoModoThemeDark;
                case COLOR_PSEUDO_ADMIN:
                    return R.color.colorPseudoAdminThemeDark;
                case HEADER_TEXT_COLOR:
                    return R.color.headerTextColorThemeDark;
                case NAVIGATION_ICON_COLOR:
                    return R.color.navigationIconColorThemeDark;
                default:
                    return R.color.colorPrimaryThemeDark;
            }
        } else {
            switch (thisColor) {
                case COLOR_PRIMARY:
                    return R.color.colorPrimaryThemeLight;
                case COLOR_PRIMARY_DARK:
                    return R.color.colorPrimaryDarkerThemeLight;
                case COLOR_ACCENT:
                    return R.color.colorAccentThemeLight;
                case DEFAULT_BACKGROUND_COLOR:
                    return R.color.defaultBackgroundColorThemeLight;
                case DARKER_BACKGROUND_COLOR:
                    return R.color.darkerBackgroundColorThemeLight;
                case MORE_DARKER_BACKGROUND_COLOR:
                    return R.color.moreDarkerBackgroundColorThemeLight;
                case ALT_BACKGROUND_COLOR:
                    return R.color.altBackgroundColorThemeLight;
                case DELETED_MESSAGE_BACKGROUND_COLOR:
                    return R.color.deletedMessagesBackgroundColorThemeLight;
                case LINK_COLOR:
                    return R.color.linkColorThemeLight;
                case TOPIC_NAME_COLOR:
                    return R.color.topicNameColorThemeLight;
                case COLOR_QUOTE_BACKGROUND:
                    return R.color.colorQuoteBackgroundThemeLight;
                case COLOR_PSEUDO_USER:
                    return R.color.colorPseudoUserThemeLight;
                case COLOR_PSEUDO_OTHER_MODE_FORUM:
                    return R.color.colorPseudoOtherModeForumThemeLight;
                case COLOR_PSEUDO_OTHER_MODE_IRC:
                    return R.color.colorPseudoOtherModeIRCThemeLight;
                case COLOR_PSEUDO_MODO:
                    return R.color.colorPseudoModoThemeLight;
                case COLOR_PSEUDO_ADMIN:
                    return R.color.colorPseudoAdminThemeLight;
                case HEADER_TEXT_COLOR:
                    return R.color.headerTextColorThemeLight;
                case NAVIGATION_ICON_COLOR:
                    return R.color.navigationIconColorThemeLight;
                default:
                    return R.color.colorPrimaryThemeLight;
            }
        }
    }

    @DrawableRes
    public static int getDrawableRes(DrawableName thisDrawable) {
        if (getThemeUsedIsDark()) {
            switch (thisDrawable) {
                case SHADOW_DRAWER:
                    return R.drawable.shadow_drawer_dark;
                case EXPAND_MORE:
                    return R.drawable.ic_action_navigation_expand_more_dark;
                case EXPAND_LESS:
                    return R.drawable.ic_action_navigation_expand_less_dark;
                case ARROW_DROP_DOWN:
                    return R.drawable.ic_action_navigation_arrow_drop_down_dark;
                case CONTENT_SEND:
                    return R.drawable.ic_action_content_send_dark;
                case CONTENT_EDIT:
                    return R.drawable.ic_action_content_edit_dark;
                case TOPIC_LOCK_ICON:
                    return R.drawable.icon_topic_lock_dark;
                case DELETED_IMAGE:
                    return R.drawable.image_deleted_dark;
                case DOWNLOAD_IMAGE:
                    return R.drawable.image_download_dark;
                default:
                    return R.drawable.ic_action_navigation_arrow_drop_down_dark;
            }
        } else {
            switch (thisDrawable) {
                case SHADOW_DRAWER:
                    return R.drawable.shadow_drawer_light;
                case EXPAND_MORE:
                    return R.drawable.ic_action_navigation_expand_more_light;
                case EXPAND_LESS:
                    return R.drawable.ic_action_navigation_expand_less_light;
                case ARROW_DROP_DOWN:
                    return R.drawable.ic_action_navigation_arrow_drop_down_light;
                case CONTENT_SEND:
                    return R.drawable.ic_action_content_send_light;
                case CONTENT_EDIT:
                    return R.drawable.ic_action_content_edit_light;
                case TOPIC_LOCK_ICON:
                    return R.drawable.icon_topic_lock_light;
                case DELETED_IMAGE:
                    return R.drawable.image_deleted_light;
                case DOWNLOAD_IMAGE:
                    return R.drawable.image_download_light;
                default:
                    return R.drawable.ic_action_navigation_arrow_drop_down_light;
            }
        }
    }

    public enum ColorName {
        COLOR_PRIMARY, COLOR_PRIMARY_DARK, COLOR_ACCENT,
        DEFAULT_BACKGROUND_COLOR, DARKER_BACKGROUND_COLOR, MORE_DARKER_BACKGROUND_COLOR, ALT_BACKGROUND_COLOR, DELETED_MESSAGE_BACKGROUND_COLOR,
        LINK_COLOR, TOPIC_NAME_COLOR, COLOR_QUOTE_BACKGROUND,
        COLOR_PSEUDO_USER, COLOR_PSEUDO_OTHER_MODE_FORUM, COLOR_PSEUDO_OTHER_MODE_IRC, COLOR_PSEUDO_MODO, COLOR_PSEUDO_ADMIN,
        HEADER_TEXT_COLOR, NAVIGATION_ICON_COLOR
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
