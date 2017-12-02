package com.franckrj.respawnirc;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Undeprecator;

public class ConnectActivity extends AbsHomeIsBackActivity {
    private EditText pseudoText = null;
    private HelpConnectDialogFragment helpDialogFragment = null;

    public void saveCookies(View buttonView) {
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

                Toast.makeText(this, R.string.connectionSuccessful, Toast.LENGTH_SHORT).show();

                finish();
                return;
            }
        } else {
            Toast.makeText(this, R.string.errorPseudoMissingConnect, Toast.LENGTH_LONG).show();

            return;
        }

        Toast.makeText(this, R.string.errorCookiesMissingConnect, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        initToolbar(R.id.toolbar_connect);

        WebView jvcWebView = findViewById(R.id.webview_connect);
        pseudoText = findViewById(R.id.pseudo_text_connect);

        helpDialogFragment = new HelpConnectDialogFragment();

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

        PrefsManager.putBool(PrefsManager.BoolPref.Names.WEBVIEW_CACHE_NEED_TO_BE_CLEAR, true);
        PrefsManager.applyChanges();
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
                helpDialogFragment.show(getSupportFragmentManager(), "HelpConnectDialogFragment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, getString(R.string.warningNotConnected), Toast.LENGTH_LONG).show();
        super.onBackPressed();
    }

    public static class HelpConnectDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
