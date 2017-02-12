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

    private static void addLongPref(LongPref.Names nameOfPref, String prefStringValue, long prefDefautlValue) {
        listOfLongPrefs.put(nameOfPref, new LongPref(prefStringValue, prefDefautlValue));
    }

    public static void initializeSharedPrefs(Context currentContext) {
        currentPrefs = currentContext.getSharedPreferences(currentContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        currentPrefsEdit = currentPrefs.edit();

        addBoolPref(BoolPref.Names.IS_FIRST_LAUNCH, "pref.isFirstLaunch", true);

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
        addStringPref(StringPref.Names.OLD_URL_FOR_TOPIC, "pref.oldUrlForTopic", "");
        addStringPref(StringPref.Names.LAST_TOPIC_TITLE_SENDED, "pref.lastTopicTitleSended", "");
        addStringPref(StringPref.Names.LAST_TOPIC_CONTENT_SENDED, "pref.lastTopicContentSended", "");

        addLongPref(LongPref.Names.OLD_LAST_ID_OF_MESSAGE, "pref.oldLastIdOfMessage", 0);
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

    public static String getStringWithSufix(StringPref.Names prefName, String sufix) {
        StringPref prefInfo = listOfStringPrefs.get(prefName);

        if (prefInfo != null) {
            return currentPrefs.getString(prefInfo.stringName + sufix, prefInfo.defaultValue);
        } else {
            return "";
        }
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
            IS_FIRST_LAUNCH
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

        StringPref(String newStringName, String newDefaultValue) {
            stringName = newStringName;
            defaultValue = newDefaultValue;
        }

        public enum Names {
            PSEUDO_OF_USER, COOKIES_LIST,
            LAST_MESSAGE_SENDED,
            FORUM_FAV_NAME, FORUM_FAV_LINK, TOPIC_FAV_NAME, TOPIC_FAV_LINK,
            TOPIC_URL_TO_FETCH, FORUM_URL_TO_FETCH,
            OLD_URL_FOR_TOPIC,
            LAST_TOPIC_TITLE_SENDED, LAST_TOPIC_CONTENT_SENDED
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
