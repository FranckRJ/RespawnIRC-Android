package com.franckrj.respawnirc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.WebManager;

public class ConnectAsModoActivity extends ThemedActivity {
    private ConnectAsModoTask currentTaskConnectAsModo = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private EditText modoPasswordText = null;
    private Button validateButton = null;
    private String latestListOfInputInAString = "";
    private String currentCookieList = "";

    private final View.OnClickListener validateButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentTaskConnectAsModo == null) {
                currentTaskConnectAsModo = new ConnectAsModoTask(modoPasswordText.getText().toString(), latestListOfInputInAString);
                currentTaskConnectAsModo.execute(currentCookieList);
            } else {
                Toast.makeText(ConnectAsModoActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
            }
        }
    };

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

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_modoconnect);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_modoconnect);
        modoPasswordText = (EditText) findViewById(R.id.password_text_modoconnect);
        validateButton = (Button) findViewById(R.id.validate_button_modoconnect);

        swipeRefresh.setEnabled(false);
        modoPasswordText.setVisibility(View.GONE);
        validateButton.setVisibility(View.GONE);
        validateButton.setOnClickListener(validateButtonClickedListener);

        currentCookieList = PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST);

        currentTaskConnectAsModo = new ConnectAsModoTask(null);
        currentTaskConnectAsModo.execute(currentCookieList);
    }

    @Override
    public void onPause() {
        stopAllCurrentTasks();
        super.onPause();
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
                    return WebManager.sendRequest("https://www.jeuxvideo.com/sso/auth.php", "POST", "password=" + passwordToUse + listOfInputInStringToUse, params[0], currentWebInfos);
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
                        modoPasswordText.setVisibility(View.VISIBLE);
                        validateButton.setVisibility(View.VISIBLE);
                        return;
                    } else if (JVCParser.getErrorMessageWhenModoConnect(pageContent).isEmpty()) {
                        PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, true);
                        PrefsManager.applyChanges();
                        Toast.makeText(ConnectAsModoActivity.this, R.string.youAreAlreadyConnectedAsModo, Toast.LENGTH_SHORT).show();
                        finish();
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
