package com.franckrj.respawnirc.base;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.Undeprecator;

public abstract class AbsThemedActivity extends AppCompatActivity {
    protected static ActivityManager.TaskDescription generalTaskDesc = null;
    protected static @ColorRes int colorUsedForGenerateTaskDesc = 0;
    protected ThemeManager.ThemeName lastThemeUsed = null;
    protected @ColorRes int lastColorPrimaryUsed = 0;
    protected @StyleRes int colorAccentStyle = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeManager.changeActivityTheme(this);
        ThemeManager.changeActivityPrimaryColorIfNeeded(this);
        lastThemeUsed = ThemeManager.getThemeUsed();
        lastColorPrimaryUsed = ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PRIMARY);

        if (colorAccentStyle != 0) {
            getTheme().applyStyle(colorAccentStyle, true);
        }

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            if (generalTaskDesc == null || colorUsedForGenerateTaskDesc != ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PRIMARY)) {
                Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_rirc);
                colorUsedForGenerateTaskDesc = ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PRIMARY);
                generalTaskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), appIcon, Undeprecator.resourcesGetColor(getResources(), colorUsedForGenerateTaskDesc));
            }
            setTaskDescription(generalTaskDesc);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if ((lastThemeUsed != null && lastThemeUsed != ThemeManager.getThemeUsed()) ||
                (lastColorPrimaryUsed != 0 && lastColorPrimaryUsed != ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PRIMARY))) {
            recreate();
        }
    }
}
