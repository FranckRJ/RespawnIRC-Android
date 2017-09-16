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

public class WebNavigatorActivity extends AbsToolbarActivity {
    public static final String EXTRA_URL_LOAD = "com.franckrj.respawnirc.webnavigatoractivity.EXTRA_URL_LOAD";

    private static final String SAVE_TITLE_FOR_NAVIGATOR = "saveTitleForNavigator";
    private static final String SAVE_URL_FOR_NAVIGATOR = "saveUrlForNavigator";

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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webnavigator);
        initToolbar(R.id.toolbar_webnavigator);

        String cookies = PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST);

        if (cookies.contains(";")) {
            String firstCookie = cookies.substring(0, cookies.indexOf(";"));
            String secondCookie = cookies.substring(cookies.indexOf(";") + 1, cookies.length());
            CookieManager.getInstance().setCookie("http://www.jeuxvideo.com/", firstCookie);
            CookieManager.getInstance().setCookie("http://www.jeuxvideo.com/", secondCookie);
        }

        navigatorWebView = findViewById(R.id.webview_webnavigator);

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
        navigatorWebView.getSettings().setUseWideViewPort(true);
        navigatorWebView.getSettings().setSupportZoom(true);
        navigatorWebView.getSettings().setBuiltInZoomControls(true);
        navigatorWebView.getSettings().setDisplayZoomControls(false);
        navigatorWebView.getSettings().setJavaScriptEnabled(true);
        Undeprecator.webSettingsSetSaveFormData(navigatorWebView.getSettings(), false);
        Undeprecator.webSettingsSetSavePassword(navigatorWebView.getSettings(), false);

        currentTitle = getString(R.string.app_name);
        if (getIntent() != null && savedInstanceState == null) {
            String newUrlToLoad = getIntent().getStringExtra(EXTRA_URL_LOAD);

            if (!Utils.stringIsEmptyOrNull(newUrlToLoad)) {
                currentUrl = newUrlToLoad;
                navigatorWebView.loadUrl(currentUrl);
            }
        } else if (savedInstanceState != null) {
            currentTitle = savedInstanceState.getString(SAVE_TITLE_FOR_NAVIGATOR, getString(R.string.app_name));
            currentUrl = savedInstanceState.getString(SAVE_URL_FOR_NAVIGATOR, "");

            if (!currentUrl.isEmpty()) {
                navigatorWebView.loadUrl(currentUrl);
            }
        }

        updateTitleAndSubtitle();

        PrefsManager.putBool(PrefsManager.BoolPref.Names.WEBVIEW_CACHE_NEED_TO_BE_CLEAR, true);
        PrefsManager.applyChanges();
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
        outState.putString(SAVE_TITLE_FOR_NAVIGATOR, currentTitle);
        outState.putString(SAVE_URL_FOR_NAVIGATOR, currentUrl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_open_in_external_browser_webnavigator:
                Utils.openLinkInExternalNavigator(currentUrl, this);
                return true;
            case R.id.action_copy_url_webnavigator:
                Utils.putStringInClipboard(currentUrl, this);
                Toast.makeText(this, R.string.copyDone, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_reload_page_webnavigator:
                navigatorWebView.reload();
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
