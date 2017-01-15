package com.franckrj.respawnirc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.franckrj.respawnirc.dialogs.SelectStickerDialogFragment;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

public class SendTopicActivity extends AppCompatActivity implements SelectStickerDialogFragment.StickerSelected {
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_FORUM_NAME";
    public static final String EXTRA_FORUM_LINK = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_FORUM_LINK";
    public static final String EXTRA_INPUT_LIST = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_INPUT_LIST";

    private SharedPreferences sharedPref = null;
    private SendTopicToJVC currentAsyncTaskForSendTopic = null;
    private String linkToSend = "";
    private String listOfInputsInAstring = "";
    private boolean lastSendIsAResend = false;
    private EditText topicTitleEdit = null;
    private EditText topicContentEdit = null;
    private String lastTopicTitleSended = "";
    private String lastTopicContentSended = "";

    private void sendNewTopic(boolean isAResend) {
        if (currentAsyncTaskForSendTopic == null && (!isAResend || !lastSendIsAResend)) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            lastTopicTitleSended = topicTitleEdit.getText().toString();
            lastTopicContentSended = topicContentEdit.getText().toString();

            lastSendIsAResend = isAResend;
            currentAsyncTaskForSendTopic = new SendTopicToJVC();
            currentAsyncTaskForSendTopic.execute(linkToSend, Utils.convertStringToUrlString(lastTopicTitleSended), Utils.convertStringToUrlString(lastTopicContentSended), listOfInputsInAstring, sharedPref.getString(getString(R.string.prefCookiesList), ""));

            sharedPrefEdit.putString(getString(R.string.prefLastTopicTitleSended), lastTopicTitleSended);
            sharedPrefEdit.putString(getString(R.string.prefLastTopicContentSended), lastTopicContentSended);
            sharedPrefEdit.apply();
        } else if (isAResend && lastSendIsAResend) {
            Toast.makeText(SendTopicActivity.this, R.string.unknownErrorPleaseResend, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAllCurrentTasks() {
        if (currentAsyncTaskForSendTopic != null) {
            currentAsyncTaskForSendTopic.cancel(true);
            currentAsyncTaskForSendTopic = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendtopic);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_sendtopic);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        topicTitleEdit = (EditText) findViewById(R.id.topic_title_edit_sendtopic);
        topicContentEdit = (EditText) findViewById(R.id.topic_content_edit_sendtopic);

        if (getIntent() != null) {
            if (getIntent().getStringExtra(EXTRA_FORUM_NAME) != null && myActionBar != null) {
                myActionBar.setSubtitle(getIntent().getStringExtra(EXTRA_FORUM_NAME));
            }
            if (getIntent().getStringExtra(EXTRA_FORUM_LINK) != null) {
                linkToSend = getIntent().getStringExtra(EXTRA_FORUM_LINK);
            }
            if (getIntent().getStringExtra(EXTRA_INPUT_LIST) != null) {
                listOfInputsInAstring = getIntent().getStringExtra(EXTRA_INPUT_LIST);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        lastTopicTitleSended = sharedPref.getString(getString(R.string.prefLastTopicTitleSended), "");
        lastTopicContentSended = sharedPref.getString(getString(R.string.prefLastTopicContentSended), "");
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAllCurrentTasks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_sendtopic, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_past_last_topic_sended_sendtopic).setEnabled(!lastTopicTitleSended.isEmpty() || !lastTopicContentSended.isEmpty());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_send_topic_sendtopic:
                sendNewTopic(false);
                return true;
            case R.id.action_past_last_topic_sended_sendtopic:
                topicTitleEdit.setText(lastTopicTitleSended);
                topicContentEdit.setText(lastTopicContentSended);
                return true;
            case R.id.action_select_sticker_sendtopic:
                SelectStickerDialogFragment selectStickerDialogFragment = new SelectStickerDialogFragment();
                selectStickerDialogFragment.show(getFragmentManager(), "SelectStickerDialogFragment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void getSelectedSticker(String newStickerToAdd) {
        topicContentEdit.append(newStickerToAdd);
    }

    private class SendTopicToJVC extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 4) {
                String pageContent;
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;
                pageContent = WebManager.sendRequest(params[0], "POST", "titre_topic=" + params[1] + "&message_topic=" + params[2] + params[3], params[4], currentWebInfos);

                if (params[0].equals(currentWebInfos.currentUrl)) {
                    pageContent = "respawnirc:resendneeded";
                }

                return pageContent;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String pageResult) {
            super.onPostExecute(pageResult);

            currentAsyncTaskForSendTopic = null;

            if (!Utils.stringIsEmptyOrNull(pageResult)) {
                if (pageResult.equals("respawnirc:resendneeded")) {
                    sendNewTopic(true);
                    return;
                } else {
                    Toast.makeText(SendTopicActivity.this, JVCParser.getErrorMessage(pageResult), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SendTopicActivity.this, R.string.topicIsSended, Toast.LENGTH_SHORT).show();
            }

            onBackPressed();
        }
    }
}
