package com.franckrj.respawnirc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.franckrj.respawnirc.jvcforumlist.SelectForumInListActivity;
import com.franckrj.respawnirc.jvcforum.ShowForumActivity;
import com.franckrj.respawnirc.jvctopic.ShowTopicActivity;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.StickerConverter;
import com.franckrj.respawnirc.utils.WebManager;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final int ACTIVITY_SHOW_FORUM = 0;
    public static final int ACTIVITY_SHOW_TOPIC = 1;
    public static final int ACTIVITY_SELECT_FORUM_IN_LIST = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int lastActivityViewed;

        PrefsManager.initializeSharedPrefs(getApplicationContext());
        lastActivityViewed = PrefsManager.getInt(PrefsManager.IntPref.Names.LAST_ACTIVITY_VIEWED);

        //vider le cache des webviews
        WebView obj = new WebView(this);
        obj.clearCache(true);

        File[] listOfImagesCached = getCacheDir().listFiles();
        if (listOfImagesCached != null) {
            if (listOfImagesCached.length > 50) {
                for (File thisFile : listOfImagesCached) {
                    if (!thisFile.isDirectory() && (thisFile.getName().startsWith("nlshck_") || thisFile.getName().startsWith("vtr_"))) {
                        //noinspection ResultOfMethodCallIgnored
                        thisFile.delete();
                    }
                }
            }
        }

        System.setProperty("http.keepAlive", "false");
        WebManager.generateNewUserAgent();
        StickerConverter.initializeBasesRules();

        if (lastActivityViewed == ACTIVITY_SELECT_FORUM_IN_LIST) {
            startActivity(new Intent(this, SelectForumInListActivity.class));
        } else {
            startActivity(new Intent(this, ShowForumActivity.class));
            if (lastActivityViewed == ACTIVITY_SHOW_TOPIC) {
                startActivity(new Intent(this, ShowTopicActivity.class));
            }
        }

        finish();
    }
}
