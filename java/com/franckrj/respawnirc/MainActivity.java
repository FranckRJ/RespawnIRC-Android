package com.franckrj.respawnirc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.franckrj.respawnirc.jvcviewers.ShowForumActivity;
import com.franckrj.respawnirc.jvcviewers.ShowTopicActivity;
import com.franckrj.respawnirc.utils.WebManager;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final int ACTIVITY_SHOW_FORUM = 0;
    public static final int ACTIVITY_SHOW_TOPIC = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int lastActivityViewed = sharedPref.getInt(getString(R.string.prefLastActivityViewed), ACTIVITY_SHOW_FORUM);

        for (File thisFile : getCacheDir().listFiles()) {
            if (!thisFile.isDirectory() && thisFile.getName().startsWith("nlshck_")) {
                //noinspection ResultOfMethodCallIgnored
                thisFile.delete();
            }
        }

        WebManager.generateNewUserAgent();

        startActivity(new Intent(this, ShowForumActivity.class));
        if (lastActivityViewed == ACTIVITY_SHOW_TOPIC) {
            startActivity(new Intent(this, ShowTopicActivity.class));
        }

        finish();
    }
}