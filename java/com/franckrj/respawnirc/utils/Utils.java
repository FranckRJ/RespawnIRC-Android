package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.franckrj.respawnirc.WebNavigatorActivity;

import java.net.URLEncoder;

public class Utils {
    public static String resColorToString(int resID, Activity baseActivity) {
        return String.format("#%06X", 0xFFFFFF & Undeprecator.resourcesGetColor(baseActivity.getResources(), resID));
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

    public static String convertStringToUrlString(String baseString) {
        try {
            baseString = URLEncoder.encode(baseString, "UTF-8");
        } catch (Exception e) {
            baseString = "";
        }
        return baseString;
    }

    public static void openLinkInExternalNavigator(String link, Activity parentActviity) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            parentActviity.startActivity(browserIntent);
        } catch (Exception e) {
            //rien
        }
    }

    public static void openLinkInInternalNavigator(String link, Activity parentActviity) {
        Intent newNavigatorIntent = new Intent(parentActviity, WebNavigatorActivity.class);
        newNavigatorIntent.putExtra(WebNavigatorActivity.EXTRA_URL_LOAD, link);
        parentActviity.startActivity(newNavigatorIntent);
    }

    public static void putStringInClipboard(String textToCopy, Activity fromThisActivity) {
        ClipboardManager clipboard = (ClipboardManager) fromThisActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(textToCopy, textToCopy);
        clipboard.setPrimaryClip(clip);
    }
}
