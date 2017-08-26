package com.franckrj.respawnirc;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.Undeprecator;

public class ThemedActivity extends AppCompatActivity {
    private static final String SAVE_IN_IN_PROCESS_OF_RECREATE = "saveIsInProcessOfRecreate";

    protected static ActivityManager.TaskDescription generalTaskDesc = null;
    protected static ThemeManager.ThemeName themeUsedForGenerateTaskDesc = null;
    protected ThemeManager.ThemeName oldThemeUsed = null;
    protected boolean isInProcessOfRecreating = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oldThemeUsed = ThemeManager.getThemeUsed();
        ThemeManager.changeActivityTheme(this);

        if (Build.VERSION.SDK_INT >= 21) {
            if (generalTaskDesc == null || themeUsedForGenerateTaskDesc != ThemeManager.getThemeUsed()) {
                Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_rirc);
                generalTaskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), appIcon, Undeprecator.resourcesGetColor(getResources(), ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PRIMARY)));
                themeUsedForGenerateTaskDesc = ThemeManager.getThemeUsed();
            }
            setTaskDescription(generalTaskDesc);
        }

        if (savedInstanceState != null) {
            isInProcessOfRecreating = savedInstanceState.getBoolean(SAVE_IN_IN_PROCESS_OF_RECREATE, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isInProcessOfRecreating = false;

        if (oldThemeUsed != null && oldThemeUsed != ThemeManager.getThemeUsed()) {
            isInProcessOfRecreating = true;
            recreate();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_IN_IN_PROCESS_OF_RECREATE, isInProcessOfRecreating);
    }
}
