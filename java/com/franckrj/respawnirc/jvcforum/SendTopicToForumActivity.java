package com.franckrj.respawnirc.jvcforum;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.AbsThemedActivity;
import com.franckrj.respawnirc.dialogs.InsertStuffDialogFragment;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class SendTopicToForumActivity extends AbsThemedActivity implements InsertStuffDialogFragment.StuffInserted {
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_FORUM_NAME";
    public static final String EXTRA_FORUM_LINK = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_FORUM_LINK";
    public static final String EXTRA_INPUT_LIST = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_INPUT_LIST";
    public static final String RESULT_EXTRA_TOPIC_LINK_TO_MOVE = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_TOPIC_LINK_TO_MOVE";

    private static final int MANAGE_SURVEY_REQUEST_CODE = 456;

    private SendTopicToJVC currentAsyncTaskForSendTopic = null;
    private SendTopicInfos currentInfos = new SendTopicInfos();
    private EditText topicTitleEdit = null;
    private EditText topicContentEdit = null;

    private final View.OnClickListener insertStuffButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            InsertStuffDialogFragment insertStuffDialogFragment = new InsertStuffDialogFragment();
            insertStuffDialogFragment.show(getFragmentManager(), "InsertStuffDialogFragment");
        }
    };

    private final View.OnClickListener manageSurveyButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent newManageSurveyIntent = new Intent(SendTopicToForumActivity.this, ManageSurveyOfTopicActivity.class);
            newManageSurveyIntent.putExtra(ManageSurveyOfTopicActivity.EXTRA_SURVEY_TITLE, currentInfos.surveyTitle);
            newManageSurveyIntent.putStringArrayListExtra(ManageSurveyOfTopicActivity.EXTRA_SURVEY_REPLYS_LIST, currentInfos.surveyReplysList);
            startActivityForResult(newManageSurveyIntent, MANAGE_SURVEY_REQUEST_CODE);
        }
    };

    private void sendNewTopic() {
        if (currentAsyncTaskForSendTopic == null) {
            currentInfos.lastTopicTitleSended = topicTitleEdit.getText().toString();
            currentInfos.lastTopicContentSended = topicContentEdit.getText().toString();

            currentAsyncTaskForSendTopic = new SendTopicToJVC();
            currentAsyncTaskForSendTopic.execute(currentInfos);

            PrefsManager.putString(PrefsManager.StringPref.Names.LAST_TOPIC_TITLE_SENDED, currentInfos.lastTopicTitleSended);
            PrefsManager.putString(PrefsManager.StringPref.Names.LAST_TOPIC_CONTENT_SENDED, currentInfos.lastTopicContentSended);
            PrefsManager.applyChanges();
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

        Toolbar myToolbar = findViewById(R.id.toolbar_sendtopic);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        Button insertStuffButton = findViewById(R.id.insertstuff_button_sendtopic);
        Button manageSurveyButton = findViewById(R.id.managesurvey_button_sendtopic);
        topicTitleEdit = findViewById(R.id.topic_title_edit_sendtopic);
        topicContentEdit = findViewById(R.id.topic_content_edit_sendtopic);

        insertStuffButton.setOnClickListener(insertStuffButtonClicked);
        manageSurveyButton.setOnClickListener(manageSurveyButtonClicked);

        if (getIntent() != null) {
            if (getIntent().getStringExtra(EXTRA_FORUM_NAME) != null && myActionBar != null) {
                myActionBar.setSubtitle(getIntent().getStringExtra(EXTRA_FORUM_NAME));
            }
            if (getIntent().getStringExtra(EXTRA_FORUM_LINK) != null) {
                currentInfos.linkToSend = getIntent().getStringExtra(EXTRA_FORUM_LINK);
            }
            if (getIntent().getStringExtra(EXTRA_INPUT_LIST) != null) {
                currentInfos.listOfInputsInAstring = getIntent().getStringExtra(EXTRA_INPUT_LIST);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        currentInfos.cookieList = PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST);
        currentInfos.lastTopicTitleSended = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_TOPIC_TITLE_SENDED);
        currentInfos.lastTopicContentSended = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_TOPIC_CONTENT_SENDED);
    }

    @Override
    public void onPause() {
        stopAllCurrentTasks();
        super.onPause();
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
        menu.findItem(R.id.action_past_last_topic_sended_sendtopic).setEnabled(!currentInfos.lastTopicTitleSended.isEmpty() || !currentInfos.lastTopicContentSended.isEmpty());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_send_topic_sendtopic:
                sendNewTopic();
                return true;
            case R.id.action_past_last_topic_sended_sendtopic:
                topicTitleEdit.setText(currentInfos.lastTopicTitleSended);
                topicContentEdit.setText(currentInfos.lastTopicContentSended);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MANAGE_SURVEY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String newSurveyTitle = data.getStringExtra(ManageSurveyOfTopicActivity.EXTRA_SURVEY_TITLE);
                ArrayList<String> newSurveyReplysList = data.getStringArrayListExtra(ManageSurveyOfTopicActivity.EXTRA_SURVEY_REPLYS_LIST);

                if (newSurveyTitle != null) {
                    currentInfos.surveyTitle = newSurveyTitle;
                }
                if (newSurveyReplysList != null) {
                    currentInfos.surveyReplysList = newSurveyReplysList;
                }
            }
        }
    }

    @Override
    public void getStringInserted(String newStringToAdd, int posOfCenterFromEnd) {
        Utils.insertStringInEditText(topicContentEdit, newStringToAdd, posOfCenterFromEnd);
    }

    private class SendTopicToJVC extends AsyncTask<SendTopicInfos, Void, String> {
        @Override
        protected String doInBackground(SendTopicInfos... infosOfSend) {
            if (infosOfSend.length == 1) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                String pageContent;
                String requestBuilded = "titre_topic=" + Utils.convertStringToUrlString(infosOfSend[0].lastTopicTitleSended) + "&message_topic=" + Utils.convertStringToUrlString(infosOfSend[0].lastTopicContentSended) + infosOfSend[0].listOfInputsInAstring;
                currentWebInfos.followRedirects = false;

                if (!Utils.stringIsEmptyOrNull(infosOfSend[0].surveyTitle)) {
                    requestBuilded += "&submit_sondage=1&question_sondage=" + Utils.convertStringToUrlString(infosOfSend[0].surveyTitle);

                    for (String thisReply : infosOfSend[0].surveyReplysList) {
                        if (!Utils.stringIsEmptyOrNull(thisReply)) {
                            requestBuilded += "&reponse_sondage[]=" + Utils.convertStringToUrlString(thisReply);
                        }
                    }
                } else {
                    requestBuilded += "&submit_sondage=0&question_sondage=&reponse_sondage[]=";
                }

                pageContent = WebManager.sendRequestWithMultipleTrys(infosOfSend[0].linkToSend, "POST", requestBuilded, infosOfSend[0].cookieList, currentWebInfos, 2);

                if (infosOfSend[0].linkToSend.equals(currentWebInfos.currentUrl)) {
                    pageContent = "respawnirc:resendneeded";
                }

                if (Utils.stringIsEmptyOrNull(pageContent)) {
                    pageContent = "respawnirc:move:" + currentWebInfos.currentUrl;
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

            if (Utils.stringIsEmptyOrNull(pageResult)) {
                pageResult = "error";
            }

            if (pageResult.equals("respawnirc:resendneeded")) {
                Toast.makeText(SendTopicToForumActivity.this, R.string.unknownErrorPleaseRetry, Toast.LENGTH_SHORT).show();
                return;
            } else if (pageResult.startsWith("respawnirc:move:")) {
                Intent data = new Intent();
                pageResult = pageResult.substring(("respawnirc:move:").length());

                if (pageResult.startsWith("/forums/")) {
                    pageResult = "http://www.jeuxvideo.com" + pageResult;
                } else if (!pageResult.startsWith("http:")) {
                    pageResult = "http:" + pageResult;
                }

                data.putExtra(RESULT_EXTRA_TOPIC_LINK_TO_MOVE, pageResult);
                setResult(Activity.RESULT_OK, data);
            } else {
                Toast.makeText(SendTopicToForumActivity.this, JVCParser.getErrorMessage(pageResult), Toast.LENGTH_SHORT).show();
            }

            finish();
        }
    }

    private static class SendTopicInfos {
        private String cookieList = "";
        private String linkToSend = "";
        private String listOfInputsInAstring = "";
        private String lastTopicTitleSended = "";
        private String lastTopicContentSended = "";
        private String surveyTitle = "";
        private ArrayList<String> surveyReplysList = new ArrayList<>();
    }
}
