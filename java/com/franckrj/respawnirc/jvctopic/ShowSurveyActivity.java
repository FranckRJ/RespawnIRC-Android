package com.franckrj.respawnirc.jvctopic;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.ThemedActivity;
import com.franckrj.respawnirc.dialogs.VoteInSurveyDialogFragment;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class ShowSurveyActivity extends ThemedActivity implements VoteInSurveyDialogFragment.VoteInSurveyRegistered {
    public static final String EXTRA_SURVEY_TITLE = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_SURVEY_TITLE";
    public static final String EXTRA_SURVEY_REPLYS_WITH_INFOS = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_SURVEY_REPLYS_WITH_INFOS";
    public static final String EXTRA_TOPIC_ID = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_TOPIC_ID";
    public static final String EXTRA_AJAX_INFOS = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_AJAX_INFOS";
    public static final String EXTRA_COOKIES = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_COOKIES";

    private static final String SAVE_VOTE_BUTTON_IS_VISIBLE = "saveVoteButtonIsVisible";
    private static final String SAVE_CONTENT_FOR_SURVEY = "saveContentForSurvey";

    private TextView contentText = null;
    private Button voteButton = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private DownloadInfosForSurvey currentTaskForSurvey = null;
    private SendVoteToSurvey currentTaskForVote = null;
    private String contentForSurvey = "";
    private ArrayList<JVCParser.SurveyReplyInfos> listOfReplysWithInfos;

    private View.OnClickListener voteButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentTaskForVote == null) {
                Bundle argForFrag = new Bundle();
                VoteInSurveyDialogFragment voteDialogFragment = new VoteInSurveyDialogFragment();
                String[] listOfReplys = new String[listOfReplysWithInfos.size()];

                for (int i = 0; i < listOfReplysWithInfos.size(); ++i) {
                    listOfReplys[i] = listOfReplysWithInfos.get(i).titleOfReply;
                }

                argForFrag.putStringArray(VoteInSurveyDialogFragment.ARG_SURVEY_REPLYS, listOfReplys);
                voteDialogFragment.setArguments(argForFrag);
                voteDialogFragment.show(getFragmentManager(), "VoteInSurveyDialogFragment");
            }
        }
    };

    private boolean analyzeSurveyContent(String pageContent) {
        if (!Utils.stringIsEmptyOrNull(pageContent)) {
            String errorContent = JVCParser.getErrorMessageInJSONMode(pageContent);

            if (errorContent == null) {
                JVCParser.SurveyInfos infosForSurvey = JVCParser.getSurveyInfosFromSurveyBlock(JVCParser.getRealSurveyContent(pageContent));
                StringBuilder newContentToShow = new StringBuilder();

                if (infosForSurvey.isOpen) {
                    newContentToShow.append(getString(R.string.titleForSurvey));
                } else {
                    newContentToShow.append(getString(R.string.titleForClosedSurvey));
                }
                newContentToShow.append("<br><big><b>").append(infosForSurvey.htmlTitle).append("</b></big><br>");

                for (JVCParser.SurveyInfos.SurveyReply currentReply : infosForSurvey.listOfReplys) {
                    newContentToShow.append("<br>").append(addStyleToPercentage(currentReply.percentageOfVotes)).append(" : ").append(currentReply.htmlTitle);
                }

                newContentToShow.append("<br><br>").append(infosForSurvey.numberOfVotes);

                contentForSurvey = newContentToShow.toString();
                contentText.setText(Undeprecator.htmlFromHtml(contentForSurvey));
                return true;
            } else {
                Toast.makeText(ShowSurveyActivity.this, errorContent, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(ShowSurveyActivity.this, R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private String addStyleToPercentage(String percentageToStyle) {
        final int spaceToTake = ("100").length();
        String numberOfPercentage = percentageToStyle.substring(0, percentageToStyle.indexOf(" "));
        String colorValueOfPercentage;

        try {
            int colorValueInNumber = (int)(Integer.parseInt(numberOfPercentage) * 2.5); //la couleur de l'int sera entre rouge 0 et rouge 250.

            if (ThemeManager.getThemeUsedIsDark()) {
                colorValueInNumber = 250 - colorValueInNumber; //inversion pour les thèmes sombres
            }

            colorValueOfPercentage = Integer.toHexString(colorValueInNumber);

            while (colorValueOfPercentage.length() < 2) {
                colorValueOfPercentage = "0" + colorValueOfPercentage;
            }
        } catch (Exception e) {
            colorValueOfPercentage = "00";
        }

        while (numberOfPercentage.length() < spaceToTake) {
            numberOfPercentage = " " + numberOfPercentage; //alt+255 pour l'espace
        }

        if (ThemeManager.getThemeUsedIsDark()) {
            return "<font face=\"monospace\" color=\"#FF" + colorValueOfPercentage + colorValueOfPercentage +"\">" + numberOfPercentage + "</font>%";
        } else {
            return "<font face=\"monospace\" color=\"#" + colorValueOfPercentage + "0000\">" + numberOfPercentage + "</font>%";
        }
    }

    private void stopAllCurrentTasks() {
        if (currentTaskForSurvey != null) {
            currentTaskForSurvey.cancel(true);
            currentTaskForSurvey = null;
        }
        if (currentTaskForVote != null) {
            currentTaskForVote.cancel(true);
            currentTaskForVote = null;
        }
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showsurvey);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_showsurvey);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        contentText = (TextView) findViewById(R.id.content_showsurvey);
        voteButton = (Button) findViewById(R.id.button_vote_showsurvey);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_showsurvey);

        voteButton.setVisibility(View.GONE);
        voteButton.setOnClickListener(voteButtonClickedListener);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorAccentThemeLight);

        if (savedInstanceState != null) {
            voteButton.setVisibility(savedInstanceState.getBoolean(SAVE_VOTE_BUTTON_IS_VISIBLE, false) ? View.VISIBLE : View.GONE);
            contentForSurvey = savedInstanceState.getString(SAVE_CONTENT_FOR_SURVEY, "");
            contentText.setText(Undeprecator.htmlFromHtml(contentForSurvey));
        }
        if (getIntent() != null) {
            if (getIntent().getStringExtra(EXTRA_SURVEY_TITLE) != null) {
                if (myActionBar != null) {
                    myActionBar.setSubtitle(getIntent().getStringExtra(EXTRA_SURVEY_TITLE));
                }
            }
            if (getIntent().getParcelableArrayListExtra(EXTRA_SURVEY_REPLYS_WITH_INFOS) != null) {
                listOfReplysWithInfos = getIntent().getParcelableArrayListExtra(EXTRA_SURVEY_REPLYS_WITH_INFOS);
            } else {
                listOfReplysWithInfos = new ArrayList<>();
            }
            if (contentForSurvey.isEmpty() && getIntent().getStringExtra(EXTRA_TOPIC_ID) != null && getIntent().getStringExtra(EXTRA_AJAX_INFOS) != null && getIntent().getStringExtra(EXTRA_COOKIES) != null) {
                currentTaskForSurvey = new DownloadInfosForSurvey();
                currentTaskForSurvey.execute(getIntent().getStringExtra(EXTRA_TOPIC_ID), getIntent().getStringExtra(EXTRA_AJAX_INFOS), getIntent().getStringExtra(EXTRA_COOKIES));
            }
        }

        if (currentTaskForSurvey == null && contentForSurvey.isEmpty()) {
            Toast.makeText(this, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        stopAllCurrentTasks();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_VOTE_BUTTON_IS_VISIBLE, voteButton.getVisibility() == View.VISIBLE);
        outState.putString(SAVE_CONTENT_FOR_SURVEY, contentForSurvey);
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
    public void getRegisteredVote(int voteIndex) {
        if (voteIndex == -1 || voteIndex >= listOfReplysWithInfos.size()) {
            Toast.makeText(this, R.string.errorVoteInvalide, Toast.LENGTH_SHORT).show();
        } else if (currentTaskForVote != null) {
            Toast.makeText(this, R.string.errorVoteAlreadyRunning, Toast.LENGTH_SHORT).show();
        } else {
            currentTaskForVote = new SendVoteToSurvey();
            currentTaskForVote.execute(getIntent().getStringExtra(EXTRA_TOPIC_ID), listOfReplysWithInfos.get(voteIndex).infosForReply, getIntent().getStringExtra(EXTRA_AJAX_INFOS), getIntent().getStringExtra(EXTRA_COOKIES));
        }
    }

    private class DownloadInfosForSurvey extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            swipeRefresh.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/ajax_topic_sondage_view_response.php", "GET", "id_topic=" + params[0] + "&action=view_vote&" + params[1], params[2], currentWebInfos);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String surveyBlock) {
            super.onPostExecute(surveyBlock);
            swipeRefresh.setRefreshing(false);

            if (listOfReplysWithInfos.isEmpty()) {
                voteButton.setVisibility(View.GONE);
            } else {
                voteButton.setVisibility(View.VISIBLE);
            }

            analyzeSurveyContent(surveyBlock);

            currentTaskForSurvey = null;
        }
    }

    private class SendVoteToSurvey extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            swipeRefresh.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length > 3) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/ajax_topic_sondage_vote.php", "GET", "id_topic=" + params[0] + "&" + params[1] + params[2], params[3], currentWebInfos);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String surveyBlock) {
            super.onPostExecute(surveyBlock);
            swipeRefresh.setRefreshing(false);

            if (analyzeSurveyContent(surveyBlock)) {
                Toast.makeText(ShowSurveyActivity.this, R.string.voteRegistered, Toast.LENGTH_SHORT).show();
                voteButton.setVisibility(View.GONE);
            }

            currentTaskForVote = null;
        }
    }
}
