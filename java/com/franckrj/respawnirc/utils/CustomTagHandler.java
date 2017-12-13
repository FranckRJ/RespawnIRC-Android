package com.franckrj.respawnirc.utils;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.StrikethroughSpan;

import org.xml.sax.XMLReader;

public class CustomTagHandler implements Html.TagHandler {
    private static @ColorInt int almostDimGray = Color.rgb(102, 102, 102);

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.toLowerCase().equals("s")) {
            processAddOfSpan(opening, output, new StrikethroughSpan());
        } else if (tag.toLowerCase().equals("bg_spoil_button")) {
            processAddOfSpan(opening, output, new BackgroundColorSpan(ThemeManager.currentThemeUseDarkColors() ? Color.WHITE : Color.BLACK));
        } else if (tag.toLowerCase().equals("bg_spoil_content")) {
            processAddOfSpan(opening, output, new BackgroundColorSpan(ThemeManager.currentThemeUseDarkColors() ? almostDimGray : Color.LTGRAY));
        } else if (tag.toLowerCase().startsWith("holdstring_")) {
            String stringToHold = tag.substring(tag.indexOf("_") + 1);
            processAddOfSpan(opening, output, new HoldingStringSpan(stringToHold));
        }
    }

    private static void processAddOfSpan(boolean opening, Editable output, Object thisSpan) {
        int len = output.length();

        if (opening) {
            output.setSpan(thisSpan, len, len, Spannable.SPAN_MARK_MARK);
        } else {
            Object obj = getLast(output, thisSpan.getClass());
            int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len) {
                output.setSpan(thisSpan, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static Object getLast(Editable text, Class spanType) {
        Object[] objectArray = text.getSpans(0, text.length(), spanType);

        for (int i = objectArray.length - 1; i >= 0; --i) {
            if (text.getSpanFlags(objectArray[i]) == Spannable.SPAN_MARK_MARK) {
                return objectArray[i];
            }
        }
        return null;
    }
}
