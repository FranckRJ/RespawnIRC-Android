package com.franckrj.respawnirc.base;

import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.franckrj.respawnirc.utils.PrefsManager;

import java.io.File;

public abstract class AbsBaseActivity extends AppCompatActivity {

    private void manageThisImageCache(String cacheName, int imageNbLimit) {
        File cacheDir = new File(getCacheDir().getPath() + "/" + cacheName);
        if (!cacheDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cacheDir.mkdirs();
        }
        File[] listOfImagesCached = cacheDir.listFiles();
        if (listOfImagesCached != null) {
            if (listOfImagesCached.length > imageNbLimit) {
                for (File thisFile : listOfImagesCached) {
                    if (!thisFile.isDirectory()) {
                        //noinspection ResultOfMethodCallIgnored
                        thisFile.delete();
                    }
                }
            }
        }
    }

    public void manageImagesCache() {
        manageThisImageCache("nlsk_mini", 500);
        manageThisImageCache("nlsk_xs", 200);
        manageThisImageCache("nlsk_md", 20);
        manageThisImageCache("nlsk_big", 5);
        manageThisImageCache("vtr_sm", 500);
    }

    public void manageWebViewCache() {
        //vidage du cache des webviews
        if (PrefsManager.getInt(PrefsManager.IntPref.Names.NUMBER_OF_WEBVIEW_OPEN_SINCE_CACHE_CLEARED) > 10) {
            WebView obj = new WebView(this);
            obj.clearCache(true);
            PrefsManager.putInt(PrefsManager.IntPref.Names.NUMBER_OF_WEBVIEW_OPEN_SINCE_CACHE_CLEARED, 0);
            PrefsManager.applyChanges();
        }
    }
}
