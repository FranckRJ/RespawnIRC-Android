package com.franckrj.respawnirc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

/*TODO: Récupérer le pseudo automatiquement.*/
public class ConnectActivity extends AppCompatActivity {
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
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();

                sharedPrefEdit.putString(getString(R.string.prefCookiesList), "dlrowolleh=" + helloCookieValue + ";coniunctio=" + connectCookieValue);
                sharedPrefEdit.putString(getString(R.string.prefPseudoUser), pseudoText.getText().toString());
                sharedPrefEdit.apply();

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

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_connect);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        jvcWebView = (WebView) findViewById(R.id.webview_connect);
        pseudoText = (EditText) findViewById(R.id.pseudo_text_connect);

        helpDialogFragment = new HelpConnectDialogFragment();

        CookieManager.getInstance().removeAllCookie();

        jvcWebView.setWebViewClient(new WebViewClient());
        jvcWebView.setWebChromeClient(new WebChromeClient());
        jvcWebView.getSettings().setJavaScriptEnabled(true);
        jvcWebView.getSettings().setSaveFormData(false);
        jvcWebView.getSettings().setSavePassword(false);
        jvcWebView.clearCache(true);
        jvcWebView.clearHistory();

        jvcWebView.loadUrl("http://www.jeuxvideo.com/login");
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
                super.onBackPressed();
                return true;
            case R.id.action_showhelp_connect:
                helpDialogFragment.show(getFragmentManager(), "HelpConnectDialogFragment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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