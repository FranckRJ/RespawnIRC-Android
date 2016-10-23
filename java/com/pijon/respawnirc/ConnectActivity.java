package com.pijon.respawnirc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class ConnectActivity extends AppCompatActivity {
    WebView jvcWebView = null;
    EditText pseudoText = null;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_connect);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();

        if(myActionBar != null) {
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        jvcWebView = (WebView) findViewById(R.id.webview_connect);
        pseudoText = (EditText) findViewById(R.id.pseudo_text_connect);

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
}