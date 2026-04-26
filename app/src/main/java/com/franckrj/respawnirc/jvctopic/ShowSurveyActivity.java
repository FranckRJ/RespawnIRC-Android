package com.franckrj.respawnirc.jvctopic;

import static com.franckrj.respawnirc.utils.WebManager.errorStringId;

import android.os.Bundle;
import androidx.transition.TransitionManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;

import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.dialogs.VoteInSurveyDialogFragment;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import org.json.JSONObject;

import java.util.ArrayList;

public class ShowSurveyActivity extends AbsHomeIsBackActivity implements VoteInSurveyDialogFragment.VoteInSurveyRegistered {
    public static final String EXTRA_SURVEY_INFOS = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_SURVEY_INFOS";
    public static final String EXTRA_TOPIC_ID = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_TOPIC_ID";
    public static final String EXTRA_COOKIES = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_COOKIES";

    private static final String SAVE_VOTE_BUTTON_IS_VISIBLE = "saveVoteButtonIsVisible";
    private static final String SAVE_SHOWRESULT_BUTTON_IS_VISIBLE = "saveShowResultButtonIsVisible";
    private static final String SAVE_CONTENT_FOR_SURVEY = "saveContentForSurvey";
    private static final String SAVE_SCROLL_POSITION = "saveScrollPosition";
    private static final String SAVE_SURVEY_INFOS = "saveSurveyInfos";

    private ScrollView mainScrollView = null;
    private TextView contentText = null;
    private Button voteButton = null;
    private Button showResultButton = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private SendVoteToSurvey currentTaskForVote = null;
    private String contentForSurvey = "";
    private JVCParser.SurveyInfos surveyInfos = new JVCParser.SurveyInfos();

    private final View.OnClickListener voteButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentTaskForVote == null && !getSupportFragmentManager().isStateSaved()) {
                Bundle argForFrag = new Bundle();
                VoteInSurveyDialogFragment voteDialogFragment = new VoteInSurveyDialogFragment();
                String[] listOfReplys = new String[surveyInfos.listOfReplys.size()];

                for (int i = 0; i < surveyInfos.listOfReplys.size(); ++i) {
                    listOfReplys[i] = surveyInfos.listOfReplys.get(i).htmlTitle;
                }

                argForFrag.putStringArray(VoteInSurveyDialogFragment.ARG_SURVEY_REPLYS, listOfReplys);
                voteDialogFragment.setArguments(argForFrag);
                voteDialogFragment.show(getSupportFragmentManager(), "VoteInSurveyDialogFragment");
            }
        }
    };

    private final View.OnClickListener showResultButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showResultButton.setVisibility(View.GONE);
            fillSurveyContentFromInfos(true);
            TransitionManager.beginDelayedTransition(mainScrollView);
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsStarted requestIsStartedListener = new AbsWebRequestAsyncTask.RequestIsStarted() {
        @Override
        public void onRequestIsStarted() {
            swipeRefresh.setRefreshing(true);
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> sendVoteIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            swipeRefresh.setRefreshing(false);

            JVCParser.SurveyInfos newSurveyInfo = null;
            try {
                JSONObject jsonResult = new JSONObject(reqResult);
                newSurveyInfo = JVCParser.getSurveyInfosFromPage(jsonResult);
                if (newSurveyInfo.htmlTitle.isEmpty()) {
                    newSurveyInfo = null;
                }
            } catch (Exception ignored) {}

            if (newSurveyInfo == null) {
                Toast.makeText(ShowSurveyActivity.this, R.string.errorVoteInvalide, Toast.LENGTH_SHORT).show();
            } else {
                surveyInfos = newSurveyInfo;
                fillSurveyContentFromInfos(true);
                Toast.makeText(ShowSurveyActivity.this, R.string.voteRegistered, Toast.LENGTH_SHORT).show();
                voteButton.setVisibility(View.GONE);
                showResultButton.setVisibility(View.GONE);
                TransitionManager.beginDelayedTransition(mainScrollView);
            }

            currentTaskForVote = null;
        }
    };

    private void fillSurveyContentFromInfos(boolean showResult) {
        StringBuilder newContentToShow = new StringBuilder();

        if (surveyInfos.isOpen) {
            newContentToShow.append(getString(R.string.titleForSurvey));
        } else {
            newContentToShow.append(getString(R.string.titleForClosedSurvey));
        }
        newContentToShow.append("<br><big><b>").append(surveyInfos.htmlTitle).append("</b></big><br>");

        for (JVCParser.SurveyInfos.SurveyReply currentReply : surveyInfos.listOfReplys) {
            newContentToShow.append("<br>");
            if (showResult) {
                newContentToShow.append(addStyleToPercentage(currentReply.percentageOfVotes)).append(" : ");
            }
            else {
                newContentToShow.append("<font face=\"monospace\" color=\"#").append(ThemeManager.currentThemeUseDarkColors() ? "FFFFFF" : "000000").append("\">&gt;</font> ");
            }
            newContentToShow.append(currentReply.htmlTitle);
        }

        newContentToShow.append("<br><br>Total des votes : ").append(surveyInfos.numberOfVotes);

        contentForSurvey = newContentToShow.toString();
        contentText.setText(Undeprecator.htmlFromHtml(contentForSurvey));
    }

    private static String addStyleToPercentage(long percentageToStyle) {
        final int spaceToTake = ("100").length();
        String numberOfPercentage = String.valueOf(percentageToStyle);
        String colorValueOfPercentage;

        try {
            int colorValueInNumber = Utils.roundToInt(percentageToStyle * 2.5); //la couleur de l'int sera entre rouge 0 et rouge 250.

            if (ThemeManager.currentThemeUseDarkColors()) {
                colorValueInNumber = 250 - colorValueInNumber; //inversion pour les thèmes sombres
            }

            colorValueOfPercentage = Integer.toHexString(colorValueInNumber);

            if (colorValueOfPercentage.length() < 2) {
                StringBuilder colorValueOfPercentageBuilder = new StringBuilder(colorValueOfPercentage);

                while (colorValueOfPercentageBuilder.length() < 2) {
                    colorValueOfPercentageBuilder.insert(0, "0");
                }

                colorValueOfPercentage = colorValueOfPercentageBuilder.toString();
            }
        } catch (Exception e) {
            colorValueOfPercentage = "00";
        }

        if (numberOfPercentage.length() < spaceToTake) {
            StringBuilder numberOfPercentageBuilder = new StringBuilder(numberOfPercentage);

            while (numberOfPercentageBuilder.length() < spaceToTake) {
                numberOfPercentageBuilder.insert(0, " "); //alt+255 pour l'espace
            }

            numberOfPercentage = numberOfPercentageBuilder.toString();
        }

        if (ThemeManager.currentThemeUseDarkColors()) {
            return "<font face=\"monospace\" color=\"#FF" + colorValueOfPercentage + colorValueOfPercentage +"\">" + numberOfPercentage + "</font>%";
        } else {
            return "<font face=\"monospace\" color=\"#" + colorValueOfPercentage + "0000\">" + numberOfPercentage + "</font>%";
        }
    }

    private void stopAllCurrentTasks() {
        if (currentTaskForVote != null) {
            currentTaskForVote.clearListenersAndCancel();
            currentTaskForVote = null;
        }
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showsurvey);
        initToolbar(R.id.toolbar_showsurvey);

        ActionBar myActionBar = getSupportActionBar();

        mainScrollView = findViewById(R.id.scrollview_showsurvey);
        contentText = findViewById(R.id.content_showsurvey);
        voteButton = findViewById(R.id.button_vote_showsurvey);
        showResultButton = findViewById(R.id.button_showresult_showsurvey);
        swipeRefresh = findViewById(R.id.swiperefresh_showsurvey);

        voteButton.setVisibility(View.GONE);
        showResultButton.setVisibility(View.GONE);
        voteButton.setOnClickListener(voteButtonClickedListener);
        showResultButton.setOnClickListener(showResultButtonClickedListener);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorControlHighlightThemeLight);

        if (savedInstanceState != null) {
            voteButton.setVisibility(savedInstanceState.getBoolean(SAVE_VOTE_BUTTON_IS_VISIBLE, false) ? View.VISIBLE : View.GONE);
            showResultButton.setVisibility(savedInstanceState.getBoolean(SAVE_SHOWRESULT_BUTTON_IS_VISIBLE, false) ? View.VISIBLE : View.GONE);
            contentForSurvey = savedInstanceState.getString(SAVE_CONTENT_FOR_SURVEY, "");
            contentText.setText(Undeprecator.htmlFromHtml(contentForSurvey));
            surveyInfos = savedInstanceState.getParcelable(EXTRA_SURVEY_INFOS);

            mainScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mainScrollView.scrollTo(0, savedInstanceState.getInt(SAVE_SCROLL_POSITION, 0));
                }
            });
        }
        if ((surveyInfos == null || surveyInfos.htmlTitle.isEmpty()) && getIntent() != null) {
            surveyInfos = getIntent().getParcelableExtra(EXTRA_SURVEY_INFOS);
            if (surveyInfos == null) {
                surveyInfos = new JVCParser.SurveyInfos();
            }
        }
        if (myActionBar != null) {
            myActionBar.setSubtitle(Utils.applyEmojiCompatIfPossible(surveyInfos.htmlTitle));
        }

        if (surveyInfos.hasVoted) {
            voteButton.setVisibility(View.GONE);
            showResultButton.setVisibility(View.GONE);
            fillSurveyContentFromInfos(true);
        } else {
            voteButton.setVisibility(View.VISIBLE);
            showResultButton.setVisibility(View.VISIBLE);
            fillSurveyContentFromInfos(false);
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
        outState.putBoolean(SAVE_SHOWRESULT_BUTTON_IS_VISIBLE, showResultButton.getVisibility() == View.VISIBLE);
        outState.putString(SAVE_CONTENT_FOR_SURVEY, contentForSurvey);
        outState.putInt(SAVE_SCROLL_POSITION, mainScrollView.getScrollY());
        outState.putParcelable(SAVE_SURVEY_INFOS, surveyInfos);
    }

    @Override
    public void getRegisteredVote(int voteIndex) {
        if (voteIndex == -1 || voteIndex >= surveyInfos.listOfReplys.size()) {
            Toast.makeText(this, R.string.errorVoteInvalide, Toast.LENGTH_SHORT).show();
        } else if (currentTaskForVote != null) {
            Toast.makeText(this, R.string.errorVoteAlreadyRunning, Toast.LENGTH_SHORT).show();
        } else {
            currentTaskForVote = new SendVoteToSurvey();
            currentTaskForVote.setRequestIsStartedListener(requestIsStartedListener);
            currentTaskForVote.setRequestIsFinishedListener(sendVoteIsFinishedListener);
            currentTaskForVote.execute(surveyInfos.ajax, getIntent().getStringExtra(EXTRA_TOPIC_ID), String.valueOf(surveyInfos.id), String.valueOf(surveyInfos.listOfReplys.get(voteIndex).id), getIntent().getStringExtra(EXTRA_COOKIES));
        }
    }

    private static class SendVoteToSurvey extends AbsWebRequestAsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 4) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[4], false);
                return WebManager.sendRequest("https://www.jeuxvideo.com/forums/survey/vote", "POST", "ajax_hash=" + params[0] + "&id_topic=" + params[1] + "&id_sondage=" + params[2] + "&id_sondage_reponse=" + params[3], currentWebInfos);
            }
            return null;
        }
    }
}
