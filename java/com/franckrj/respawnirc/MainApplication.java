package com.franckrj.respawnirc;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.FontRequestEmojiCompatConfig;
import androidx.core.provider.FontRequest;

import com.franckrj.respawnirc.utils.AccountManager;
import com.franckrj.respawnirc.utils.IgnoreListManager;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.StickerConverter;
import com.franckrj.respawnirc.utils.ThemeManager;

public class MainApplication extends Application {
    private void initializeEmojiCompat() {
        FontRequest fontRequest = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs);
        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(this, fontRequest).setReplaceAll(false);
        EmojiCompat.init(config);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.setProperty("http.keepAlive", "true");

        PrefsManager.initializeSharedPrefs(getApplicationContext());
        ThemeManager.updateThemeUsed();
        ThemeManager.updateToolbarTextColor();
        ThemeManager.updateColorsUsed(getResources());
        AccountManager.loadListOfAccountsInReserve();
        IgnoreListManager.loadListOfIgnoredPseudos();
        StickerConverter.initializeBasesRules();

        NetworkBroadcastReceiver.updateConnectionInfos(getApplicationContext());
        getApplicationContext().registerReceiver(new NetworkBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        initializeEmojiCompat();
    }
}
