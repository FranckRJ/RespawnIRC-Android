package com.franckrj.respawnirc.jvcforum;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.dialogs.InsertStuffDialogFragment;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;
import java.util.Arrays;

public class SendTopicToForumActivity extends AbsHomeIsBackActivity implements InsertStuffDialogFragment.StuffInserted {
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_FORUM_NAME";
    public static final String EXTRA_FORUM_LINK = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_FORUM_LINK";
    public static final String EXTRA_INPUT_LIST = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_INPUT_LIST";
    public static final String RESULT_EXTRA_TOPIC_LINK_TO_MOVE = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_TOPIC_LINK_TO_MOVE";

    private static final int MANAGE_SURVEY_REQUEST_CODE = 456;
    private static final String SAVE_SURVEY_TITLE = "saveSurveyTitle";
    private static final String SAVE_SURVEY_REPLY_LIST = "saveSurveyReplyList";

    private SendTopicToJVC currentAsyncTaskForSendTopic = null;
    private SendTopicInfos currentInfos = new SendTopicInfos();
    private EditText topicTitleEdit = null;
    private EditText topicContentEdit = null;
    private Button manageSurveyButton = null;
    private String lastTopicTitleSended = "";
    private String lastTopicContentSended = "";
    private String lastSurveyTitleSended = "";
    private String lastSurveyReplySendedInAString = "";
    private boolean saveTopicsAsDraft = true;

    private final View.OnClickListener insertStuffButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            InsertStuffDialogFragment insertStuffDialogFragment = new InsertStuffDialogFragment();
            insertStuffDialogFragment.show(getSupportFragmentManager(), "InsertStuffDialogFragment");
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

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> sendTopicIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            currentAsyncTaskForSendTopic = null;

            if (Utils.stringIsEmptyOrNull(reqResult)) {
                reqResult = "error";
            }

            if (reqResult.equals("respawnirc:resendneeded")) {
                Toast.makeText(SendTopicToForumActivity.this, R.string.unknownErrorPleaseRetry, Toast.LENGTH_SHORT).show();
                return;
            } else if (reqResult.startsWith("respawnirc:move:")) {
                Intent data = new Intent();
                reqResult = reqResult.substring(("respawnirc:move:").length());

                if (reqResult.startsWith("/forums/")) {
                    reqResult = "http://www.jeuxvideo.com" + reqResult;
                } else if (!reqResult.startsWith("http:")) {
                    reqResult = "http:" + reqResult;
                }

                data.putExtra(RESULT_EXTRA_TOPIC_LINK_TO_MOVE, reqResult);
                setResult(Activity.RESULT_OK, data);
            } else {
                Toast.makeText(SendTopicToForumActivity.this, JVCParser.getErrorMessage(reqResult), Toast.LENGTH_SHORT).show();
            }

            clearWholeTopicIncludingSurvey();
            finish();
        }
    };

    private final DialogInterface.OnClickListener onClickInClearWholeTopicConfirmationListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                clearWholeTopicIncludingSurvey();
            }
        }
    };

    private void sendNewTopic() {
        Utils.hideSoftKeyboard(this);

        if (currentAsyncTaskForSendTopic == null) {
            lastSurveyTitleSended = currentInfos.surveyTitle;
            lastSurveyReplySendedInAString = surveyReplyListToString(currentInfos.surveyReplysList);
            lastTopicTitleSended = topicTitleEdit.getText().toString();
            lastTopicContentSended = topicContentEdit.getText().toString();

            currentInfos.topicTitle = lastTopicTitleSended;
            currentInfos.topicContent = lastTopicContentSended;

            currentAsyncTaskForSendTopic = new SendTopicToJVC();
            currentAsyncTaskForSendTopic.setRequestIsFinishedListener(sendTopicIsFinishedListener);
            currentAsyncTaskForSendTopic.execute(new SendTopicInfos(currentInfos));

            PrefsManager.putString(PrefsManager.StringPref.Names.LAST_TOPIC_TITLE_SENDED, lastTopicTitleSended);
            PrefsManager.putString(PrefsManager.StringPref.Names.LAST_TOPIC_CONTENT_SENDED, lastTopicContentSended);
            PrefsManager.putString(PrefsManager.StringPref.Names.LAST_SURVEY_TITLE_SENDED, lastSurveyTitleSended);
            PrefsManager.putString(PrefsManager.StringPref.Names.LAST_SURVEY_REPLY_SENDED_IN_A_STRING, lastSurveyReplySendedInAString);
            PrefsManager.applyChanges();
        }
    }

    private void updateManageSurveyButtonText() {
        if (currentInfos.surveyTitle.isEmpty()) {
            manageSurveyButton.setText(R.string.createSurvey);
        } else {
            manageSurveyButton.setText(R.string.manageSurvey);
        }
    }

    private void clearWholeTopicIncludingSurvey() {
        topicTitleEdit.setText("");
        topicContentEdit.setText("");
        currentInfos.surveyTitle = "";
        currentInfos.surveyReplysList.clear();
        updateManageSurveyButtonText();
    }

    private void stopAllCurrentTasks() {
        if (currentAsyncTaskForSendTopic != null) {
            currentAsyncTaskForSendTopic.clearListenersAndCancel();
            currentAsyncTaskForSendTopic = null;
        }
    }

    private void initializeSettings() {
        currentInfos.cookieList = PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST);
        lastTopicTitleSended = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_TOPIC_TITLE_SENDED);
        lastTopicContentSended = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_TOPIC_CONTENT_SENDED);
        lastSurveyTitleSended = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_SURVEY_TITLE_SENDED);
        lastSurveyReplySendedInAString = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_SURVEY_REPLY_SENDED_IN_A_STRING);
        saveTopicsAsDraft = PrefsManager.getBool(PrefsManager.BoolPref.Names.AUTO_SAVE_MESSAGES_AND_TOPICS_AS_DRAFT);
    }

    private static String surveyReplyListToString(ArrayList<String> thisSurveyReplyList) {
        return TextUtils.join("&", Utils.mapStringArrayList(thisSurveyReplyList,
                                                                     new Utils.StringModifier() {
                                                                         @Override
                                                                         public String changeString(String baseString) {
                                                                             return Utils.encodeStringToUrlString(baseString);
                                                                         }
                                                                     }));
    }

    private static ArrayList<String> surveyReplyStringToList(String thisSurveyReplyString) {
        ArrayList<String> tmpList = Utils.mapStringArrayList(Arrays.asList(thisSurveyReplyString.split("&")),
                                                             new Utils.StringModifier() {
                                                                 @Override
                                                                 public String changeString(String baseString) {
                                                                     return Utils.decodeUrlStringToString(baseString);
                                                                 }
                                                             });
        tmpList.removeAll(Arrays.asList("", null));
        return tmpList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendtopic);
        initToolbar(R.id.toolbar_sendtopic);

        ActionBar myActionBar = getSupportActionBar();

        Button insertStuffButton = findViewById(R.id.insertstuff_button_sendtopic);
        manageSurveyButton = findViewById(R.id.managesurvey_button_sendtopic);
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

        initializeSettings();
        if (savedInstanceState != null) {
            ArrayList<String> tmpListOfReply = savedInstanceState.getStringArrayList(SAVE_SURVEY_REPLY_LIST);
            currentInfos.surveyTitle = savedInstanceState.getString(SAVE_SURVEY_TITLE, "");

            if (tmpListOfReply != null) {
                currentInfos.surveyReplysList = tmpListOfReply;
            }
        } else {
            if (saveTopicsAsDraft) {
                topicTitleEdit.setText(PrefsManager.getString(PrefsManager.StringPref.Names.TOPIC_TITLE_DRAFT));
                topicContentEdit.setText(PrefsManager.getString(PrefsManager.StringPref.Names.TOPIC_CONTENT_DRAFT));
                currentInfos.surveyTitle = PrefsManager.getString(PrefsManager.StringPref.Names.SURVEY_TITLE_DRAFT);
                currentInfos.surveyReplysList = surveyReplyStringToList(PrefsManager.getString(PrefsManager.StringPref.Names.SURVEY_REPLY_IN_A_STRING_DRAFT));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateManageSurveyButtonText();
    }

    @Override
    public void onPause() {
        stopAllCurrentTasks();

        PrefsManager.putString(PrefsManager.StringPref.Names.TOPIC_TITLE_DRAFT, topicTitleEdit.getText().toString());
        PrefsManager.putString(PrefsManager.StringPref.Names.TOPIC_CONTENT_DRAFT, topicContentEdit.getText().toString());
        PrefsManager.putString(PrefsManager.StringPref.Names.SURVEY_TITLE_DRAFT, currentInfos.surveyTitle);
        PrefsManager.putString(PrefsManager.StringPref.Names.SURVEY_REPLY_IN_A_STRING_DRAFT, surveyReplyListToString(currentInfos.surveyReplysList));
        PrefsManager.applyChanges();

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_SURVEY_TITLE, currentInfos.surveyTitle);
        outState.putStringArrayList(SAVE_SURVEY_REPLY_LIST, currentInfos.surveyReplysList);
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
        menu.findItem(R.id.action_past_last_topic_sended_sendtopic).setEnabled(!lastTopicTitleSended.isEmpty() || !lastTopicContentSended.isEmpty() ||
                                                                               !lastSurveyTitleSended.isEmpty() || !lastSurveyReplySendedInAString.isEmpty());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send_topic_sendtopic:
                sendNewTopic();
                return true;
            case R.id.action_past_last_topic_sended_sendtopic:
                topicTitleEdit.setText(lastTopicTitleSended);
                topicContentEdit.setText(lastTopicContentSended);
                currentInfos.surveyTitle = lastSurveyTitleSended;
                currentInfos.surveyReplysList = surveyReplyStringToList(lastSurveyReplySendedInAString);
                updateManageSurveyButtonText();
                return true;
            case R.id.action_clear_whole_topic_and_survey_sendtopic:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.deleteTopic).setMessage(R.string.deleteWholeTopicWarning)
                        .setPositiveButton(R.string.yes, onClickInClearWholeTopicConfirmationListener).setNegativeButton(R.string.no, null);
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (saveTopicsAsDraft && (!topicTitleEdit.getText().toString().isEmpty() || !topicContentEdit.getText().toString().isEmpty() ||
                                  !currentInfos.surveyTitle.isEmpty() || !currentInfos.surveyReplysList.isEmpty())) {
            Toast.makeText(this, getString(R.string.topicDraftSaved), Toast.LENGTH_SHORT).show();
        }
        super.onBackPressed();
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

    private static class SendTopicToJVC extends AbsWebRequestAsyncTask<SendTopicInfos, Void, String> {
        @Override
        protected String doInBackground(SendTopicInfos... infosOfSend) {
            if (infosOfSend.length == 1) {
                WebManager.WebInfos currentWebInfos = initWebInfos(infosOfSend[0].cookieList, false);
                String pageContent;
                StringBuilder requestBuilder = new StringBuilder("titre_topic=").append(Utils.encodeStringToUrlString(infosOfSend[0].topicTitle))
                                                                                .append("&message_topic=").append(Utils.encodeStringToUrlString(infosOfSend[0].topicContent))
                                                                                .append(infosOfSend[0].listOfInputsInAstring);

                if (!Utils.stringIsEmptyOrNull(infosOfSend[0].surveyTitle)) {
                    requestBuilder.append("&submit_sondage=1&question_sondage=").append(Utils.encodeStringToUrlString(infosOfSend[0].surveyTitle));

                    for (String thisReply : infosOfSend[0].surveyReplysList) {
                        if (!Utils.stringIsEmptyOrNull(thisReply)) {
                            requestBuilder.append("&reponse_sondage[]=").append(Utils.encodeStringToUrlString(thisReply));
                        }
                    }
                } else {
                    requestBuilder.append("&submit_sondage=0&question_sondage=&reponse_sondage[]=");
                }

                pageContent = WebManager.sendRequestWithMultipleTrys(infosOfSend[0].linkToSend, "POST", requestBuilder.toString(), currentWebInfos, 2);

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
    }

    private static class SendTopicInfos {
        private String cookieList = "";
        private String linkToSend = "";
        private String listOfInputsInAstring = "";
        private String topicTitle = "";
        private String topicContent = "";
        private String surveyTitle = "";
        private ArrayList<String> surveyReplysList = new ArrayList<>();

        public SendTopicInfos() {}

        public SendTopicInfos(SendTopicInfos baseForCopy) {
            cookieList = baseForCopy.cookieList;
            linkToSend = baseForCopy.linkToSend;
            listOfInputsInAstring = baseForCopy.listOfInputsInAstring;
            topicTitle = baseForCopy.topicTitle;
            topicContent = baseForCopy.topicContent;
            surveyTitle = baseForCopy.surveyTitle;
            surveyReplysList = new ArrayList<>(baseForCopy.surveyReplysList);
        }
    }
}
