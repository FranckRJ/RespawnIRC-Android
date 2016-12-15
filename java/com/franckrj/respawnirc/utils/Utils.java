package com.franckrj.respawnirc.utils;

import android.app.Activity;

public class Utils {
    public static String resColorToString(int resID, Activity baseActivity) {
        return String.format("#%06X", 0xFFFFFF & Undeprecator.resourcesGetColor(baseActivity.getResources(), resID));
    }
}
