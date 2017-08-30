package com.franckrj.respawnirc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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

import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Undeprecator;

public class ConnectActivity extends AbsThemedActivity {
    private WebView jvcWebView = null;
    private EditText pseudoText = null;
    private HelpConnectDialogFragment helpDialogFragment = null;

    public void saveCookies(View buttonView) {
        if (!pseudoText.getText().toString().isEmpty()) {
            String allCookiesInstring = CookieManager.getInstance().getCookie("http://www.jeuxvideo.com/");
            String[] allCookiesInStringArray = TextUtils.split(allCookiesInstring, ";");
            String helloCookieValue = null;
            String connectCookieValue = null;

            for (String thisCookie : allCookiesInStringArray) {
                String[] cookieInfos;

                thisCookie = thisCookie.trim();
                cookieInfos = TextUtils.split(thisCookie, "=");

                if (cookieInfos.length > 1) {
                    if (cookieInfos[0].equals("dlrowolleh")) {
                        helloCookieValue = cookieInfos[1];
                    } else if (cookieInfos[0].equals("coniunctio")) {
                        connectCookieValue = cookieInfos[1];
                    }
                }

                if (helloCookieValue != null && connectCookieValue != null) {
                    break;
                }
            }

            if (helloCookieValue != null && connectCookieValue != null) {
                PrefsManager.putString(PrefsManager.StringPref.Names.COOKIES_LIST, "dlrowolleh=" + helloCookieValue + ";coniunctio=" + connectCookieValue);
                PrefsManager.putString(PrefsManager.StringPref.Names.PSEUDO_OF_USER, pseudoText.getText().toString().trim());
                PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, false);
                PrefsManager.applyChanges();

                Toast.makeText(this, R.string.connectionSucessful, Toast.LENGTH_SHORT).show();

                super.onBackPressed();
                return;
            }
        } else {
            Toast.makeText(this, R.string.errorPseudoMissingConnect, Toast.LENGTH_LONG).show();

            return;
        }

        Toast.makeText(this, R.string.errorCookiesMissingConnect, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Toolbar myToolbar = findViewById(R.id.toolbar_connect);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        jvcWebView = findViewById(R.id.webview_connect);
        pseudoText = findViewById(R.id.pseudo_text_connect);

        helpDialogFragment = new HelpConnectDialogFragment();

        Undeprecator.cookieManagerRemoveAllCookies(CookieManager.getInstance());

        jvcWebView.setWebViewClient(new WebViewClient());
        jvcWebView.setWebChromeClient(new WebChromeClient());
        jvcWebView.getSettings().setJavaScriptEnabled(true);
        jvcWebView.getSettings().setSaveFormData(false);
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
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_showhelp_connect:
                helpDialogFragment.show(getFragmentManager(), "HelpConnectDialogFragment");
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
            super.onCreateDialog(savedInstanceState);
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
