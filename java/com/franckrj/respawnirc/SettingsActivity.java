package com.franckrj.respawnirc;

import android.content.Intent;
import android.os.Bundle;

import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;

public class SettingsActivity extends AbsHomeIsBackActivity implements SettingsFragment.NewSettingsFileNeedALoad {
    public static final String EXTRA_FILE_TO_LOAD = "com.respawnirc.settingsactivity.EXTRA_FILE_TO_LOAD";
    public static final String EXTRA_PREF_TITLE = "com.respawnirc.settingsactivity.EXTRA_PREF_TITLE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initToolbar(R.id.toolbar_settings);

        if (savedInstanceState == null) {
            int idOfFileToLoad = R.xml.main_settings;
            SettingsFragment newFragmentToAdd = new SettingsFragment();
            Bundle argForFragment = new Bundle();

            if (getIntent() != null) {
                idOfFileToLoad = getIntent().getIntExtra(EXTRA_FILE_TO_LOAD, R.xml.main_settings);
            }

            argForFragment.putInt(SettingsFragment.ARG_FILE_TO_LOAD, idOfFileToLoad);
            newFragmentToAdd.setArguments(argForFragment);

            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame_settings, newFragmentToAdd).commit();
        }

        if (getIntent() != null) {
            String newTitle = getIntent().getStringExtra(EXTRA_PREF_TITLE);

            if (newTitle != null) {
                setTitle(newTitle);
            }
        }
    }

    @Override
    public void getNewSettingsFileId(int fileID, String newTitle) {
        Intent newSettingsActivityIntent = new Intent(this, SettingsActivity.class);
        newSettingsActivityIntent.putExtra(EXTRA_FILE_TO_LOAD, fileID);
        newSettingsActivityIntent.putExtra(EXTRA_PREF_TITLE, newTitle);
        startActivity(newSettingsActivityIntent);
    }
}
