package com.franckrj.respawnirc;

import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.franckrj.respawnirc.jvcforumlist.SelectForumInListActivity;
import com.franckrj.respawnirc.jvcforum.ShowForumActivity;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final int ACTIVITY_SHOW_FORUM = 0;
    public static final int ACTIVITY_SHOW_TOPIC = 1;
    public static final int ACTIVITY_SELECT_FORUM_IN_LIST = 2;

    public static final String ACTION_OPEN_LINK = "com.franckrj.respawnirc.ACTION_OPEN_LINK";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int lastActivityViewed = PrefsManager.getInt(PrefsManager.IntPref.Names.LAST_ACTIVITY_VIEWED);
        String linkToOpen = null;

        //vidage du cache des webviews
        if (PrefsManager.getBool(PrefsManager.BoolPref.Names.WEBVIEW_CACHE_NEED_TO_BE_CLEAR)) {
            WebView obj = new WebView(this);
            obj.clearCache(true);
            PrefsManager.putBool(PrefsManager.BoolPref.Names.WEBVIEW_CACHE_NEED_TO_BE_CLEAR, false);
            PrefsManager.applyChanges();
        }

        File[] listOfImagesCached = getCacheDir().listFiles();
        if (listOfImagesCached != null) {
            if (listOfImagesCached.length > 100) {
                for (File thisFile : listOfImagesCached) {
                    if (!thisFile.isDirectory() && thisFile.getName().startsWith("img_")) {
                        //noinspection ResultOfMethodCallIgnored
                        thisFile.delete();
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 25) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            int sizeOfForumFavArray = PrefsManager.getInt(PrefsManager.IntPref.Names.FORUM_FAV_ARRAY_SIZE);

            if (sizeOfForumFavArray > 0 && shortcutManager.getDynamicShortcuts().size() == 0) {
                Utils.updateShortcuts(this, shortcutManager, sizeOfForumFavArray);
            }
        }

        if (getIntent() != null) {
            linkToOpen = getIntent().getDataString();
        }

        if (Utils.stringIsEmptyOrNull(linkToOpen)) {
            Intent firstIntentToLaunch;
            if (lastActivityViewed == ACTIVITY_SELECT_FORUM_IN_LIST) {
                firstIntentToLaunch = new Intent(this, SelectForumInListActivity.class);
                firstIntentToLaunch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(firstIntentToLaunch);
            } else {
                firstIntentToLaunch = new Intent(this, ShowForumActivity.class);
                firstIntentToLaunch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                firstIntentToLaunch.putExtra(ShowForumActivity.EXTRA_ITS_FIRST_START, true);
                startActivity(firstIntentToLaunch);
            }
        } else {
            Intent newShowForumIntent = new Intent(this, ShowForumActivity.class);
            newShowForumIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            newShowForumIntent.putExtra(ShowForumActivity.EXTRA_NEW_LINK, linkToOpen);
            startActivity(newShowForumIntent);
        }

        finish();
    }
}
