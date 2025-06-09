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

import java.util.ArrayList;

public class ShowSurveyActivity extends AbsHomeIsBackActivity implements VoteInSurveyDialogFragment.VoteInSurveyRegistered {
    public static final String EXTRA_SURVEY_TITLE = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_SURVEY_TITLE";
    public static final String EXTRA_SURVEY_REPLYS_WITH_INFOS = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_SURVEY_REPLYS_WITH_INFOS";
    public static final String EXTRA_TOPIC_ID = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_TOPIC_ID";
    public static final String EXTRA_AJAX_INFOS = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_AJAX_INFOS";
    public static final String EXTRA_COOKIES = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_COOKIES";

    private static final String SAVE_VOTE_BUTTON_IS_VISIBLE = "saveVoteButtonIsVisible";
    private static final String SAVE_SHOWRESULT_BUTTON_IS_VISIBLE = "saveShowResultButtonIsVisible";
    private static final String SAVE_MAIN_CARD_IS_VISIBLE = "saveMainCardIsVisible";
    private static final String SAVE_UNPARSED_SURVEY_CONTENT = "saveUnparsedSurveyContent";
    private static final String SAVE_CONTENT_FOR_SURVEY = "saveContentForSurvey";
    private static final String SAVE_SCROLL_POSITION = "saveScrollPosition";

    private ScrollView mainScrollView = null;
    private TextView backgroundErrorText = null;
    private View mainCardView = null;
    private TextView contentText = null;
    private Button voteButton = null;
    private Button showResultButton = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private DownloadInfosForSurvey currentTaskForSurvey = null;
    private SendVoteToSurvey currentTaskForVote = null;
    private String unparsedSurveyContent = "";
    private String contentForSurvey = "";
    private ArrayList<JVCParser.SurveyReplyInfos> listOfReplysWithInfos;

    private final View.OnClickListener voteButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentTaskForVote == null && !getSupportFragmentManager().isStateSaved()) {
                Bundle argForFrag = new Bundle();
                VoteInSurveyDialogFragment voteDialogFragment = new VoteInSurveyDialogFragment();
                String[] listOfReplys = new String[listOfReplysWithInfos.size()];

                for (int i = 0; i < listOfReplysWithInfos.size(); ++i) {
                    listOfReplys[i] = listOfReplysWithInfos.get(i).titleOfReply;
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
            fillSurveyContentAndShowError(unparsedSurveyContent, true);
            TransitionManager.beginDelayedTransition(mainScrollView);
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsStarted requestIsStartedListener = new AbsWebRequestAsyncTask.RequestIsStarted() {
        @Override
        public void onRequestIsStarted() {
            backgroundErrorText.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(true);
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> downloadInfosIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            boolean analyseSucceded;
            swipeRefresh.setRefreshing(false);
            unparsedSurveyContent = reqResult;

            if (listOfReplysWithInfos.isEmpty()) {
                voteButton.setVisibility(View.GONE);
                showResultButton.setVisibility(View.GONE);
                analyseSucceded = fillSurveyContentAndShowError(unparsedSurveyContent, true);
            } else {
                voteButton.setVisibility(View.VISIBLE);
                showResultButton.setVisibility(View.VISIBLE);
                analyseSucceded = fillSurveyContentAndShowError(unparsedSurveyContent, false);
            }

            if (analyseSucceded) {
                mainCardView.setVisibility(View.VISIBLE);
            } else {
                backgroundErrorText.setVisibility(View.VISIBLE);
            }

            currentTaskForSurvey = null;
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> sendVoteIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            swipeRefresh.setRefreshing(false);

            if (fillSurveyContentAndShowError(reqResult, true)) {
                unparsedSurveyContent = reqResult;
                Toast.makeText(ShowSurveyActivity.this, R.string.voteRegistered, Toast.LENGTH_SHORT).show();
                voteButton.setVisibility(View.GONE);
                showResultButton.setVisibility(View.GONE);
                TransitionManager.beginDelayedTransition(mainScrollView);
            }

            currentTaskForVote = null;
        }
    };

    private boolean fillSurveyContentAndShowError(String pageContent, boolean showResult) {
        if (!Utils.stringIsEmptyOrNull(pageContent)) {
            String errorContent = JVCParser.getErrorMessageInJsonMode(pageContent);

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
                    newContentToShow.append("<br>");
                    if (showResult) {
                        newContentToShow.append(addStyleToPercentage(currentReply.percentageOfVotes)).append(" : ");
                    }
                    else {
                        newContentToShow.append("<font face=\"monospace\" color=\"#").append(ThemeManager.currentThemeUseDarkColors() ? "FFFFFF" : "000000").append("\">&gt;</font> ");
                    }
                    newContentToShow.append(currentReply.htmlTitle);
                }

                newContentToShow.append("<br><br>").append(infosForSurvey.numberOfVotes);

                contentForSurvey = newContentToShow.toString();
                contentText.setText(Undeprecator.htmlFromHtml(contentForSurvey));
                return true;
            } else {
                if (mainCardView.getVisibility() == View.GONE) {
                    backgroundErrorText.setText(errorContent);
                } else {
                    Toast.makeText(ShowSurveyActivity.this, errorContent, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        } else {
            if (mainCardView.getVisibility() == View.GONE) {
                backgroundErrorText.setText(errorStringId);
            } else {
                Toast.makeText(ShowSurveyActivity.this, R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

    private static String addStyleToPercentage(String percentageToStyle) {
        final int spaceToTake = ("100").length();
        String numberOfPercentage = percentageToStyle.substring(0, percentageToStyle.indexOf(" "));
        String colorValueOfPercentage;

        try {
            int colorValueInNumber = Utils.roundToInt(Integer.parseInt(numberOfPercentage) * 2.5); //la couleur de l'int sera entre rouge 0 et rouge 250.

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
        if (currentTaskForSurvey != null) {
            currentTaskForSurvey.clearListenersAndCancel();
            currentTaskForSurvey = null;
        }
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
        backgroundErrorText = findViewById(R.id.text_errorbackgroundmessage_showsurvey);
        mainCardView = findViewById(R.id.card_showsurvey);
        contentText = findViewById(R.id.content_showsurvey);
        voteButton = findViewById(R.id.button_vote_showsurvey);
        showResultButton = findViewById(R.id.button_showresult_showsurvey);
        swipeRefresh = findViewById(R.id.swiperefresh_showsurvey);

        mainCardView.setVisibility(View.GONE);
        backgroundErrorText.setVisibility(View.GONE);
        voteButton.setVisibility(View.GONE);
        showResultButton.setVisibility(View.GONE);
        voteButton.setOnClickListener(voteButtonClickedListener);
        showResultButton.setOnClickListener(showResultButtonClickedListener);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorControlHighlightThemeLight);

        if (savedInstanceState != null) {
            mainCardView.setVisibility(savedInstanceState.getBoolean(SAVE_MAIN_CARD_IS_VISIBLE, false) ? View.VISIBLE : View.GONE);
            voteButton.setVisibility(savedInstanceState.getBoolean(SAVE_VOTE_BUTTON_IS_VISIBLE, false) ? View.VISIBLE : View.GONE);
            showResultButton.setVisibility(savedInstanceState.getBoolean(SAVE_SHOWRESULT_BUTTON_IS_VISIBLE, false) ? View.VISIBLE : View.GONE);
            unparsedSurveyContent = savedInstanceState.getString(SAVE_UNPARSED_SURVEY_CONTENT, "");
            contentForSurvey = savedInstanceState.getString(SAVE_CONTENT_FOR_SURVEY, "");
            contentText.setText(Undeprecator.htmlFromHtml(contentForSurvey));

            mainScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mainScrollView.scrollTo(0, savedInstanceState.getInt(SAVE_SCROLL_POSITION, 0));
                }
            });
        }
        if (getIntent() != null) {
            if (getIntent().getStringExtra(EXTRA_SURVEY_TITLE) != null && myActionBar != null) {
                myActionBar.setSubtitle(Utils.applyEmojiCompatIfPossible(getIntent().getStringExtra(EXTRA_SURVEY_TITLE)));
            }
            if (getIntent().getParcelableArrayListExtra(EXTRA_SURVEY_REPLYS_WITH_INFOS) != null) {
                listOfReplysWithInfos = getIntent().getParcelableArrayListExtra(EXTRA_SURVEY_REPLYS_WITH_INFOS);
            } else {
                listOfReplysWithInfos = new ArrayList<>();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (contentForSurvey.isEmpty()) {
            if (getIntent() != null) {
                if (getIntent().getStringExtra(EXTRA_TOPIC_ID) != null && getIntent().getStringExtra(EXTRA_AJAX_INFOS) != null && getIntent().getStringExtra(EXTRA_COOKIES) != null) {
                    currentTaskForSurvey = new DownloadInfosForSurvey();
                    currentTaskForSurvey.setRequestIsStartedListener(requestIsStartedListener);
                    currentTaskForSurvey.setRequestIsFinishedListener(downloadInfosIsFinishedListener);
                    currentTaskForSurvey.execute(getIntent().getStringExtra(EXTRA_TOPIC_ID), getIntent().getStringExtra(EXTRA_AJAX_INFOS), getIntent().getStringExtra(EXTRA_COOKIES));
                }
            }

            if (currentTaskForSurvey == null) {
                backgroundErrorText.setVisibility(View.VISIBLE);
                backgroundErrorText.setText(errorStringId);
            }
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
        outState.putBoolean(SAVE_MAIN_CARD_IS_VISIBLE, mainCardView.getVisibility() == View.VISIBLE);
        outState.putBoolean(SAVE_VOTE_BUTTON_IS_VISIBLE, voteButton.getVisibility() == View.VISIBLE);
        outState.putBoolean(SAVE_SHOWRESULT_BUTTON_IS_VISIBLE, showResultButton.getVisibility() == View.VISIBLE);
        outState.putString(SAVE_UNPARSED_SURVEY_CONTENT, unparsedSurveyContent);
        outState.putString(SAVE_CONTENT_FOR_SURVEY, contentForSurvey);
        outState.putInt(SAVE_SCROLL_POSITION, mainScrollView.getScrollY());
    }

    @Override
    public void getRegisteredVote(int voteIndex) {
        if (voteIndex == -1 || voteIndex >= listOfReplysWithInfos.size()) {
            Toast.makeText(this, R.string.errorVoteInvalide, Toast.LENGTH_SHORT).show();
        } else if (currentTaskForVote != null) {
            Toast.makeText(this, R.string.errorVoteAlreadyRunning, Toast.LENGTH_SHORT).show();
        } else {
            currentTaskForVote = new SendVoteToSurvey();
            currentTaskForVote.setRequestIsStartedListener(requestIsStartedListener);
            currentTaskForVote.setRequestIsFinishedListener(sendVoteIsFinishedListener);
            currentTaskForVote.execute(getIntent().getStringExtra(EXTRA_TOPIC_ID), listOfReplysWithInfos.get(voteIndex).infosForReply, getIntent().getStringExtra(EXTRA_AJAX_INFOS), getIntent().getStringExtra(EXTRA_COOKIES));
        }
    }

    private static class DownloadInfosForSurvey extends AbsWebRequestAsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[2], false);
                return WebManager.sendRequest("https://www.jeuxvideo.com/forums/ajax_topic_sondage_view_response.php", "GET", "id_topic=" + params[0] + "&action=view_vote&" + params[1], currentWebInfos);
            }
            return null;
        }
    }

    private static class SendVoteToSurvey extends AbsWebRequestAsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 3) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[3], false);
                return WebManager.sendRequest("https://www.jeuxvideo.com/forums/ajax_topic_sondage_vote.php", "GET", "id_topic=" + params[0] + "&" + params[1] + "&" + params[2], currentWebInfos);
            }
            return null;
        }
    }
}
