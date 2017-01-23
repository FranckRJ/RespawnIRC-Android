package com.franckrj.respawnirc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

public class ShowSurveyActivity extends AppCompatActivity {
    public static final String EXTRA_SURVEY_TITLE = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_SURVEY_TITLE";
    public static final String EXTRA_TOPIC_ID = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_TOPIC_ID";
    public static final String EXTRA_AJAX_INFOS = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_AJAX_INFOS";
    public static final String EXTRA_COOKIES = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_COOKIES";

    private TextView contentText = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private DownloadInfosForSurvey currentTaskForSurvey = null;
    private String contentForSurvey = "";

    private void stopAllCurrentTasks() {
        if (currentTaskForSurvey != null) {
            currentTaskForSurvey.cancel(true);
            currentTaskForSurvey = null;
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
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_showsurvey);

        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);

        if (savedInstanceState != null) {
            contentForSurvey = savedInstanceState.getString(getString(R.string.saveContentForSurvey), "");
            contentText.setText(contentForSurvey);
        }
        if (contentForSurvey.isEmpty() && getIntent() != null) {
            if (getIntent().getStringExtra(EXTRA_SURVEY_TITLE) != null) {
                if (myActionBar != null) {
                    myActionBar.setSubtitle(getIntent().getStringExtra(EXTRA_SURVEY_TITLE));
                }
            }
            if (getIntent().getStringExtra(EXTRA_TOPIC_ID) != null && getIntent().getStringExtra(EXTRA_AJAX_INFOS) != null && getIntent().getStringExtra(EXTRA_COOKIES) != null) {
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
        super.onPause();
        stopAllCurrentTasks();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveContentForSurvey), contentForSurvey);
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

            if (!Utils.stringIsEmptyOrNull(surveyBlock)) {
                String errorContent = JVCParser.getErrorMessageInJSONMode(surveyBlock);

                if (errorContent == null) {
                    JVCParser.SurveyInfos infosForSurvey = JVCParser.getSurveyInfosFromSurveyBlock(JVCParser.getRealSurveyContent(surveyBlock));
                    StringBuilder newContentToShow = new StringBuilder();

                    if (infosForSurvey.isOpen) {
                        newContentToShow.append(getString(R.string.titleForSurvey, infosForSurvey.title));
                    } else {
                        newContentToShow.append(getString(R.string.titleForClosedSurvey, infosForSurvey.title));
                    }
                    newContentToShow.append("\n");

                    for (JVCParser.SurveyInfos.SurveyReply currentReply : infosForSurvey.listOfReplys) {
                        newContentToShow.append("\n").append(currentReply.percentageOfVotes).append(" : ").append(currentReply.title);
                    }

                    newContentToShow.append("\n\n").append(infosForSurvey.numberOfVotes);

                    contentForSurvey = newContentToShow.toString();
                    contentText.setText(contentForSurvey);
                } else {
                    Toast.makeText(ShowSurveyActivity.this, errorContent, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ShowSurveyActivity.this, R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
            }

            currentTaskForSurvey = null;
        }
    }
}
