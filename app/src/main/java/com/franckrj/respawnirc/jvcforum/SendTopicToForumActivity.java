package com.franckrj.respawnirc.jvcforum;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.franckrj.respawnirc.DraftUtils;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.dialogs.InsertStuffDialogFragment;
import com.franckrj.respawnirc.utils.AccountManager;
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
    public static final String EXTRA_USER_CAN_POST_AS_MODO = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_USER_CAN_POST_AS_MODO";
    public static final String RESULT_EXTRA_TOPIC_LINK_TO_MOVE = "com.franckrj.respawnirc.sendtopicactivity.EXTRA_TOPIC_LINK_TO_MOVE";

    private static final int MANAGE_SURVEY_REQUEST_CODE = 456;
    private static final String SAVE_SURVEY_TITLE = "saveSurveyTitle";
    private static final String SAVE_SURVEY_REPLY_LIST = "saveSurveyReplyList";

    private SendTopicToJVC currentAsyncTaskForSendTopic = null;
    private SendTopicInfos currentInfos = new SendTopicInfos();
    private EditText topicTitleEdit = null;
    private EditText topicContentEdit = null;
    private Button manageSurveyButton = null;
    private TextView postTypeText = null;
    private String lastTopicTitleSended = "";
    private String lastTopicContentSended = "";
    private String lastSurveyTitleSended = "";
    private String lastSurveyReplySendedInAString = "";
    private DraftUtils utilsForDraft = new DraftUtils(PrefsManager.SaveDraftType.ALWAYS, PrefsManager.BoolPref.Names.USE_LAST_TOPIC_DRAFT_SAVED);
    private boolean userCanPostAsModo = false;

    private final View.OnClickListener insertStuffButtonClicked = view -> {
        if (!getSupportFragmentManager().isStateSaved()) {
            InsertStuffDialogFragment insertStuffDialogFragment = new InsertStuffDialogFragment();
            insertStuffDialogFragment.show(getSupportFragmentManager(), "InsertStuffDialogFragment");
        }
    };

    private final View.OnClickListener manageSurveyButtonClicked = view -> {
        Intent newManageSurveyIntent = new Intent(SendTopicToForumActivity.this, ManageSurveyOfTopicActivity.class);
        newManageSurveyIntent.putExtra(ManageSurveyOfTopicActivity.EXTRA_SURVEY_TITLE, currentInfos.surveyTitle);
        newManageSurveyIntent.putStringArrayListExtra(ManageSurveyOfTopicActivity.EXTRA_SURVEY_REPLYS_LIST, currentInfos.surveyReplysList);
        startActivityForResult(newManageSurveyIntent, MANAGE_SURVEY_REQUEST_CODE);
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> sendTopicIsFinishedListener = reqResult -> {
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
    };

    private final DialogInterface.OnClickListener onClickInClearWholeTopicConfirmationListener = (dialog, which) -> {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            clearWholeTopicIncludingSurvey();
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
            currentInfos.postAsModo = userCanPostAsModo && PrefsManager.getBool(PrefsManager.BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE);

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

    private void updatePostTypeTextAndVisibility() {
        if (AccountManager.getCurrentAccount().isModo || userCanPostAsModo) {
            postTypeText.setVisibility(View.VISIBLE);

            if (userCanPostAsModo && PrefsManager.getBool(PrefsManager.BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE)) {
                postTypeText.setText(R.string.topicPostTypeIsModo);
            } else {
                postTypeText.setText(R.string.topicPostTypeIsUser);
            }
        } else {
            postTypeText.setVisibility(View.GONE);
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
        currentInfos.cookieList = AccountManager.getCurrentAccount().cookie;
        lastTopicTitleSended = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_TOPIC_TITLE_SENDED);
        lastTopicContentSended = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_TOPIC_CONTENT_SENDED);
        lastSurveyTitleSended = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_SURVEY_TITLE_SENDED);
        lastSurveyReplySendedInAString = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_SURVEY_REPLY_SENDED_IN_A_STRING);
        utilsForDraft.loadPrefsInfos();
    }

    private static String surveyReplyListToString(ArrayList<String> thisSurveyReplyList) {
        return TextUtils.join("&", Utils.mapStringArrayList(thisSurveyReplyList,
                Utils::encodeStringToUrlString));
    }

    private static ArrayList<String> surveyReplyStringToList(String thisSurveyReplyString) {
        ArrayList<String> tmpList = Utils.mapStringArrayList(Arrays.asList(thisSurveyReplyString.split("&")),
                Utils::decodeUrlStringToString);
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
        postTypeText = findViewById(R.id.text_posttype_sendtpopic);

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
            userCanPostAsModo = getIntent().getBooleanExtra(EXTRA_USER_CAN_POST_AS_MODO, false);
        }

        initializeSettings();
        if (savedInstanceState != null) {
            ArrayList<String> tmpListOfReply = savedInstanceState.getStringArrayList(SAVE_SURVEY_REPLY_LIST);
            currentInfos.surveyTitle = savedInstanceState.getString(SAVE_SURVEY_TITLE, "");

            if (tmpListOfReply != null) {
                currentInfos.surveyReplysList = tmpListOfReply;
            }
        } else {
            if (utilsForDraft.lastDraftSavedHasToBeUsed()) {
                topicTitleEdit.setText(PrefsManager.getString(PrefsManager.StringPref.Names.TOPIC_TITLE_DRAFT));
                topicContentEdit.setText(PrefsManager.getString(PrefsManager.StringPref.Names.TOPIC_CONTENT_DRAFT));
                currentInfos.surveyTitle = PrefsManager.getString(PrefsManager.StringPref.Names.SURVEY_TITLE_DRAFT);
                currentInfos.surveyReplysList = surveyReplyStringToList(PrefsManager.getString(PrefsManager.StringPref.Names.SURVEY_REPLY_IN_A_STRING_DRAFT));
            }
        }

        updatePostTypeTextAndVisibility();
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
        utilsForDraft.afterDraftIsSaved();

        PrefsManager.applyChanges();

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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

        MenuItem postAsModoItem = menu.findItem(R.id.enable_postasmodo_sendtopic);
        postAsModoItem.setChecked(PrefsManager.getBool(PrefsManager.BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE));
        postAsModoItem.setEnabled(userCanPostAsModo);

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
            case R.id.enable_postasmodo_sendtopic:
                /* La valeur de isChecked est inversée car le changement d'état ne se fait pas automatiquement
                 * donc c'est la valeur avant d'avoir cliqué qui est retournée. */
                PrefsManager.putBool(PrefsManager.BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE, !item.isChecked());
                PrefsManager.applyChanges();
                updatePostTypeTextAndVisibility();
                return true;
            case R.id.action_clear_whole_topic_and_survey_sendtopic:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.deleteTopic).setMessage(R.string.deleteWholeTopicWarning)
                        .setPositiveButton(R.string.yes, onClickInClearWholeTopicConfirmationListener).setNegativeButton(R.string.no, null);
                builder.show();
                return true;
            case R.id.action_past_last_topic_sended_sendtopic:
                topicTitleEdit.setText(lastTopicTitleSended);
                topicContentEdit.setText(lastTopicContentSended);
                currentInfos.surveyTitle = lastSurveyTitleSended;
                currentInfos.surveyReplysList = surveyReplyStringToList(lastSurveyReplySendedInAString);
                updateManageSurveyButtonText();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!topicTitleEdit.getText().toString().isEmpty() || !topicContentEdit.getText().toString().isEmpty() ||
                !currentInfos.surveyTitle.isEmpty() || !currentInfos.surveyReplysList.isEmpty()) {
            utilsForDraft.whenUserTryToLeaveWithDraft(R.string.topicDraftSaved, R.string.saveTopicDraftExplained, this);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void insertThisString(@NonNull String stringToInsert, int posOfCenterOfString) {
        Utils.insertStringInEditText(topicContentEdit, stringToInsert, posOfCenterOfString);
    }

    private static class SendTopicToJVC extends AbsWebRequestAsyncTask<SendTopicInfos, Void, String> {
        @Override
        protected String doInBackground(SendTopicInfos... infosOfSend) {
            if (infosOfSend.length == 1) {
                WebManager.WebInfos currentWebInfos = initWebInfos(infosOfSend[0].cookieList, false);
                String pageContent;
                StringBuilder requestBuilder = new StringBuilder("titre_topic=").append(Utils.encodeStringToUrlString(infosOfSend[0].topicTitle))
                                                                                .append("&message_topic=").append(Utils.encodeStringToUrlString(infosOfSend[0].topicContent))
                                                                                .append(infosOfSend[0].listOfInputsInAstring).append("&spotify_topic=");

                if (infosOfSend[0].postAsModo) {
                    requestBuilder.append("&form_alias_rang=2");
                } else {
                    requestBuilder.append("&form_alias_rang=1");
                }

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

                if (Utils.stringIsEmptyOrNull(pageContent) || pageContent.contains("<meta http-equiv=\"refresh\"")) {
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
        private boolean postAsModo = false;

        public SendTopicInfos() {}

        public SendTopicInfos(SendTopicInfos baseForCopy) {
            cookieList = baseForCopy.cookieList;
            linkToSend = baseForCopy.linkToSend;
            listOfInputsInAstring = baseForCopy.listOfInputsInAstring;
            topicTitle = baseForCopy.topicTitle;
            topicContent = baseForCopy.topicContent;
            surveyTitle = baseForCopy.surveyTitle;
            surveyReplysList = new ArrayList<>(baseForCopy.surveyReplysList);
            postAsModo = baseForCopy.postAsModo;
        }
    }
}
