package com.franckrj.respawnirc;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.franckrj.respawnirc.base.AbsToolbarActivity;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

public class WebBrowserActivity extends AbsToolbarActivity {
    public static final String EXTRA_URL_LOAD = "com.franckrj.respawnirc.webbrowseractivity.EXTRA_URL_LOAD";

    private static final String SAVE_TITLE_FOR_BROWSER = "saveTitleForBrowser";
    private static final String SAVE_URL_FOR_BROWSER = "saveUrlForBrowser";

    private WebView browserWebView = null;
    private String currentUrl = "";
    private String currentTitle = "";

    private void updateTitleAndSubtitle() {
        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setTitle(currentTitle);
            myActionBar.setSubtitle(currentUrl);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webbrowser);
        initToolbar(R.id.toolbar_webbrowser);

        String cookies = PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST);

        /*L'ancienne COOKIES_LIST contenait deux cookies, la nouvelle n'en contient plus qu'un.*/
        if (cookies.contains(";")) {
            String firstCookie = cookies.substring(0, cookies.indexOf(";"));
            String secondCookie = cookies.substring(cookies.indexOf(";") + 1, cookies.length());
            CookieManager.getInstance().setCookie("http://www.jeuxvideo.com/", firstCookie);
            CookieManager.getInstance().setCookie("http://www.jeuxvideo.com/", secondCookie);
        } else {
            CookieManager.getInstance().setCookie("http://www.jeuxvideo.com/", cookies);
        }

        browserWebView = findViewById(R.id.webview_webbrowser);

        browserWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted (WebView view, String url, Bitmap favicon) {
                currentUrl = url;
                updateTitleAndSubtitle();
            }
        });
        browserWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                currentTitle = title;
                updateTitleAndSubtitle();
            }
        });
        browserWebView.getSettings().setUseWideViewPort(true);
        browserWebView.getSettings().setSupportZoom(true);
        browserWebView.getSettings().setBuiltInZoomControls(true);
        browserWebView.getSettings().setDisplayZoomControls(false);
        browserWebView.getSettings().setJavaScriptEnabled(true);
        Undeprecator.webSettingsSetSaveFormData(browserWebView.getSettings(), false);
        Undeprecator.webSettingsSetSavePassword(browserWebView.getSettings(), false);

        currentTitle = getString(R.string.app_name);
        if (getIntent() != null && savedInstanceState == null) {
            String newUrlToLoad = getIntent().getStringExtra(EXTRA_URL_LOAD);

            if (!Utils.stringIsEmptyOrNull(newUrlToLoad)) {
                currentUrl = newUrlToLoad;
                browserWebView.loadUrl(currentUrl);
            }
        } else if (savedInstanceState != null) {
            currentTitle = savedInstanceState.getString(SAVE_TITLE_FOR_BROWSER, getString(R.string.app_name));
            currentUrl = savedInstanceState.getString(SAVE_URL_FOR_BROWSER, "");

            if (!currentUrl.isEmpty()) {
                browserWebView.loadUrl(currentUrl);
            }
        }

        updateTitleAndSubtitle();

        PrefsManager.putInt(PrefsManager.IntPref.Names.NUMBER_OF_WEBVIEW_OPEN_SINCE_CACHE_CLEARED,
                            PrefsManager.getInt(PrefsManager.IntPref.Names.NUMBER_OF_WEBVIEW_OPEN_SINCE_CACHE_CLEARED) + 1);
        PrefsManager.applyChanges();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_webbrowser, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_TITLE_FOR_BROWSER, currentTitle);
        outState.putString(SAVE_URL_FOR_BROWSER, currentUrl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_open_in_external_browser_webbrowser:
                Utils.openLinkInExternalBrowser(currentUrl, this);
                return true;
            case R.id.action_copy_url_webbrowser:
                Utils.putStringInClipboard(currentUrl, this);
                Toast.makeText(this, R.string.copyDone, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_reload_page_webbrowser:
                browserWebView.reload();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (browserWebView.canGoBack()) {
            browserWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
