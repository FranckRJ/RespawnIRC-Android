package com.franckrj.respawnirc;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Undeprecator;

public class ConnectActivity extends AbsHomeIsBackActivity {
    private static final long MAX_TIME_USER_HAVE_TO_LEAVE_IN_MS = 3_500;

    private WebView jvcWebView = null;
    private EditText pseudoText = null;
    private HelpConnectDialogFragment helpDialogFragment = null;
    private long lastTimeUserTryToLeaveInMs = -MAX_TIME_USER_HAVE_TO_LEAVE_IN_MS;

    private final View.OnClickListener saveCookieClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!pseudoText.getText().toString().isEmpty()) {
                String allCookiesInstring = CookieManager.getInstance().getCookie("http://www.jeuxvideo.com/");
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
                    PrefsManager.putString(PrefsManager.StringPref.Names.COOKIES_LIST, "coniunctio=" + connectCookieValue);
                    PrefsManager.putString(PrefsManager.StringPref.Names.PSEUDO_OF_USER, pseudoText.getText().toString().trim());
                    PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, false);
                    PrefsManager.applyChanges();

                    Toast.makeText(ConnectActivity.this, R.string.connectionSuccessful, Toast.LENGTH_SHORT).show();

                    finish();
                    return;
                }
            } else {
                Toast.makeText(ConnectActivity.this, R.string.errorPseudoMissingConnect, Toast.LENGTH_LONG).show();

                return;
            }

            Toast.makeText(ConnectActivity.this, R.string.errorCookiesMissingConnect, Toast.LENGTH_LONG).show();
        }
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

        Undeprecator.cookieManagerRemoveAllCookies(CookieManager.getInstance());
        //suppression de la notification d'utilisation de cookie de JVC dans la webview
        CookieManager.getInstance().setCookie("http://www.jeuxvideo.com/", "wbCookieNotifier=1");

        jvcWebView.setWebViewClient(new WebViewClient());
        jvcWebView.setWebChromeClient(new WebChromeClient());
        jvcWebView.getSettings().setJavaScriptEnabled(true);
        Undeprecator.webSettingsSetSaveFormData(jvcWebView.getSettings(), false);
        Undeprecator.webSettingsSetSavePassword(jvcWebView.getSettings(), false);
        jvcWebView.clearCache(true);
        jvcWebView.clearHistory();

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
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        }
    }
}
