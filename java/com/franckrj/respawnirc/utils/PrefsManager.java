package com.franckrj.respawnirc.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.AbsShowTopicFragment;

import java.util.HashMap;

public class PrefsManager {
    private static SharedPreferences currentPrefs = null;
    private static SharedPreferences.Editor currentPrefsEdit = null;
    private static HashMap<BoolPref.Names, BoolPref> listOfBoolPrefs = new HashMap<>();
    private static HashMap<IntPref.Names, IntPref> listOfIntPrefs = new HashMap<>();
    private static HashMap<StringPref.Names, StringPref> listOfStringPrefs = new HashMap<>();
    private static HashMap<LongPref.Names, LongPref> listOfLongPrefs = new HashMap<>();

    private static void addBoolPref(BoolPref.Names nameOfPref, String prefStringValue, boolean prefDefautlValue) {
        listOfBoolPrefs.put(nameOfPref, new BoolPref(prefStringValue, prefDefautlValue));
    }

    private static void addIntPref(IntPref.Names nameOfPref, String prefStringValue, int prefDefautlValue) {
        listOfIntPrefs.put(nameOfPref, new IntPref(prefStringValue, prefDefautlValue));
    }

    private static void addStringPref(StringPref.Names nameOfPref, String prefStringValue, String prefDefautlValue) {
        listOfStringPrefs.put(nameOfPref, new StringPref(prefStringValue, prefDefautlValue));
    }

    private static void addStringPref(StringPref.Names nameOfPref, String prefStringValue, String prefDefautlValue, int newMinVal, int newMaxVal) {
        listOfStringPrefs.put(nameOfPref, new StringPref(prefStringValue, prefDefautlValue, newMinVal, newMaxVal));
    }

    private static void addLongPref(LongPref.Names nameOfPref, String prefStringValue, long prefDefautlValue) {
        listOfLongPrefs.put(nameOfPref, new LongPref(prefStringValue, prefDefautlValue));
    }

    public static void initializeSharedPrefs(Context currentContext) {
        currentPrefs = currentContext.getSharedPreferences(currentContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        currentPrefsEdit = currentPrefs.edit();

        addBoolPref(BoolPref.Names.IS_FIRST_LAUNCH, "pref.isFirstLaunch", true);
        addBoolPref(BoolPref.Names.USER_IS_MODO, "pref.userIsModo", false);

        addIntPref(IntPref.Names.LAST_ACTIVITY_VIEWED, "pref.lastActivityViewed", MainActivity.ACTIVITY_SELECT_FORUM_IN_LIST);
        addIntPref(IntPref.Names.CURRENT_TOPIC_MODE, "pref.currentTopicMode", AbsShowTopicFragment.MODE_FORUM);
        addIntPref(IntPref.Names.FORUM_FAV_ARRAY_SIZE, "pref.forumFavArraySize", 0);
        addIntPref(IntPref.Names.TOPIC_FAV_ARRAY_SIZE, "pref.topicFavArraySize", 0);

        addStringPref(StringPref.Names.PSEUDO_OF_USER, "pref.pseudoUser", "");
        addStringPref(StringPref.Names.COOKIES_LIST, "pref.cookiesList", "");
        addStringPref(StringPref.Names.LAST_MESSAGE_SENDED, "pref.lastMessageSended", "");
        addStringPref(StringPref.Names.FORUM_FAV_NAME, "pref.forumFavName.", "");
        addStringPref(StringPref.Names.FORUM_FAV_LINK, "pref.forumFavLink.", "");
        addStringPref(StringPref.Names.TOPIC_FAV_NAME, "pref.topicFavName.", "");
        addStringPref(StringPref.Names.TOPIC_FAV_LINK, "pref.topicFavLink.", "");
        addStringPref(StringPref.Names.TOPIC_URL_TO_FETCH, "pref.topicUrlToFetch", "");
        addStringPref(StringPref.Names.FORUM_URL_TO_FETCH, "pref.forumUrlToFetch", "");
        addStringPref(StringPref.Names.PSEUDO_OF_AUTHOR_OF_TOPIC, "pref.pseudoOfAuthorOfTopic", "");
        addStringPref(StringPref.Names.OLD_URL_FOR_TOPIC, "pref.oldUrlForTopic", "");
        addStringPref(StringPref.Names.LAST_TOPIC_TITLE_SENDED, "pref.lastTopicTitleSended", "");
        addStringPref(StringPref.Names.LAST_TOPIC_CONTENT_SENDED, "pref.lastTopicContentSended", "");
        addStringPref(StringPref.Names.IGNORED_PSEUDOS_IN_LC_LIST, "pref.ignoredPseudosInLCList", "");

        addLongPref(LongPref.Names.OLD_LAST_ID_OF_MESSAGE, "pref.oldLastIdOfMessage", 0);

        addBoolPref(BoolPref.Names.TRANSFORM_STICKER_TO_SMILEY, currentContext.getString(R.string.settingsTransformStickerToSmiley), false);
        addBoolPref(BoolPref.Names.SHOW_OVERVIEW_ON_IMAGE_CLICK, currentContext.getString(R.string.settingsShowOverviewOnImageClick), true);
        addBoolPref(BoolPref.Names.USE_DIRECT_NOELSHACK_LINK, currentContext.getString(R.string.settingsUseDirectNoelshackLink), false);
        addBoolPref(BoolPref.Names.SHORTEN_LONG_LINK, currentContext.getString(R.string.settingsShortenLongLink), true);
        addBoolPref(BoolPref.Names.USE_INTERNAL_NAVIGATOR, currentContext.getString(R.string.settingsUseInternalNavigator), false);
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
        addBoolPref(BoolPref.Names.MARK_AUTHOR_PSEUDO_MODE_FORUM, currentContext.getString(R.string.settingsMarkAuthorPseudoModeForum), false);
        addBoolPref(BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE, currentContext.getString(R.string.settingsPostAsModoWhenPossible), true);
        addBoolPref(BoolPref.Names.IGNORE_TOPIC_TOO, currentContext.getString(R.string.settingsIgnoreTopicToo), true);

        addStringPref(StringPref.Names.MAX_NUMBER_OF_OVERLY_QUOTE, currentContext.getString(R.string.settingsMaxNumberOfOverlyQuote), "2", 0, 15);
        addStringPref(StringPref.Names.SHOW_AVATAR_MODE_FORUM, currentContext.getString(R.string.settingsShowAvatarModeForum), "1");
        addStringPref(StringPref.Names.SHOW_NOELSHACK_IMAGE, currentContext.getString(R.string.settingsShowNoelshackImage), "1");
        addStringPref(StringPref.Names.REFRESH_TOPIC_TIME, currentContext.getString(R.string.settingsRefreshTopicTime), "10000", 2500, 60000);
        addStringPref(StringPref.Names.MAX_NUMBER_OF_MESSAGES, currentContext.getString(R.string.settingsMaxNumberOfMessages), "60", 1, 120);
        addStringPref(StringPref.Names.INITIAL_NUMBER_OF_MESSAGES, currentContext.getString(R.string.settingsInitialNumberOfMessages), "10", 1, 20);
        addStringPref(StringPref.Names.THEME_USED, currentContext.getString(R.string.settingsThemeUsed), "0");
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
        for (HashMap.Entry<BoolPref.Names, BoolPref> thisPref : listOfBoolPrefs.entrySet()) {
            if (thisPref.getValue().stringName.equals(prefName)) {
                return currentPrefs.getBoolean(thisPref.getValue().stringName, thisPref.getValue().defaultValue);
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

    public static String getString(String prefName) {
        for (HashMap.Entry<StringPref.Names, StringPref> thisPref : listOfStringPrefs.entrySet()) {
            if (thisPref.getValue().stringName.equals(prefName)) {
                return currentPrefs.getString(thisPref.getValue().stringName, thisPref.getValue().defaultValue);
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
        for (HashMap.Entry<StringPref.Names, StringPref> thisPref : listOfStringPrefs.entrySet()) {
            if (thisPref.getValue().stringName.equals(prefName)) {
                return thisPref.getValue();
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
            USE_INTERNAL_NAVIGATOR,
            SHOW_SIGNATURE_MODE_FORUM, SHOW_SIGNATURE_MODE_IRC,
            TOPIC_ALTERNATE_BACKGROUND_MODE_FORUM, TOPIC_ALTERNATE_BACKGROUND_MODE_IRC, FORUM_ALTERNATE_BACKGROUND,
            TOPIC_CLEAR_ON_REFRESH_MODE_FORUM,
            TOPIC_SHOW_REFRESH_WHEN_MESSAGE_SHOWED_MODE_IRC,
            ENABLE_CARD_DESIGN_MODE_FORUM, SEPARATION_BETWEEN_MESSAGES_BLACK_THEM_MODE_FORUM,
            HIDE_UGLY_IMAGES,
            ENABLE_SMOOTH_SCROLL, ENABLE_AUTO_SCROLL_MODE_FORUM,
            ENABLE_GO_TO_BOTTOM_ON_LOAD,
            DEFAULT_SHOW_SPOIL_VAL,
            MARK_AUTHOR_PSEUDO_MODE_FORUM,
            USER_IS_MODO, POST_AS_MODO_WHEN_POSSIBLE,
            IGNORE_TOPIC_TOO
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
            FORUM_FAV_ARRAY_SIZE, TOPIC_FAV_ARRAY_SIZE
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
            TOPIC_URL_TO_FETCH, FORUM_URL_TO_FETCH, PSEUDO_OF_AUTHOR_OF_TOPIC,
            OLD_URL_FOR_TOPIC,
            LAST_TOPIC_TITLE_SENDED, LAST_TOPIC_CONTENT_SENDED,
            MAX_NUMBER_OF_OVERLY_QUOTE,
            SHOW_AVATAR_MODE_FORUM,
            SHOW_NOELSHACK_IMAGE,
            REFRESH_TOPIC_TIME,
            MAX_NUMBER_OF_MESSAGES, INITIAL_NUMBER_OF_MESSAGES,
            THEME_USED,
            IGNORED_PSEUDOS_IN_LC_LIST
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
}
