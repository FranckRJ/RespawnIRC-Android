package com.franckrj.respawnirc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.franckrj.respawnirc.utils.ThemeManager;

public class ThemedActivity extends AppCompatActivity {
    protected ThemeManager.ThemeName oldThemeUsed = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oldThemeUsed = ThemeManager.getThemeUsed();
        ThemeManager.changeActivityTheme(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ThemeManager.ThemeName currentTheme = ThemeManager.getThemeUsed();

        if (oldThemeUsed != null && currentTheme != oldThemeUsed) {
            recreate();
        }
    }
}
