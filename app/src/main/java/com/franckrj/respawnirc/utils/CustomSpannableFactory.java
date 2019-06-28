package com.franckrj.respawnirc.utils;

import android.text.Spannable;

public class CustomSpannableFactory extends Spannable.Factory {
    @Override
    public Spannable newSpannable(CharSequence source) {
        if (source instanceof Spannable) {
            return (Spannable) source;
        } else {
            return super.newSpannable(source);
        }
    }
}
