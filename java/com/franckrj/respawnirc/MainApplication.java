package com.franckrj.respawnirc;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.franckrj.respawnirc.utils.IgnoreListManager;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.StickerConverter;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.WebManager;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        System.setProperty("http.keepAlive", "true");

        PrefsManager.initializeSharedPrefs(getApplicationContext());
        ThemeManager.updateThemeUsed();
        IgnoreListManager.loadListOfIgnoredPseudos();
        StickerConverter.initializeBasesRules();

        NetworkBroadcastReceiver.updateConnectionInfos(getApplicationContext());
        getApplicationContext().registerReceiver(new NetworkBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        WebManager.generateNewUserAgent();
    }
}
