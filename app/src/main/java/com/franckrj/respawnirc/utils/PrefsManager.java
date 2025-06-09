package com.franckrj.respawnirc.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.collection.SimpleArrayMap;

import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.AbsShowTopicFragment;

public class PrefsManager {
    public static final int CURRENT_SHORTCUT_VERSION_NUMBER = 2;

    private static SharedPreferences currentPrefs = null;
    private static SharedPreferences.Editor currentPrefsEdit = null;
    private static SimpleArrayMap<BoolPref.Names, BoolPref> listOfBoolPrefs = new SimpleArrayMap<>();
    private static SimpleArrayMap<IntPref.Names, IntPref> listOfIntPrefs = new SimpleArrayMap<>();
    private static SimpleArrayMap<StringPref.Names, StringPref> listOfStringPrefs = new SimpleArrayMap<>();
    private static SimpleArrayMap<LongPref.Names, LongPref> listOfLongPrefs = new SimpleArrayMap<>();

    private static void addBoolPref(BoolPref.Names nameOfPref, String prefStringValue, boolean prefDefaultValue) {
        listOfBoolPrefs.put(nameOfPref, new BoolPref(prefStringValue, prefDefaultValue));
    }

    private static void addIntPref(IntPref.Names nameOfPref, String prefStringValue, int prefDefaultValue) {
        listOfIntPrefs.put(nameOfPref, new IntPref(prefStringValue, prefDefaultValue));
    }

    private static void addStringPref(StringPref.Names nameOfPref, String prefStringValue, String prefDefaultValue) {
        listOfStringPrefs.put(nameOfPref, new StringPref(prefStringValue, prefDefaultValue));
    }

    private static void addStringPref(StringPref.Names nameOfPref, String prefStringValue, String prefDefaultValue, int newMinVal, int newMaxVal) {
        listOfStringPrefs.put(nameOfPref, new StringPref(prefStringValue, prefDefaultValue, newMinVal, newMaxVal));
    }

    @SuppressWarnings("SameParameterValue")
    private static void addLongPref(LongPref.Names nameOfPref, String prefStringValue, long prefDefaultValue) {
        listOfLongPrefs.put(nameOfPref, new LongPref(prefStringValue, prefDefaultValue));
    }

    @SuppressLint("CommitPrefEdits")
    public static void initializeSharedPrefs(Context currentContext) {
        currentPrefs = currentContext.getSharedPreferences(currentContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        currentPrefsEdit = currentPrefs.edit();

        addBoolPref(BoolPref.Names.IS_FIRST_LAUNCH, "pref.isFirstLaunch", true);
        addBoolPref(BoolPref.Names.USER_IS_MODO, "pref.userIsModo", false);
        addBoolPref(BoolPref.Names.USE_LAST_MESSAGE_DRAFT_SAVED, "pref.useLastMessageDraftSaved", true);
        addBoolPref(BoolPref.Names.USE_LAST_TOPIC_DRAFT_SAVED, "pref.useLastTopicDraftSaved", true);

        addIntPref(IntPref.Names.LAST_ACTIVITY_VIEWED, "pref.lastActivityViewed", MainActivity.ACTIVITY_SELECT_FORUM_IN_LIST);
        addIntPref(IntPref.Names.CURRENT_TOPIC_MODE, "pref.currentTopicMode", AbsShowTopicFragment.MODE_FORUM);
        addIntPref(IntPref.Names.FORUM_FAV_ARRAY_SIZE, "pref.forumFavArraySize", 0);
        addIntPref(IntPref.Names.TOPIC_FAV_ARRAY_SIZE, "pref.topicFavArraySize", 0);
        addIntPref(IntPref.Names.ACCOUNT_ARRAY_SIZE, "pref.reserveAccountArraySize", 0);
        addIntPref(IntPref.Names.LAST_ROW_SELECTED_INSERTSTUFF, "pref.lastRowSelecetdInsertstuff", 1);
        addIntPref(IntPref.Names.NUMBER_OF_WEBVIEW_OPEN_SINCE_CACHE_CLEARED, "pref.numberOfWebviewOpenSinceCacheCleared", 0);
        addIntPref(IntPref.Names.SHORTCUT_VERSION_NUMBER, "pref.shortcutVersionNumber", 0);

        addStringPref(StringPref.Names.PSEUDO_OF_USER, "pref.pseudoUser", "");
        addStringPref(StringPref.Names.COOKIES_LIST, "pref.cookiesList", "");
        addStringPref(StringPref.Names.LAST_MESSAGE_SENDED, "pref.lastMessageSended", "");
        addStringPref(StringPref.Names.FORUM_FAV_NAME, "pref.forumFavName.", "");
        addStringPref(StringPref.Names.FORUM_FAV_LINK, "pref.forumFavLink.", "");
        addStringPref(StringPref.Names.TOPIC_FAV_NAME, "pref.topicFavName.", "");
        addStringPref(StringPref.Names.TOPIC_FAV_LINK, "pref.topicFavLink.", "");
        addStringPref(StringPref.Names.ACCOUNT_PSEUDO, "pref.reserveAccountPseudo.", "");
        addStringPref(StringPref.Names.ACCOUNT_COOKIE, "pref.reserveAccountCookie.", "");
        addStringPref(StringPref.Names.ACCOUNT_IS_MODO, "pref.reserveAccountIsModo.", "false");
        addStringPref(StringPref.Names.TOPIC_URL_TO_FETCH, "pref.topicUrlToFetch", "");
        addStringPref(StringPref.Names.FORUM_URL_TO_FETCH, "pref.forumUrlToFetch", "");
        addStringPref(StringPref.Names.PSEUDO_OF_AUTHOR_OF_TOPIC, "pref.pseudoOfAuthorOfTopic", "");
        addStringPref(StringPref.Names.OLD_URL_FOR_TOPIC, "pref.oldUrlForTopic", "");
        addStringPref(StringPref.Names.LAST_TOPIC_TITLE_SENDED, "pref.lastTopicTitleSended", "");
        addStringPref(StringPref.Names.LAST_TOPIC_CONTENT_SENDED, "pref.lastTopicContentSended", "");
        addStringPref(StringPref.Names.LAST_SURVEY_TITLE_SENDED, "pref.lastSurveyTitleSended", "");
        addStringPref(StringPref.Names.LAST_SURVEY_REPLY_SENDED_IN_A_STRING, "pref.lastSurveyReplySendedInAString", "");
        addStringPref(StringPref.Names.IGNORED_PSEUDOS_IN_LC_LIST, "pref.ignoredPseudosInLCList", "");
        addStringPref(StringPref.Names.MESSAGE_DRAFT, "pref.messageDraft", "");
        addStringPref(StringPref.Names.TOPIC_TITLE_DRAFT, "pref.topicTitleDraft", "");
        addStringPref(StringPref.Names.TOPIC_CONTENT_DRAFT, "pref.topicContentDraft", "");
        addStringPref(StringPref.Names.SURVEY_TITLE_DRAFT, "pref.surveyTitleDraft", "");
        addStringPref(StringPref.Names.SURVEY_REPLY_IN_A_STRING_DRAFT, "pref.surveyReplyInAStringDraft", "");

        addLongPref(LongPref.Names.OLD_LAST_ID_OF_MESSAGE, "pref.oldLastIdOfMessage", 0);

        addBoolPref(BoolPref.Names.TRANSFORM_STICKER_TO_SMILEY, currentContext.getString(R.string.settingsTransformStickerToSmiley), false);
        addBoolPref(BoolPref.Names.SHOW_OVERVIEW_ON_IMAGE_CLICK, currentContext.getString(R.string.settingsShowOverviewOnImageClick), true);
        addBoolPref(BoolPref.Names.USE_DIRECT_NOELSHACK_LINK, currentContext.getString(R.string.settingsUseDirectNoelshackLink), false);
        addBoolPref(BoolPref.Names.SHORTEN_LONG_LINK, currentContext.getString(R.string.settingsShortenLongLink), true);
        addBoolPref(BoolPref.Names.SHOW_SIGNATURE_MODE_FORUM, currentContext.getString(R.string.settingsShowSignatureModeForum), true);
        addBoolPref(BoolPref.Names.SHOW_SIGNATURE_MODE_IRC, currentContext.getString(R.string.settingsShowSignatureModeIRC), false);
        addBoolPref(BoolPref.Names.TOPIC_ALTERNATE_BACKGROUND_MODE_FORUM, currentContext.getString(R.string.settingsTopicAlternateBackgroundColorModeForum), true);
        addBoolPref(BoolPref.Names.TOPIC_ALTERNATE_BACKGROUND_MODE_IRC, currentContext.getString(R.string.settingsTopicAlternateBackgroundColorModeIRC), false);
        addBoolPref(BoolPref.Names.TOPIC_CLEAR_ON_REFRESH_MODE_FORUM, currentContext.getString(R.string.settingsTopicClearOnRefresh), true);
        addBoolPref(BoolPref.Names.TOPIC_SHOW_REFRESH_WHEN_MESSAGE_SHOWED_MODE_IRC, currentContext.getString(R.string.settingsShowRefreshWhenMessagesShowedModeIRC), false);
        addBoolPref(BoolPref.Names.ENABLE_CARD_DESIGN_MODE_FORUM, currentContext.getString(R.string.settingsEnableCardDesignModeForum), true);
        addBoolPref(BoolPref.Names.SEPARATION_BETWEEN_MESSAGES_BLACK_THEM_MODE_FORUM, currentContext.getString(R.string.settingsSeparationBetweenMessagesBlackThemeModeForum), true);
        addBoolPref(BoolPref.Names.HIDE_UGLY_IMAGES, currentContext.getString(R.string.settingsHideUglyImages), false);
        addBoolPref(BoolPref.Names.FORUM_ALTERNATE_BACKGROUND, currentContext.getString(R.string.settingsForumAlternateBackgroundColor), true);
        addBoolPref(BoolPref.Names.ENABLE_SMOOTH_SCROLL, currentContext.getString(R.string.settingsEnableSmoothScroll), true);
        addBoolPref(BoolPref.Names.ENABLE_GO_TO_BOTTOM_ON_LOAD, currentContext.getString(R.string.settingsEnableGoToBottomOnLoad), true);
        addBoolPref(BoolPref.Names.ENABLE_AUTO_SCROLL_MODE_FORUM, currentContext.getString(R.string.settingsEnableAutoScrollModeForum), true);
        addBoolPref(BoolPref.Names.DEFAULT_SHOW_SPOIL_VAL, currentContext.getString(R.string.settingsDefaultShowSpoilVal), false);
        addBoolPref(BoolPref.Names.MARK_AUTHOR_PSEUDO_MODE_FORUM, currentContext.getString(R.string.settingsMarkAuthorPseudoModeForum), true);
        addBoolPref(BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE, currentContext.getString(R.string.settingsPostAsModoWhenPossible), true);
        addBoolPref(BoolPref.Names.IGNORE_TOPIC_TOO, currentContext.getString(R.string.settingsIgnoreTopicToo), true);
        addBoolPref(BoolPref.Names.HIDE_TOTALLY_MESSAGES_OF_IGNORED_PSEUDOS, currentContext.getString(R.string.settingsHideTotallyMessagesOfIgnoredPseudos), true);
        addBoolPref(BoolPref.Names.ENABLE_FAST_REFRESH_OF_IMAGES, currentContext.getString(R.string.settingsEnableFastRefreshOfImages), false);
        addBoolPref(BoolPref.Names.ENABLE_COLOR_DELETED_MESSAGES, currentContext.getString(R.string.settingsEnableColorDeletedMessages), true);
        addBoolPref(BoolPref.Names.BACK_IS_OPEN_DRAWER, currentContext.getString(R.string.settingsBackIsOpenDrawer), false);
        addBoolPref(BoolPref.Names.SAVE_LAST_ROW_USED_INSERTSTUFF, currentContext.getString(R.string.settingsSaveLastRowUsedInsertstuff), true);
        addBoolPref(BoolPref.Names.INVERT_TOOLBAR_TEXT_COLOR, currentContext.getString(R.string.settingsInvertToolbarTextColor), false);
        addBoolPref(BoolPref.Names.REFRESH_FORUM_ON_RESUME, currentContext.getString(R.string.settingsRefreshForumOnResume), false);
        addBoolPref(BoolPref.Names.ENABLE_ALPHA_IN_NOELSHACK_MINI, currentContext.getString(R.string.settingsEnableAlphaInNoelshackMini), false);
        addBoolPref(BoolPref.Names.BRIGHTEN_ALT_COLOR, currentContext.getString(R.string.settingsBrightenAltColor), false);
        addBoolPref(BoolPref.Names.ENABLE_NIVEAU_MODE_FORUM, currentContext.getString(R.string.settingsEnableNiveauModeForum), true);

        addIntPref(IntPref.Names.HEADER_COLOR_OF_LIGHT_THEME, currentContext.getString(R.string.settingsHeaderColorOfLightTheme), Undeprecator.resourcesGetColor(currentContext.getResources(), R.color.defaultHeaderColorThemeLight));
        addIntPref(IntPref.Names.PRIMARY_COLOR_OF_LIGHT_THEME, currentContext.getString(R.string.settingsPrimaryColorOfLightTheme), 0);
        addIntPref(IntPref.Names.TOPIC_NAME_AND_ACCENT_COLOR_OF_LIGHT_THEME, currentContext.getString(R.string.settingsTopicNameAndAccentColorOfLightTheme), 0);
        addIntPref(IntPref.Names.ALT_COLOR_OF_LIGHT_THEME, currentContext.getString(R.string.settingsAltColorOfLightTheme), 0);
        addIntPref(IntPref.Names.SURVEY_COLOR_OF_LIGHT_THEME, currentContext.getString(R.string.settingsSurveyColorOfLightTheme), 0);
        addIntPref(IntPref.Names.DELETED_COLOR_OF_LIGHT_THEME, currentContext.getString(R.string.settingsDeletedColorOfLightTheme), 0);
        addIntPref(IntPref.Names.PSEUDO_USER_COLOR_OF_LIGHT_THEME, currentContext.getString(R.string.settingsPseudoUserColorOfLightTheme), 0);

        addStringPref(StringPref.Names.MAX_NUMBER_OF_OVERLY_QUOTE, currentContext.getString(R.string.settingsMaxNumberOfOverlyQuote), "2", 0, 15);
        addStringPref(StringPref.Names.SHOW_AVATAR_MODE_FORUM, currentContext.getString(R.string.settingsShowAvatarModeForum), String.valueOf(ShowImageType.ALWAYS));
        addStringPref(StringPref.Names.SHOW_NOELSHACK_IMAGE, currentContext.getString(R.string.settingsShowNoelshackImage), String.valueOf(ShowImageType.ALWAYS));
        addStringPref(StringPref.Names.REFRESH_TOPIC_TIME, currentContext.getString(R.string.settingsRefreshTopicTime), "7500", 1_500, 60_000);
        addStringPref(StringPref.Names.MAX_NUMBER_OF_MESSAGES, currentContext.getString(R.string.settingsMaxNumberOfMessages), "60", 1, 120);
        addStringPref(StringPref.Names.INITIAL_NUMBER_OF_MESSAGES, currentContext.getString(R.string.settingsInitialNumberOfMessages), "10", 1, 20);
        addStringPref(StringPref.Names.THEME_USED, currentContext.getString(R.string.settingsThemeUsed), "0");
        addStringPref(StringPref.Names.AVATAR_SIZE, currentContext.getString(R.string.settingsAvatarSize), "45", 40, 60);
        addStringPref(StringPref.Names.STICKER_SIZE, currentContext.getString(R.string.settingsStickerSize), "50", 35, 70);
        addStringPref(StringPref.Names.MINI_NOELSHACK_WIDTH, currentContext.getString(R.string.settingsMiniNoelshackWidth), "68", 68, 136);
        addStringPref(StringPref.Names.LINK_TYPE_FOR_INTERNAL_BROWSER, currentContext.getString(R.string.settingsLinkTypeForInternalBrowser), String.valueOf(LinkType.NO_LINKS));
        addStringPref(StringPref.Names.SAVE_MESSAGES_AND_TOPICS_AS_DRAFT_TYPE, currentContext.getString(R.string.settingsSaveMessagesAndTopicsAsDraftType), String.valueOf(SaveDraftType.ALWAYS));
        addStringPref(StringPref.Names.TOPIC_TITLE_FONT_SIZE, currentContext.getString(R.string.settingsTopicTitleFontSize), "14");
        addStringPref(StringPref.Names.TOPIC_INFOS_FONT_SIZE, currentContext.getString(R.string.settingsTopicInfosFontSize), "14");
        addStringPref(StringPref.Names.MESSAGE_FONT_SIZE, currentContext.getString(R.string.settingsMessageFontSize), "14");
        addStringPref(StringPref.Names.MESSAGE_INFOS_FONT_SIZE, currentContext.getString(R.string.settingsMessageInfosFontSize), "14");
        addStringPref(StringPref.Names.MESSAGE_SIGNATURE_FONT_SIZE, currentContext.getString(R.string.settingsMessageSignatureFontSize), "14");
        addStringPref(StringPref.Names.TYPE_OF_PSEUDO_TO_COLOR_IN_INFO, currentContext.getString(R.string.settingsTypeOfPseudoToColorInInfo), String.valueOf(PseudoColorType.ALL_ACCOUNTS));
        addStringPref(StringPref.Names.TYPE_OF_PSEUDO_TO_COLOR_IN_MESSAGE, currentContext.getString(R.string.settingsTypeOfPseudoToColorInMessage), String.valueOf(PseudoColorType.ALL_ACCOUNTS));

        // Cookies CloudFlare ; ils sont stockés globalement et non pas par compte (contrairement à COOKIES_LIST).
        addStringPref(StringPref.Names.CLOUDFLARE_BOT_PROTECTION, "pref.cloudflareBotProtection", "");
        addStringPref(StringPref.Names.CLOUDFLARE_CLEARANCE, "pref.cloudflareClearance", "");
    }

    public static boolean getBool(BoolPref.Names prefName) {
        BoolPref prefInfo = listOfBoolPrefs.get(prefName);

        //noinspection SimplifiableIfStatement
        if (prefInfo != null) {
            return currentPrefs.getBoolean(prefInfo.stringName, prefInfo.defaultValue);
        } else {
            return false;
        }
    }

    public static boolean getBool(String prefName) {
        for (int i = 0; i < listOfBoolPrefs.size(); ++i) {
            BoolPref tmpPref = listOfBoolPrefs.valueAt(i);
            if (tmpPref.stringName.equals(prefName)) {
                return currentPrefs.getBoolean(tmpPref.stringName, tmpPref.defaultValue);
            }
        }

        return false;
    }

    public static int getInt(IntPref.Names prefName) {
        IntPref prefInfo = listOfIntPrefs.get(prefName);

        if (prefInfo != null) {
            return currentPrefs.getInt(prefInfo.stringName, prefInfo.defaultValue);
        } else {
            return 0;
        }
    }

    public static String getString(StringPref.Names prefName) {
        StringPref prefInfo = listOfStringPrefs.get(prefName);

        if (prefInfo != null) {
            return currentPrefs.getString(prefInfo.stringName, prefInfo.defaultValue);
        } else {
            return "";
        }
    }

    public static int getStringAsInt(StringPref.Names prefName) {
        StringPref prefInfo = listOfStringPrefs.get(prefName);

        if (prefInfo != null) {
            try {
                return Integer.parseInt(currentPrefs.getString(prefInfo.stringName, prefInfo.defaultValue));
            } catch (Exception e) {
                try {
                    return Integer.parseInt(prefInfo.defaultValue);
                } catch (Exception ee) {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    public static String getString(String prefName) {
        for (int i = 0; i < listOfStringPrefs.size(); ++i) {
            StringPref tmpPref = listOfStringPrefs.valueAt(i);
            if (tmpPref.stringName.equals(prefName)) {
                return currentPrefs.getString(tmpPref.stringName, tmpPref.defaultValue);
            }
        }

        return "";
    }

    public static String getStringWithSufix(StringPref.Names prefName, String sufix) {
        StringPref prefInfo = listOfStringPrefs.get(prefName);

        if (prefInfo != null) {
            return currentPrefs.getString(prefInfo.stringName + sufix, prefInfo.defaultValue);
        } else {
            return "";
        }
    }

    public static StringPref getStringInfos(String prefName) {
        for (int i = 0; i < listOfStringPrefs.size(); ++i) {
            StringPref tmpPref = listOfStringPrefs.valueAt(i);
            if (tmpPref.stringName.equals(prefName)) {
                return tmpPref;
            }
        }

        return new StringPref(prefName, "");
    }

    public static long getLong(LongPref.Names prefName) {
        LongPref prefInfo = listOfLongPrefs.get(prefName);

        if (prefInfo != null) {
            return currentPrefs.getLong(prefInfo.stringName, prefInfo.defaultValue);
        } else {
            return 0;
        }
    }

    public static void putBool(BoolPref.Names prefName, boolean newVal) {
        BoolPref prefInfo = listOfBoolPrefs.get(prefName);

        if (prefInfo != null) {
            currentPrefsEdit.putBoolean(prefInfo.stringName, newVal);
        }
    }

    public static void putInt(IntPref.Names prefName, int newVal) {
        IntPref prefInfo = listOfIntPrefs.get(prefName);

        if (prefInfo != null) {
            currentPrefsEdit.putInt(prefInfo.stringName, newVal);
        }
    }

    public static void putString(StringPref.Names prefName, String newVal) {
        StringPref prefInfo = listOfStringPrefs.get(prefName);

        if (prefInfo != null) {
            currentPrefsEdit.putString(prefInfo.stringName, newVal);
        }
    }

    public static void putStringWithSufix(StringPref.Names prefName, String sufix, String newVal) {
        StringPref prefInfo = listOfStringPrefs.get(prefName);

        if (prefInfo != null) {
            currentPrefsEdit.putString(prefInfo.stringName + sufix, newVal);
        }
    }

    public static void putLong(LongPref.Names prefName, long newVal) {
        LongPref prefInfo = listOfLongPrefs.get(prefName);

        if (prefInfo != null) {
            currentPrefsEdit.putLong(prefInfo.stringName, newVal);
        }
    }

    public static void removeStringWithSufix(StringPref.Names prefName, String sufix) {
        StringPref prefInfo = listOfStringPrefs.get(prefName);

        if (prefInfo != null) {
            currentPrefsEdit.remove(prefInfo.stringName + sufix);
        }
    }

    public static void applyChanges() {
        currentPrefsEdit.apply();
    }

    public static class BoolPref {
        public final String stringName;
        public final boolean defaultValue;

        BoolPref(String newStringName, boolean newDefaultValue) {
            stringName = newStringName;
            defaultValue = newDefaultValue;
        }

        public enum Names {
            IS_FIRST_LAUNCH,
            TRANSFORM_STICKER_TO_SMILEY,
            SHOW_OVERVIEW_ON_IMAGE_CLICK,
            USE_DIRECT_NOELSHACK_LINK,
            SHORTEN_LONG_LINK,
            SHOW_SIGNATURE_MODE_FORUM, SHOW_SIGNATURE_MODE_IRC,
            TOPIC_ALTERNATE_BACKGROUND_MODE_FORUM, TOPIC_ALTERNATE_BACKGROUND_MODE_IRC, FORUM_ALTERNATE_BACKGROUND,
            TOPIC_CLEAR_ON_REFRESH_MODE_FORUM,
            TOPIC_SHOW_REFRESH_WHEN_MESSAGE_SHOWED_MODE_IRC,
            ENABLE_CARD_DESIGN_MODE_FORUM, SEPARATION_BETWEEN_MESSAGES_BLACK_THEM_MODE_FORUM, ENABLE_COLOR_DELETED_MESSAGES,
            HIDE_UGLY_IMAGES,
            ENABLE_SMOOTH_SCROLL, ENABLE_AUTO_SCROLL_MODE_FORUM,
            ENABLE_GO_TO_BOTTOM_ON_LOAD,
            DEFAULT_SHOW_SPOIL_VAL,
            MARK_AUTHOR_PSEUDO_MODE_FORUM,
            USER_IS_MODO, POST_AS_MODO_WHEN_POSSIBLE,
            IGNORE_TOPIC_TOO, HIDE_TOTALLY_MESSAGES_OF_IGNORED_PSEUDOS,
            ENABLE_FAST_REFRESH_OF_IMAGES,
            BACK_IS_OPEN_DRAWER,
            SAVE_LAST_ROW_USED_INSERTSTUFF,
            INVERT_TOOLBAR_TEXT_COLOR,
            USE_LAST_MESSAGE_DRAFT_SAVED, USE_LAST_TOPIC_DRAFT_SAVED,
            REFRESH_FORUM_ON_RESUME,
            ENABLE_ALPHA_IN_NOELSHACK_MINI,
            BRIGHTEN_ALT_COLOR,
            ENABLE_NIVEAU_MODE_FORUM
        }
    }

    public static class IntPref {
        public final String stringName;
        public final int defaultValue;

        IntPref(String newStringName, int newDefaultValue) {
            stringName = newStringName;
            defaultValue = newDefaultValue;
        }

        public enum Names {
            LAST_ACTIVITY_VIEWED,
            CURRENT_TOPIC_MODE,
            FORUM_FAV_ARRAY_SIZE, TOPIC_FAV_ARRAY_SIZE, ACCOUNT_ARRAY_SIZE,
            LAST_ROW_SELECTED_INSERTSTUFF,
            HEADER_COLOR_OF_LIGHT_THEME, PRIMARY_COLOR_OF_LIGHT_THEME, TOPIC_NAME_AND_ACCENT_COLOR_OF_LIGHT_THEME,
            ALT_COLOR_OF_LIGHT_THEME, SURVEY_COLOR_OF_LIGHT_THEME, DELETED_COLOR_OF_LIGHT_THEME,
            NUMBER_OF_WEBVIEW_OPEN_SINCE_CACHE_CLEARED,
            SHORTCUT_VERSION_NUMBER,
            PSEUDO_USER_COLOR_OF_LIGHT_THEME
        }
    }

    public static class StringPref {
        public final String stringName;
        public final String defaultValue;
        public final boolean isInt;
        public final int minVal;
        public final int maxVal;

        StringPref(String newStringName, String newDefaultValue) {
            stringName = newStringName;
            defaultValue = newDefaultValue;
            isInt = false;
            minVal = 0;
            maxVal = 0;
        }

        StringPref(String newStringName, String newDefaultValue, int newMinVal, int newMaxVal) {
            stringName = newStringName;
            defaultValue = newDefaultValue;
            isInt = true;
            minVal = newMinVal;
            maxVal = newMaxVal;
        }

        public enum Names {
            PSEUDO_OF_USER, COOKIES_LIST,
            LAST_MESSAGE_SENDED,
            FORUM_FAV_NAME, FORUM_FAV_LINK, TOPIC_FAV_NAME, TOPIC_FAV_LINK,
            ACCOUNT_PSEUDO, ACCOUNT_COOKIE, ACCOUNT_IS_MODO,
            TOPIC_URL_TO_FETCH, FORUM_URL_TO_FETCH, PSEUDO_OF_AUTHOR_OF_TOPIC,
            OLD_URL_FOR_TOPIC,
            LAST_TOPIC_TITLE_SENDED, LAST_TOPIC_CONTENT_SENDED, LAST_SURVEY_TITLE_SENDED, LAST_SURVEY_REPLY_SENDED_IN_A_STRING,
            MAX_NUMBER_OF_OVERLY_QUOTE,
            SHOW_AVATAR_MODE_FORUM,
            SHOW_NOELSHACK_IMAGE,
            REFRESH_TOPIC_TIME,
            MAX_NUMBER_OF_MESSAGES, INITIAL_NUMBER_OF_MESSAGES,
            THEME_USED,
            IGNORED_PSEUDOS_IN_LC_LIST,
            AVATAR_SIZE, STICKER_SIZE, MINI_NOELSHACK_WIDTH,
            LINK_TYPE_FOR_INTERNAL_BROWSER,
            SAVE_MESSAGES_AND_TOPICS_AS_DRAFT_TYPE,
            MESSAGE_DRAFT, TOPIC_TITLE_DRAFT, TOPIC_CONTENT_DRAFT, SURVEY_TITLE_DRAFT, SURVEY_REPLY_IN_A_STRING_DRAFT,
            TOPIC_TITLE_FONT_SIZE, TOPIC_INFOS_FONT_SIZE, MESSAGE_FONT_SIZE, MESSAGE_INFOS_FONT_SIZE, MESSAGE_SIGNATURE_FONT_SIZE,
            TYPE_OF_PSEUDO_TO_COLOR_IN_INFO, TYPE_OF_PSEUDO_TO_COLOR_IN_MESSAGE,
            CLOUDFLARE_BOT_PROTECTION, CLOUDFLARE_CLEARANCE
        }
    }

    public static class LongPref {
        public final String stringName;
        public final long defaultValue;

        LongPref(String newStringName, long newDefaultValue) {
            stringName = newStringName;
            defaultValue = newDefaultValue;
        }

        public enum Names {
            OLD_LAST_ID_OF_MESSAGE
        }
    }

    public static class PseudoColorType extends IntValueType{
        public static final int ALL_ACCOUNTS = 0;
        public static final int CURRENT_ONLY = 1;
        @SuppressWarnings("unused")
        public static final int NONE = 2;

        public PseudoColorType(int newDefaultType) {
            super(newDefaultType);
        }
    }

    public static class LinkType extends IntValueType{
        public static final int ALL_LINKS = 0;
        public static final int JVC_LINKS_ONLY = 1;
        public static final int NO_LINKS = 2;

        public LinkType(int newDefaultType) {
            super(newDefaultType);
        }
    }

    public static class ShowImageType extends IntValueType {
        public static final int ALWAYS = 0;
        public static final int WIFI_ONLY = 1;
        @SuppressWarnings("unused")
        public static final int NEVER = 2;

        public ShowImageType(int newDefaultType) {
            super(newDefaultType);
        }
    }

    public static class SaveDraftType extends IntValueType {
        public static final int ALWAYS = 0;
        public static final int ASK_BEFORE = 1;
        public static final int NEVER = 2;

        public SaveDraftType(int newDefaultType) {
            super(newDefaultType);
        }
    }

    private abstract static class IntValueType {
        protected final int defaultType;
        public int type;

        public IntValueType(int newDefaultType) {
            defaultType = newDefaultType;
            type = defaultType;
        }

        public int getDefaultType() {
            return defaultType;
        }

        public void setTypeFromString(String newType) {
            try {
                type = Integer.parseInt(newType);
            } catch (Exception e) {
                type = defaultType;
            }
        }
    }
}
