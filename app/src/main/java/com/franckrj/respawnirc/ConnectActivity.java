package com.franckrj.respawnirc;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.utils.AccountManager;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

public class ConnectActivity extends AbsHomeIsBackActivity {
    private static final long MAX_TIME_USER_HAVE_TO_LEAVE_IN_MS = 3_500;

    private WebView jvcWebView = null;
    private EditText pseudoText = null;
    private HelpConnectDialogFragment helpDialogFragment = null;
    private long lastTimeUserTryToLeaveInMs = -MAX_TIME_USER_HAVE_TO_LEAVE_IN_MS;

    private final View.OnClickListener saveCookieClickedListener = view -> {
        String allCookiesInstring = CookieManager.getInstance().getCookie("https://www.jeuxvideo.com/");
        if (!pseudoText.getText().toString().isEmpty()) {
            String[] allCookiesInStringArray = TextUtils.split(allCookiesInstring, ";");
            String connectCookieValue = null;

            for (String thisCookie : allCookiesInStringArray) {
                String[] cookieInfos;

                thisCookie = thisCookie.trim();
                cookieInfos = TextUtils.split(thisCookie, "=");

                if (cookieInfos.length > 1) {
                    if (cookieInfos[0].equals("coniunctio")) {
                        connectCookieValue = cookieInfos[1];
                        break;
                    }
                }
            }

            if (connectCookieValue != null) {
                String pseudo = pseudoText.getText().toString().trim();
                String cookie = "coniunctio=" + connectCookieValue;
                AccountManager.setCurrentAccount(new AccountManager.AccountInfos(pseudo, cookie, false));

                Toast.makeText(ConnectActivity.this, R.string.connectionSuccessful, Toast.LENGTH_SHORT).show();

                finish();
                return;
            }
        } else {
            Toast.makeText(ConnectActivity.this, R.string.errorPseudoMissingConnect, Toast.LENGTH_LONG).show();

            return;
        }

        Toast.makeText(ConnectActivity.this, R.string.errorCookiesMissingConnect, Toast.LENGTH_LONG).show();
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        initToolbar(R.id.toolbar_connect);

        Button saveCookieButton = findViewById(R.id.savecookie_button_connect);
        jvcWebView = findViewById(R.id.webview_connect);
        pseudoText = findViewById(R.id.pseudo_text_connect);

        helpDialogFragment = new HelpConnectDialogFragment();
        saveCookieButton.setOnClickListener(saveCookieClickedListener);

        /* On efface tous les cookies de la WebView pour se déconnecter.   */
        /* On perd les cookies CloudFlare de la WebView mais pas le choix. */
        CookieManager.getInstance().removeAllCookies(null);

        jvcWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.startsWith("https://www.jeuxvideo.com")) {
                    jvcWebView.evaluateJavascript("Didomi.setUserAgreeToAll();", null);
                }
            }
        });
        jvcWebView.setWebChromeClient(new WebChromeClient());
        jvcWebView.getSettings().setJavaScriptEnabled(true);
        jvcWebView.getSettings().setDomStorageEnabled(true);
        Undeprecator.webSettingsSetSaveFormData(jvcWebView.getSettings(), false);
        Undeprecator.webSettingsSetSavePassword(jvcWebView.getSettings(), false);
        jvcWebView.clearCache(true);
        jvcWebView.clearHistory();

        // Clears HTML5, SQL and JS cache (DOM storage).
        // Disables dark mode as a side effect, however.
        WebStorage.getInstance().deleteAllData();

        jvcWebView.loadUrl("https://www.jeuxvideo.com/login");

        PrefsManager.putInt(PrefsManager.IntPref.Names.NUMBER_OF_WEBVIEW_OPEN_SINCE_CACHE_CLEARED,
                PrefsManager.getInt(PrefsManager.IntPref.Names.NUMBER_OF_WEBVIEW_OPEN_SINCE_CACHE_CLEARED) + 1);
        PrefsManager.applyChanges();
    }

    @Override
    public void onResume() {
        super.onResume();
        jvcWebView.resumeTimers();
        jvcWebView.onResume();
    }

    @Override
    public void onPause() {
        jvcWebView.onPause();
        jvcWebView.pauseTimers();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        jvcWebView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_showhelp_connect:
                if (!getSupportFragmentManager().isStateSaved()) {
                    helpDialogFragment.show(getSupportFragmentManager(), "HelpConnectDialogFragment");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        long currentTimeInMs = System.currentTimeMillis();

        if (currentTimeInMs - lastTimeUserTryToLeaveInMs < MAX_TIME_USER_HAVE_TO_LEAVE_IN_MS) {
            Toast.makeText(this, getString(R.string.warningNotConnected), Toast.LENGTH_LONG).show();
            super.onBackPressed();
        } else {
            Toast.makeText(this, getString(R.string.pressBackTwoTimesToLeaveConnect), Toast.LENGTH_LONG).show();
        }

        lastTimeUserTryToLeaveInMs = currentTimeInMs;
    }

    public static class HelpConnectDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.help).setMessage(R.string.help_dialog_connect)
                    .setNeutralButton(R.string.ok, (dialog, id) -> dialog.dismiss());
            return builder.create();
        }
    }
}
