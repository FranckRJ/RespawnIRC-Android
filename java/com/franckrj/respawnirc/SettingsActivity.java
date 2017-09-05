package com.franckrj.respawnirc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class SettingsActivity extends AbsThemedActivity implements SettingsFragment.NewSettingsFileNeedALoad {
    public static final String EXTRA_FILE_TO_LOAD = "com.respawnirc.settingsactivity.EXTRA_FILE_TO_LOAD";
    public static final String EXTRA_PREF_TITLE = "com.respawnirc.settingsactivity.EXTRA_PREF_TITLE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            int idOfFileToLoad = R.xml.main_settings;
            SettingsFragment newFragmentToAdd = new SettingsFragment();
            Bundle argForFragment = new Bundle();

            if (getIntent() != null) {
                idOfFileToLoad = getIntent().getIntExtra(EXTRA_FILE_TO_LOAD, R.xml.main_settings);
            }

            argForFragment.putInt(SettingsFragment.ARG_FILE_TO_LOAD, idOfFileToLoad);
            newFragmentToAdd.setArguments(argForFragment);

            getFragmentManager().beginTransaction().replace(R.id.content_frame_settings, newFragmentToAdd).commit();
        }

        if (getIntent() != null) {
            String newTitle = getIntent().getStringExtra(EXTRA_PREF_TITLE);

            if (newTitle != null) {
                setTitle(newTitle);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
