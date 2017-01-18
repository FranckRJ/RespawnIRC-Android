package com.franckrj.respawnirc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

public class WebNavigatorActivity extends AppCompatActivity {
    public static final String EXTRA_URL_LOAD = "com.franckrj.respawnirc.webnavigatoractivity.EXTRA_URL_LOAD";

    private WebView navigatorWebView = null;
    private String currentUrl = "";
    private String currentTitle = "";

    private void updateTitleAndSubtitle() {
        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setTitle(currentTitle);
            myActionBar.setSubtitle(currentUrl);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webnavigator);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_webnavigator);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String cookies = sharedPref.getString(getString(R.string.prefCookiesList), "");

        if (cookies.contains(";")) {
            String firstCookie = cookies.substring(0, cookies.indexOf(";"));
            String secondCookie = cookies.substring(cookies.indexOf(";") + 1, cookies.length());
            CookieManager.getInstance().setCookie("http://www.jeuxvideo.com/", firstCookie);
            CookieManager.getInstance().setCookie("http://www.jeuxvideo.com/", secondCookie);
        }

        navigatorWebView = (WebView) findViewById(R.id.webview_webnavigator);

        navigatorWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted (WebView view, String url, Bitmap favicon) {
                currentUrl = url;
                updateTitleAndSubtitle();
            }
        });
        navigatorWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                currentTitle = title;
                updateTitleAndSubtitle();
            }
        });
        navigatorWebView.getSettings().setJavaScriptEnabled(true);
        navigatorWebView.getSettings().setSaveFormData(false);
        Undeprecator.webSettingsSetSavePassword(navigatorWebView.getSettings(), false);

        currentTitle = getString(R.string.app_name);
        if (getIntent() != null && savedInstanceState == null) {
            String newUrlToLoad = getIntent().getStringExtra(EXTRA_URL_LOAD);

            if (!Utils.stringIsEmptyOrNull(newUrlToLoad)) {
                currentUrl = newUrlToLoad;
                navigatorWebView.loadUrl(currentUrl);
            }
        } else if (savedInstanceState != null) {
            currentTitle = savedInstanceState.getString(getString(R.string.saveTitleForNavigator), getString(R.string.app_name));
            currentUrl = savedInstanceState.getString(getString(R.string.saveUrlForNavigator), "");

            if (!currentUrl.isEmpty()) {
                navigatorWebView.loadUrl(currentUrl);
            }
        }

        updateTitleAndSubtitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_webnavigator, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveUrlForNavigator), currentUrl);
        outState.putString(getString(R.string.saveTitleForNavigator), currentTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_open_in_external_browser_webnavigator:
                Utils.openLinkInExternalNavigator(currentUrl, this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (navigatorWebView.canGoBack()) {
            navigatorWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
