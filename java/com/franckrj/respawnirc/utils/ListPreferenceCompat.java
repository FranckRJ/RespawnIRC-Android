package com.franckrj.respawnirc.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class ListPreferenceCompat extends ListPreference {
    @SuppressWarnings("unused")
    @TargetApi(21)
    public ListPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressWarnings("unused")
    @TargetApi(21)
    public ListPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public ListPreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public ListPreferenceCompat(Context context) {
        super(context);
    }

    // Correction d'un bug présent avant android KITKAT
    // http://stackoverflow.com/a/21642401/7413918
    @Override
    public void setValue(String value) {
        if (Build.VERSION.SDK_INT >= 19) {
            super.setValue(value);
        } else {
            String oldValue = getValue();
            super.setValue(value);
            if (!Utils.stringsAreEquals(value, oldValue)) {
                notifyChanged();
            }
        }
    }
}
