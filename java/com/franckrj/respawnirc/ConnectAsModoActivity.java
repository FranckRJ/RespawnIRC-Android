package com.franckrj.respawnirc;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

public class ConnectAsModoActivity extends AbsThemedActivity {
    private static final String SAVE_LIST_OF_INPUT = "saveListOfInput";

    private ConnectAsModoTask currentTaskConnectAsModo = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private EditText modoPasswordText = null;
    private Button validateButton = null;
    private String latestListOfInputInAString = "";
    private String currentCookieList = "";

    private final View.OnClickListener validateButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            performConnect();
        }
    };

    private final TextView.OnEditorActionListener actionInPasswordEditTextListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performConnect();
                return true;
            }
            return false;
        }
    };

    private void performConnect() {
        Utils.hideSoftKeyboard(this);

        if (currentTaskConnectAsModo == null) {
            currentTaskConnectAsModo = new ConnectAsModoTask(modoPasswordText.getText().toString(), latestListOfInputInAString);
            currentTaskConnectAsModo.execute(currentCookieList);
        } else {
            Toast.makeText(ConnectAsModoActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAllCurrentTasks() {
        if (currentTaskConnectAsModo != null) {
            currentTaskConnectAsModo.cancel(true);
            currentTaskConnectAsModo = null;
        }
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modoconnect);

        Toolbar myToolbar = findViewById(R.id.toolbar_modoconnect);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        swipeRefresh = findViewById(R.id.swiperefresh_modoconnect);
        modoPasswordText = findViewById(R.id.password_text_modoconnect);
        validateButton = findViewById(R.id.validate_button_modoconnect);

        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorAccentThemeLight);
        modoPasswordText.setOnEditorActionListener(actionInPasswordEditTextListener);
        validateButton.setOnClickListener(validateButtonClickedListener);

        currentCookieList = PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST);

        if (savedInstanceState != null) {
            String tmpListOfInputInAString = savedInstanceState.getString(SAVE_LIST_OF_INPUT, null);

            if (!Utils.stringIsEmptyOrNull(tmpListOfInputInAString)) {
                latestListOfInputInAString = tmpListOfInputInAString;
                return;
            }
        }

        modoPasswordText.setVisibility(View.GONE);
        validateButton.setVisibility(View.GONE);
        currentTaskConnectAsModo = new ConnectAsModoTask(null);
        currentTaskConnectAsModo.execute(currentCookieList);
    }

    @Override
    public void onPause() {
        stopAllCurrentTasks();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_LIST_OF_INPUT, latestListOfInputInAString);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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

    private class ConnectAsModoTask extends AsyncTask<String, Void, String> {
        String passwordToUse = null;
        String listOfInputInStringToUse = "";

        public ConnectAsModoTask(String newPasswordToUse) {
            passwordToUse = newPasswordToUse;
        }

        public ConnectAsModoTask(String newPasswordToUse, String newListOfInputInStringToUse) {
            passwordToUse = newPasswordToUse;
            listOfInputInStringToUse = newListOfInputInStringToUse;
        }

        @Override
        protected void onPreExecute() {
            swipeRefresh.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length > 0) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;

                if (passwordToUse == null) {
                    return WebManager.sendRequest("https://www.jeuxvideo.com/sso/auth.php", "GET", "", params[0], currentWebInfos);
                } else {
                    return WebManager.sendRequest("https://www.jeuxvideo.com/sso/auth.php", "POST", "password=" +
                            Utils.convertStringToUrlString(passwordToUse) + listOfInputInStringToUse, params[0], currentWebInfos);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String pageContent) {
            super.onPostExecute(pageContent);
            swipeRefresh.setRefreshing(false);
            currentTaskConnectAsModo = null;

            if (pageContent != null) {
                if (passwordToUse == null) {
                    latestListOfInputInAString = JVCParser.getListOfInputInAStringInModoConnectFormForThisPage(pageContent);
                    if (!latestListOfInputInAString.isEmpty()) {
                        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                        modoPasswordText.setVisibility(View.VISIBLE);
                        validateButton.setVisibility(View.VISIBLE);
                        modoPasswordText.requestFocus();
                        inputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);

                        return;
                    } else if (JVCParser.getErrorMessageWhenModoConnect(pageContent).isEmpty()) {
                        PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, true);
                        PrefsManager.applyChanges();
                        Toast.makeText(ConnectAsModoActivity.this, R.string.youAreAlreadyConnectedAsModo, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                } else {
                    String errorWhenConnecting = JVCParser.getErrorMessageWhenModoConnect(pageContent);
                    latestListOfInputInAString = JVCParser.getListOfInputInAStringInModoConnectFormForThisPage(pageContent);

                    if (errorWhenConnecting.isEmpty()) {
                        PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, true);
                        PrefsManager.applyChanges();
                        Toast.makeText(ConnectAsModoActivity.this, R.string.connectionSucessful, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ConnectAsModoActivity.this, errorWhenConnecting, Toast.LENGTH_SHORT).show();
                    }

                    return;
                }
            }

            Toast.makeText(ConnectAsModoActivity.this, R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
        }
    }
}
