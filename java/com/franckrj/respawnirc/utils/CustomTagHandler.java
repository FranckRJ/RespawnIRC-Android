package com.franckrj.respawnirc.utils;

import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;

import org.xml.sax.XMLReader;

public class CustomTagHandler implements Html.TagHandler {
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.toLowerCase().equals("s")) {
            processStrike(opening, output);
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
