package com.franckrj.respawnirc;

import android.content.Intent;
import android.content.pm.ShortcutInfo;
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
import java.util.ArrayList;
import java.util.List;

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
        if (PrefsManager.getInt(PrefsManager.IntPref.Names.NUMBER_OF_WEBVIEW_OPEN_SINCE_CACHE_CLEARED) > 10) {
            WebView obj = new WebView(this);
            obj.clearCache(true);
            PrefsManager.putInt(PrefsManager.IntPref.Names.NUMBER_OF_WEBVIEW_OPEN_SINCE_CACHE_CLEARED, 0);
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

            if (shortcutManager != null) {
                int sizeOfForumFavArray = PrefsManager.getInt(PrefsManager.IntPref.Names.FORUM_FAV_ARRAY_SIZE);
                int oldShortcutVersionNumber = PrefsManager.getInt(PrefsManager.IntPref.Names.SHORTCUT_VERSION_NUMBER);

                try {
                    if (oldShortcutVersionNumber != PrefsManager.CURRENT_SHORTCUT_VERSION_NUMBER) {
                        Utils.updateShortcuts(this, shortcutManager, sizeOfForumFavArray);

                        List<ShortcutInfo> currentPinnedShortcuts = shortcutManager.getPinnedShortcuts();
                        List<ShortcutInfo> currentDynamicShortcuts = shortcutManager.getDynamicShortcuts();
                        List<String> shortcutsIdToDisable = new ArrayList<>();

                        /* Désactivation des shortcuts épinglés mais non présents dans la liste. */
                        for (ShortcutInfo thisPinnedShortcut : currentPinnedShortcuts) {
                            shortcutsIdToDisable.add(thisPinnedShortcut.getId());
                        }
                        for (ShortcutInfo thisDynamicShortcut : currentDynamicShortcuts) {
                            shortcutsIdToDisable.remove(thisDynamicShortcut.getId());
                        }

                        shortcutManager.disableShortcuts(shortcutsIdToDisable, getString(R.string.disabledAfterShortcutUpdate));

                        PrefsManager.putInt(PrefsManager.IntPref.Names.SHORTCUT_VERSION_NUMBER, PrefsManager.CURRENT_SHORTCUT_VERSION_NUMBER);
                        PrefsManager.applyChanges();
                    } else if (sizeOfForumFavArray > 0 && shortcutManager.getDynamicShortcuts().size() == 0) {
                        Utils.updateShortcuts(this, shortcutManager, sizeOfForumFavArray);
                    }
                } catch (Exception e) {
                    /* À ce qu'il parait les fonctions de shortcutManager peuvent crash "when the user is locked",
                     * je sais pas ce que ça veut dire donc dans le doute je mets ça là. */
                }
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
