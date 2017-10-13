package com.franckrj.respawnirc.base;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.Undeprecator;

public abstract class AbsThemedActivity extends AppCompatActivity {
    protected static ActivityManager.TaskDescription generalTaskDesc = null;
    protected static ThemeManager.ThemeName themeUsedForGenerateTaskDesc = null;
    protected ThemeManager.ThemeName oldThemeUsed = null;
    protected @StyleRes int colorAccentStyle = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        oldThemeUsed = ThemeManager.getThemeUsed();
        ThemeManager.changeActivityTheme(this);

        if (colorAccentStyle != 0) {
            getTheme().applyStyle(colorAccentStyle, true);
        }

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            if (generalTaskDesc == null || themeUsedForGenerateTaskDesc != ThemeManager.getThemeUsed()) {
                Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_rirc);
                generalTaskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), appIcon, Undeprecator.resourcesGetColor(getResources(), ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PRIMARY)));
                themeUsedForGenerateTaskDesc = ThemeManager.getThemeUsed();
            }
            setTaskDescription(generalTaskDesc);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (oldThemeUsed != null && oldThemeUsed != ThemeManager.getThemeUsed()) {
            recreate();
        }
    }
}
