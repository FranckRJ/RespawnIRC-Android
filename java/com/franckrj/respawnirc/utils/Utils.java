package com.franckrj.respawnirc.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.text.Spannable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.WebNavigatorActivity;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String resColorToString(int resId, Activity baseActivity) {
        return String.format("#%06X", 0xFFFFFF & Undeprecator.resourcesGetColor(baseActivity.getResources(), resId));
    }

    public static String resColorToStringWithAlpha(int resId, Activity baseActivity) {
        return String.format("#%08X", Undeprecator.resourcesGetColor(baseActivity.getResources(), resId));
    }

    public static int roundToInt(double valToRound) {
        return (int) (valToRound + 0.5);
    }

    public static boolean stringsAreEquals(String str1, String str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }

    public static String truncateString(String baseString, int maxSize, String endingPartIfCuted) {
        if (baseString.length() > maxSize) {
            baseString = baseString.substring(0, maxSize - endingPartIfCuted.length()) + endingPartIfCuted;
        }

        return baseString;
    }

    public static boolean stringIsEmptyOrNull(String thisString) {
        //noinspection SimplifiableIfStatement
        if (thisString == null) {
            return true;
        }
        return thisString.isEmpty();
    }

    public static void hideSoftKeyboard(Activity fromThisActivity) {
        InputMethodManager inputManager = (InputMethodManager) fromThisActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = fromThisActivity.getCurrentFocus();
        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void replaceSpanByAnotherSpan(Spannable inThisSpan, Object oldSpan, Object newSpan) {
        int start = inThisSpan.getSpanStart(oldSpan);
        int end = inThisSpan.getSpanEnd(oldSpan);
        int flags = inThisSpan.getSpanFlags(oldSpan);
        inThisSpan.setSpan(newSpan, start, end, flags);
        inThisSpan.removeSpan(oldSpan);
    }

    public static String encodeStringToUrlString(String baseString) {
        try {
            baseString = URLEncoder.encode(baseString, "UTF-8");
        } catch (Exception e) {
            baseString = "";
        }
        return baseString;
    }

    public static String decodeUrlStringToString(String baseString) {
        try {
            baseString = URLDecoder.decode(baseString, "UTF-8");
        } catch (Exception e) {
            baseString = "";
        }
        return baseString;
    }

    public static ArrayList<String> mapStringArrayList(List<String> baseList, StringModifier mapFunction) {
        ArrayList<String> newList = new ArrayList<>();

        for (String currentString : baseList) {
            newList.add(mapFunction.changeString(currentString));
        }

        return newList;
    }

    public static void openLinkInExternalNavigator(String link, Activity parentActivity) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            parentActivity.startActivity(browserIntent);
        } catch (Exception e) {
            //rien
        }
    }

    public static void openLinkInInternalNavigator(String link, Activity parentActivity) {
        Intent newNavigatorIntent = new Intent(parentActivity, WebNavigatorActivity.class);
        newNavigatorIntent.putExtra(WebNavigatorActivity.EXTRA_URL_LOAD, link);
        parentActivity.startActivity(newNavigatorIntent);
    }

    public static void putStringInClipboard(String textToCopy, Activity fromThisActivity) {
        ClipboardManager clipboard = (ClipboardManager) fromThisActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(textToCopy, textToCopy);
        clipboard.setPrimaryClip(clip);
    }

    public static void insertStringInEditText(EditText currentEditText, String stringToInsert, int posOfCenterFromEnd) {
        int currentCursorPos = currentEditText.getSelectionStart();
        int currentEndOfSelec = currentEditText.getSelectionEnd();
        if (currentCursorPos == -1) {
            currentCursorPos = 0;
        }
        if (currentEndOfSelec > currentCursorPos && posOfCenterFromEnd > 0) {
            String firstStringToAdd = stringToInsert.substring(0, stringToInsert.length() - posOfCenterFromEnd);
            String secondStringToAdd = stringToInsert.substring(stringToInsert.length() - posOfCenterFromEnd);
            currentEditText.getText().insert(currentEndOfSelec, secondStringToAdd);
            currentEditText.getText().insert(currentCursorPos, firstStringToAdd);
            currentEditText.setSelection(currentEndOfSelec + stringToInsert.length() - posOfCenterFromEnd);
        } else {
            currentEditText.getText().insert(currentCursorPos, stringToInsert);
            currentEditText.setSelection(currentCursorPos + stringToInsert.length() - posOfCenterFromEnd);
        }
    }

    @TargetApi(25)
    public static void updateShortcuts(Activity parentActivity, ShortcutManager shortcutManager, int sizeOfForumFavArray) {
        ArrayList<ShortcutInfo> listOfShortcuts = new ArrayList<>();
        int sizeOfShortcutArray = (sizeOfForumFavArray > 4 ? 4 : sizeOfForumFavArray);

        for (int i = 0; i < sizeOfShortcutArray; ++i) {
            String currentShortcutLink = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.FORUM_FAV_LINK, String.valueOf(i));
            String currentShortcutName = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.FORUM_FAV_NAME, String.valueOf(i));
            ShortcutInfo newShortcut = new ShortcutInfo.Builder(parentActivity, String.valueOf(i) + "_" + currentShortcutLink)
                    .setShortLabel(currentShortcutName)
                    .setLongLabel(currentShortcutName)
                    .setIcon(Icon.createWithResource(parentActivity, R.mipmap.ic_shortcut))
                    .setIntent(new Intent(MainActivity.ACTION_OPEN_LINK, Uri.parse(currentShortcutLink))).build();

            listOfShortcuts.add(newShortcut);
        }

        shortcutManager.setDynamicShortcuts(listOfShortcuts);
    }

    public interface StringModifier {
        String changeString(String baseString);
    }
}
