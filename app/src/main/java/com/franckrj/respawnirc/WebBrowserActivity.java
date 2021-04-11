package com.franckrj.respawnirc;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.franckrj.respawnirc.base.AbsToolbarActivity;
import com.franckrj.respawnirc.utils.AccountManager;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

import org.jetbrains.annotations.NotNull;

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

        String cookies = AccountManager.getCurrentAccount().cookie;

        /*L'ancienne COOKIES_LIST contenait deux cookies, la nouvelle n'en contient plus qu'un.*/
        if (cookies.contains(";")) {
            String firstCookie = cookies.substring(0, cookies.indexOf(";"));
            String secondCookie = cookies.substring(cookies.indexOf(";") + 1);
            CookieManager.getInstance().setCookie("https://www.jeuxvideo.com/", firstCookie);
            CookieManager.getInstance().setCookie("https://www.jeuxvideo.com/", secondCookie);
        } else {
            CookieManager.getInstance().setCookie("https://www.jeuxvideo.com/", cookies);
        }

        browserWebView = findViewById(R.id.webview_webbrowser);

        browserWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                currentUrl = url;
                updateTitleAndSubtitle();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.startsWith("https://www.jeuxvideo.com")) {
                    browserWebView.evaluateJavascript("Didomi.setUserAgreeToAll();", null);
                }
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
    public void onResume() {
        super.onResume();
        browserWebView.resumeTimers();
        browserWebView.onResume();
    }

    @Override
    public void onPause() {
        browserWebView.onPause();
        browserWebView.pauseTimers();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        browserWebView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_webbrowser, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
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
                return true;
            case R.id.action_share_url_webbrowser:
                Utils.shareThisLink(currentUrl, this);
                return true;
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
