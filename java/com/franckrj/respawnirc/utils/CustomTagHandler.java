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
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.toLowerCase().equals("s")) {
            processStrike(opening, output);
        } else if (tag.toLowerCase().equals("bg_closed_spoil")) {
            processBackgroundColor(opening, output, (ThemeManager.getThemeUsedIsDark() ? Color.WHITE : Color.BLACK));
        }
    }

    private void processStrike(boolean opening, Editable output) {
        int len = output.length();

        if (opening) {
            output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
        } else {
            Object obj = getLast(output, StrikethroughSpan.class);
            int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len) {
                output.setSpan(new StrikethroughSpan(), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void processBackgroundColor(boolean opening, Editable output, @ColorInt int color) {
        int len = output.length();

        if (opening) {
            output.setSpan(new BackgroundColorSpan(color), len, len, Spannable.SPAN_MARK_MARK);
        } else {
            Object obj = getLast(output, BackgroundColorSpan.class);
            int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len) {
                output.setSpan(new BackgroundColorSpan(color), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private Object getLast(Editable text, Class spanType) {
        Object[] objectArray = text.getSpans(0, text.length(), spanType);

        for (int i = objectArray.length - 1; i >= 0; --i) {
            if (text.getSpanFlags(objectArray[i]) == Spannable.SPAN_MARK_MARK) {
                return objectArray[i];
            }
        }
        return null;
    }
}
