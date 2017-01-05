package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Utils {
    public static String resColorToString(int resID, Activity baseActivity) {
        return String.format("#%06X", 0xFFFFFF & Undeprecator.resourcesGetColor(baseActivity.getResources(), resID));
    }

    public static boolean compareStrings(String str1, String str2) {
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
}
